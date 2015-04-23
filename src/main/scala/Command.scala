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
import net.sourceforge.argparse4j.inf.ArgumentParser
import net.sourceforge.argparse4j.internal.HelpScreenException

/**
 * Base trait for commands. The argparse4s library is built around
 * commands, defined by this trait, which define and access their
 * arguments using the provided OptionSet.
 */
trait Command
extends CommandLike
with OptFlagImplicits
with OptionType.Implicits {
  protected val context = ArgContext("command:" + name)

  /**
   * Create an argument parser for this command.
   */
  def parser: ArgumentParser = {
    val parser = ArgumentParsers.newArgumentParser(name)
    for (desc <- Option(description)) {
      parser.description(desc)
    }
    addArguments(parser)
    parser
  }

  /**
   * Run a command with some arguments. This parses the arguments with the
   * parser, prepares and [[ExecutionContext]], and invokes the other
   * `run` method.
   */
  def run(args: Seq[String]) {
    val p = parser
    try {
      val ns = p.parseArgs(args.toArray)
      implicit val exc = ExecutionContext(ns)
      run()
    }
    catch {
      case e: HelpScreenException => return
    }
  }
}
