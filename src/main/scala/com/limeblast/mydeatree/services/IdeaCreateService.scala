package com.limeblast.mydeatree.services

import android.app.{NotificationManager, PendingIntent, IntentService}
import android.content.{Context, Intent}
import scala.Some
import android.support.v4.app.NotificationCompat.Builder
import android.support.v4.app.NotificationCompat
import android.graphics.Color
import com.limeblast.androidhelpers.{JsonModule, ProviderHelper}
import com.limeblast.mydeatree._
import com.limeblast.mydeatree.activities.MainActivity
import com.limeblast.mydeatree.providers.RESTfulProvider
import com.limeblast.mydeatree.AppSettings._
import scala.Some
import scala.Some
import scala.Some

object IdeaCreateService {
  val IDEA_CREATED = 1000
  val IDEA_CREATION_FAILED = 1001
}
class IdeaCreateService extends IntentService("IdeaCreateService") with JsonModule {

  def onHandleIntent(intent: Intent) {
    val ideaJson = intent.getStringExtra("idea")

    // Make sure we have an idea to upload
    if (ideaJson == null || ideaJson.equals("")) {
      throw new IllegalStateException("Have to pass idea to update.")
    }


    val passedIdea = getMainObject(ideaJson, classOf[Idea])

    val whereMap = Map(IdeaHelper.KEY_TITLE -> passedIdea.title,
      IdeaHelper.KEY_TEXT -> passedIdea.text,
      IdeaHelper.KEY_CREATED_DATE -> passedIdea.created_date)

    ProviderHelper.updateObjects(getContentResolver, RESTfulProvider.CONTENT_URI,
      whereMap, null, Map(IdeaHelper.KEY_IS_IDEA_SYNCING -> true))

    App.PersonalIdeaResource.postIdea(IDEA_URL, passedIdea) match {
      case Some(idea) => {
        insertIdea(passedIdea, idea)
        createNotification(idea)
      }
      case _ =>
    }

  }


  private def insertIdea(passedIdea: Idea, returnedIdea: Idea) {
    val values = IdeaTableHelper.createNewIdeaValues(returnedIdea)
    values.put(IdeaHelper.KEY_IS_IDEA_NEW, false)
    values.put(IdeaHelper.KEY_IS_IDEA_SYNCING, false)

    val whereMap = Map(IdeaHelper.KEY_TITLE -> passedIdea.title,
      IdeaHelper.KEY_TEXT -> passedIdea.text,
      IdeaHelper.KEY_CREATED_DATE -> passedIdea.created_date)

    ProviderHelper.updateObjects(getContentResolver, RESTfulProvider.CONTENT_URI, whereMap, null, values)
  }


  private def createNotification(idea: Idea) {
      // Set the intent so it would go into personal ideas
      // and set parent idea to
      val intent = new Intent(this, classOf[MainActivity])

      val ideaJson = convertObjectToJson(idea)
      intent.putExtra("idea", ideaJson)

      val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

      // Notifaction
      val builder: Builder = new NotificationCompat.Builder(this)
        .setSmallIcon(R.drawable.icon)
        .setTicker("Notification")
        .setContentTitle("Idea Uploaded")
        .setContentText(idea.title + "\n" + idea.text)
        .setWhen(System.currentTimeMillis())
        .setLights(Color.RED, 0, 1)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)

      val notification = builder.getNotification

      val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE).asInstanceOf[NotificationManager]
      notificationManager.notify(1, notification)
  }


}
