package com.limeblast.mydeatree

import android.content.{Intent, Context}
import java.util
import android.widget._
import android.view.{ViewGroup, View, LayoutInflater}
import android.view.View.OnClickListener
import android.net.Uri

class PublicIdeaListAdapter(c: Context, resourceId: Int, objects: util.List[PublicIdea]) extends ArrayAdapter(c, resourceId, objects) {

  private val inflater = LayoutInflater.from(c)

  override def getView(position: Int, convertView: View, parent: ViewGroup): View = {
    var cView = inflater.inflate(resourceId, null).asInstanceOf[LinearLayout]

    val idea: PublicIdea = getItem(position)

    var favorited = false


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
        if(AppSettings.getUsername(getContext).equals("")) {
          Toast.makeText(getContext, "You need to login to favorite ideas.", Toast.LENGTH_SHORT).show()
        }
        else if (AppSettings.USERNAME == idea.owner.username) {
          Toast.makeText(getContext, "Cannot favorite your own idea", Toast.LENGTH_SHORT).show()
        } else {
          favorited = !favorited

          val favIcon = cView.findViewById(R.id.favorite_icon).asInstanceOf[ImageView]
          //val favText = cView.findViewById(R.id.favorite_txt).asInstanceOf[TextView]

          if (favorited) {
            favIcon.setBackgroundResource(R.drawable.ic_star_full)
            //favText.setText(R.string.unfavorite)
          } else {
            favIcon.setBackgroundResource(R.drawable.ic_star_empty)
            //favText.setText(R.string.favorite)
          }

        }
      }
    })



    return cView
  }

}
