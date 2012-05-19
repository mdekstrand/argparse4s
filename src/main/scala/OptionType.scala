/* Copyright ⓒ 2012 Michael Ekstrand
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject
 * to the following conditions:
 *
 *  - The above copyright notice and this permission notice shall be
 *    included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package net.elehack.argparse4s

import collection.JavaConverters._
import net.sourceforge.argparse4j.inf.ArgumentType
import java.io.File

/**
 * Type class for valid types of options.
 */
trait OptionType[+T] {
  def isOptional: Boolean = false
  def isMulti: Boolean = false
  def typeSpec: Either[Class[_], ArgumentType[_]]
  def convert(obj: AnyRef): T
  def defaultMetaVar: Option[String] = None
}

object OptionType {
  class Default[A](cls: Class[_]) extends OptionType[A] {
    def this()(implicit mf: Manifest[A]) {
      this(mf.erasure)
    }

    def typeSpec = Left(cls)
    def convert(obj: AnyRef) = obj.asInstanceOf[A]
  }

  def default[A <: AnyRef](implicit mf: Manifest[A]) =
    new Default(mf.erasure)

  trait Implicits {

    implicit val fileOptionType: OptionType[File] = new Default[File] {
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

    implicit def sequenceOptionType[A : OptionType]:
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

    implicit def strOptionType = new OptionType[String] {
      def typeSpec = Left(classOf[String])
      def convert(obj: AnyRef) = obj.toString
    }
  }

  object Implicits extends Implicits
}
