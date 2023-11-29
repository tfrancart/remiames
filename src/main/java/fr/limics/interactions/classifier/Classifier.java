package fr.limics.interactions.classifier;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.Reasoner.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.InconsistentOntologyException;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Classifier {

	private Logger log = LoggerFactory.getLogger(Classifier.class.getName());
	
	private File ontologiesFolder;
	
	private transient OWLOntology instancesOntology;
	private transient OWLReasoner instancesReasoner;
	
	public Classifier(File ontologiesFolder) {
		super();
		this.ontologiesFolder = ontologiesFolder;
	}
	
	public void classify(File instanceFile) throws OWLOntologyCreationException {
		// Créer l'Ontology Manager
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
		// Utiliser un IRI mapper qui va aller trouver l'ontologie avec le même nom dans le répertoire
		FolderIRIMapper iriMapper = new FolderIRIMapper(this.ontologiesFolder);
		manager.getIRIMappers().add(iriMapper);
		
		log.info("Loading ontology...");
		this.instancesOntology = manager.loadOntologyFromOntologyDocument(instanceFile);
		
		log.info("Reasoning...");
		this.reason(this.instancesOntology);
		
		// Set<OWLNamedIndividual> interactions = listInferredInstancesOfClass(manager.getOWLDataFactory().getOWLClass(IRI.create(Remiames.INTERACTION)), instancesReasoner);
	}
	
	public void reason(OWLOntology ontology) {
		// init HermiT
    	OWLReasonerFactory reasonerFactory = new org.semanticweb.HermiT.ReasonerFactory();

    	// basic instantiation
    	this.instancesReasoner = reasonerFactory.createReasoner(ontology);
    	
    	// avec progress monitor
    	// ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
    	// OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
    	// OWLReasoner reasoner = reasonerFactory.createReasoner(ontology, config);
    	
    	// HermiT specific
	 	// org.semanticweb.HermiT.Configuration configuration = new org.semanticweb.HermiT.Configuration();
    	// org.semanticweb.owlapi.reasoner.OWLReasoner reasoner = reasonerFactory.createReasoner(ontology, configuration);
	
    	
        // pas obligatoire, mais bon
    	this.instancesReasoner.precomputeInferences(InferenceType.CLASS_ASSERTIONS);
    	this.instancesReasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
        
        // Vérifier si l'ontologie est cohérente
        if (!this.instancesReasoner.isConsistent()) {
            throw new InconsistentOntologyException("L'ontologie est incohérente.");
        }
        
        Node<OWLClass> topNode = this.instancesReasoner.getTopClassNode();

        printInferredSubclasses(topNode, this.instancesReasoner, 0, 10);
    }
	
	public void close() {
		this.instancesReasoner.dispose();
	}
	
	public Set<String> listInferredInstancesOfClass(String classIri) {
		log.debug("Retrieving instances of "+classIri);
    	return Classifier.listInferredInstancesOfClass(
    			this.instancesOntology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IRI.create(classIri)),
    			this.instancesReasoner
    	).stream()
    			.map(namedIndi -> namedIndi.getIRI().getIRIString())
    			.collect(Collectors.toSet());
	}
	
	public Set<String> listInferredSubClassesOfClass(String classIri) {
		log.debug("Listing subclasses of "+classIri);
		return Classifier.listInferredSubClassesOfClass(
    			this.instancesOntology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IRI.create(classIri)),
    			this.instancesReasoner
    	).stream()
    			.map(owlClass -> owlClass.getIRI().getIRIString())
    			.collect(Collectors.toSet());
	}
	
    public static Set<OWLNamedIndividual> listInstancesOfClass(OWLClass owlClass, OWLOntology ontology) {
        Set<OWLNamedIndividual> instances = new HashSet<>();

        for (OWLOntology ont : ontology.getImportsClosure()) {
            for (OWLNamedIndividual individual : ont.getIndividualsInSignature()) {
                for (OWLClassAssertionAxiom classAssertion : ontology.getClassAssertionAxioms(individual)) {
                    if (classAssertion.getClassExpression().equals(owlClass)) {
                        instances.add(individual);
                    }
                }
            }
        }

        return instances;
    }
    
    public static Set<OWLNamedIndividual> listInferredInstancesOfClass(OWLClass owlClass, OWLReasoner reasoner) {
    	NodeSet<OWLNamedIndividual> reasonerinstances = reasoner.getInstances(owlClass, false);
    	return reasonerinstances.getFlattened();
    }
    
    public static Set<OWLClass> listInferredSubClassesOfClass(OWLClass parent, OWLReasoner reasoner) {
    	NodeSet<OWLClass> inferredSubclasses = reasoner.getSubClasses(parent, true);
    	return inferredSubclasses.getFlattened();
    }
	
    public static void printInferredSubclasses(Node<OWLClass> parent, OWLReasoner reasoner, int depth, int limit) {
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

	public OWLOntology getInstancesOntology() {
		return instancesOntology;
	}
	
	
	
}
