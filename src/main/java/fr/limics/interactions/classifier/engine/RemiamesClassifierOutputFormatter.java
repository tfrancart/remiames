package fr.limics.interactions.classifier.engine;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import fr.limics.interactions.classifier.Remiames;

public class RemiamesClassifierOutputFormatter {

	private ReasonedOntology reasonedOntology;

	public RemiamesClassifierOutputFormatter(ReasonedOntology reasonedOntology) {
		super();
		this.reasonedOntology = reasonedOntology;
	}
	
	public List<String> formatClassificationOutput(String uri) {
		List<String> output = new ArrayList<String>();
		
		// obtain ontology as an RDF4J Model
		// Model m = this.reasonedOntology.asModel();
		
		// ValueFactory vf = new 
		
		// 1. patient id
		// String patientId = m.getStatements(null, null, null, null)
		
		return output;
	}
	
}
