package fr.limics.interactions.classifier;

import java.io.File;

public class Main {

	public static void main(String...args) throws Exception {
		Classifier c = new Classifier(new File(args[1]));
		c.classify(new File(args[0]));
		System.out.println(c.listInferredInstancesOfClass(Remiames.ADMINISTRATION));
		System.out.println(c.listInferredInstancesOfClass(RemiamesAdministrations.ADMINISTRATION_ACIDE_ACETYLSALICYLIQUE));
		
		System.out.println(c.listInferredSubClassesOfClass(RemiamesAdministrations.ADMINISTRATION_ACIDE_ACETYLSALICYLIQUE));
		
		c.close();
	}
	
}
