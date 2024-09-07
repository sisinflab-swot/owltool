package it.poliba.sisinflab.owl.owltool.util;

import org.semanticweb.owlapi.formats.DLSyntaxDocumentFormat;
import org.semanticweb.owlapi.formats.FunctionalSyntaxDocumentFormat;
import org.semanticweb.owlapi.formats.KRSSDocumentFormat;
import org.semanticweb.owlapi.formats.KRSS2DocumentFormat;
import org.semanticweb.owlapi.formats.ManchesterSyntaxDocumentFormat;
import org.semanticweb.owlapi.formats.OBODocumentFormat;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.model.OWLDocumentFormat;

final class OntologyFormat {

    private static final String DL = "dl";
    private static final String FUNCTIONAL = "functional";
    private static final String KRSS = "krss";
    private static final String KRSS2 = "krss2";
    private static final String MANCHESTER = "manchester";
    private static final String OBO = "obo";
    private static final String OWLXML = "owlxml";
    private static final String RDFXML = "rdfxml";
    private static final String TURTLE = "turtle";

    static OWLDocumentFormat getDocumentFormat(String format) {
        switch (format) {
        case DL:
            return new DLSyntaxDocumentFormat();
        case FUNCTIONAL:
            return new FunctionalSyntaxDocumentFormat();
        case KRSS:
            return new KRSSDocumentFormat();
        case KRSS2:
            return new KRSS2DocumentFormat();
        case MANCHESTER:
            return new ManchesterSyntaxDocumentFormat();
        case OBO:
            return new OBODocumentFormat();
        case OWLXML:
            return new OWLXMLDocumentFormat();
        case TURTLE:
            return new TurtleDocumentFormat();
        case RDFXML:
        default:
            return new RDFXMLDocumentFormat();
        }
    }

    private OntologyFormat() {}
}
