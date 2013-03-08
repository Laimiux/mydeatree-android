package com.limeblast.scaliteorm

import reflect.Manifest

class Attribute(val _name: String, val _type: String, val _attributeType: AttributeType)



class AttributeType()


trait IClassMap[T] {
  def ID()
}