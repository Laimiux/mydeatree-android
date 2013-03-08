package com.limeblast.scaliteorm

trait DatabaseColumn{
  val columnName: String
}

class SimpleDatabaseColumn(val columnName: String, val extraDefinition: String) extends DatabaseColumn {
  override def toString(): String = columnName + " " + extraDefinition
}
