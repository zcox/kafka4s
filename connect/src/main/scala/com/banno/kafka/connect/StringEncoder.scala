package com.banno.kafka.connect

import scala.concurrent.duration._
import io.circe.{Encoder => CirceEncoder}
import io.circe.syntax._

trait StringEncoder[A] {
  def encode(a: A): String
}

object StringEncoder {

  def apply[A](implicit A: StringEncoder[A]): StringEncoder[A] = A

  def stringEncoder[A](f: A => String): StringEncoder[A] = new StringEncoder[A] {
    override def encode(a: A): String = f(a)
  }

  implicit val string: StringEncoder[String] = stringEncoder(identity)
  implicit val boolean: StringEncoder[Boolean] = stringEncoder(_.toString)
  implicit val int: StringEncoder[Int] = stringEncoder(_.toString)
  implicit val long: StringEncoder[Long] = stringEncoder(_.toString)
  implicit val float: StringEncoder[Float] = stringEncoder(_.toString)
  implicit val double: StringEncoder[Double] = stringEncoder(_.toString)
  implicit val short: StringEncoder[Short] = stringEncoder(_.toString)
  implicit val byte: StringEncoder[Byte] = stringEncoder(_.toString)
  implicit val finiteDuration: StringEncoder[FiniteDuration] = stringEncoder(_.toString)

  implicit val none: StringEncoder[None.type] = stringEncoder(_ => null)
  implicit def option[A: StringEncoder]: StringEncoder[Option[A]] =
    stringEncoder(_.map(StringEncoder[A].encode).orNull)

  implicit def circe[T: CirceEncoder]: StringEncoder[T] = stringEncoder(_.asJson.noSpaces)

  //UUID
  //LocalTime
  //LocalDate
  //Instant
  //LocalDateTime
  //Map
  //List, Seq, Vector
  //Array[Byte]
  //Option
  //Either
  //BigDecimal
  //Java enum
  //Scala enum
  //CNil and Coproduct
}
