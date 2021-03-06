package com.limeblast.mydeatree.adapters

import android.content.Context
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget._
import java.util
import com.limeblast.mydeatree.{Helpers, R, Idea}

/**
 * Created with IntelliJ IDEA.
 * User: Laimiux
 * Date: 12/22/12
 * Time: 5:14 PM
 * To change this template use File | Settings | File Templates.
 */
class IdeaListAdapter(val context: Context, resourceId: Int, objects: util.List[Idea])
  extends ArrayAdapter(context, resourceId, objects) {


  override def getView(position: Int, convertView: View, parent: ViewGroup): View = {
    val inflater: LayoutInflater = LayoutInflater.from(context)

    var cView = inflater.inflate(resourceId, null).asInstanceOf[LinearLayout]

    val idea: Idea = getItem(position)

    val txtTitle = cView.findViewById(R.id.idea_title).asInstanceOf[TextView]
    txtTitle.setText(idea.title)

    val txtText = cView.findViewById(R.id.idea_text).asInstanceOf[TextView]
    txtText.setText(idea.text)

    val dateText = cView.findViewById(R.id.idea_date).asInstanceOf[TextView]
    val date = Helpers.stringToDate(idea.modified_date)
    dateText.setText(Helpers.formatDate(date))

    if (!idea.public){
      val publicIcon = cView.findViewById(R.id.idea_public).asInstanceOf[ImageView]
      publicIcon.setVisibility(View.GONE)
    }

    return cView
  }
}
