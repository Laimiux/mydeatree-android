package com.limeblast.mydeatree



import scala.concurrent._
import scala.util.{Failure, Success}
import android.util.Log
import ExecutionContext.Implicits.global

class A
class B
class C(a: A, b: B) { override def toString = s"${super.toString}(${a}, ${b})" }
class D(b: B, c: C) { override def toString = s"${super.toString}(${b}, ${c})" }

object SimpleFeatureTest {

  lazy val APP_TAG = "SimpleFeatureTest"

  def test() {


    val futureList: Future[List[String]] = future {
      List("Some", "Are", "Better")
    }

    futureList onComplete {
      case Success(entries) =>  Log.d("SimpleFeatureTest", "Success: " + entries.foldLeft("")(_ + " " + _))
      case Failure(t) => Log.d("SimpleFeatureTest", "A problem occurred: " + t.getMessage)
    }

    import com.limeblast.androidhelpers.LoggerMacros.logger

    val username = "laimis"

    logger(APP_TAG, username)


    /*
   val theC = wire[C]

val d = wire[D]
*/
  }
}

