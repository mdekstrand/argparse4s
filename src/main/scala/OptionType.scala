package net.elehack.argparse4s

import collection.JavaConverters._
import net.sourceforge.argparse4j.inf.ArgumentType
import java.io.File

/**
 * Type class for valid types of options.
 */
trait OptionType[T] {
  def isOptional: Boolean = false
  def isMulti: Boolean = false
  def typeSpec: Either[Class[_], ArgumentType[_]]
  def convert(obj: AnyRef): T
  def defaultMetaVar: Option[String] = None
}

object OptionType {
  class Default[A](cls: Class[_]) extends OptionType[A] {
    def typeSpec = Left(cls)
    def convert(obj: AnyRef) = obj.asInstanceOf[A]
  }
  trait FallbackImplicits {
    implicit def anyOptionType[A](implicit mf: Manifest[A]): OptionType[A] =
      new Default[A](mf.erasure)
  }

  trait Implicits extends FallbackImplicits {
    implicit val fileOptionType: OptionType[File] = new Default[File](classOf[File]) {
      override def defaultMetaVar = Some("FILE")
    }

    implicit def optionOptionType[A : OptionType]: OptionType[Option[A]] = {
      new OptionType[Option[A]] {
        val otype = implicitly[OptionType[A]]
        override def isOptional = true
        override def isMulti = false
        def typeSpec = otype.typeSpec
        def convert(obj: AnyRef) = Option(obj).map(_.asInstanceOf[A])
      }
    }

    implicit def sequenceOptionType[A: OptionType]:
    OptionType[Seq[A]] = {
      new OptionType[Seq[A]] {
        val otype = implicitly[OptionType[A]]
        override def isOptional = false
        override def isMulti = true
        def typeSpec = otype.typeSpec
        def convert(obj: AnyRef) =
          obj.asInstanceOf[java.util.List[A]].asScala
      }
    }

    /* Since argparse4j doesn't recognize primitive classes,
     * we just define implicits for all the primitives. */
    implicit def intOptionType = new Default[Int](classOf[Integer])
    implicit def longOptionType = new Default[Long](classOf[java.lang.Long])
    implicit def shortOptionType = new Default[Short](classOf[java.lang.Short])
    implicit def byteOptionType = new Default[Byte](classOf[java.lang.Byte])
    implicit def floatOptionType = new Default[Float](classOf[java.lang.Float])
    implicit def dblOptionType = new Default[Double](classOf[java.lang.Double])
    implicit def chrOptionType = new Default[Char](classOf[java.lang.Character])
    implicit def boolOptionType =
      new Default[Boolean](classOf[java.lang.Boolean])
  }

  object Implicits extends Implicits
}
