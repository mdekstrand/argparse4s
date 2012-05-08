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

import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.inf.{ArgumentParser, Namespace}
import net.sourceforge.argparse4j.impl.Arguments
import util.DynamicVariable

class OptionSet(val parser: ArgumentParser) {
  private var currentNamespace: Option[Namespace] = None

  def namespace: Namespace = currentNamespace getOrElse {
    throw new IllegalStateException(
      "accessed option outside cmd line invocation")
  }

  class Opt[T : OptionType](val name: String) {
    private val optionType = implicitly[OptionType[T]]
    def get: T = {
      val ns = namespace
      optionType.convert(ns.get(name))
    }
  }

  def this(name: String) {
    this(ArgumentParsers.newArgumentParser(name))
  }

  def this() {
    this("command")
  }

  def argument[T : OptionType](name: String): Opt[T] = {
    val typ = implicitly[OptionType[T]]
    val arg = parser.addArgument(name)
    arg.dest(name)
    if (typ.isMulti) {
      arg.nargs("*")
    }
    typ.typeSpec match {
      case Left(t) => arg.`type`(t)
      case Right(t) => arg.`type`(t)
    }
    new Opt(name)
  }

  def option[T : OptionType](flags: OptFlag*): Opt[T] = {
    require(flags.length > 0, "not enough arguments")
    val typ = implicitly[OptionType[T]]
    val arg = parser.addArgument(flags.map(_.flag): _*)
    arg.required(!(typ.isOptional || typ.isMulti))
    arg.dest(flags.head.flag)
    if (typ.isMulti) {
      arg.action(Arguments.append())
    }
    typ.typeSpec match {
      case Left(t) => arg.`type`(t)
      case Right(t) => arg.`type`(t)
    }
    new Opt(flags.head.flag)
  }

  def flag(flags: OptFlag*): Opt[Boolean] = flag(false, flags: _*)

  def flag(dft: Boolean, flags: OptFlag*): Opt[Boolean] = {
    val arg = parser.addArgument(flags.map(_.flag): _*)
    arg.dest(flags.head.flag)
    if (dft) {
      arg.action(Arguments.storeFalse())
    } else {
      arg.action(Arguments.storeTrue())
    }
    import OptionType.Implicits._
    new Opt[Boolean](flags.head.flag)
  }

  private[argparse4s]
  def withNamespace[R](ns: Namespace)(block: => R) = {
    val old = currentNamespace
    currentNamespace = Some(ns)
    try {
      block
    } finally {
      currentNamespace = old
    }
  }
}

