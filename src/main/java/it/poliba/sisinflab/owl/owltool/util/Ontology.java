package it.poliba.sisinflab.owl.owltool.util;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Ontology {

    public static OWLOntology load(String path) throws IOException {
        try {
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            return manager.loadOntologyFromOntologyDocument(new File(path));
        } catch (OWLOntologyCreationException ex) {
            throw new IOException(ex);
        }
    }

    public static void save(OWLOntology onto, String path, String format) throws IOException {
        OWLOntologyManager mgr = onto.getOWLOntologyManager();
        OWLDocumentFormat inputFormat = mgr.getNonnullOntologyFormat(onto);
        OWLDocumentFormat outputFormat = inputFormat;

        if (format != null) {
            outputFormat = OntologyFormat.getDocumentFormat(format);

            if (inputFormat != null &&
                inputFormat.isPrefixOWLDocumentFormat() &&
                outputFormat.isPrefixOWLDocumentFormat()) {
                outputFormat.asPrefixOWLDocumentFormat()
                            .copyPrefixesFrom(outputFormat.asPrefixOWLDocumentFormat());
            }
        }

        try (OutputStream outStream = new FileOutputStream(new File(path))) {
            mgr.saveOntology(onto, outputFormat, outStream);
        } catch (OWLOntologyStorageException ex) {
            throw new IOException(ex);
        }
    }

    public static void printTaxonomy(OWLOntology onto, String path) throws IOException {
        OWLOntologyManager mgr = onto.getOWLOntologyManager();
        OWLClass top = mgr.getOWLDataFactory().getOWLThing();
        Map<OWLClass, List<OWLClass>> cache = new HashMap<>();

        PrintStream stream;

        if (path == null) {
            stream = System.out;
        } else {
            stream = new PrintStream(new FileOutputStream(path));
        }

        printSubTaxonomy(stream, top, onto, 0, cache);
    }

    private static void printSubTaxonomy(PrintStream stream, OWLClass owlClass,
                                         OWLOntology ontology, int depth, Map<OWLClass,
                                         List<OWLClass>> cache) {
        if (owlClass.isOWLNothing()) return;

        StringBuilder line = new StringBuilder();
        for (int i = 0; i < depth; ++i) line.append("\t");

        List<OWLClass> equivalents = equivalentClassesForClass(owlClass, ontology, cache);
        line.append(equivalents.get(0).getIRI().toString());

        for (int i = 1; i < equivalents.size(); ++i) {
            line.append(" = ");
            line.append(equivalents.get(i).getIRI().toString());
        }

        stream.println(line.toString());

        equivalents.stream()
                   .flatMap(ontology::subClassAxiomsForSuperClass)
                   .map(OWLSubClassOfAxiom::getSubClass)
                   .filter(OWLClassExpression::isOWLClass)
                   .map(c -> equivalentClassesForClass(c.asOWLClass(), ontology, cache).get(0))
                   .distinct()
                   .sorted(Comparator.comparing(c -> c.getIRI().toString()))
                   .forEach(c -> printSubTaxonomy(stream, c, ontology, depth + 1, cache));
    }

    private static List<OWLClass> equivalentClassesForClass(OWLClass owlClass,
                                                            OWLOntology ontology,
                                                            Map<OWLClass, List<OWLClass>> cache) {
        List<OWLClass> equivalents = cache.get(owlClass);
        if (equivalents != null) return equivalents;

        if (ontology.equivalentClassesAxioms(owlClass).count() == 0) {
            return Collections.singletonList(owlClass);
        }

        final boolean reserved = owlClass.isOWLThing() || owlClass.isOWLNothing();

        Stream<OWLClass> clsStream;
        clsStream = ontology.equivalentClassesAxioms(owlClass)
                            .flatMap(OWLEquivalentClassesAxiom::classesInSignature);

        if (reserved) {
            clsStream = clsStream.filter(c -> !c.equals(owlClass));
        } else {
            clsStream = Stream.concat(clsStream, Stream.of(owlClass));
        }

        clsStream = clsStream.distinct()
                             .sorted(Comparator.comparing(c -> c.getIRI().toString()));

        if (reserved) {
            clsStream = Stream.concat(Stream.of(owlClass), clsStream);
        }

        equivalents = clsStream.collect(Collectors.toList());
        cache.put(owlClass, equivalents);

        return equivalents;
    }

    private Ontology() { /* Disallow instantiation */ }
}
