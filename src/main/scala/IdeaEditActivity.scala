package com.limeblast.mydeatree

import android.os.Bundle
import android.widget.{Toast, EditText, CheckBox, Button}
import android.view.View

import android.content.{ContentUris, Intent}
import com.actionbarsherlock.app.SherlockActivity
import android.view.ViewGroup.LayoutParams
import scala.concurrent.ops.spawn

import com.limeblast.androidhelpers.AndroidImplicits.toListener

import com.limeblast.androidhelpers.AndroidHelpers

class IdeaEditActivity extends SherlockActivity with TypedActivity {

  lazy val submitButton: Button = findView(TR.submit_button)
  lazy val publicCheckBox: CheckBox = findView(TR.idea_public_check_box)
  lazy val titleEdit: EditText = findView(TR.title_edit)
  lazy val textEdit: EditText = findView(TR.text_edit)
  lazy val oldIdea = JsonWrapper.getMainObject(getIntent.getStringExtra("idea"), classOf[Idea])

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.idea_edit_layout)

    getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT)

    titleEdit.setText(oldIdea.title)
    textEdit.setText(oldIdea.text)

    if (oldIdea.public) {
      publicCheckBox.setChecked(true)
    }

    submitButton.setOnClickListener((view: View) => {
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
          oldIdea.created_date, modified_date, oldIdea.resource_uri, newPublic)

        val (isIdeaValid, message) = ResourceValidation.validate_idea(newIdea)
        if (isIdeaValid) {
          updateIdea(newIdea)
        } else {
          Toast.makeText(IdeaEditActivity.this, message, Toast.LENGTH_LONG).show()
        }
      }
    })

    findView(TR.cancel_button).setOnClickListener((view: View) => finish())
  }

  private def updateIdea(idea: Idea) {
    val isIdeaOnServer: Boolean = idea.id != null
    spawn {

      // Get the values
      val values = IdeaTableHelper.createNewIdeaValues(idea)

      // If idea has id update it by finding it by id
      if (isIdeaOnServer) {
        values.put(IdeaHelper.KEY_IS_IDEA_EDITED, true)
        // Get the address
        val ideaAddress = ContentUris.withAppendedId(RESTfulProvider.CONTENT_URI, idea.id.toLong)
        // Update the idea
        val cr = getContentResolver
        cr.update(ideaAddress, values, null, null)
      } else {



        // No id exists so it will be a little harder to find it
        val resolver = getContentResolver
        val where = IdeaHelper.KEY_TITLE + "='" + oldIdea.title + "' AND " + IdeaHelper.KEY_TEXT +
          "='" + oldIdea.text + "' AND " + IdeaHelper.KEY_CREATED_DATE + "='" + oldIdea.created_date + "'"

        resolver.update(RESTfulProvider.CONTENT_URI, values, where, null)
      }


    }

    // Only start update service if there is network connection
    if (AndroidHelpers.isOnline(this)) {
      // If idea is on server, update it
      if (isIdeaOnServer)
        startServiceToUpdateIdea(idea)
      // otherwise upload a new one
      else
        startServiceToCreateIdea(idea)
    }

    finish()
  }

  private def startServiceToCreateIdea(idea: Idea) {

  }

  private def startServiceToUpdateIdea(idea: Idea) {
    // Create the intent for the service
    val intent = new Intent(this, classOf[IdeaUpdateService])
    intent.putExtra("idea", JsonWrapper.convertObjectToJson(idea))

    // Start service
    startService(intent)
  }

}
