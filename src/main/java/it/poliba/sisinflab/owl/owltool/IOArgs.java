package it.poliba.sisinflab.owl.owltool;

import com.beust.jcommander.Parameter;

final class IOArgs {

    @Parameter(names = { "--input", "-i" }, description = "Input file.", required = true)
    String input;

    @Parameter(names = { "--output", "-o" }, description = "Output file.")
    String output;
}
