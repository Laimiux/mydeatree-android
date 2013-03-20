package com.limeblast.mydeatree.adapters

import android.content.{Intent, Context}
import java.util
import android.widget._
import android.view.{ViewGroup, View}
import android.view.View.OnClickListener
import android.database.Cursor

import com.limeblast.androidhelpers.{ProviderAccessModule, Inflater}
import com.limeblast.mydeatree._
import com.limeblast.mydeatree.providers.FavoriteIdeaProvider
import scala.App


class PublicIdeaListAdapter(val context: Context, resourceId: Int, objects: util.List[PublicIdea])
  extends ArrayAdapter(context, resourceId, objects) with Inflater with ProviderAccessModule {

  def getFavoriteIdea(idea: Idea): Cursor = getContext.getApplicationContext.getContentResolver.query(FavoriteIdeaProvider.CONTENT_URI,
    null, makeWhereClause(FavoriteIdeaColumns.KEY_IDEA -> idea.resource_uri, FavoriteIdeaColumns.KEY_IS_DELETED -> false), null, null)

  override def getView(position: Int, convertView: View, parent: ViewGroup): View = {
    var cView = inflater.inflate(resourceId, null).asInstanceOf[LinearLayout]

    val idea: PublicIdea = getItem(position)


    // Set this value based on db


    val cursor: Cursor = getContext.getApplicationContext.getContentResolver.query(FavoriteIdeaProvider.CONTENT_URI,
      null, makeWhereClause(FavoriteIdeaColumns.KEY_IDEA -> idea.resource_uri, FavoriteIdeaColumns.KEY_IS_DELETED -> false), null, null)


    var favorited: Boolean = if (cursor.getCount() > 0) true else false

    cursor.close()





    val txtTitle = cView.findViewById(R.id.idea_title).asInstanceOf[TextView]
    txtTitle.setText(idea.title)

    val txtOwner = cView.findViewById(R.id.idea_owner).asInstanceOf[TextView]
    txtOwner.setText(" by " + idea.owner.username)

    val txtText = cView.findViewById(R.id.idea_text).asInstanceOf[TextView]
    txtText.setText(idea.text)

    val dateText = cView.findViewById(R.id.public_idea_date).asInstanceOf[TextView]
    val date = Helpers.stringToDate(idea.modified_date)
    dateText.setText(Helpers.formatDate(date))

    val shareButton = cView.findViewById(R.id.share_button).asInstanceOf[Button]
    shareButton.setOnClickListener(new OnClickListener {
      def onClick(v: View) {
        val sharingIntent = new Intent(Intent.ACTION_SEND)
        sharingIntent.setType("text/plain")
        sharingIntent.putExtra(Intent.EXTRA_TEXT, "https://mydeatree.appspot.com/idea/" + idea.id + "/")
        getContext.startActivity(Intent.createChooser(sharingIntent, "Share with "))
      }
    })


    val favoriteButton = cView.findViewById(R.id.favorite_button).asInstanceOf[Button]

    // Set to full star if idea is favorited
    if (favorited)
      favoriteButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_star_full, 0, 0, 0)


    favoriteButton.setOnClickListener(new OnClickListener {
      def onClick(v: View) {
        if (App.getUsername(getContext).equals("")) {
          Toast.makeText(getContext, "You need to login to favorite ideas.", Toast.LENGTH_SHORT).show()
        }
        else if (App.USERNAME == idea.owner.username) {
          Toast.makeText(getContext, "Cannot favorite your own idea", Toast.LENGTH_SHORT).show()
        } else {

         // val favIcon = cView.findViewById(R.id.favorite_icon).asInstanceOf[ImageView]
          //val favText = cView.findViewById(R.id.favorite_txt).asInstanceOf[TextView]


          val resolver = getContext.getContentResolver

          if (favorited) {
            favoriteButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_star_empty, 0, 0, 0)

            // Set favorite to deleted
            ProviderHelper.updateObjects(resolver, FavoriteIdeaProvider.CONTENT_URI,
              (FavoriteIdeaColumns.KEY_IDEA -> idea.resource_uri),
              null,
              Map(FavoriteIdeaColumns.KEY_IS_DELETED -> true))



          } else {
            favoriteButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_star_full, 0, 0, 0)

            val cursor: Cursor = resolver.query(FavoriteIdeaProvider.CONTENT_URI,
              null, makeWhereClause(FavoriteIdeaColumns.KEY_IDEA -> idea.resource_uri, FavoriteIdeaColumns.KEY_IS_DELETED -> true), null, null)
            //favText.setText(R.string.favorite)

            val count = cursor.getCount

            cursor.close()

            if (count > 0) {


              ProviderHelper.updateObjects(resolver, FavoriteIdeaProvider.CONTENT_URI,
                (FavoriteIdeaColumns.KEY_IDEA -> idea.resource_uri),
                null,
                Map(FavoriteIdeaColumns.KEY_IS_DELETED -> false))


            } else {


              ProviderHelper.insertObject(FavoriteIdeaProvider.CONTENT_URI)(resolver)(FavoriteIdeaColumns.KEY_OWNER -> App.USERNAME,
                FavoriteIdeaColumns.KEY_IDEA -> idea.resource_uri,
                FavoriteIdeaColumns.KEY_IS_NEW -> true)



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
