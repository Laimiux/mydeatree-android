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

import com.limeblast.androidhelpers.{WhereClauseHelper, ScalifiedActivity}
import storage.PrivateIdeaTableInfo

class IdeaEditActivity extends Activity with TypedActivity
with JsonModule with BasicIdeaModule with ScalifiedActivity{

  lazy val submitButton: Button = findView(TR.submit_button)
  lazy val publicCheckBox: CheckBox = findView(TR.idea_public_check_box)
  lazy val titleEdit: EditText = findView(TR.idea_title_edit)
  lazy val textEdit: EditText = findView(TR.idea_text_edit)


  lazy val ideaId = getIntent.getStringExtra("idea_id")

  private var oldIdea: Idea = _

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.idea_edit_layout)


    val form = findView(TR.idea_edit_form_holder)
    form.setVisibility(View.INVISIBLE)

    getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT)

    // if idea is passed, then everything is good. Otherwise we need to retrieve the idea.
    val intent = getIntent()

    val ideaJson = intent.getStringExtra("idea")



    if (ideaJson == null || ideaJson == "") {
      if (ideaId == null || ideaId == "")
         throw new IllegalArgumentException("IdeaEditActivity either needs either idea json object or ideas id. Parameters: idea || idea_id")

      // No idea json was passed so we have to get the idea
      getIdeaFromDatabase(ideaId) match {
        case None => //DOwnload idea from the server
        case Some(idea) => oldIdea = idea
      }


    } else {
      oldIdea = getMainObject(getIntent.getStringExtra("idea"), classOf[Idea])
    }


    // Hide the progress bar, show the form
    findView(TR.idea_edit_form_loader).setVisibility(View.GONE)
    form.setVisibility(View.VISIBLE)



    // Should instead get an id. And using id try to retrieve from database
    // If not on database, then try downloading it. If can't download, remove from database.
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


  private def getIdeaFromDatabase(id: String): Option[Idea] = {
    val select = WhereClauseHelper.makeWhereClause(PrivateIdeaTableInfo.KEY_ID -> id)
    val cursor = getContentResolver.query(PrivateIdeaProvider.CONTENT_URI, null, select, null, null)

    PersonalIdeaAccessor.getIdeaFromCursor(cursor)
  }
}
