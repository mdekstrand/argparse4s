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

import java.io.File
import org.scalatest.{FunSpec, GivenWhenThen}
import org.scalatest.matchers.ShouldMatchers

import net.sourceforge.argparse4j.inf.ArgumentParserException

import util.DynamicVariable

class SubcommandSpec
extends FunSpec
with ShouldMatchers
with GivenWhenThen {
  import TestUtils._

  describe("Subcomand") {
    it("should register a subcommand") {
      given("a subcommand")
      object subcmd extends Subcommand {
        def name = "foo"
        var ran = false
        def run()(implicit exc: ExecutionContext) {
          ran = true
        }
      }
      and("a command tha uses it")
      object cmd extends TestCommand("test-command") with MasterCommand {
        override def subcommands = Seq(subcmd)
      }

      when("the command is run")
      cmd.withArgs("foo") {
        then("the subcommand is selected")
        cmd.subcommand should be (Some(subcmd))
      }
    }


    it("should propagate arguments to the subcommand") {
      given("a subcommand")
      object subcmd extends Subcommand {
        def name = "foo"
        def arg = argument[File]("file")
        def verbose = flag('v', "verbose")
        def delim = option[Option[String]]('d', "delim")
        var ran = false
        def run()(implicit exc: ExecutionContext) {
          arg.get should equal (new File("/dev/null"))
          verbose.get should be (false)
          delim.get should be (Some(":"))
        }
      }
      and("a command tha uses it")
      object cmd extends TestCommand("test-command") with MasterCommand {
        def subcommands = Seq(subcmd)
      }

      when("the command is run")
      cmd.withArgs("foo", "-d", ":", "/dev/null") {
        then("the subcommand is selected")
        cmd.subcommand should be (subcmd)
        and("the arguments are passed correctly")
        subcmd.run()
      }
    }
  }
}
