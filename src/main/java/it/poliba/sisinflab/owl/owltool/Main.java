package it.poliba.sisinflab.owl.owltool;

import com.beust.jcommander.JCommander;
import it.poliba.sisinflab.owl.owltool.util.Ontology;
import org.semanticweb.owlapi.model.OWLOntology;

import java.io.IOException;
import java.util.Arrays;

final class Main {

    private static final String CONVERT_SUB = "convert";
    private static final String TAXONOMY_SUB = "taxonomy";
    private static final String METADATA_SUB = "metadata";
    private static final String HELP_SUB = "help";
    private static final String[] ALL_SUBS = { CONVERT_SUB, TAXONOMY_SUB, METADATA_SUB, HELP_SUB };

    public static void main(String[] args) {
        try {
            if (args.length < 1) { throw new IllegalArgumentException("Too few arguments."); }

            String subCommand = args[0];
            String[] otherArgs = Arrays.copyOfRange(args, 1, args.length);

            switch (subCommand) {
            case CONVERT_SUB:
                convertSub(otherArgs);
                break;

            case TAXONOMY_SUB:
                taxonomySub(otherArgs);
                break;

            case METADATA_SUB:
                metadataSub(otherArgs);
                break;

            case HELP_SUB:
            case "-h":
            case "-help":
            case "--help":
                helpSub();
                break;

            default:
                helpSub();
                throw new IllegalArgumentException("Illegal argument \"" + subCommand + "\"");
            }
        } catch (Exception exc) {
            exc.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private static void helpSub() { System.out.println("Subcommands: " + String.join(", ", ALL_SUBS)); }

    private static void taxonomySub(String[] args) throws IOException {
        IOArgs ioArgs = new IOArgs();
        JCommander.newBuilder().addObject(ioArgs).build().parse(args);
        Ontology.printTaxonomy(Ontology.load(ioArgs.input), ioArgs.output);
    }

    private static void convertSub(String[] args) throws IOException {
        IOArgs ioArgs = new IOArgs();
        FormatArgs formatArgs = new FormatArgs();
        JCommander.newBuilder().addObject(ioArgs).addObject(formatArgs).build().parse(args);
        OWLOntology onto = Ontology.load(ioArgs.input);
        Ontology.save(onto, ioArgs.output, formatArgs.format);
    }

    private static void metadataSub(String[] args) throws IOException {
        IOArgs ioArgs = new IOArgs();
        JCommander.newBuilder().addObject(ioArgs).build().parse(args);
        Ontology.printMetadata(Ontology.load(ioArgs.input), ioArgs.output);
    }
}
