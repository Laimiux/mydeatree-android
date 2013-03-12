package com.limeblast.mydeatree

/**
 * Created with IntelliJ IDEA.
 * User: Laimiux
 * Date: 3/11/13
 * Time: 11:11 PM
 * To change this template use File | Settings | File Templates.
 */
trait IdeaValidationModule extends ValidationModule {self: Idea =>
  //type Idea
  private val idea = self

  def validate(): (Boolean, String) = {
    if(idea.title.length < 5)
      (false, "Title is too short; it has to be 5 or more characters")
    else if(idea.title.length > 30)
      (false, "Title is too long; it has to be 30 or less characters")
    else if(idea.text.length < 10)
      (false, "Text is too short; it has to be 10 or more characters")
    else if (idea.text.length > 140)
      (false, "Text is too long: it has to be 140 or less characters")
    else
      (true, "Validated correctly")
  }
}

