package com.limeblast.mydeatree.adapters

import android.content.{Intent, Context}
import java.util
import android.widget._
import android.view.{ViewGroup, View}
import android.view.View.OnClickListener
import android.database.Cursor

import com.limeblast.androidhelpers.{Inflater}
import com.limeblast.mydeatree._



class PublicIdeaListAdapter(val context: Context, resourceId: Int, objects: util.List[PublicIdea])
  extends ArrayAdapter(context, resourceId, objects) with Inflater with FavoriteIdeaProviderModule {

  /*
  def getFavoriteIdea(idea: Idea): Cursor = getContext.getApplicationContext.getContentResolver.query(FavoriteIdeaProvider.CONTENT_URI,
    null, makeWhereClause(FavoriteIdeaColumns.KEY_IDEA -> idea.resource_uri, FavoriteIdeaColumns.KEY_IS_DELETED -> false), null, null)
    */

  def getFavoriteIdea(idea: Idea): Option[FavoriteIdea] = ???

  override def getView(position: Int, convertView: View, parent: ViewGroup): View = {
    var cView = inflater.inflate(resourceId, null).asInstanceOf[LinearLayout]

    val idea: PublicIdea = getItem(position)


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

    // Checks if idea is favorited
    var favorited: Boolean = isFavorite(idea)


    val favoriteButton = cView.findViewById(R.id.favorite_button).asInstanceOf[Button]

    // Set to full star if idea is favorited
    if (favorited)
      favoriteButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_star_full, 0, 0, 0)


    favoriteButton.setOnClickListener(new OnClickListener {
      def onClick(v: View) {
        if (App.getUsername(getContext).equals("")) {
          Toast.makeText(getContext, "You need to login to favorite ideas.", Toast.LENGTH_SHORT).show()
        }
        else if (App.USERNAME.equals(idea.owner.username)) {
          Toast.makeText(getContext, "Cannot favorite your own idea", Toast.LENGTH_SHORT).show()
        } else {

          if (favorited) {
            // Sets favorite to appropriate image
            favoriteButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_star_empty, 0, 0, 0)

            removeFavorite(idea)
          } else {
            // Sets favorite to appropriate image
            favoriteButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_star_full, 0, 0, 0)

            setToFavorite(idea)
          }


          favorited = !favorited
        }

      }
    })

    if (idea.children_count > 0) {

    }


    return cView
  }

  private def isFavorite(idea: PublicIdea): Boolean = {

      val resolver = context.getContentResolver
      val select = makeWhereClause(FavoriteIdeaColumns.KEY_IDEA -> idea.resource_uri, FavoriteIdeaColumns.KEY_IS_DELETED -> false)

      val cursor: Cursor = getObjects(resolver, null, select, null, null)

      val isIdeaFavorited = cursor.getCount() > 0
      cursor.close()

      isIdeaFavorited

  }

  /**
   * Save to database a favorite idea
   * @param idea PublicIdea that is being favorited
   */
  private def setToFavorite(idea: PublicIdea) {
    val resolver = context.getContentResolver
    val select = makeWhereClause(FavoriteIdeaColumns.KEY_IDEA -> idea.resource_uri, FavoriteIdeaColumns.KEY_IS_DELETED -> true)
    val cursor = getObjects(resolver, null, select, null, null)


    val isNew = cursor.getCount > 0

    cursor.close()

    if (isNew)  // Update the favorite
      updateObjects(resolver, (FavoriteIdeaColumns.KEY_IDEA -> idea.resource_uri), null, Map(FavoriteIdeaColumns.KEY_IS_DELETED -> false))
    else  // Insert a new favorite
      insertObject(resolver)(FavoriteIdeaColumns.KEY_IDEA -> idea.resource_uri, FavoriteIdeaColumns.KEY_IS_NEW -> true)
  }


  /**
   * Checks if favorite is on server than marks it for deletion,
   * otherwise completely removes it from database
   * @param idea PublicIdea to be removed from favorites
   */
  private def removeFavorite(idea: PublicIdea) {
    val resolver = context.getContentResolver
    val select = makeWhereClause(FavoriteIdeaColumns.KEY_IDEA -> idea.resource_uri, FavoriteIdeaColumns.KEY_IS_NEW -> true)
    val cursor = getObjects(resolver, null, select, null, null)

    val isOnServer = cursor.getCount == 0

    cursor.close()

    if (isOnServer) {
      updateObjects(resolver, (FavoriteIdeaColumns.KEY_IDEA -> idea.resource_uri), null, Map(FavoriteIdeaColumns.KEY_IS_DELETED -> true))
    } else {
      deleteObjects(resolver, (FavoriteIdeaColumns.KEY_IDEA -> idea.resource_uri), null)
    }
  }
}
