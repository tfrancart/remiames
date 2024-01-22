package fr.limics.interactions.classifier.engine;

import java.io.File;
import java.util.function.Supplier;

import org.semanticweb.owl.explanation.api.ExplanationGenerator;
import org.semanticweb.owl.explanation.api.ExplanationGeneratorFactory;
import org.semanticweb.owl.explanation.api.ExplanationProgressMonitor;
import org.semanticweb.owl.explanation.impl.blackbox.Configuration;
import org.semanticweb.owl.explanation.impl.blackbox.DivideAndConquerContractionStrategy;
import org.semanticweb.owl.explanation.impl.blackbox.EntailmentCheckerFactory;
import org.semanticweb.owl.explanation.impl.blackbox.InitialEntailmentCheckStrategy;
import org.semanticweb.owl.explanation.impl.blackbox.StructuralTypePriorityExpansionStrategy;
import org.semanticweb.owl.explanation.impl.blackbox.checker.BlackBoxExplanationGeneratorFactory;
import org.semanticweb.owl.explanation.impl.blackbox.checker.SatisfiabilityEntailmentCheckerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.reasoner.TimedConsoleProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassifierEngine {

	private Logger log = LoggerFactory.getLogger(ClassifierEngine.class.getName());
	
	private File ontologiesFolder;

	private transient ReasonedOntology reasonedOntology;
	
	public ClassifierEngine(File ontologiesFolder) {
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
		OWLOntology anOntology = manager.loadOntologyFromOntologyDocument(instanceFile);
		
		log.info("Reasoning...");
		this.classify(anOntology);
	}
	
	public void classify(OWLOntology ontology) {
		
		// init HermiT
    	OWLReasonerFactory reasonerFactory = new org.semanticweb.HermiT.ReasonerFactory();

    	// avec progress monitor
    	TimedConsoleProgressMonitor progressMonitor = new TimedConsoleProgressMonitor();
    	OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
    	
    	// basic instantiation
    	OWLReasoner reasoner = reasonerFactory.createReasoner(ontology, config);
    	
    	// HermiT specific
	 	// org.semanticweb.HermiT.Configuration configuration = new org.semanticweb.HermiT.Configuration();
    	// org.semanticweb.owlapi.reasoner.OWLReasoner reasoner = reasonerFactory.createReasoner(ontology, configuration);
	
    	
        // pas obligatoire, mais bon
    	reasoner.precomputeInferences(InferenceType.CLASS_ASSERTIONS);
    	reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
        
    	ExplanationGeneratorFactory<OWLAxiom> genFac =
                createExplanationGeneratorFactory(reasonerFactory, null, OWLManager::createOWLOntologyManager);
    
        // Now create the actual explanation generator for our ontology
        ExplanationGenerator<OWLAxiom> explanationGeneration = genFac.createExplanationGenerator(ontology);
    
    	
    	ReasonedOntology result = new ReasonedOntology(ontology, reasoner, explanationGeneration);
    	this.reasonedOntology = result;
    }
	
    /**
     * See https://stackoverflow.com/questions/70593249/explanations-in-consistent-owl-ontologies
     */
    // this method replicates code existing in the owlexplanation project; it's needed because the factories in owlexplanation do not set InitialEntailmentCheckStrategy correctly
    public static ExplanationGeneratorFactory<OWLAxiom> createExplanationGeneratorFactory(
        OWLReasonerFactory reasonerFactory, 
        ExplanationProgressMonitor<OWLAxiom> progressMonitor,
        Supplier<OWLOntologyManager> m
    ) {
        EntailmentCheckerFactory<OWLAxiom> checker =
            new SatisfiabilityEntailmentCheckerFactory(reasonerFactory, m);
        Configuration<OWLAxiom> config = new Configuration<>(checker,
            new StructuralTypePriorityExpansionStrategy<OWLAxiom>(
                InitialEntailmentCheckStrategy.PERFORM, m),
            new DivideAndConquerContractionStrategy<OWLAxiom>(), progressMonitor, m);
        return new BlackBoxExplanationGeneratorFactory<>(config);
    }
	
	public void close() {
		this.reasonedOntology.getReasoner().dispose();
	}

	public ReasonedOntology getReasonedOntology() {
		return reasonedOntology;
	}
	
	
}
