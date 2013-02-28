package com.limeblast.mydeatree

object ResourceValidation {

  def validate_idea(idea: Idea): (Boolean, String) = {
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
