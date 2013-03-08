package com.limeblast.scaliteorm

import scala.reflect.runtime.universe._


class Attribute(val _name: String, val _type: String, val _attributeType: AttributeType)



class AttributeType()


trait IClassMap[T] {
  def ID()
}