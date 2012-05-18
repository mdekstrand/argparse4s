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

import net.sourceforge.argparse4j.inf.{Argument, ArgumentParser}
import net.sourceforge.argparse4j.impl.Arguments
import OptionType.Implicits._

/**
 * A command argument (flag, argument, option, etc.).
 */
abstract class CmdArg[T : OptionType](val name: String) {
  protected val typ = implicitly[OptionType[T]]
  /**
   * Add this argument to an argument parser.
   */
  def addTo(parser: ArgumentParser)

  /**
   * Helper method to set an argument's type based on the
   * option type.
   */
  protected def setType(arg: Argument) {
    typ.typeSpec match {
      case Left(t) => arg.`type`(t)
      case Right(t) => arg.`type`(t)
    }
  }

  /**
   * Get the argument's value.
   */
  def get(implicit exc: ExecutionContext): T = {
    typ.convert(exc.namespace.get(name))
  }
}

class Arg[T: OptionType](name: String)
extends CmdArg[T](name) {
  def addTo(parser: ArgumentParser) {
    val arg = parser.addArgument(name)
    arg.dest(name)
    setType(arg)
    if (typ.isMulti) {
      arg.nargs("*")
    }
  }
}

class Opt[T: OptionType](flags: Seq[OptFlag])
extends CmdArg[T](flags.head.flag) {
  def addTo(parser: ArgumentParser) {
    val arg = parser.addArgument(flags.map(_.flag): _*)
    arg.required(!(typ.isOptional || typ.isMulti))
    arg.dest(flags.head.flag)
    if (typ.isMulti) {
      arg.action(Arguments.append)
    }
    setType(arg)
  }
}

class Flag(dft: Boolean, flags: Seq[OptFlag])
extends CmdArg[Boolean](flags.head.flag) {
  def addTo(parser: ArgumentParser) {
    val arg = parser.addArgument(flags.map(_.flag): _*)
    arg.dest(flags.head.flag)
    if (dft) {
      arg.action(Arguments.storeFalse())
    } else {
      arg.action(Arguments.storeTrue())
    }
  }
}

/**
 * Methods to create different types of options.
 */
object Options {
  def argument[T: OptionType](name: String) = new Arg[T](name)
  def option[T: OptionType](flags: OptFlag*) = new Opt[T](flags)
  def flag(flags: OptFlag*) = new Flag(false, flags)
  def flag(dft: Boolean, flags: OptFlag*) = new Flag(dft, flags)
}
