package fr.limics.interactions.classifier.cli;

import com.beust.jcommander.Parameter;

public class ArgumentsMain {

	@Parameter(
			names = { "-h", "--help" },
			description = "Affiche le message d'aide",
			help = true
	)
	private boolean help = false;
	
	
	public boolean isHelp() {
		return help;
	}

	public void setHelp(boolean help) {
		this.help = help;
	}

}
