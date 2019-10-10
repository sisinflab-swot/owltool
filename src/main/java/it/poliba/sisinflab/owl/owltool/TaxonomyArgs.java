package it.poliba.sisinflab.owl.owltool;

import com.beust.jcommander.Parameter;

final class TaxonomyArgs {

    @Parameter(names={"--input", "-i"}, description = "Input file.", required = true)
    String inputFile;

    @Parameter(names={"--output", "-o"}, description = "Output file.")
    String outputFile;
}
