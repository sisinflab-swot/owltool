package it.poliba.sisinflab.owl.owltool;

import com.beust.jcommander.Parameter;

final class ConvertArgs {

    @Parameter(names={"--input", "-i"}, description = "Input file.", required = true)
    String inputFile;

    @Parameter(names={"--output", "-o"}, description = "Output file.", required = true)
    String outputFile;

    @Parameter(names={"--format", "-f"}, description = "Output format.", required = true)
    String format;
}
