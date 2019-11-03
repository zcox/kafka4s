package com.banno.kafka.connect

import shapeless._
import shapeless.ops.hlist.{LeftFolder, Zip}
import org.apache.kafka.common.config.ConfigDef

trait ConfigDefEncoder[A] {
  def encode: ConfigDef
}

object ConfigDefEncoder {

  case class Documentation(s: String)

  def apply[A](implicit A: ConfigDefEncoder[A]): ConfigDefEncoder[A] = A

  object ConfigDefFold extends Poly2 {
    implicit def a[V: StringEncoder] =
      at[ConfigDef, ((FieldName[V], Option[V]), Some[Documentation])] {
        case (cd, ((f, d), Some(Documentation(docs)))) =>
          d match {
            case Some(default) =>
              cd.define(
                f.name,
                ConfigDef.Type.STRING,
                StringEncoder[V].encode(default),
                ConfigDef.Importance.MEDIUM,
                docs
              )
            case None =>
              cd.define(f.name, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, docs)
          }
      }
  }

  implicit def genericConfigDefEncoder[
      A,
      R <: HList,
      F <: HList,
      D <: HList,
      N <: HList,
      Z1 <: HList,
      Z2 <: HList
  ](
      implicit gen: LabelledGeneric.Aux[A, R],
      fieldNames: FieldNames.Aux[R, F],
      defaults: Default.AsOptions.Aux[A, D],
      annotations: Annotations.Aux[Documentation, A, N],
      z1: Zip.Aux[F :: D :: HNil, Z1],
      z2: Zip.Aux[Z1 :: N :: HNil, Z2],
      folder: LeftFolder.Aux[Z2, ConfigDef, ConfigDefFold.type, ConfigDef]
  ): ConfigDefEncoder[A] =
    new ConfigDefEncoder[A] {
      override def encode: ConfigDef = {
        val _ = gen //convince compiler that we need gen
        fieldNames().zip(defaults()).zip(annotations()).foldLeft(new ConfigDef())(ConfigDefFold)
      }
    }
}
