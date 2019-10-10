package it.poliba.sisinflab.owl.owltool;

import com.beust.jcommander.JCommander;
import it.poliba.sisinflab.owl.owltool.util.Ontology;
import org.semanticweb.owlapi.model.OWLOntology;

import java.io.IOException;
import java.util.Arrays;

final class Main {

    private static final String CONVERT_SUB = "convert";
    private static final String TAXONOMY_SUB = "taxonomy";
    private static final String HELP_SUB = "help";
    private static final String[] ALL_SUBS = { CONVERT_SUB, TAXONOMY_SUB, HELP_SUB };

    public static void main(String[] args) {
        try {
            if (args.length < 1) {
                throw new IllegalArgumentException("Too few arguments.");
            }

            String subCommand = args[0];
            String[] otherArgs = Arrays.copyOfRange(args, 1, args.length);

            switch (subCommand) {
            case CONVERT_SUB:
                convertSub(otherArgs);
                break;

            case TAXONOMY_SUB:
                taxonomySub(otherArgs);
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

    private static void helpSub() {
        System.out.println("Subcommands: " + String.join(", ", ALL_SUBS));
    }

    private static void taxonomySub(String[] args) throws IOException {
        TaxonomyArgs arguments = new TaxonomyArgs();
        JCommander.newBuilder().addObject(arguments).build().parse(args);
        Ontology.printTaxonomy(Ontology.load(arguments.inputFile), arguments.outputFile);
    }

    private static void convertSub(String[] args) throws IOException {
        ConvertArgs arguments = new ConvertArgs();
        JCommander.newBuilder().addObject(arguments).build().parse(args);
        OWLOntology onto = Ontology.load(arguments.inputFile);
        Ontology.save(onto, arguments.outputFile, arguments.format);
    }
}
