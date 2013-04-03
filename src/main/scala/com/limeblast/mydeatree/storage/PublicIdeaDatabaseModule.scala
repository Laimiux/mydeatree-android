package com.limeblast.mydeatree.storage

/**
 * Singleton object that holds important
 * variables about the database.
 */

object PublicIdeaTableInfo extends BasicIdeaColumns {
  // For Idea Table
  val TABLE_NAME = "public_ideas"
  // Public Idea Specific Columns
  val KEY_OWNER = "owner"
  val KEY_CHILDREN_COUNT = "children_count"
}

trait PublicIdeaDatabaseModule {

  object PublicIdeaHelper extends BasicIdeaColumns {
    // For Idea Table
    val TABLE_NAME = "public_ideas"
    // Public Idea Specific Columns
    val KEY_OWNER = "owner"
  }
}
