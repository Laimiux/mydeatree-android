package com.limeblast.utils

trait Resource[T] {
  def close(t: T): Unit
}

object Resource {
  implicit def genericResourceTrait[A <: {def close() : Unit}] = new Resource[A] {
    override def close(r: A) = r.close()
  }
}

object ResourceHelper {
  def withResource[A: Resource, B](resource: => A)(f: A => B): Unit = {
    try {
      f(resource)
    } finally {
      implicitly[Resource[A]].close(resource)
    }
  }
}

