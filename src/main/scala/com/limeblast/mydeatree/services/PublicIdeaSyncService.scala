package com.limeblast.mydeatree.services

import android.app._
import android.content.{ContentUris, Context, Intent}
import android.os.{ResultReceiver, SystemClock}
import java.util
import scala.collection.JavaConversions._
import android.preference.PreferenceManager
import android.util.Log
import android.support.v4.app.NotificationCompat.Builder
import android.support.v4.app.NotificationCompat
import android.graphics.Color
import com.limeblast.mydeatree._
import com.limeblast.mydeatree.AppSettings._
import providers.{PublicIdeaProvider}
import com.limeblast.mydeatree.activities.MainActivity
import scala.Some
import storage.{PublicIdeaTableInfo, PublicIdeaDatabaseModule}
import com.limeblast.androidhelpers.WhereClauseModule


private object SyncServiceVars {
  val PUBLIC_SYNC_TAG = "PUBLIC_IDEA_SYNC_SERVICE"
}

class PublicIdeaSyncService extends IntentService("PublicIdeaSyncService")
with PublicIdeaDatabaseModule with BasicIdeaModule with DatedObjectModule with WhereClauseModule {

  private var alarmManager: AlarmManager = _
  private var alarmIntent: PendingIntent = _

  override def onCreate() {
    super.onCreate()

    if (App.DEBUG) Log.d(SyncServiceVars.PUBLIC_SYNC_TAG, "Creating the alarm.")

    // Get Alarm Manager
    alarmManager = getSystemService(Context.ALARM_SERVICE).asInstanceOf[AlarmManager]
    val ALARM_ACTION = PublicSyncAlarmReceiver.ACTION_REFRESH_PUBLIC_IDEAS_ALARM

    val intentToFire = new Intent(ALARM_ACTION)
    alarmIntent = PendingIntent.getBroadcast(this, 0, intentToFire, 0)

  }


  override def onHandleIntent(intent: Intent) {
    // Retrieve shared prefs
    val context = getApplicationContext
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    val updateFreq = Integer.parseInt(prefs.getString(AppSettings.PREF_UPDATE_FREQ_INDEX, "60"))
    val autoUpdateChecked = prefs.getBoolean(AppSettings.PREF_AUTO_UPDATE, true)

    if (autoUpdateChecked) {
      if (App.DEBUG) Log.d(APP_TAG, "Starting public idea sync alarm, alarm will be called every " + updateFreq)
      val alarmType = AlarmManager.ELAPSED_REALTIME_WAKEUP
      val timeToRefresh = SystemClock.elapsedRealtime() + updateFreq * 60 * 1000

      alarmManager.setInexactRepeating(alarmType, timeToRefresh,
        updateFreq * 60 * 1000, alarmIntent)

    } else {
      alarmManager.cancel(alarmIntent)
    }

    // Check if logged in
    if (App.isLoggedIn(getApplicationContext)) {
      val receiver: ResultReceiver = intent.getParcelableExtra(PUBLIC_IDEA_RESULT_RECEIVER)
      refreshPublicIdeas()

      if (receiver != null) {
        receiver.send(0, null)
      }
    }

  }


  private def refreshPublicIdeas() {
    if (App.DEBUG) Log.d(SyncServiceVars.PUBLIC_SYNC_TAG, "Starting to sync public ideas...")

    // Get ideas from the server
    App.PublicIdeaResource.getObjects match {
      case Some(publicIdeas) => {
        val objectsInDb = getSavedPublicIdeas()

        // Different counters
        var ideasAdded = 0
        var ideasUpdated = 0
        var ideasRemoved = 0

        if (publicIdeas != null) {
          /* For all ideas retrieves insert or update or do nothing if old */
          for (idea <- publicIdeas) {
            var isNew = true


            // Check if already in db
            for (obj <- objectsInDb) {
              // idea found in database
              if (idea.id == obj.id) {
                isNew = false
                // check if it is updated
                if (idea.modified_date != obj.modified_date) {
                  updateIdea(idea)
                  ideasUpdated += 1
                }
              }
            }

            // idea is new so insert it into the database
            if (isNew) {
              insertIdea(idea)
              ideasAdded += 1
            }
          }
        }

        if (App.DEBUG) Log.d(APP_TAG, "Public ideas added: " + ideasAdded + ", public ideas updated: " + ideasUpdated)

        // Throw a notification if app is not running
        if (!IS_MAIN_ACTIVITY_RUNNING && ideasAdded > 0) {
          // Set the intent so it would go into personal ideas
          // and set parent idea to
          val intent = new Intent(this, classOf[MainActivity])
          intent.putExtra(HAS_NEW_PUBLIC_IDEAS, true)
          val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

          // Notifaction
          val builder: Builder = new NotificationCompat.Builder(this)
            .setSmallIcon(R.drawable.icon)
            .setTicker("Notification")
            .setContentTitle("Public Ideas Updated")
            .setContentText("There are " + ideasAdded + " new public ideas.")
            .setWhen(System.currentTimeMillis())
            .setLights(Color.RED, 0, 1)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

          val notification = builder.getNotification

          val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE).asInstanceOf[NotificationManager]
          notificationManager.notify(1, notification)
        }


      }
      case None => if (App.DEBUG) Log.d(APP_TAG, "There was an error syncing public ideas.")
    }


    if (App.DEBUG) Log.d(SyncServiceVars.PUBLIC_SYNC_TAG, "Public Idea synchronization finished.")
  }

  private def insertIdea(idea: PublicIdea) {
    val cr = getContentResolver

    val values = getContentValues(idea)
    // Add more specific content values
    values.put(PublicIdeaTableInfo.KEY_OWNER, idea.owner.username)
    values.put(PublicIdeaTableInfo.KEY_CHILDREN_COUNT, new Integer(idea.children_count))

    cr.insert(PublicIdeaProvider.CONTENT_URI, values)

  }

  private def getSavedPublicIdeas(): util.ArrayList[DatedObject] = {

    val resolver = getContentResolver

    getDatedObjects(PublicIdeaProvider.CONTENT_URI, resolver)
  }


  private def updateIdea(idea: PublicIdea) {
    //val ideaAddress = ContentUris.withAppendedId(PublicIdeaProvider.CONTENT_URI, idea.id.toLong)
    val cr = getContentResolver

    val where = makeWhereClause(PublicIdeaTableInfo.KEY_ID -> idea.id)

    val values = getContentValues(idea)
    // Add more specific content values
    values.put(PublicIdeaTableInfo.KEY_OWNER, idea.owner.username)
    values.put(PublicIdeaTableInfo.KEY_CHILDREN_COUNT, new Integer(idea.children_count))


    cr.update(PublicIdeaProvider.CONTENT_URI, values, where, null)
  }
}

