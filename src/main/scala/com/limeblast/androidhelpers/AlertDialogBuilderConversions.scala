package com.limeblast.androidhelpers

import android.app.AlertDialog.Builder
import android.content.DialogInterface


sealed trait ImplicitFunctionToDialogOnClickListener {
  implicit def functionToDialogOnClickListener[F](f:(DialogInterface, Int) => F): DialogInterface.OnClickListener =
    new DialogInterface.OnClickListener {
      def onClick(dialog: DialogInterface, position: Int) {
        f(dialog, position)
      }
    }
}

sealed trait AlertBuilderAcceptFunctions extends ImplicitFunctionToDialogOnClickListener {
  def bldr: Builder

  def setItems[F](itemsid: Int, f:(DialogInterface, Int) => F): Builder = bldr.setItems(itemsid, f)

  def setNegativeButton[F](itemsid: Int, f:(DialogInterface, Int) => F): Builder = bldr.setNegativeButton(itemsid, f)

  def setPositiveButton[F](itemsid: Int, f:(DialogInterface, Int) => F): Builder = bldr.setPositiveButton(itemsid, f)

  def setSingleChoiceItems[F](itemsid: Int, checkedItem: Int, f: (DialogInterface, Int) => F): Builder = bldr.setSingleChoiceItems(itemsid, checkedItem, f)
}

trait AlertDialogBuilderConversions {
  implicit def alertDialogBuilderAcceptsFunction(builder: Builder): AlertBuilderAcceptFunctions = new AlertBuilderAcceptFunctions {
    val bldr: Builder = builder
  }
}


/*

  */