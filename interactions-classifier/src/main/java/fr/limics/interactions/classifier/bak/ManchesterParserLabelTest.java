package fr.limics.interactions.classifier.bak;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxEditorParser;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.expression.ShortFormEntityChecker;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.AnnotationValueShortFormProvider;
import org.semanticweb.owlapi.util.BidirectionalShortFormProvider;
import org.semanticweb.owlapi.util.BidirectionalShortFormProviderAdapter;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import org.semanticweb.owlapi.util.mansyntax.ManchesterOWLSyntaxParser;

public class ManchesterParserLabelTest {

	public static void main(String...args) throws Exception {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
		final String TEST_ONTO = "Prefix: : <http://www.co-ode.org/ontologies/pizza/pizza.owl#>\n"
				+ "Prefix: owl: <http://www.w3.org/2002/07/owl#>\n"
				+ "Prefix: rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				+ "Prefix: rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
				+ "Prefix: xml: <http://www.w3.org/XML/1998/namespace>\n"
				+ "Prefix: xsd: <http://www.w3.org/2001/XMLSchema#>\n"
				+ "\n"
				+ "\n"
				+ "\n"
				+ "Ontology: <http://www.co-ode.org/ontologies/pizza>\n"
				+ "<http://www.co-ode.org/ontologies/pizza/2.0.0>\n"
				+ "    \n"
				+ "    \n"
				+ "ObjectProperty: hasTopping\n"
				+ "    \n"
				+ "\n"
				+ "Class: Pizza\n"
				+ "\n"
				+ "    Annotations: \n"
				+ "        rdfs:label \"A_Pizza\"@en\n"
				+ "\n"
				+ "Class: FishTopping\n"
				+ "\n"
				+ "    Annotations: \n"
				+ "        rdfs:label \"Fish_Topping\"@en\n"
				+ "    \n"
				+ "Class: MeatTopping\n"
				+ "\n"
				+ "    Annotations: \n"
				+ "        rdfs:label \"Meat topping\"@en\n"
				+ "    \n"
				+ "Class: XXX\n"
				+ "\n"
				+ "    EquivalentTo: \n"
				+ "        Pizza\n"
				+ "         and (not (hasTopping some MeatTopping))";
		
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new ByteArrayInputStream(TEST_ONTO.getBytes()));
		OWLDataFactory df = manager.getOWLDataFactory();

		
		ManchesterOWLSyntaxParser manchesterParser = OWLManager.createManchesterParser();
		manchesterParser.setDefaultOntology(ontology); // my ontology
		ShortFormEntityChecker checker = new ShortFormEntityChecker(getShortFormProvider(manager, ontology));
		// prints : <http://www.co-ode.org/ontologies/pizza/pizza.owl#MeatTopping>
		System.out.println("Meat topping : ");
		System.out.println(checker.getOWLClass("Meat topping"));
        // prints : null
        System.out.println("'Meat topping' : ");
        System.out.println(checker.getOWLClass("'Meat topping'"));
        
        manchesterParser.setOWLEntityChecker(checker);
        
        try {
			// This works
			// prints : 
			// ObjectIntersectionOf(<http://www.co-ode.org/ontologies/pizza/pizza.owl#Pizza> ObjectSomeValuesFrom(<http://www.co-ode.org/ontologies/pizza/pizza.owl#hasTopping> <http://www.co-ode.org/ontologies/pizza/pizza.owl#FishTopping>))
			System.out.println("Class expression containing labels without spaces:");
			final String CLASS_EXPRESSION_WITH_LABEL_NO_SPACE = "A_Pizza and hasTopping some Fish_Topping";
			OWLClassExpression exprWithLabelNoSpace = manchesterParser.parseClassExpression(CLASS_EXPRESSION_WITH_LABEL_NO_SPACE);
			System.out.println(exprWithLabelNoSpace);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       
        
        try {
			System.out.println("Class expression containing IRIs local parts:");
			final String CLASS_EXPRESSION_WITHOUT_LABEL = "Pizza and hasTopping some MeatTopping";
			// Throws ParserException : Encountered Pizza at line 1 column 1
			// this shows the ShortFormProvider works only with labels, not with IRIs
			OWLClassExpression exprWithout = manchesterParser.parseClassExpression(CLASS_EXPRESSION_WITHOUT_LABEL);
			System.out.println(exprWithout);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
        try {
			System.out.println("Class expression containing labels with space:");
			// throws ParserException : Encountered 'Meat topping' at line 1 column 27.
			// If debugging, we can see that the ShortFormEntityChecker receives the string "'Meat topping'"
			// with quotes included
			final String CLASS_EXPRESSION_WITH_LABEL = "A_Pizza and hasTopping some 'Meat topping'";
			OWLClassExpression exprWith = manchesterParser.parseClassExpression(CLASS_EXPRESSION_WITH_LABEL);
			System.out.println(exprWith);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        ManchesterOWLSyntaxEditorParser editorParser = new ManchesterOWLSyntaxEditorParser(df, "");
        editorParser.setDefaultOntology(ontology);
        editorParser.setOWLEntityChecker(checker);
        OWLClassExpression exprTest = editorParser.parseClassExpression("A_Pizza and hasTopping some Fish_Topping");
        System.out.println(exprTest);
	}
	
    private static BidirectionalShortFormProvider getShortFormProvider(OWLOntologyManager manager, OWLOntology ont) {
        Set<OWLOntology> ontologies = manager.getOntologies(); // my OWLOntologyManager        
        
        // OWLAnnotationProperty rdfsLabelProp = manager.getOWLDataFactory().getOWLAnnotationProperty("http://www.w3.org/2000/01/rdf-schema#label");
        AnnotationValueShortFormProvider sfp = new AnnotationValueShortFormProvider(
        		Collections.singletonList(manager.getOWLDataFactory().getRDFSLabel()),
        		new HashMap<OWLAnnotationProperty, List<String>>() {{
        			put(manager.getOWLDataFactory().getRDFSLabel(), Arrays.asList(new String[] {"en"}));
        		}},
        		manager,
        		new SimpleShortFormProvider()
        );
        
        BidirectionalShortFormProvider shortFormProvider = new BidirectionalShortFormProviderAdapter(
                ontologies, sfp);
        return shortFormProvider;
    }
}
