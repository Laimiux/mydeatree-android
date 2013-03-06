package com.limeblast.mydeatree

import com.actionbarsherlock.app.SherlockActivity
import android.os.{ResultReceiver, Handler, Bundle}
import android.widget._
import android.view.View


import AppSettings._

import android.app._
import android.content.{ContentValues}
import android.view.ViewGroup.LayoutParams


import scala.concurrent.ops.spawn
import android.text.TextUtils

import com.limeblast.androidhelpers.{ScalaHandler, AndroidImplicits}
import AndroidImplicits.toListener


class NewIdeaActivity extends SherlockActivity with TypedActivity {

  lazy val titleField = findView(TR.idea_title_edit)
  lazy val textField = findView(TR.idea_text_edit)
  lazy val publicCheckBox = findView(TR.idea_public_check_box)

  private var parent_uri: String = null

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.idea_new)

    getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT)

    // Set the check box
    val isPublic = getIntent.getBooleanExtra("public", false)
    publicCheckBox.setChecked(isPublic)

    parent_uri = getIntent.getStringExtra("parent_uri")


    // Set the submit button click listener
    findView(TR.idea_submit_button).setOnClickListener((view: View) => {
      val title = TextUtils.htmlEncode(titleField.getText.toString)
      val text = TextUtils.htmlEncode(textField.getText.toString)
      val isPublic = publicCheckBox.isChecked


      val idea = new Idea(title, text, null, parent_uri, null, null, null, isPublic)

      /*
      val (isIdeaValid, message) = ResourceValidation.validate_idea(idea)

      if (isIdeaValid) {

        createNewIdea(idea)
        //startIdeaCreateService(idea)
      } else {
        Toast.makeText(NewIdeaActivity.this, message, Toast.LENGTH_LONG).show()
      }
      */


      ResourceValidation.validate_idea(idea) match {
        case (false, msg: String) => Toast.makeText(NewIdeaActivity.this, msg, Toast.LENGTH_LONG).show()
        case (true, _) =>  createNewIdea(idea)
        case _ =>
      }
    })

    // Set cancel button click listener
    findView(TR.idea_cancel_button).setOnClickListener((view: View) => finish())

  }

  private def createNewIdea(idea: Idea) {
    val timeString = Helpers.getNow()

    val values = new ContentValues()
    values.put(IdeaHelper.KEY_TITLE, idea.title)
    values.put(IdeaHelper.KEY_TEXT, idea.text)
    values.put(IdeaHelper.KEY_PUBLIC, idea.public)
    values.put(IdeaHelper.KEY_PARENT, idea.parent)
    values.put(IdeaHelper.KEY_CREATED_DATE, timeString)
    values.put(IdeaHelper.KEY_MODIFIED_DATE, timeString)
    values.put(IdeaHelper.KEY_OWNER, USERNAME)
    values.put(IdeaHelper.KEY_IS_IDEA_NEW, true)

    val resolver = getContentResolver
    resolver.insert(RESTfulProvider.CONTENT_URI, values)

    finish()
  }
}
