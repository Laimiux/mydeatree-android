package com.limeblast.scaliteorm


object Implicits {
  implicit def tupleToSimpleColumn(column: (String, String)): SimpleDatabaseColumn = new SimpleDatabaseColumn(column._1, column._2)
  implicit def tupleToSimpleColumn2(column: { val _1: Any; val _2: Any}): SimpleDatabaseColumn = new SimpleDatabaseColumn(column._1.toString, column._2.toString)
}

class TableDefinition(val tableName: String) {
  //import Implicits.tupleToSimpleColumn
  var columns = List[DatabaseColumn]()


  def insert(columnMap: (String, String)*) = {
    columnMap.foreach(column => {
      columns = columns :+ new SimpleDatabaseColumn(column._1, column._2)
    })
  }

  def getColumnString(): String = (columns :\ "")((column, string) => {
    string.isEmpty match {
      case true => column.toString
      case false => string + ", " + column.toString()
    }
  })

  override def toString(): String = "CREATE TABLE " + tableName + " ( " + getColumnString() + " );"
}
