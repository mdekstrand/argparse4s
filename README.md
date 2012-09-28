argparse4s is an argument parsing library for Scala built on
[argparse4j][], which in turn is inspired by the Python `argparse`
module.

[argparse4j]: http://argparse4j.sourceforge.net

# Using argparse4s

First, add argparse4s to your project dependencies. In SBT:

    libraryDependencies += "net.elehack.argparse4s" %% "argparse4s" % "0.2"

The main entry point for argparse4s is the `Command` trait. To use argparse4s,
create an object extending `Command` whose `main` method calls `Command`'s
`run(Array[String])` method. It will then parse the arugments, prepare an
`ExecutionContext`, and pass it to your command's `run()` method.

Options are defined using the `option`, `flag`, and `argument` methods (from
`CommandLike`). These methods return objects representing the arguments which
can in turn be used to set additional parameters (defaults, help messages,
etc.) and to get the actual value (using the `get` method, which requires an
implicit execution context).

An example:

    object MyCommand extends Command {
        /* characters are short options (-v), strings long (--verbose). */
        val verbose = flag('v', "verbose").
                      help("emit verbose output")
        /* Option-typed options are optional. Files have a default
           metavar of FILE. /
        val outputFile = option[Option[File]]('o', "output-file").
                         help("output to FILE")
        /* Options can have defaults. */
        val count = option[Int]('n', "count").
                    default(5).
                    metavar("N").
                    help("run up to N iterations")

        /* accumulate all arguments as an input */
        val inputs = argument[Seq[File]]()

        /**
         * The run() method contains your program logic.
         */
        def run()(implicit exc: ExecutionContext) {
            /* get argument values with the get method */
            for (i <- 1 to count.get) {
                /* do something */
            }
        }

        /**
         * main calls out to argparse4s.
         */
        def main(args: Array[String]) {
            run(args)
        }
    }

For more details see the [scaladoc][].

[scaladoc]: http://elehack.net/projects/argparse4s/scaladoc/

# Customizing argument types

The `OptionType` typeclass allows you to specify behavior for specific
option/argument types, including parsing strategies and the default metavar
name.

# Subcommands

argparse4s inherits from argparse4j the ability to have subcommands. To
support subcommands, extend the `MasterCommand` trait. Each subcommand must
extends `Subcommand`.
