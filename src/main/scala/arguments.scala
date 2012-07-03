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

  var hlp: Option[String] = None
 
  def help: String = hlp.get
  def help_=(s: String) { hlp = Some(s) }
  def help(s: String): this.type = {
    help = s
    this
  }

  /**
   * Get the argument's value.
   */
  def get(implicit exc: ExecutionContext): T = {
    typ.convert(exc.namespace.get(name))
  }

  /**
   * Override to create argument & do any initial setup.
   */
  protected def createArg(parser: ArgumentParser): Argument

  /**
   * Add this argument to an argument parser. Delegates to
   * createArg.
   */
  def addTo(parser: ArgumentParser): Argument = {
    val arg = createArg(parser)
    arg.dest(name)
    typ.typeSpec match {
      case Left(t) => arg.`type`(t)
      case Right(t) => arg.`type`(t)
    }
    hlp.foreach(arg.help(_))
    arg
  }
}

/**
 * A command argument with a value.
 */
abstract class CmdOption[T: OptionType](name: String)
extends CmdArg[T](name) {
  var meta: Option[String] = None

  def metavar: String = meta.get
  def metavar_=(s: String) { meta = Some(s) }
  def metavar(s: String): this.type = {
    metavar = s
    this
  }

  protected var dft: Option[T] = None

  def default: T = dft.get
  def default_=(v: T) { dft = Some(v) }
  def default(v: T): CmdOption[T] = {
    default = v
    this
  }

  override def addTo(parser: ArgumentParser) = {
    val arg = super.addTo(parser)
    meta.orElse(typ.defaultMetaVar).foreach(arg.metavar(_))
    for (d <- dft) {
      ArgConfig.setDefault(arg, d)
    }
    arg
  }
}

class Arg[T: OptionType](name: String)
extends CmdOption[T](name) {
  def createArg(parser: ArgumentParser) = {
    val arg = parser.addArgument(name)
    if (typ.isMulti) {
      arg.nargs("*")
    }
    arg
  }
}

class Opt[T: OptionType](flags: Seq[OptFlag])
extends CmdOption[T](flags.head.flag) {
  def createArg(parser: ArgumentParser) = {
    val arg = parser.addArgument(flags.map(_.flag): _*)
    arg.required(dft.isEmpty && !(typ.isOptional || typ.isMulti))
    if (typ.isMulti) {
      arg.action(Arguments.append)
    }
    arg
  }
}

class Flag(dft: Boolean, flags: Seq[OptFlag])
extends CmdArg[Boolean](flags.head.flag) {
  def createArg(parser: ArgumentParser) = {
    val arg = parser.addArgument(flags.map(_.flag): _*)
    if (dft) {
      arg.action(Arguments.storeFalse())
    } else {
      arg.action(Arguments.storeTrue())
    }
    arg
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
