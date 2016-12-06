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
    public HashMap<String, ArrayList<String[]> concordance;

    // Mapping from Word -> (Index -> Value).
    public HashMap<String, HashMap<Integer, Integer>> randomIndex = new HashMap<String, HashMap<Integer, Integer>>();
    public HashMap<String, HashMap<Integer, Integer>> context = new HashMap<String, HashMap<Integer, Integer>>();

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
	
	for (String t : tokens){
	    // Random indexing: generate the random vector for this word if it doesn't exist
	    if (!randomIndex.containsKey(t)){
		randomIndex.put(t, createRandomVector());
	    }

	    // add this sentence to this word's concordance entry
	    ArrayList<String> entry = concordance.get(t);
	    if (entry == null){
		entry = new ArrayList<String>();
		concordance.put(t, tokens);
	    } 
	    entry.add(sentence);
	}
	
	for (int i = 0; i < tokens.length; i++){
	    for (int c = Math.max(0, i-CONTEXT_WINDOW_SIZE);
		 c <= Math.min(tokens.length-1, i+CONTEXT_WINDOW_SIZE);
		 c++){
		if (!tokens[c].equals(tokens[i])){
		    HashMap<Integer, Integer> wordContext = context.get(tokens[i]);
		    if (wordContext == null){
			wordContext = new HashMap<Integer, Integer>();
		    }
		    sumVectors(wordContext, randomIndex.get(tokens[c]));
		}
	    }
	}
    }

    
    // Creates a random vector using VECTOR_SIZE and VECTOR_FILL constants.
    public HashMap<Integer, Integer> createRandomVector() {
	
	HashMap<Integer, Integer> vector = new HashMap<Integer, Integer>();
	Random r = new Random();
	
	while (vector.size() < VECTOR_FILL){
	    // set random index in vector to 1 or -1 (1 - (0*2), or 1 - (1*2));
	    vector.put(r.nextInt(VECTOR_SIZE), 1-(r.nextInt(2)*2));
	}

	return vector;
    }

    // Retrieve list of sentences that use this word ranked by
    // closest word sense.
    public ArrayList<String> retrieve(String inputSentence, String word) {
	return null;
    }
    
    public ArrayList<String[]> retrieveSentenceMatch(String inputSentence, String word) {
    	String[] tokens = tokenizer.tokenize(inputSentence);

  
    	ArrayList<String[]> results = new ArrayList<String[]>();

    	// copy over sentences so we can sort them undestructively
    	ArrayList<String[]> matches = concordance.get(word);
    	for (String[] sentence : matches){
    		results.add(sentence);
    	}

    	HashMap<Integer, Integer> queryContext = buildSentenceWindowContext(inputSentence, word);

    	HashMap<String[], HashMap<Integer, Integer>> sentenceContexts = new HashMap<String[], HashMap<Integer, Integer>>();
    	for (String[] s: results){
    		sentenceContexts.add(s, buildSentenceWindowContext(s, word));
    	}

    	// next:  sort results by distance between queryContext and each result's sentenceContext vector
    	// will need a comparator

    	return null;
    }

    // Returns context vector for a single word in this specific sentence
    public HashMap<Integer, Integer> buildSentenceWindowContext(String[] sentence, String word){
    	// find the first occurence of target word in sentence
    	int wordPos;
    	for (wordPos = 0; wordPos < tokens.length; wordPos++){
    		if (tokens[wordPos].equals(word)) break;
    	}
    	// word not found in sentence; return empty results
    	if (wordPos >= tokens.length) return new HashMap<Integer, Integer>;

    	// build context vector
    	HashMap<Integer, Integer> res = new HashMap<Integer, Integer>();
    	for (int i = Math.max(0,wordPos-CONTEXT_WINDOW_SIZE); i <= Math.min(tokens.length-1, wordPos + CONTEXT_WINDOW_SIZE); i++){
    		if (i != wordPos){
    			sumVectors(res, randomIndex.get(t));
    		}    		
    	}

    	return res;
    }
    
    // Score a sentence based on "similarity" with original sentence.
    public double score(String sentence, String inputSentence, String word) {
	return 0.0;
    }

    // Scores two sentences by looking at individual words in the training sentence
    // as well as the input sentence.
    // @param word Is the ambiguous word in inputSentence.
    public double wordByWordScore(String sentence, String inputSentence, String word) {

	String[] trainingWords = tokenizer.tokenize(sentence);
	String[] inputWords = tokenizer.tokenize(sentence);

	// For each word in the inputSentence, I want to look at a window of words in the
	// training sentence.

	for(int i = 0; i < inputWords.length; i++) {
	    
	}
	
	return 0.0;
    }


    //////////////////////////////////
    //
    // Helper Methods
    //

    public int manhattenDistance(HashMap<Integer, Integer> vector1, HashMap<Integer, Integer> vector2) {
	int dist = 0;

	// We create a common set of all indices that have non-zero values.
	HashSet<Integer> indices = new HashSet<Integer>();

	indices.addAll(vector1.keySet());
	indices.addAll(vector2.keySet());

	// Now we go through those indices and take manhatten distances.
	for(int index : indices) {
	    int val1 = (vector1.containsKey(index)) ? vector1.get(index) : 0;
	    int val2 = (vector2.containsKey(index)) ? vector2.get(index) : 0;

	    dist += Math.abs(val1 - val2);
	}

	return dist;
    }


    // Derives cosine similarity from this formula:
    // v1 o v2 = ||v1|| * ||v2|| * cos(0)
    public double cosineSimilarity(HashMap<Integer, Integer> vector1, HashMap<Integer, Integer> vector2) {
	
	// ||v1|| * ||v2|| should be set to the below value.
	double sqrSize = VECTOR_FILL;
	
	double dprod = (double) dotProduct(vector1, vector2);

	double cosVal = dprod / sqrSize;
	double radDiff = Math.acos(cosVal);
	
	return radDiff;
    }

    // Adds contents of v2 to v1, and returns v1
    public void sumVectors(HashMap<Integer, Integer> v1, HashMap<Integer, Integer> v2){
	for (int index : v2.keySet()){
	    int toAdd = v2.get(index);

	    int val = (v1.get(index) == null) ? 0 : v1.get(index);
	    v1.put(index, val + toAdd);
	}
    }

    // Returns the result of v1 o v2
    public int dotProduct(HashMap<Integer, Integer> v1, HashMap<Integer, Integer> v2) {
	int output = 0;
	
	// I really only have to go through the indices for one of the HashVectors.
	// If they don't share a common index, then the added sum is 0.
	for(int index : v1.keySet()) {
	    int val1 = (v1.containsKey(index)) ? v1.get(index) : 0;
	    int val2 = (v2.containsKey(index)) ? v2.get(index) : 0;

	    output += (val1 * val2);
	}

	return output;
    }
}
