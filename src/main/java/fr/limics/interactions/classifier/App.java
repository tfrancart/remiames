package fr.limics.interactions.classifier;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.InconsistentOntologyException;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {

	private static Logger log = LoggerFactory.getLogger(App.class.getName());
	
	public static void main(String[] args) throws OWLOntologyCreationException, MalformedURLException, IOException {
		// Créer l'Ontology Manager
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

		// Créer un mapper pour résoudre les imports
		// AutoIRIMapper iriMapper = new AutoIRIMapper(new File(args[1]), true);
		// manager.getIRIMappers().add(iriMapper);O
		
		/*
		manager.getIRIMappers().add(new SimpleIRIMapper(
				IRI.create("http://www.limics.org/remiames_ontology"),
		        IRI.create("file:///home/thomas/sparna/00-Clients/LIMICS/REMIAMES/07-Interactions_classifier_data/remiames_ontology_v4.ttl"))
		);
		
		CommonBaseIRIMapper baseMapper = new CommonBaseIRIMapper(IRI.create("file:///home/thomas/sparna/00-Clients/LIMICS/REMIAMES/07-Interactions_classifier_data/"));
		manager.getIRIMappers().add(baseMapper);
		*/
		
		// Utiliser un IRI Mapper sur la base d'un fichier de catalog pour mapper les IRI d'ontologie vers des fichiers
		// XMLCatalogIRIMapper catalogIriMapper = new XMLCatalogIRIMapper(new File("/home/thomas/sparna/00-Clients/LIMICS/REMIAMES/07-Interactions_classifier_data/catalog-v001.xml"));
		// manager.getIRIMappers().add(catalogIriMapper);
		
		
		log.info("Loading ontology...");
        String ontFilePath = args[0];
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(Paths.get(ontFilePath).toFile());
        log.info("Done loading");

        
        doReasoningWith(ontology);
    }
	
    public static void doReasoningWith(OWLOntology ontology) {
    	log.info("Reasoning...");
    	OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();

    	// basic
    	OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);
    	
    	// avec progress monitor
    	// ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
    	// OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
    	// OWLReasoner reasoner = reasonerFactory.createReasoner(ontology, config);
    	

    	
    	// HermiT specific
	 	// org.semanticweb.HermiT.Configuration configuration = new org.semanticweb.HermiT.Configuration();
    	// org.semanticweb.owlapi.reasoner.OWLReasoner reasoner = reasonerFactory.createReasoner(ontology, configuration);
	
    	
        // pas obligatoire, mais bon
        reasoner.precomputeInferences();
        
        // Vérifier si l'ontologie est cohérente
        if (!reasoner.isConsistent()) {
            throw new InconsistentOntologyException("L'ontologie est incohérente.");
        }
        
        Node<OWLClass> topNode = reasoner.getTopClassNode();
        log.info("Top node is "+topNode.getEntities());

        printInferredSubclasses(topNode, reasoner, 0, 10);
        	
        // testInteractions(ontology, reasoner);

        printIndividualInferences(ontology, reasoner);
        
        reasoner.dispose();
    }

    private static void printInferredSubclasses(Node<OWLClass> parent, OWLReasoner reasoner, int depth, int limit) {
        // Using depth to format output
        if (parent.getSize() > 0) {
            String indent = new String(new char[depth]).replace("\0", "--");
            for (OWLClass cls : parent) {
                System.out.println(indent + cls.getIRI().getShortForm());
            }
        }

        int count = 0;
        for (Node<OWLClass> child : reasoner.getSubClasses(parent.getRepresentativeElement(), true)) {
            if(limit > 0 && count < limit) {
	        	if (!child.isBottomNode()) {
	                printInferredSubclasses(child, reasoner, depth + 1, limit);
	                count++;
	            }
            }
        }
        if(count == limit) {
        	String indent = new String(new char[depth+1]).replace("\0", "--");
        	System.out.println(indent + "(...)");
        }
    }
    
    private static void testInteractions(OWLOntology ontology, OWLReasoner reasoner) {
        // Effectuer des requêtes
        OWLDataFactory dataFactory = ontology.getOWLOntologyManager().getOWLDataFactory();

        // Par exemple, récupérer toutes les sous-classes de la classe "Animal"
        OWLClass interactionClass = dataFactory.getOWLClass(IRI.create("http://www.limics.org/remiames_ontology#Interaction"));
        NodeSet<OWLClass> interactions = reasoner.getSubClasses(interactionClass, true);
        Iterator<Node<OWLClass>> it = interactions.iterator();
        
        
        
        while(it.hasNext()) {
        	Node<OWLClass> uneInteraction = it.next();
        	log.info("Testing "+uneInteraction.getRepresentativeElement()+"...");
        	NodeSet<OWLClass> admnistrationsEnInteraction = reasoner.getSubClasses(uneInteraction.getRepresentativeElement(), true);
        	if(admnistrationsEnInteraction.getNodes().size() > 0) {
        		log.info("FOUND Interactions in "+uneInteraction.getRepresentativeElement());
        		admnistrationsEnInteraction.entities().forEach(owlclass -> log.debug(owlclass.toString()));
        	}
        }
    }
    
    private static void printIndividualInferences(OWLOntology ontology, OWLReasoner reasoner) {
    	ontology.individualsInSignature(Imports.INCLUDED).forEach(individual -> {

    		log.debug("Examining "+individual.toString());
    		
            // Direct types...
    		// Set<OWLClass> directTypes = new HashSet();
    		Set<OWLClass> directTypes = new App().new TypeRetriever().getTypesOf(individual, ontology);
            // Set<OWLClass> directTypes = ontology.axioms(individual, Imports.INCLUDED).map(OWLClassExpression::asOWLClass).collect(Collectors.toSet());
    		
    		System.out.println("direct types");
    		System.out.println(directTypes);
    		
            // Inferred types...
            Set<OWLClass> inferredTypes = reasoner.getTypes(individual.asOWLNamedIndividual(), true)
                    .entities()
                    .collect(Collectors.toSet());

            System.out.println("inferred types");
            System.out.println(inferredTypes);
            
            inferredTypes.removeAll(directTypes);

            // print only individuals with at least one inferred type 
            if (!inferredTypes.isEmpty()) {
                System.out.println("Individual: " + individual.getIRI().toString());
                System.out.println("Inferred new types:");
                inferredTypes.forEach(owlClass -> System.out.println("  " + owlClass.getIRI().toString()));
            }
        });

    }
    
    public class TypeRetriever {

        public Set<OWLClass> getTypesOf(OWLNamedIndividual individual, OWLOntology ontology) {
            return ontology.axioms(AxiomType.CLASS_ASSERTION, Imports.INCLUDED)
                    .filter(axiom -> axiom.getIndividual().equals(individual))
                    .map(OWLClassAssertionAxiom::getClassExpression)
                    .filter(OWLClassExpression::isNamed) // ignore anonymous class expressions
                    .map(OWLClassExpression::asOWLClass)
                    .collect(Collectors.toSet());
        }
    }
}
