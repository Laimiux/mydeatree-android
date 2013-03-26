import java.nio.ByteBuffer
import java.sql.{DriverManager, Connection}
import scalaz.Scalaz._
/**
 * Created with IntelliJ IDEA.
 * User: Laimiux
 * Date: 3/25/13
 * Time: 1:44 PM
 * To change this template use File | Settings | File Templates.
 */
trait Encodable[T] {
  def encode(t: T): Array[Byte]
  def decode(buf: ByteBuffer): T
}

object Encodable {
  implicit object IntEncodable extends Encodable[Int] {
    def decode(buf: ByteBuffer): Int = ???

    def encode(t: Int): Array[Byte] = ???
  }
  def encode[T: Encodable](t: T) = implicitly[Encodable[T]].encode(t)
}


object Applicative {
  def connection(url: Option[String], username: Option[String], password: Option[String]): Option[Connection] =
    (url |@| username |@| password) apply DriverManager.getConnection
}