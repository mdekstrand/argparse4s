package net.elehack.argparse4s

abstract sealed class OptFlag {
  def flag: String
}
case class ShortOpt(name: Char) extends OptFlag {
  override val flag = "-" + name
}
case class LongOpt(name: String) extends OptFlag {
  override val flag = "--" + name
}


