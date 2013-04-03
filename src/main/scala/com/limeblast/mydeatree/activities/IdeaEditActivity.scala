package com.limeblast.mydeatree.activities

import android.os.Bundle
import android.widget.{Toast, EditText, CheckBox, Button}
import android.view.View

import android.view.ViewGroup.LayoutParams
import scala.concurrent.ops.spawn


import android.app.Activity
import com.limeblast.mydeatree._
import com.limeblast.mydeatree.providers.PrivateIdeaProvider
import com.limeblast.rest.JsonModule

import com.limeblast.androidhelpers.ScalifiedActivity
import storage.PrivateIdeaTableInfo

class IdeaEditActivity extends Activity with TypedActivity
with JsonModule with BasicIdeaModule with ScalifiedActivity{

  lazy val submitButton: Button = findView(TR.submit_button)
  lazy val publicCheckBox: CheckBox = findView(TR.idea_public_check_box)
  lazy val titleEdit: EditText = findView(TR.title_edit)
  lazy val textEdit: EditText = findView(TR.text_edit)
  lazy val oldIdea = getMainObject(getIntent.getStringExtra("idea"), classOf[Idea])

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.idea_edit_layout)

    getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT)

    titleEdit.setText(oldIdea.title)
    textEdit.setText(oldIdea.text)


    publicCheckBox.setChecked(oldIdea.public)


    submitButton.onClick((view: View) => {
      val newTitle = titleEdit.getText.toString
      val newText = textEdit.getText.toString
      val newPublic = publicCheckBox.isChecked

      if (newTitle == oldIdea.title && newText == oldIdea.text && oldIdea.public == newPublic) {
        Toast.makeText(IdeaEditActivity.this, "Idea wasn't changed", Toast.LENGTH_SHORT).show()
        finish()
      } else {
        // Get new python date
        val modified_date = Helpers.getNow()

        val newIdea = new Idea(newTitle, newText, oldIdea.id, oldIdea.parent,
          oldIdea.created_date, modified_date, oldIdea.resource_uri, newPublic) with IdeaValidationModule


        val (isIdeaValid, message) = newIdea.validate()
        if (isIdeaValid) {
          updateIdea(newIdea)
        } else {
          Toast.makeText(IdeaEditActivity.this, message, Toast.LENGTH_LONG).show()
        }
      }
    })

    findView(TR.cancel_button).onClick((view: View) => finish())
  }

  private def updateIdea(idea: Idea) {
    val isIdeaOnServer: Boolean = idea.id != null || idea.id != ""

    spawn {
      // Get the values
      val values = getContentValues(idea)
      // Add personal idea specific values
      values.put(PrivateIdeaTableInfo.KEY_PUBLIC, idea.public)

      // If idea has id update it by finding it by id
      if (isIdeaOnServer) {
        values.put(PrivateIdeaTableInfo.KEY_IS_IDEA_EDITED, true)
        // Get the address
        // Causes a crash
        //val ideaAddress = ContentUris.withAppendedId(PrivateIdeaProvider.CONTENT_URI, idea.id.toLong)

        val select = PrivateIdeaTableInfo.KEY_ID + "=" + idea.id
        // Update the idea

        getContentResolver.update(PrivateIdeaProvider.CONTENT_URI, values, select, null)
      } else {
        // No id exists so it will be a little harder to find it
        val where = PrivateIdeaTableInfo.KEY_TITLE + "='" + oldIdea.title + "' AND " + PrivateIdeaTableInfo.KEY_TEXT +
          "='" + oldIdea.text + "' AND " + PrivateIdeaTableInfo.KEY_CREATED_DATE + "='" + oldIdea.created_date + "'"

        getContentResolver.update(PrivateIdeaProvider.CONTENT_URI, values, where, null)
      }
    }



    finish()
  }
}
