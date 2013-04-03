package com.limeblast.mydeatree.activities

import android.os.Bundle


import android.app._
import android.view.ViewGroup.LayoutParams


import android.text.TextUtils


import com.limeblast.mydeatree._
import providers.PrivateIdeaProvider

import com.limeblast.androidhelpers.ScalifiedActivity
import storage.PrivateIdeaTableInfo

object NewIdeaActivity {
  val NEW_IDEA_RESULT = 99
}


class NewIdeaActivity extends Activity with TypedActivity with BasicIdeaModule with ScalifiedActivity {

  lazy val titleField = findView(TR.idea_title_edit)
  lazy val textField = findView(TR.idea_text_edit)
  lazy val publicCheckBox = findView(TR.idea_public_check_box)

  private var parent_uri: String = null

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.idea_new)

    getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)

    // Set the check box
    val isPublic = getIntent.getBooleanExtra("public", false)
    publicCheckBox.setChecked(isPublic)

    parent_uri = getIntent.getStringExtra("parent_uri")



    // Set the submit button click listener
    findView(TR.idea_submit_button).onClick({
      val title = TextUtils.htmlEncode(titleField.getText.toString)
      val text = TextUtils.htmlEncode(textField.getText.toString)
      val isPublic = publicCheckBox.isChecked
      //Toast.makeText(NewIdeaActivity.this, msg, Toast.LENGTH_LONG).show()

      val timeString = Helpers.getNow()
      val idea = new Idea(title, text, null, parent_uri, timeString, timeString, null, isPublic) with IdeaValidationModule

      idea.validate() match {
        case (false, msg: String) => shortToast(msg)
        case (true, _) => createNewIdea(idea)
        case _ =>
      }
    })


    // Set cancel button click listener
    findView(TR.idea_cancel_button).onClick(finish())

  }

  private def createNewIdea(idea: Idea) {
    val values = getContentValues(idea)
    values.put(PrivateIdeaTableInfo.KEY_PUBLIC, idea.public)
    values.put(PrivateIdeaTableInfo.KEY_IS_IDEA_NEW, true)


    getContentResolver.insert(PrivateIdeaProvider.CONTENT_URI, values)


    setResult(Activity.RESULT_OK)
    finish()
  }
}
