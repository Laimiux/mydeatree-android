package com.limeblast.mydeatree

import java.util.{List => JList, Comparator, Collections}
import android.util.Log


sealed trait SortStrategy[T] {
  def sort(list: JList[T]): Unit
}


sealed class BasicIdeaSortStrategy[T <: BasicIdea](comparator: Comparator[T]) extends SortStrategy[T] {
  def sort(list: JList[T]) = Collections.sort(list, comparator)
}

object IdeaSortStrategy {
  def getStrategy[T <: BasicIdea](sortBy: Int): Option[SortStrategy[T]] = sortBy match {
    case 0 => Some(new BasicIdeaSortStrategy(new IdeaComparatorByModifiedDate[T]))
    case 1 => Some(new BasicIdeaSortStrategy(new IdeaComparatorByCreatedDate[T]))
    case 2 => Some(new BasicIdeaSortStrategy(new IdeaComparatorByTitle[T]))
    case z => {
      Log.d("IdeaSortStrategy", "Unhandled case " + z)
      None
    }
  }


  def compareByModifiedDate(first: BasicIdea, second: BasicIdea): Boolean = {
    val firstDate = Helpers.stringToDate(first.modified_date)
    val secondDate = Helpers.stringToDate(second.modified_date)

    firstDate.after(secondDate)
  }

  def compareByCreatedDate(first: BasicIdea, second: BasicIdea): Boolean = {
    val firstDate = Helpers.stringToDate(first.modified_date)
    val secondDate = Helpers.stringToDate(second.modified_date)

    firstDate.after(secondDate)
  }

}


//-------------------------------------------------------\\
//----------- Idea Comparators for Sorting --------------\\
//-------------------------------------------------------\\
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
    val firstTitle = first.title.toUpperCase()
    val secondTitle = second.title.toUpperCase()

    firstTitle.compareTo(secondTitle)
  }
}

