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

import collection.mutable.{Buffer, ListBuffer}
import net.sourceforge.argparse4j.inf.ArgumentParser

/**
 * Things that are like commands (commands, subcommands). They
 * can be invoked and have options.
 */
trait CommandLike {
  /**
   * The command name. For commands, this is used when printing
   * help; for subcommands, it is the name used to invoke them.
   */
  def name: String
  /**
   * The command's descriptive text.
   */
  def description: String = null

  protected def context: ArgContext

  /**
   * Execute the command. Override this method to define
   * the command's behavior.
   * 
   * @param exc The execution context. Carry this around
   *            to get access to your options.
   */
  def run()(implicit exc: ExecutionContext)

  import OptionType.Implicits._
  private var optAccum: Buffer[CmdArg[_]] = new ListBuffer
  
  private def record[A <: CmdArg[_]](arg: A): A = {
    optAccum += arg
    arg
  }
    
  protected def argument[T: OptionType](name: String) =
    record(Options.argument[T](context, name))
  protected def option[T: OptionType](flags: OptFlag*) =
    record(Options.option[T](context, flags: _*))
  protected def flag(flags: OptFlag*) =
      record(Options.flag(context, flags: _*))
  protected def flag(dft: Boolean, flags: OptFlag*) =
    record(Options.flag(context, dft, flags: _*))

  def addArguments(parser: ArgumentParser) {
    optAccum.foreach(_ addTo parser)
  }
}
