package it.poliba.sisinflab.owl.owltool;

import com.beust.jcommander.Parameter;

final class FormatArgs {

    @Parameter(names = { "--format", "-f" }, description = "Output format.", required = true)
    String format;
}
