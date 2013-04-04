package com.limeblast.androidhelpers

trait WhereClauseModule {
  def makeWhereClause(tuple: (String, Any)*): String =
    (tuple :\ "")((tuple, where) => {
      where.isEmpty match {
        case true => makeWhereClause(tuple)
        case false => where + " AND " + makeWhereClause(tuple)
      }
    })

  def makeWhereClause(tuple: (String, Any)): String =
    tuple match {
      case (one: String, two: Int) => one + "=" + two
      case (one: String, two: String) => one + "='" + two + "'"
      case (one: String, two: Boolean) => {
        two match {
          case true => one + "=" + 1
          case false => one + "=" + 0
        }

      }
      case (one: String, null) =>  one + " IS NULL"
    }

  def makeWhereClause(whereArgs: Map[String, Any]): String =
    (whereArgs :\ "")((tuple, where) =>
      where match {
        case empty: String if (empty.equals("")) => makeWhereClause(tuple)
        case _ => where + " AND " + makeWhereClause(tuple)
      })
}

trait WhereClauseImplicitModule extends WhereClauseModule {
  implicit def mapToString(map: Map[String, Any]): String = makeWhereClause(map)

  implicit def tupleToString(tuple: (String, Any)): String = makeWhereClause(tuple)
}


object WhereClauseHelper extends WhereClauseModule