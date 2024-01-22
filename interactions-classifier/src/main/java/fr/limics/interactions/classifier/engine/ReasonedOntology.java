package fr.limics.interactions.classifier.engine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.RDFParserRegistry;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.semanticweb.owl.explanation.api.Explanation;
import org.semanticweb.owl.explanation.api.ExplanationGenerator;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.InferredAxiomGenerator;
import org.semanticweb.owlapi.util.InferredClassAssertionAxiomGenerator;
import org.semanticweb.owlapi.util.InferredDisjointClassesAxiomGenerator;
import org.semanticweb.owlapi.util.InferredEquivalentClassAxiomGenerator;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;
import org.semanticweb.owlapi.util.InferredSubClassAxiomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReasonedOntology {

	private Logger log = LoggerFactory.getLogger(ReasonedOntology.class.getName());
	
	private OWLOntology ontology;
	private OWLReasoner reasoner;
	private ExplanationGenerator explanationGenerator;
	
	public ReasonedOntology(OWLOntology ontology, OWLReasoner reasoner, ExplanationGenerator explanationGenerator) {
		super();
		this.ontology = ontology;
		this.reasoner = reasoner;
		this.explanationGenerator = explanationGenerator;
	}

	public boolean isConsistent() {
		return reasoner.isConsistent();
	}
	
	public OWLOntology getInferredOntology() {
		log.debug("Getting inferred ontology... ");
		try {
			List<InferredAxiomGenerator<? extends OWLAxiom>> gens = new ArrayList<>();
			gens.add(new InferredSubClassAxiomGenerator());  
			gens.add(new InferredClassAssertionAxiomGenerator());
			// gens.add( new InferredDisjointClassesAxiomGenerator());
			// gens.add( new InferredEquivalentClassAxiomGenerator());
			// gens.add( new InferredEquivalentDataPropertiesAxiomGenerator());
			// gens.add( new InferredEquivalentObjectPropertyAxiomGenerator());
			// gens.add( new InferredInverseObjectPropertiesAxiomGenerator());
			// gens.add( new InferredObjectPropertyCharacteristicAxiomGenerator());
			// gens.add( new InferredPropertyAssertionGenerator());
			// gens.add( new InferredSubDataPropertyAxiomGenerator());
			// gens.add(new InferredDataPropertyCharacteristicAxiomGenerator());
			// gens.add(new InferredObjectPropertyCharacteristicAxiomGenerator());
			// gens.add( new InferredSubObjectPropertyAxiomGenerator());
			
			// reasoner.flush();
			// reasoner.getKB().realize(); 
			
			InferredOntologyGenerator iog = new InferredOntologyGenerator(reasoner, gens);
			OWLOntology infOnt = this.ontology.getOWLOntologyManager().createOntology();
			iog.fillOntology(this.ontology.getOWLOntologyManager().getOWLDataFactory(), infOnt);
			
			log.debug("Done with "+infOnt.getAxiomCount()+" axioms in inferred ontology");
			
			return infOnt;
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Model toModel(OWLOntology ontology) {
		try {
			//Save the ontology in a different format
			OWLDocumentFormat format = ontology.getOWLOntologyManager().getOntologyFormat(ontology);
			TurtleDocumentFormat turtleFormat = new TurtleDocumentFormat();
			if (format.isPrefixOWLDocumentFormat()) { 
				turtleFormat.asPrefixOWLDocumentFormat().copyPrefixesFrom(format.asPrefixOWLDocumentFormat()); 
			}
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ontology.saveOntology(turtleFormat, baos);
			
			Model ontologyModel = new LinkedHashModel();
			StatementCollector collector = new StatementCollector();
			RDFParser parser = RDFParserRegistry.getInstance().get(RDFFormat.TURTLE).get().getParser();
			
			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			parser.setRDFHandler(collector);
			parser.parse(bais);
      
			ontologyModel.addAll(collector.getStatements());
			
			return ontologyModel;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Set<String> listExplanationsOfClassification(String individualIri, String classIri) {
		OWLAxiom axiom = this.ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClassAssertionAxiom(
				this.ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IRI.create(classIri)),
				this.ontology.getOWLOntologyManager().getOWLDataFactory().getOWLNamedIndividual(IRI.create(individualIri))
		);
		return ((Set<Explanation<OWLAxiom>>)ReasonedOntology.explain(
				this.explanationGenerator,
				axiom
		)).stream()
    			.map(expl -> expl.toString())
    			.collect(Collectors.toSet());
	}
	
	public String readAsString(String classIri, String propertyIri) {
		OWLClass c = this.ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IRI.create(classIri));
			
		// Convert the property IRI string to an IRI object
	    IRI pIri = IRI.create(propertyIri);
		
		// Get annotations of the class
	    Set<OWLAnnotationAssertionAxiom> axioms = this.ontology.getAnnotationAssertionAxioms(IRI.create(classIri));
	    
	    // Iterate over the set of OWLAnnotationAssertionAxiom objects
	    for (OWLAnnotationAssertionAxiom axiom : axioms) {
	    	// Check if the property IRI of the current axiom matches the given IRI
	    	if (axiom.getProperty().getIRI().equals(pIri)) {
	    		// If it matches, then return the OWLAnnotationProperty of the current axiom
	    		return axiom.toString();
	    	}
	    }
	    
	    return "";
	}
	
	public Set<String> listIndividuals() {
		log.debug("Retrieving all individuals ");
		return ReasonedOntology.listIndividuals(
    			this.ontology
    	).stream()
    			.map(namedIndi -> namedIndi.getIRI().getIRIString())
    			.collect(Collectors.toSet());
	}
	
	public Set<String> listInferredTypesOfIndividual(String individualIri, boolean direct) {
		log.debug("Retrieving types of "+individualIri);
    	return ReasonedOntology.listInferredTypesOfIndividual(
    			this.reasoner,
    			this.ontology.getOWLOntologyManager().getOWLDataFactory().getOWLNamedIndividual(IRI.create(individualIri)),
    			direct
    	).stream()
    			.map(owlClass -> owlClass.getIRI().getIRIString())
    			.collect(Collectors.toSet());
	}
	
	public Set<String> listInstancesOfClass(String classIri) {
		log.debug("Retrieving instances of "+classIri);
    	return ReasonedOntology.listInstancesOfClass(
    			this.ontology,
    			this.ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IRI.create(classIri))
    	).stream()
    			.map(namedIndi -> namedIndi.getIRI().getIRIString())
    			.collect(Collectors.toSet());
	}

	public Set<String> listInferredInstancesOfClass(String classIri) {
		log.debug("Retrieving inferred instances of "+classIri);
    	return ReasonedOntology.listInferredInstancesOfClass(
    			this.reasoner,
    			this.ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IRI.create(classIri))
    	).stream()
    			.map(namedIndi -> namedIndi.getIRI().getIRIString())
    			.collect(Collectors.toSet());
	}
	
	public Set<String> listInferredSubClassesOfClass(String classIri) {
		log.debug("Retrieving inferred subclasses of "+classIri);
		return ReasonedOntology.listInferredSubClassesOfClass(
				this.reasoner,
    			this.ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IRI.create(classIri))
    	).stream()
    			.map(owlClass -> owlClass.getIRI().getIRIString())
    			.collect(Collectors.toSet());
	}
	

	public OWLOntology getOntology() {
		return ontology;
	}

	public OWLReasoner getReasoner() {
		return reasoner;
	}
	
	
    public static Set<OWLNamedIndividual> listInstancesOfClass(OWLOntology ontology, OWLClass owlClass) {
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
    
    public static Set<OWLNamedIndividual> listIndividuals(OWLOntology ontology) {
        Set<OWLNamedIndividual> instances = new HashSet<>();

        for (OWLOntology ont : ontology.getImportsClosure()) {
            for (OWLNamedIndividual individual : ont.getIndividualsInSignature()) {
            	instances.add(individual);
            }
        }

        return instances;
    }
	
    public static Set<OWLNamedIndividual> listInferredInstancesOfClass(OWLReasoner reasoner, OWLClass owlClass) {
    	NodeSet<OWLNamedIndividual> reasonerinstances = reasoner.getInstances(owlClass, false);
    	return reasonerinstances.getFlattened();
    }
    
    public static Set<OWLClass> listInferredSubClassesOfClass(OWLReasoner reasoner, OWLClass parent) {
    	NodeSet<OWLClass> inferredSubclasses = reasoner.getSubClasses(parent, true);
    	return inferredSubclasses.getFlattened();
    }
    
    public static Set<OWLClass> listInferredTypesOfIndividual(OWLReasoner reasoner, OWLNamedIndividual owlNamedIndividual) {
    	return listInferredTypesOfIndividual(reasoner, owlNamedIndividual, false);
    }
    
    public static Set<OWLClass> listInferredTypesOfIndividual(OWLReasoner reasoner, OWLNamedIndividual owlNamedIndividual, boolean direct) {
    	// direct = true/false
    	NodeSet<OWLClass> reasonertypes = reasoner.getTypes(owlNamedIndividual, direct);
    	return reasonertypes.getFlattened();
    }
    
    public static Set<Explanation<OWLAxiom>> explain(ExplanationGenerator<OWLAxiom> gen, OWLAxiom entailment) {
        // Get our explanations. Ask for a maximum of 5.
        try {
            Set<Explanation<OWLAxiom>> expl = gen.getExplanations(entailment, 3);
            return expl;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    
    
    public static void printInferredSubclasses(OWLReasoner reasoner, Node<OWLClass> parent, int limit) {
    	ReasonedOntology.printInferredSubclasses(reasoner, parent, 0, limit);
    }
    
    
    /**
     * 
     * @param reasoner the resoner to user
     * @param parent the root class for which we want to display the hierarchy
     * @param depth the current depth, used to indent
     * @param limit the maximum number of children we want to display at a given level
     */
    public static void printInferredSubclasses(OWLReasoner reasoner, Node<OWLClass> parent, int depth, int limit) {
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
	                printInferredSubclasses(reasoner, child, depth + 1, limit);
	                count++;
	            }
            }
        }
        if(count == limit) {
        	String indent = new String(new char[depth+1]).replace("\0", "--");
        	System.out.println(indent + "(...)");
        }
    }
	
}
