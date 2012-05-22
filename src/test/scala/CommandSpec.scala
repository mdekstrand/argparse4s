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

class CommandSpec extends FunSpec
with ShouldMatchers
with GivenWhenThen {
  import TestUtils._

  describe("Command trait") {
    it("should parse no arguments") {
      given("an empty command")
      object command extends TestCommand("no-arg")
      when("the command is executed with no arguments")
      command.run(Seq())
      then("the command runs")
      command.hasRun should be (true)
    }

    def oneArgCommand = {
      given("a command with a single named argument")
      new TestCommand("one-arg") {
        val file = argument[String]("file")
      }
    }

    it("should parse a string argument") {
      val cmd = oneArgCommand
      when("the arguments are parsed")
      cmd.withArgs("foo") {
        then("the name should be set")
        cmd.file.get should equal("foo")
      }
    }

    it("should fail with missing required argument") {
      val cmd = oneArgCommand
      when("the command is executed with no arguments")
      then("the execution should fail")
      evaluating {
        cmd.run(Seq())
      } should produce [ArgumentParserException]
    }

    it("should parse a multi-item argument") {
      given("a command with a Seq argument")
      object cmd extends TestCommand("seq-arg") {
        val args = argument[Seq[String]]("files")
      }
      when("the command is with multiple arguments")
      cmd.withArgs("foo", "bar") {
        then("the args are set")
        cmd.args.get should equal (Seq("foo", "bar"))
      }
    }

    it("should parse a multi-item argument to empty") {
      given("a command with a Seq argument")
      object cmd extends TestCommand("seq-arg") {
        val args = argument[Seq[String]]("files")
      }
      when("the command is with multiple arguments")
      cmd.withArgs() {
        then("the args are set")
        cmd.args.get should be ('empty)
      }
    }

    it("should parse an option") {
      given("a command with a single option")
      object cmd extends TestCommand("opt") {
        val o = option[String]("foo")
      }

      when("the command is executed with its argument")
      cmd.withArgs("--foo", "wombat") {
        then("the option has the provided value")
        cmd.o.get should equal ("wombat")
      }
    }

    it("should parse an optional option") {
      given("a command with a single option")
      object cmd extends TestCommand("opt") {
        val o = option[Option[String]]("foo")
      }

      when("the command is executed with its argument")
      cmd.withArgs("--foo", "wombat") {
        then("the option has the provided value")
        cmd.o.get should equal (Some("wombat"))
      }
    }

    it("should parse an option w/ short name") {
      given("a command with a single option")
      object cmd extends TestCommand("opt") {
        val o = option[String]('f')
      }

      when("the command is executed with its argument")
      cmd.withArgs("-f", "wombat") {
        then("the option has the provided value")
        cmd.o.get should equal ("wombat")
      }
    }

    it("should parse an int option") {
      given("a command with a single int option")
      object cmd extends TestCommand("int-opt") {
        val opt = option[Int]('n')
      }

      when("the command is executed with its argument")
      cmd.withArgs("-n", "32") {
        then("the option has the provided value")
        cmd.opt.get should equal (32)
      }
    }

    it("should parse a flag") {
      given("a command with a flag")
      object cmd extends TestCommand("flag") {
        val f = flag('v', "verbose")
      }
      when("the command is executed with the flag")
      cmd.withArgs("--verbose") {
        then("the flag is set")
        cmd.f.get should be (true)
      }
    }

    it("should parse a negated flag") {
      given("a command with a flag")
      object cmd extends TestCommand("flag") {
        val f = flag(true, 'v', "verbose")
      }
      when("the command is executed with the flag")
      cmd.withArgs("-v") {
        then("the flag is set")
        cmd.f.get should be (false)
      }
    }

    it("should default a missing flag") {
      given("a command with flag")
      object cmd extends TestCommand("flag") {
        val f = flag('v', "verbose")
      }
      when("the command is executed without the flag")
      cmd.withArgs() {
        then("the flag is unset")
        cmd.f.get should be (false)
      }
    }

    it("should parse blended options & args") {
      given("a command with some options and arguments")
      object cmd extends TestCommand("multi-opt") {
        val n = option[Int]('n')
        val outFile = option[Option[File]]('o', "out")
        val dbgFile = option[Option[File]]('D', "debug-file")
        val dryRun = flag("dry-run")
        val verbose = flag('v', "verbose")
        val inFiles = argument[Seq[File]]("file")
      }
      when("the command is invoked")
      cmd.withArgs("-o", "/dev/null", "-n", "42", "--dry-run",
                       "foo", "bar", "blam") {
        then("the options should be set")
        cmd.n.get should equal(42)
        cmd.outFile.get should be (Some(new File("/dev/null")))
        cmd.inFiles.get should have length 3
        cmd.inFiles.get should contain(new File("bar"))
        cmd.dryRun.get should be (true)
        and("the missing option should be unset")
        cmd.dbgFile.get should be (None)
        cmd.verbose.get should be (false)
      }
    }

    it("should use a default value") {
      given("a command wth an argument w/ a default value")
      object cmd extends TestCommand("default-opt") {
        val n = option[Int]('n').default(10)
      }
      when("the command is invoked without arguments")
      cmd.withArgs() {
        then("the option should have its default value")
        cmd.n.get should equal (10)
      }
    }
  }
}
