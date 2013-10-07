credentials ++= {
  Option(System.getenv("SONATYPE_USER")).
    zip(Option(System.getenv("SONATYPE_PASSWORD"))).
    headOption match {
      case Some((user,passwd)) => Seq(
        Credentials("Sonatype Nexus Repository Manager",
          "oss.sonatype.org", user, passwd))
      case None => Seq.empty
    }
}
