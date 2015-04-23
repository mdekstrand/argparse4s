package net.elehack.argparse4s;

import net.sourceforge.argparse4j.inf.Argument;

class ArgConfig {
    public static void setDefault(Argument arg, Object dft) {
        arg.setDefault(dft);
    }
}
