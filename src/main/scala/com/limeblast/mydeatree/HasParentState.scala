package com.limeblast.mydeatree

/**
 * Created with IntelliJ IDEA.
 * User: Laimiux
 * Date: 4/3/13
 * Time: 4:31 PM
 * To change this template use File | Settings | File Templates.
 */
trait HasParentState[T] {
  // Parent object can be None
  var parentObject: Option[T]

  def setParent(parent: Option[T]) {
    parentObject = parent
  }
}
