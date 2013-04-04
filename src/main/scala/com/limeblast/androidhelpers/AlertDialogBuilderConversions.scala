package com.limeblast.androidhelpers

import android.app.AlertDialog.Builder
import android.content.{Context, DialogInterface}
import android.app.AlertDialog


sealed trait ImplicitFunctionToDialogOnClickListener {
  implicit def functionToDialogOnClickListener[F](f: (DialogInterface, Int) => F): DialogInterface.OnClickListener =
    new DialogInterface.OnClickListener {
      def onClick(dialog: DialogInterface, position: Int) {
        f(dialog, position)
      }
    }
}

sealed trait AlertBuilderAcceptFunctions extends ImplicitFunctionToDialogOnClickListener {
  def bldr: Builder

  def setItems[F](itemsid: Int, f: (DialogInterface, Int) => F): Builder = bldr.setItems(itemsid, f)

  def setNegativeButton[F](itemsid: Int, f: (DialogInterface, Int) => F): Builder = bldr.setNegativeButton(itemsid, f)

  def setPositiveButton[F](itemsid: Int, f: (DialogInterface, Int) => F): Builder = bldr.setPositiveButton(itemsid, f)

  def setSingleChoiceItems[F](itemsid: Int, checkedItem: Int, f: (DialogInterface, Int) => F): Builder = bldr.setSingleChoiceItems(itemsid, checkedItem, f)
}

trait AlertDialogBuilderConversions {
  implicit def alertDialogBuilderAcceptsFunction(builder: Builder): AlertBuilderAcceptFunctions = new AlertBuilderAcceptFunctions {
    val bldr: Builder = builder
  }
}

object AlertDialogHelper extends ImplicitFunctionToDialogOnClickListener {
  type DialogClickHandler = (DialogInterface, Int) => Unit

  private def closeDialogOnCancel(dialog: DialogInterface, arg: Int) = dialog.cancel()

  def showYesCancelDialog(title: String, message: String, onYesClick: DialogClickHandler,
                          onCancelClick: DialogClickHandler = closeDialogOnCancel)(implicit context: Context): AlertDialog = {

    val builder = new Builder(context: Context)
    builder.setTitle(title)
    builder.setMessage(message)

    builder.setPositiveButton("Yes", onYesClick)
    builder.setNegativeButton("No", onCancelClick)


    builder.show()
  }
}


/*
     /*
    private def showIdeaDeleteDialog(idea: Idea) {
    val builder = new AlertDialog.Builder(getActivity)
    builder.setTitle("Delete " + idea.title + "?")
    builder.setMessage("Are you sure you want to delete this idea?")
    builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
      def onClick(dialog: DialogInterface, arg: Int) {
        spawn {
          removeIdeaRequest(idea)
        }
        dialog.dismiss()
      }
    })

    builder.setNegativeButton(R.string.cancel, (dialog: DialogInterface, arg: Int) => dialog.cancel())

    builder.create().show()
  }

   */
  */