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


class FavoriteIdeaListAdapter(val context: Context, resourceId: Int, objects: util.List[PublicIdea])
  extends ArrayAdapter(context, resourceId, objects) with Inflater with ProviderAccessModule {

  def getFavoriteIdea(idea: Idea): Cursor = getContext.getApplicationContext.getContentResolver.query(FavoriteIdeaProvider.CONTENT_URI,
    null, makeWhereClause(FavoriteIdeaColumns.KEY_IDEA -> idea.resource_uri, FavoriteIdeaColumns.KEY_IS_DELETED -> false), null, null)

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


    val favoriteButton = cView.findViewById(R.id.favorite_button).asInstanceOf[Button]
    favoriteButton.setVisibility(View.GONE)


    if (idea.children_count > 0) {

    }


    return cView
  }

}