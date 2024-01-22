package fr.limics.interactions.classifier.cli.classify;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Set;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriterRegistry;
import org.eclipse.rdf4j.rio.Rio;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.limics.interactions.classifier.cli.CliCommandIfc;
import fr.limics.interactions.classifier.engine.ClassifierEngine;
import fr.limics.interactions.classifier.engine.ReasonedOntology;

import static org.eclipse.rdf4j.model.util.Statements.statement;
import static org.eclipse.rdf4j.model.util.Values.iri;
import static org.eclipse.rdf4j.model.util.Values.literal;

public class Classify implements CliCommandIfc {

	private Logger log = LoggerFactory.getLogger(this.getClass().getName());
	
	@Override
	public void execute(Object args) throws Exception {
		ArgumentsClassify a = (ArgumentsClassify)args;
		
		File inputOntology = a.getOntology();
		File inputFolder = inputOntology.getAbsoluteFile().getParentFile();
		
		ClassifierEngine c = new ClassifierEngine(inputFolder);
		c.classify(inputOntology);
		
		ReasonedOntology ro = c.getReasonedOntology();
		
		if(a.getOutputAll() != null) {
			OWLOntology inferredOntology = ro.getInferredOntology();
			Model inferredOntologyModel = ReasonedOntology.toModel(inferredOntology);
	
			System.out.println("Inferred ontology model has "+inferredOntologyModel.size()+" statements");
			
			try(FileOutputStream out = new FileOutputStream(a.getOutputAll())) {
				Rio.write(
						inferredOntologyModel,
						out,
						RDFFormat.matchFileName(a.getOutputAll().getName(), RDFWriterRegistry.getInstance().getKeys()).orElse(RDFFormat.TURTLE)
				);
			}
		}
		
		// list the instances of the requested classes
		Model outputModel = new LinkedHashModel();
		for (String aClassUri : a.getClasses()) {
			System.out.println("Instance of "+aClassUri);
			Set<String> inferredInstances = ro.listInferredInstancesOfClass(aClassUri);			
			for (String anInstance : inferredInstances) {
				System.out.println("  - "+anInstance+" - direct types :");
				Set<String> directTypes = ro.listInferredTypesOfIndividual(anInstance, true);
				for (String aDirectType : directTypes) {
					System.out.println("    - "+aDirectType);
					outputModel.add(statement(iri(anInstance), RDF.TYPE, iri(aDirectType), null));
				}
				if(a.isExplain()) {
					Set<String> explanations = ro.listExplanationsOfClassification(anInstance, aClassUri);
					for (String anExplanation : explanations) {
						System.out.println("  Explanation "+anExplanation);
					}
				}
			}
			
			System.out.println("");
		}
		
		// then write output model
		try(FileOutputStream out = new FileOutputStream(a.getOutput())) {
			Rio.write(
					outputModel,
					out,
					RDFFormat.matchFileName(a.getOutput().getName(), RDFWriterRegistry.getInstance().getKeys()).orElse(RDFFormat.TURTLE)
			);
		}
		
		/*
		for (String aNamedIndividual : ro.listIndividuals()) {
			System.out.println("Inferred types of "+aNamedIndividual);
			for (String aType : ro.listTypesOfIndividual(aNamedIndividual)) {
				System.out.println("  - "+aType);
			}
		}
		*/
		
		
		// System.out.println(ro.listInferredInstancesOfClass(Remiames.ADMINISTRATION));
		// System.out.println(ro.listInferredInstancesOfClass(RemiamesAdministrations.ADMINISTRATION_ACIDE_ACETYLSALICYLIQUE));
		
		// System.out.println(ro.listInferredSubClassesOfClass(RemiamesAdministrations.ADMINISTRATION_ACIDE_ACETYLSALICYLIQUE));
		
		c.close();
	}
}
