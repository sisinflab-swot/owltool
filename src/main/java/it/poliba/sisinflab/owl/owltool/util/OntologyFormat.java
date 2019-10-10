package it.poliba.sisinflab.owl.owltool.util;

import org.semanticweb.owlapi.formats.FunctionalSyntaxDocumentFormat;
import org.semanticweb.owlapi.formats.ManchesterSyntaxDocumentFormat;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.OWLDocumentFormat;

final class OntologyFormat {

    private static final String FUNCTIONAL = "functional";
    private static final String MANCHESTER = "manchester";
    private static final String OWLXML = "owlxml";
    private static final String RDFXML = "rdfxml";

    static OWLDocumentFormat getDocumentFormat(String format) {
        switch (format) {
        case FUNCTIONAL: return new FunctionalSyntaxDocumentFormat();
        case MANCHESTER: return new ManchesterSyntaxDocumentFormat();
        case OWLXML: return new OWLXMLDocumentFormat();
        case RDFXML: default: return new RDFXMLDocumentFormat();
        }
    }

    private OntologyFormat() { /* Disallow instantiation */ }
}
