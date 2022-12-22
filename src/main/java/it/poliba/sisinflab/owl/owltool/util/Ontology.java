package it.poliba.sisinflab.owl.owltool.util;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AsOWLClass;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OntologyConfigurator;
import org.semanticweb.owlapi.search.EntitySearcher;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Ontology {

    public static OWLOntology load(String path) throws IOException {
        try {
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            OntologyConfigurator config = manager.getOntologyConfigurator();
            manager.setOntologyConfigurator(config.withRemapAllAnonymousIndividualsIds(false));
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

        try (OutputStream outStream = Files.newOutputStream(Paths.get(path))) {
            mgr.saveOntology(onto, outputFormat, outStream);
        } catch (OWLOntologyStorageException ex) {
            throw new IOException(ex);
        }
    }

    private static void addImplicitTopSubClassOfAxioms(OWLOntology onto, Map<OWLClass, List<OWLClass>> cache) {
        OWLDataFactory factory = onto.getOWLOntologyManager().getOWLDataFactory();
        OWLClass thing = factory.getOWLThing();

        onto.classesInSignature().forEach(c -> {
            List<OWLClass> eq = equivalentClassesForClass(c, onto, cache);
            if (!eq.get(0).isOWLThing() && !eq.get(0).isOWLNothing() &&
                    !eq.stream().flatMap(onto::subClassAxiomsForSubClass).findAny().isPresent()) {
                OWLSubClassOfAxiom axiom = factory.getOWLSubClassOfAxiom(eq.get(0), thing);
                onto.addAxiom(axiom);
            }
        });
    }

    public static void printTaxonomy(OWLOntology onto, String path) throws IOException {
        Map<OWLClass, List<OWLClass>> cache = new HashMap<>();
        addImplicitTopSubClassOfAxioms(onto, cache);

        PrintStream stream;

        if (path == null) {
            stream = System.out;
        } else {
            stream = new PrintStream(Files.newOutputStream(Paths.get(path)));
        }

        OWLClass thing = onto.getOWLOntologyManager().getOWLDataFactory().getOWLThing();
        printSubTaxonomy(stream, thing, onto, 0, cache);
    }

    private static void printSubTaxonomy(PrintStream stream, OWLClass owlClass,
                                         OWLOntology ontology, int depth, Map<OWLClass,
                                         List<OWLClass>> cache) {
        List<OWLClass> equivalents = equivalentClassesForClass(owlClass, ontology, cache);
        OWLClass first = equivalents.get(0);
        if (first.isOWLNothing() || (first.isOWLThing() && depth > 0)) return;

        for (int i = 0; i < depth; ++i) stream.print('\t');
        stream.print(first.getIRI().toString());

        for (int i = 1; i < equivalents.size(); ++i) {
            stream.print(" = ");
            stream.print(equivalents.get(i).getIRI().toString());
        }

        stream.println();

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
        List<OWLClass> ret = cache.get(owlClass);
        if (ret != null) return ret;

        Set<OWLClass> eq = EntitySearcher.getEquivalentClasses(owlClass, ontology)
                .filter(AsOWLClass::isOWLClass)
                .map(AsOWLClass::asOWLClass)
                .collect(Collectors.toSet());
        eq.add(owlClass);

        OWLDataFactory f = ontology.getOWLOntologyManager().getOWLDataFactory();
        OWLClass thing = f.getOWLThing();
        OWLClass nothing = f.getOWLNothing();

        boolean isThing = eq.contains(thing);

        if (isThing) {
            EntitySearcher.getSuperClasses(thing, ontology)
                    .filter(AsOWLClass::isOWLClass)
                    .map(AsOWLClass::asOWLClass)
                    .forEach(eq::add);
        }

        if (eq.size() == 1) {
            ret = Collections.singletonList(owlClass);
        } else if (eq.contains(nothing)) {
            ret = isThing ? Arrays.asList(thing, nothing) : Collections.singletonList(nothing);
        } else if (isThing) {
            eq.remove(thing);
            Stream<OWLClass> tmp = eq.stream().sorted(Comparator.comparing(c -> c.getIRI().toString()));
            ret = Stream.concat(Stream.of(thing), tmp).collect(Collectors.toList());
            eq.add(thing);
        } else {
            ret = eq.stream().sorted(Comparator.comparing(c -> c.getIRI().toString())).collect(Collectors.toList());
        }

        for (OWLClass cls : eq) {
            cache.put(cls, ret);
        }

        return ret;
    }

    private Ontology() { /* Disallow instantiation */ }
}
