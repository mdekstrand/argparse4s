package net.elehack.argparse4s.example;

import java.io.File

import net.elehack.argparse4s.{Command, ExecutionContext}

/**
 * Created by cy on 2015/4/23 22:58.
 */
object MyCommand extends Command {
  /* characters are short options (-v), strings long (--verbose). */
  val verbose = flag('v', "verbose").
    help("emit verbose output")
  /* Option-typed options are optional. Files have a default
     metavar of FILE. */
  val outputFile = option[Option[File]]('o', "output-file").
    help("output to FILE")
  /* Options can have defaults. */
  val count = option[Int]('n', "count").
    default(5).
    metavar("N").
    help("run up to N iterations")

  /* accumulate all arguments as an input */
  val inputs = argument[Seq[File]]("124")

  /**
   * The run() method contains your program logic.
   */
  def run()(implicit exc: ExecutionContext) {
    /* get argument values with the get method */
    for (i <- 1 to count.get) {
      /* do something */
      println(i)
    }
  }

  /**
   * main calls out to argparse4s.
   */
  def main(args: Array[String]) {
    run(args)
  }

  /**
   * The command name. For commands, this is used when printing
   * help; for subcommands, it is the name used to invoke them.
   */
  override def name: String = "MyCommand for example"
}
