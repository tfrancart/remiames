package fr.limics.interactions.classifier.bak;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxEditorParser;
import org.coode.owlapi.turtle.TurtleOntologyFormat;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.RDFParserRegistry;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.eclipse.rdf4j.rio.turtle.TurtleParser;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.expression.ShortFormEntityChecker;
import org.semanticweb.owlapi.formats.FunctionalSyntaxDocumentFormat;
import org.semanticweb.owlapi.formats.LabelFunctionalDocumentFormat;
import org.semanticweb.owlapi.formats.ManchesterSyntaxDocumentFormat;
import org.semanticweb.owlapi.formats.RioTurtleDocumentFormat;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxPrefixNameShortFormProvider;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.rdf.turtle.renderer.TurtleRenderer;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.util.AnnotationValueShortFormProvider;
import org.semanticweb.owlapi.util.BidirectionalShortFormProvider;
import org.semanticweb.owlapi.util.BidirectionalShortFormProviderAdapter;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.util.QNameShortFormProvider;
import org.semanticweb.owlapi.util.ShortFormFromRDFSLabelAxiomListProvider;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import org.semanticweb.owlapi.util.StringLengthComparator;
import org.semanticweb.owlapi.util.mansyntax.ManchesterOWLSyntaxParser;

/**
 * Hello world!
 *
 */
public class ManchesterParserLabelTest2 
{
    public static void main( String[] args ) throws Exception
    {
        System.out.println( "Hello World!" );
        
        
        /*
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.loadOntology(IRI.create("https://protege.stanford.edu/ontologies/pizza/pizza.owl"));
        // ontology.saveOntology(new FunctionalSyntaxDocumentFormat(), System.out);
        ManchesterSyntaxDocumentFormat format = new ManchesterSyntaxDocumentFormat();
        format.setDefaultPrefix("http://www.co-ode.org/ontologies/pizza/pizza.owl#");
        // format.setPrefix("pizza", "http://www.co-ode.org/ontologies/pizza/pizza.owl#");
        // ontology.saveOntology(format, System.out);
         */
        
        
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        // OWLOntology ontology = manager.loadOntology(IRI.create("https://protege.stanford.edu/ontologies/pizza/pizza.owl"));
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File("src/test/resources/test_manchester_syntax.owl"));
        OWLDataFactory df = manager.getOWLDataFactory();

        /*
        System.out.println(ontology.getFormat().asPrefixOWLDocumentFormat().getPrefixName2PrefixMap());
        DefaultPrefixManager dpm = new DefaultPrefixManager(
        		ontology.getFormat().asPrefixOWLDocumentFormat(),
        		new StringLengthComparator(),
        		ontology.getFormat().asPrefixOWLDocumentFormat().getDefaultPrefix());
        
        ManchesterOWLSyntaxPrefixNameShortFormProvider sfp = new ManchesterOWLSyntaxPrefixNameShortFormProvider(dpm);
        System.out.println("Avec ManchesterOWLSyntaxPrefixNameShortFormProvider: ");
        System.out.println(sfp.getShortForm(df.getOWLClass("https://protege.stanford.edu/ontologies/pizza/pizza.owl#Pizza")));
        
        QNameShortFormProvider qnsfp = new QNameShortFormProvider(ontology.getFormat().asPrefixOWLDocumentFormat().getPrefixName2PrefixMap());
        System.out.println("Avec QNameShortFormProvider: ");
        System.out.println(qnsfp.getShortForm(df.getOWLClass("https://protege.stanford.edu/ontologies/pizza/pizza.owl#Pizza")));
        
        SimpleShortFormProvider ssfp = new SimpleShortFormProvider();
        System.out.println("Avec SimpleShortFormProvider: ");
        System.out.println(ssfp.getShortForm(df.getOWLClass("https://protege.stanford.edu/ontologies/pizza/pizza.owl#Pizza")));

        OWLAnnotationProperty rdfsLabelProp = df.getOWLAnnotationProperty("http://www.w3.org/2000/01/rdf-schema#label");
        AnnotationValueShortFormProvider avsfp = new AnnotationValueShortFormProvider(
        		Collections.singletonList(rdfsLabelProp),
        		new HashMap<OWLAnnotationProperty, List<String>>() {{
        			put(rdfsLabelProp, Arrays.asList(new String[] {"en"}));
        		}},
        		manager
        );
        System.out.println("Avec AnnotationValueShortFormProvider: ");
        System.out.println(avsfp.getShortForm(df.getOWLClass("https://protege.stanford.edu/ontologies/pizza/pizza.owl#MeatTopping")));
        		
        
        List<OWLAxiom> labelAxioms = new ArrayList<>();
        for (OWLEntity owlEntity : ontology.signature().toList()) {
			if(owlEntity.isOWLClass()) {
				System.out.println(owlEntity);
				List<OWLAnnotationAssertionAxiom> axioms = EntitySearcher.getAnnotationAssertionAxioms(owlEntity, ontology).toList();
				for (OWLAxiom anAxiom : axioms) {
					System.out.println("  "+anAxiom);
				}
				// System.out.println(owlEntity);
				labelAxioms.addAll(EntitySearcher.getAnnotationAssertionAxioms(owlEntity, ontology).toList());
			}
		}
        
        ShortFormFromRDFSLabelAxiomListProvider sffrlalp = new ShortFormFromRDFSLabelAxiomListProvider(
        		Arrays.asList(new String[] {"en"}),
        		labelAxioms,
        		new SimpleShortFormProvider()
        );
        System.out.println("Avec ShortFormFromRDFSLabelAxiomListProvider: ");
        System.out.println(sffrlalp.getShortForm(df.getOWLClass("https://protege.stanford.edu/ontologies/pizza/pizza.owl#MeatTopping")));
*/        
        
        ManchesterOWLSyntaxParser manchesterParser = OWLManager.createManchesterParser();
        
        // ManchesterOWLSyntaxEditorParser manchesterParser = new ManchesterOWLSyntaxEditorParser(df, null);
        String toParse = "Pizza and hasTopping some \"The MeatTopping\"";
        
        
        manchesterParser.setDefaultOntology(ontology); // my ontology
        ShortFormEntityChecker checker = new ShortFormEntityChecker(getShortFormProvider(manager, ontology));
        System.out.println(checker.getOWLClass("The MeatTopping"));
        System.out.println(checker.getOWLClass("'The MeatTopping'"));
        
        manchesterParser.setOWLEntityChecker(checker);
        OWLClassExpression expr = manchesterParser.parseClassExpression(toParse);
        
        
        // OWLDataFactory df = manager.getOWLDataFactory();
        // ontology.add(df.getOWLEquivalentClassesAxiom(df.getOWLClass("http://exemple.fr"), expr));
        
        OWLOntology tempOntology = manager.createOntology();
        tempOntology.add(df.getOWLEquivalentClassesAxiom(df.getOWLClass("http://x"), expr));
        
        
        
//        RioTurtleDocumentFormat turtleFormat = new RioTurtleDocumentFormat();
//        TurtleRenderer tr = new TurtleRenderer(tempOntology, new OutputStreamWriter(System.out), turtleFormat);
//        tr.render();
        
        TurtleDocumentFormat turtle = new TurtleDocumentFormat();
        Model ontologyModel = new LinkedHashModel();
        StatementCollector collector = new StatementCollector();
        RDFParser parser = RDFParserRegistry.getInstance().get(RDFFormat.TURTLE).get().getParser();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        tempOntology.saveOntology(turtle, baos);
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        parser.setRDFHandler(collector);
        parser.parse(bais);
       
        ontologyModel.addAll(collector.getStatements());
        
	        ValueFactory factory = SimpleValueFactory.getInstance();
	        Value equivalentClassEntity = ontologyModel.filter(
	        		factory.createIRI("http://x"),
	        		factory.createIRI("http://www.w3.org/2002/07/owl#equivalentClass"),
	        		null
	        )
	        .objects().iterator().next();
        
        Model theInterestingTriples = retrieveStatementsTreeRec(ontologyModel, (Resource)equivalentClassEntity);
        for (Statement statement : theInterestingTriples) {
			System.out.println(statement);
		}
        
        

        /*
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File("src/test/resources/test_manchester_syntax.owl"));
        ManchesterSyntaxDocumentFormat format = new ManchesterSyntaxDocumentFormat();
        format.setDefaultPrefix("http://www.co-ode.org/ontologies/pizza/pizza.owl#");
        // format.setPrefix("pizza", "http://www.co-ode.org/ontologies/pizza/pizza.owl#");
        ontology.saveOntology(format, System.out);
        
        OWLDataFactory df = ontology.getOWLOntologyManager().getOWLDataFactory();
        OWLClass xxx = df.getOWLClass("http://www.co-ode.org/ontologies/pizza/pizza.owl#XXX");
        Set<OWLClassAxiom> axioms = ontology.getAxioms(xxx);
        OWLClassAxiom theEquivalentClassAxiom;
        for (OWLClassAxiom anAxiom : axioms) {
        	if(anAxiom.isOfType(AxiomType.EQUIVALENT_CLASSES)) {
        		System.out.println("ha ha");
        		theEquivalentClassAxiom = anAxiom;
        	}
			
		}
        
        TurtleDocumentFormat turtle = new TurtleDocumentFormat();
        Model ontologyModel = new LinkedHashModel();
        StatementCollector collector = new StatementCollector();
        RDFParser parser = RDFParserRegistry.getInstance().get(RDFFormat.TURTLE).get().getParser();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ontology.saveOntology(turtle, baos);
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        parser.setRDFHandler(collector);
        parser.parse(bais);
       
        ontologyModel.addAll(collector.getStatements());
        
        ValueFactory factory = SimpleValueFactory.getInstance();
        Value equivalentClassEntity = ontologyModel.filter(
        		factory.createIRI("http://www.co-ode.org/ontologies/pizza/pizza.owl#XXX"),
        		factory.createIRI("http://www.w3.org/2002/07/owl#equivalentClass"),
        		null
        )
        .objects().iterator().next();
        
        Model theInterestingTriples = retrieveStatementsTreeRec(ontologyModel, (Resource)equivalentClassEntity);
        for (Statement statement : theInterestingTriples) {
			System.out.println(statement);
		}
		*/
    }
    
    public static Model retrieveStatementsTreeRec(final Model m, Resource r) {
    	final Model output = new LinkedHashModel();
    	Model statementsWhereRIsSubject = m.filter(r, null, null);
    	output.addAll(statementsWhereRIsSubject);
    	statementsWhereRIsSubject.forEach(s -> {
    		if(s.getObject() instanceof BNode) {
    			output.addAll(retrieveStatementsTreeRec(m,(Resource)s.getObject()));
    		}	
    	});
    	return output;
    }
    
    private static BidirectionalShortFormProvider getShortFormProvider(OWLOntologyManager manager, OWLOntology ont) {
        Set<OWLOntology> ontologies = manager.getOntologies(); // my OWLOntologyManager
       
        // all my attempts to use different short form providers :
        
        // ShortFormProvider sfp = new ManchesterOWLSyntaxPrefixNameShortFormProvider(ont);
        // ShortFormProvider sfp = new SimpleShortFormProvider();
        
        /*
        ShortFormFromRDFSLabelAxiomListProvider sfp = new ShortFormFromRDFSLabelAxiomListProvider(
        		Arrays.asList(new String[] {"en"}),
        		ont.axioms().toList(),
        		new SimpleShortFormProvider()
        );
        */
        
        
        // OWLAnnotationProperty rdfsLabelProp = manager.getOWLDataFactory().getOWLAnnotationProperty("http://www.w3.org/2000/01/rdf-schema#label");
        AnnotationValueShortFormProvider sfp = new AnnotationValueShortFormProvider(
        		Collections.singletonList(manager.getOWLDataFactory().getRDFSLabel()),
        		new HashMap<OWLAnnotationProperty, List<String>>() {{
        			put(manager.getOWLDataFactory().getRDFSLabel(), Arrays.asList(new String[] {"en"}));
        		}},
        		manager
        );
        
        
        
        BidirectionalShortFormProvider shortFormProvider = new BidirectionalShortFormProviderAdapter(
                ontologies, sfp);
        return shortFormProvider;
    }
}
