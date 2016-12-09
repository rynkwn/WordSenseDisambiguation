import java.io.*; // Shorten afterwards.
import java.util.*;
import opennlp.tools.sentdetect.*;
import opennlp.tools.tokenize.*;
import opennlp.tools.postag.*;
import opennlp.tools.lemmatizer.SimpleLemmatizer;

import java.util.ArrayList;
import java.util.HashMap;

public class WordSenseTrainer {

    //////////////////////////////////
    //
    // GENERAL CONSTANTS
    //
    public final int CONTEXT_WINDOW_SIZE = 3;
    public final int VECTOR_SIZE = 2000;
    public final int VECTOR_FILL = 100;

    //////////////////////////////////
    //
    // CONSTANTS RELATED TO wordByWordScore
    //

    public final int WORD_BY_WORD_WINDOW_SIZE = 3;


    public static SentenceDetectorME sentenceDetector;
    public static Tokenizer tokenizer;
    public static POSTaggerME posTagger;
    public static SimpleLemmatizer lemmatizer;

    public HashMap<String, ArrayList<String[]>> concordance = new HashMap<String, ArrayList<String[]>>();

    // Mapping from Word -> (Index -> Value).
    public HashMap<String, HashMap<Integer, Integer>> randomIndex = new HashMap<String, HashMap<Integer, Integer>>();
    public HashMap<String, HashMap<Integer, Integer>> context = new HashMap<String, HashMap<Integer, Integer>>();

    // Constructor that does training.
    // Reading in the corpus and tokenizing it.
    // Doing random indexing, constructing the sentence mapping.

    // Takes in a String name of training corpus/Directory.
    public WordSenseTrainer(String dirName) throws FileNotFoundException {
	
	// Build up Sentence model/detector.
	InputStream modelIn = new FileInputStream("bin/en-sent.bin");
	
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
	InputStream tokenModelIn = new FileInputStream("bin/en-token.bin");
	
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

	// Build up a part of speech tagger
	InputStream posModelIn = new FileInputStream("bin/en-pos-maxent.bin");
	
	try {
	    POSModel posModel = new POSModel(posModelIn);
	    posTagger = new POSTaggerME(posModel);
	}
	catch (IOException e) {
	    e.printStackTrace();
	}
	finally {
	    if (posModelIn != null) {
		try {
		    posModelIn.close();
		}
		catch (IOException e) {
		}
	    }
	}

	// Create Lemmatizer
	InputStream lemmaIn = new FileInputStream("bin/en-lemmatizer.dict");
	
	lemmatizer = new SimpleLemmatizer(lemmaIn);

	try {
	    lemmaIn.close();
	} catch(IOException e) {
	    
	}

	// For every file in our data set, we want to loop over.
	File dataDir = new File(dirName);	
	
	processFiles(dataDir);
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

		content = content.toLowerCase();

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
	String[] tags = posTagger.tag(tokens);

	for(int i = 0; i < tokens.length; i++) {
	    tokens[i] = lemmatizer.lemmatize(tokens[i], tags[i]);

	    String word = tokens[i];
	    
	    // Random indexing: generate the random vector for this word if it doesn't exist
	    if(!randomIndex.containsKey(word)) {
		randomIndex.put(word, createRandomVector());
	    }

	    // add this sentence to this word's concordance entry
	    ArrayList<String[]> entry = concordance.get(word);
	    if (entry == null){
		entry = new ArrayList<String[]>();
		concordance.put(word, entry);
	    } 
	    entry.add(tokens);
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

    // Retrieve list of sentences that use this word ranked by
    // closest word sense.
    public ArrayList<String[]> retrieve(String inputSentence, String word) {
    	String[] tokens = tokenizer.tokenize(inputSentence);

	//concordancePrint();
  
    	ArrayList<String[]> results = new ArrayList<String[]>();

	// Always possible we don't actually have the word.
	if(concordance.containsKey(word)) {
	    // copy over sentences so we can sort them undestructively
	    for (String[] sentence : concordance.get(word)){
    		results.add(sentence);
	    }
	    
	    HashMap<String[], Double> sentenceScores = new HashMap<String[], Double>();
	    for (String[] s: results){
    		sentenceScores.put(s, score(s, tokens, word));
	    }
	    
	    // next:  sort results by distance between queryContext and each result's sentenceContext vector
	    // will need a comparator
	    Collections.sort(results, new ScoreComparator(sentenceScores));	    
	}

    	return results;
    }
    
    // Score a sentence based on "similarity" with original sentence.
    public double score(String[] trainingWords, String[] inputWords, String word) {
	return wordByWordScore(trainingWords, inputWords, word);
    }

    
    //////////////////////////////////
    //
    // Scoring Methods
    //

    public double sentenceMatchScore(String[] trainingWords, String[] inputWords, String word) {
    	HashMap<Integer, Integer> queryContext = buildSentenceWindowContext(inputWords, word);
    	HashMap<Integer, Integer> sentenceContext = buildSentenceWindowContext(trainingWords, word);
    	return (double) manhattenDistance(queryContext, sentenceContext);
    }

    // Returns context vector for a single word in this specific sentence
    public HashMap<Integer, Integer> buildSentenceWindowContext(String[] sentence, String word){
    	// find the first occurence of target word in sentence
    	int wordPos;
    	for (wordPos = 0; wordPos < sentence.length; wordPos++){
    		if (sentence[wordPos].equals(word)) break;
    	}
    	// word not found in sentence; return empty results
    	if (wordPos >= sentence.length) return new HashMap<Integer, Integer>();

    	// build window context vector
    	HashMap<Integer, Integer> res = new HashMap<Integer, Integer>();
    	for (int i = Math.max(0,wordPos-CONTEXT_WINDOW_SIZE); i <= Math.min(sentence.length-1, wordPos + CONTEXT_WINDOW_SIZE); i++){

    		if (i != wordPos){
    			sumVectors(res, getRandomVector(sentence[i]));
    		}
    	}

    	return res;
    }

    // Scores two sentences by looking at individual words in the training sentence
    // as well as the input sentence.
    // @param word Is the ambiguous word in inputSentence.
    public double wordByWordScore(String[] trainingWords, String[] inputWords, String word) {

	// We want to compare contextVectors.
	processSentence(join(inputWords));

	double finalScore = 0.0;

	// For each word in the inputSentence, I want to look at a window of words in the
	// training sentence.

	for(int i = 0; i < inputWords.length; i++) {
	    
	    double windowScore = 0.0;
	    double numWordsCompared = 0;
	    
	    // We don't want to perform this process on the actual input word.
	    // Instead, we'll only look at nearby words.
	    if(!inputWords[i].equals(word)) {
		HashMap<Integer, Integer> curWordContext = context.get(inputWords[i]);
		
		// Now loop through the largest possible window constrained by
		// WORD_BY_WORD_WINDOW_SIZE.
		for(int j = Math.max(0, i - WORD_BY_WORD_WINDOW_SIZE);
		    j <= Math.min(trainingWords.length - 1, i + WORD_BY_WORD_WINDOW_SIZE);
		    j++) {
		    
		    HashMap<Integer, Integer> targetWordContext = context.get(trainingWords[j]);

		    windowScore += (double) manhattenDistance(curWordContext, targetWordContext);
		    numWordsCompared++;
		    
		}
	    }

	    windowScore /= numWordsCompared;

	    finalScore += windowScore;
	}
	
	return finalScore;
    }

    //////////////////////////////////
    //
    // Helper Methods
    //

    // A safe way of getting a random vector.
    public HashMap<Integer, Integer> getRandomVector(String word) {
	if(randomIndex.containsKey(word))
	    return randomIndex.get(word);

	HashMap<Integer, Integer> vect = createRandomVector();
	randomIndex.put(word, vect);
	return vect;
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
    
    // Prints the contents of our concordance map.
    public void concordancePrint() {
	for(String word : concordance.keySet()) {
	    ArrayList<String[]> sentences = concordance.get(word);

	    System.out.println(word);
	    for(String[] sentence : sentences) {
		System.out.print("\t");
		printSentence(sentence);
	    }
	}
    }

    // Prints a sentence given in array form.
    public void printSentence(String[] sent) {
	for(String word : sent) {
	    System.out.print(word + " ");
	}

	System.out.println();
    }

    // Joins an array of String.
    public String join(String[] ar) {
	StringBuilder sb = new StringBuilder();
	for(String s : ar)
	    sb.append(s + " ");
	return sb.toString();
    }
}
