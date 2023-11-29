package fr.limics.interactions.classifier;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FolderIRIMapper implements OWLOntologyIRIMapper {
	
	private Logger log = LoggerFactory.getLogger(Classifier.class.getName());
	
	private File ontologiesFolder;
	private List<File> filenames;
	
	
	public FolderIRIMapper(File folder) {
		this.ontologiesFolder = folder;
		this.filenames = Arrays.asList(this.ontologiesFolder.listFiles());
	}

	@Override
	public IRI getDocumentIRI(IRI original) {
		String file = extractFile(original.getIRIString());
		String fileWithoutExtension = extractFileNameWithoutExtension(file); 
		
		// now test if we find a file with the same name in the folder
		for (File aFile : filenames) {
			if(fileWithoutExtension.equals(extractFileNameWithoutExtension(aFile.getName()))) {
				log.debug("Mapped "+original.getIRIString()+" to "+aFile.getAbsolutePath());
				// return the IRI of that file
				return IRI.create(aFile);
			}
		}
		return null;
	}
	
	public static String extractFile(String uri) {
	    // Suppression du fragment avec "#"
	    int fragmentIndex = uri.lastIndexOf("#");
	    if (fragmentIndex != -1) {
	        uri = uri.substring(0, fragmentIndex);
	    }

	    // Suppression du "/" final si présent
	    if (uri.endsWith("/")) {
	        uri = uri.substring(0, uri.length() - 1);
	    }

	    // Suppression de la partie avant le dernier "/"
	    int slashIndex = uri.lastIndexOf("/");
	    if (slashIndex != -1) {
	        uri = uri.substring(slashIndex + 1);
	    }

	    return uri;
	}
	
	public static String extractFileNameWithoutExtension(String fileName) {
	    int dotIndex = fileName.lastIndexOf(".");
	    if (dotIndex != -1 && dotIndex < fileName.length() - 1) {
	        return fileName.substring(0, dotIndex);
	    }
	    return fileName;
	}
}
