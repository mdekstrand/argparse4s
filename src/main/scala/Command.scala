package net.elehack.argparse4s

/**
 * Base trait for commands. The argparse4s library is built around
 * commands, defined by this trait, which define and access their
 * arguments using the provided OptionSet.
 */
trait Command extends Runnable with OptionType.Implicits {
  val name: String
  final val options = new OptionSet(name) {
    // FIXME This is bad design - should not use virtual methods
    if (description != null) {
      parser.description(description)
    }
  }
  val description: String = null

  protected implicit def shortOptName(ch: Char): ShortOpt = new ShortOpt(ch)
  protected implicit def longOptName(str: String): LongOpt = new LongOpt(str)

  def execute(args: Seq[String]) {
    val ns = options.parser.parseArgs(args.toArray)
    options.withNamespace(ns) {
      run()
    }
  }
}
