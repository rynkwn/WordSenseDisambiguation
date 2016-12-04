import java.io.*; // Shorten afterwards.
import java.util.*;
import opennlp.tools.sentdetect.*;
import opennlp.tools.tokenize.*;

import java.util.ArrayList;
import java.util.HashMap;

public class WordSenseTrainer {

    public final int CONTEXT_WINDOW_SIZE = 3;
    public final int VECTOR_SIZE = 2000;
    public final int VECTOR_FILL = 100;

    public static SentenceDetectorME sentenceDetector;
    public static Tokenizer tokenizer;
    public HashMap<String, List<String>> concordance;

    // Mapping from Word -> (Index -> Value).
    public HashMap<String, HashMap<Integer, Integer>> randomIndex;

    // Constructor that does training.
    // Reading in the corpus and tokenizing it.
    // Doing random indexing, constructing the sentence mapping.

    // Takes in a String name of training corpus/Directory.
    public WordSenseTrainer(String dirName) throws FileNotFoundException {
	

	// Build up Sentence model/detector.
	InputStream modelIn = new FileInputStream("en-sent.bin");
	
	try {
	    SentenceModel model = new SentenceModel(modelIn);
	    sentenceDetector = new SentenceDetectorME(model);
	}
	catch (IOException e) {
	    e.printStackTrace();
	}
	finally {
	    if (modelIn != null) {
		try {
		    modelIn.close();
		}
		catch (IOException e) {
		}
	    }
	}
	
	


	// Build up token model/detector.
	InputStream tokenModelIn = new FileInputStream("en-token.bin");
	
	try {
	    TokenizerModel tokenModel = new TokenizerModel(tokenModelIn);
	    tokenizer = new TokenizerME(tokenModel);
	}
	catch (IOException e) {
	    e.printStackTrace();
	}
	finally {
	    if (tokenModelIn != null) {
		try {
		    tokenModelIn.close();
		}
		catch (IOException e) {
		}
	    }
	}

	
	

	
	// For every file in our data set, we want to loop over.
	File dataDir = new File(dirName);	
	
    }

    
    /**
     *  Original Author: Johan Boye
     *
     *  Tokenizes and indexes the file @code{f}. If @code{f} is a directory,
     *  all its files and subdirectories are recursively processed.
     */
    public void processFiles( File f ) {
	// do not try to index fs that cannot be read
	if ( f.canRead() ) {
	    if ( f.isDirectory() ) {
		String[] fs = f.list();
		// an IO error could occur
		if ( fs != null ) {
		    for ( int i=0; i<fs.length; i++ ) {
			processFiles( new File( f, fs[i] ));
		    }
		}
	    } else {
		
		// We have a file.

		// Grab the contents of the file.

		// TODO: May be painful.
		String content = "";
		
		try{
		    content = new Scanner(f).useDelimiter("\\Z").next();
		} catch(FileNotFoundException e) {
		    e.printStackTrace();
		}

		// Does below work?
		String[] sentences = sentenceDetector.sentDetect(content);

		// For each sentence, build up context vector of relevant words.
		// Add to hashmap of Word -> Sentences.
		for(String sentence : sentences) {
		    processSentence(sentence);
		}
		
	    }
	}
    }


    // Turn sentence into array of tokens.
    // Use sliding window with array indexes.
    public void processSentence(String sentence) {
	 
	String[] tokens = tokenizer.tokenize(sentence);
	
	
	// Random Indexing.
	//
    }

    
    // Creates a random vector using VECTOR_SIZE and VECTOR_FILL constants.
    public HashMap<Integer, Integer> createRandomVector() {
	HashMap<Integer, Integer> vector = new HashMap<Integer, Integer>();

	
    }
    
    
    // Retrieve list of sentences that use this word ranked by
    // closest word sense.
    public ArrayList<String> retrieve(String inputSentence, String word) {
	return null;
    }
    
    
    // Score a sentence based on "similarity" with original sentence.
    public double score(String sentence, String inputSentence, String word) {
	return 0.0;
    }
}
