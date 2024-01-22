package fr.limics.interactions.classifier.cli.classify;

import java.io.File;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandDescription = "...")
public class ArgumentsClassify {

	@Parameter(
			names = { "-i", "--input" },
			description = "...",
			required = true
	)
	private File ontology;
	
	@Parameter(
			names = { "-c", "--class" },
			description = "...",
			required = true,
			variableArity = true
	)
	private List<String> classes;
	
	@Parameter(
			names = { "-o", "--output" },
			description = "...",
			required = true,
			variableArity = false
	)
	private File output;
	
	@Parameter(
			names = { "-oa", "--outputAll" },
			description = "...",
			required = false,
			variableArity = false
	)
	private File outputAll;
	
	
	@Parameter(
			names = { "-x", "--explain" },
			description = "...",
			required = false
	)
	private boolean explain;

	public File getOntology() {
		return ontology;
	}

	public void setOntology(File ontology) {
		this.ontology = ontology;
	}

	public List<String> getClasses() {
		return classes;
	}

	public void setClasses(List<String> classes) {
		this.classes = classes;
	}

	public boolean isExplain() {
		return explain;
	}

	public void setExplain(boolean explain) {
		this.explain = explain;
	}

	public File getOutput() {
		return output;
	}

	public void setOutput(File output) {
		this.output = output;
	}

	public File getOutputAll() {
		return outputAll;
	}

	public void setOutputAll(File outputAll) {
		this.outputAll = outputAll;
	}
	
	
}
