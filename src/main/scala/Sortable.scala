package com.limeblast.mydeatree

import java.util.Comparator

trait Sortable {
  private var sort_by = 0
 // private var SORT_PREF_NAME: String

  def sortObjects(mode: Int){
    sort_by = mode
    sortObjects()
  }



  def sortObjects()

  def getSortMode(): Int = sort_by
}


class IdeaComparatorByModifiedDate[A <: BasicIdea] extends Comparator[A] {

  def compare(first: A, second: A): Int = {
    val firstDate = Helpers.stringToDate(first.modified_date)
    val secondDate = Helpers.stringToDate(second.modified_date)

    if (firstDate.before(secondDate)) 1
    else if (firstDate.equals(secondDate)) 0
    else -1
  }

}

class IdeaComparatorByCreatedDate[A <: BasicIdea] extends Comparator[A] {
  def compare(first: A, second: A): Int = {
    val firstDate = Helpers.stringToDate(first.created_date)
    val secondDate = Helpers.stringToDate(second.created_date)

    if (firstDate.before(secondDate)) 1
    else if (firstDate.equals(secondDate)) 0
    else -1
  }
}

class IdeaComparatorByTitle[A <: BasicIdea] extends Comparator[A] {
  def compare(first: A, second: A): Int = {
    val firstTitle = first.title
    val secondTitle = second.title

    firstTitle.compareTo(secondTitle)
  }
}
