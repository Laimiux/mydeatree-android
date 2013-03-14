package com.limeblast.mydeatree

import android.content.{Intent, Context}
import java.util
import android.widget._
import android.view.{ViewGroup, View}
import android.view.View.OnClickListener
import providers.FavoriteIdeaProvider
import android.database.Cursor

import scala.concurrent.Future
import scala.concurrent.future

import scala.concurrent.ExecutionContext.Implicits.global


import com.limeblast.androidhelpers.{ProviderModule, Inflater}
import android.util.Log
import android.net.Uri

class PublicIdeaListAdapter(val context: Context, resourceId: Int, objects: util.List[PublicIdea])
  extends ArrayAdapter(context, resourceId, objects) with Inflater with ProviderModule {

  def getFavoriteIdea(idea: Idea): Cursor = getContext.getApplicationContext.getContentResolver.query(FavoriteIdeaProvider.CONTENT_URI,
    null, ProviderHelper.makeWhereClause(FavoriteIdeaColumns.KEY_IDEA -> idea.resource_uri, FavoriteIdeaColumns.KEY_IS_DELETED -> false), null, null)

  override def getView(position: Int, convertView: View, parent: ViewGroup): View = {
    var cView = inflater.inflate(resourceId, null).asInstanceOf[LinearLayout]

    val idea: PublicIdea = getItem(position)


    // Set this value based on db


    val cursor: Cursor = getContext.getApplicationContext.getContentResolver.query(FavoriteIdeaProvider.CONTENT_URI,
      null, ProviderHelper.makeWhereClause(FavoriteIdeaColumns.KEY_IDEA -> idea.resource_uri, FavoriteIdeaColumns.KEY_IS_DELETED -> false), null, null)


    var favorited: Boolean = if (cursor.getCount() > 0) true else false

    cursor.close()


    if (favorited) {
      val favIcon = cView.findViewById(R.id.favorite_icon).asInstanceOf[ImageView]
      favIcon.setBackgroundResource(R.drawable.ic_star_full)
    }


    val txtTitle = cView.findViewById(R.id.idea_title).asInstanceOf[TextView]
    txtTitle.setText(idea.title)

    val txtOwner = cView.findViewById(R.id.idea_owner).asInstanceOf[TextView]
    txtOwner.setText(" by " + idea.owner.username)

    val txtText = cView.findViewById(R.id.idea_text).asInstanceOf[TextView]
    txtText.setText(idea.text)

    val dateText = cView.findViewById(R.id.public_idea_date).asInstanceOf[TextView]
    val date = Helpers.stringToDate(idea.modified_date)
    dateText.setText(Helpers.formatDate(date))


    var shareLayout = cView.findViewById(R.id.public_idea_share_layout).asInstanceOf[LinearLayout]
    shareLayout.setOnClickListener(new OnClickListener {
      def onClick(view: View) {
        val sharingIntent = new Intent(Intent.ACTION_SEND)
        sharingIntent.setType("text/plain")
        sharingIntent.putExtra(Intent.EXTRA_TEXT, "https://mydeatree.appspot.com/idea/" + idea.id + "/")
        getContext.startActivity(Intent.createChooser(sharingIntent, "Share with "))
      }
    })

    var favoriteLayout = cView.findViewById(R.id.public_idea_favorite_layout).asInstanceOf[LinearLayout]
    favoriteLayout.setOnClickListener(new OnClickListener {
      def onClick(view: View) {
        if (App.getUsername(getContext).equals("")) {
          Toast.makeText(getContext, "You need to login to favorite ideas.", Toast.LENGTH_SHORT).show()
        }
        else if (App.USERNAME == idea.owner.username) {
          Toast.makeText(getContext, "Cannot favorite your own idea", Toast.LENGTH_SHORT).show()
        } else {

          val favIcon = cView.findViewById(R.id.favorite_icon).asInstanceOf[ImageView]
          //val favText = cView.findViewById(R.id.favorite_txt).asInstanceOf[TextView]


          if (favorited) {
            favIcon.setBackgroundResource(R.drawable.ic_star_empty)


            ProviderHelper.updateObjects(getContext.getContentResolver, FavoriteIdeaProvider.CONTENT_URI,
              (FavoriteIdeaColumns.KEY_IDEA -> idea.resource_uri),
              null,
              Map(FavoriteIdeaColumns.KEY_IS_DELETED -> true))

            //getContext.getContentResolver.delete(FavoriteIdeaProvider.CONTENT_URI,
            //makeWhereClause())
            //favText.setText(R.string.unfavorite)

            // Set favorite to deleted
          } else {
            favIcon.setBackgroundResource(R.drawable.ic_star_full)

            val cursor: Cursor = getContext.getApplicationContext.getContentResolver.query(FavoriteIdeaProvider.CONTENT_URI,
              null, ProviderHelper.makeWhereClause(FavoriteIdeaColumns.KEY_IDEA -> idea.resource_uri, FavoriteIdeaColumns.KEY_IS_DELETED -> true), null, null)
            //favText.setText(R.string.favorite)

            val count = cursor.getCount

            cursor.close()

            if (count > 0) {

              val f: Future[Int] = future {
                ProviderHelper.updateObjects(getContext.getContentResolver, FavoriteIdeaProvider.CONTENT_URI,
                  (FavoriteIdeaColumns.KEY_IDEA -> idea.resource_uri),
                  null,
                  Map(FavoriteIdeaColumns.KEY_IS_DELETED -> false))
              }

              f onSuccess {
                case number => Log.d("PublicIdeaListAdapter", number + " idea favorited")
              }
            } else {

              val f: Future[Unit] = future {
                ProviderHelper.insertObject(FavoriteIdeaProvider.CONTENT_URI)(getContext.getContentResolver)(FavoriteIdeaColumns.KEY_OWNER -> App.USERNAME,
                  FavoriteIdeaColumns.KEY_IDEA -> idea.resource_uri,
                  FavoriteIdeaColumns.KEY_IS_NEW -> true)
              }


              f onSuccess {
                case _ => Log.d("PublicIdeaListAdapter", "Idea favorited")
              }

            }
            // Either create a new one or set it to not deleted
          }


          favorited = !favorited
        }
      }
    })


    if (idea.children_count > 0) {

    }


    return cView
  }

}
