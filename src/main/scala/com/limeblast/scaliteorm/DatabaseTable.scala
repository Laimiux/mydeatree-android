package com.limeblast.scaliteorm

import scala.language.experimental.macros


class Database(val name: String, val version: Int) extends scala.annotation.StaticAnnotation

class DatabaseTable(val tableName: String) extends scala.annotation.StaticAnnotation


@Database(name = "mydeadb", version = 1) class AndroidDB

@DatabaseTable("notes") class Note(val text: String, val title: String)



