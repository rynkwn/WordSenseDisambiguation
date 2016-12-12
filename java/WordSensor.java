import java.util.Scanner;
import java.util.List;

// The interface for WordSenseTrainer
public class WordSensor {

    // method to use for scoring: 0 = sentence match score, 1 = word by word score
    int scoreMethod;
    // context window size (on either side)
    int windowSize;
    // what corpus to rank for retrieval; 0 = training corpus, 1 = dictionary
    int rankCorpus;

    public static void main(String[] args) {
 
	// FLAG: Set to false if we should stop.
	boolean keepRunning = true;

	// Use the first argument as the directory name.
	String dirName = args[0];
	boolean useDefinitions = Boolean.parseBoolean(args[1]);

	// We build up our Word Sense magic
	WordSenseTrainer wordSense;

	try{
	    wordSense = new WordSenseTrainer(dirName);
	} catch(Exception e) {
	    e.printStackTrace();
	    System.err.println("File not found, or file not a directory!");
	    return;
	}


	// A scanner to read in user input.
	Scanner scan = new Scanner(System.in);

	int method = promptForScoringSystem(scan);

	String sentence;
	String word;
	List<String[]> results;

	while(keepRunning) {
	    
	    // We read in the user's input.
	    // TODO: GARY. IF YOU KNOW A BETTER WAY TO READ IN A FULL LINE OF INPUT
	    // MINUS THE NEWLINE CHARACTER, LET ME KNOW. CURRENTLY HACKY.
	    prompt("Please enter a sentence.");
	    sentence = readNextLine(scan);


	    // If the "sentence" is just a "q", we break.
	    if(sentence.equalsIgnoreCase("q")) {
		keepRunning = false;
		break;
	    }

	    prompt("Which word in that sentence is ambiguous?");
	    word = readNextLine(scan);

	    flush(1);

	    results = wordSense.retrieve(sentence, word, method, useDefinitions);
	    printResults(results, wordSense);

	    flush(2);
	}
    }

    
    //////////////////////////////////
    //
    // Helper Methods
    //

    public static void printResults(List<String[]> results, WordSenseTrainer wordSense) {

	if(results.size() == 0) {
	    System.out.println("No results!");
	} else {
	    for(String[] sentence : results) {
		for(String word : sentence) {
		    System.out.print(word + " ");
		}

		System.out.print("\t" + wordSense.getScore(sentence));
		System.out.println();
	    }
	}
    }

    // Reads the next line of input and removes the newline character at the end.
    public static String readNextLine(Scanner scan) {
	String line = scan.nextLine();
	//line = line.substring(0, line.length());
	return line;
    }

    // Prompt the user for which scoring system we should use.
    public static int promptForScoringSystem(Scanner scan) {
	flush(1);

	System.out.println("Which scoring system would you like to use?");
	flush(1);
	System.out.println("\t0) Sentence Match -> Compare a window around the first instance of the ambiguous word in the input and training sentences");
	System.out.println("\t1) Word by Word -> Look at every non-ambiguous word in the input sentence and compare its context vector with a window of words in the training sentence.");
	
	int val = -1;
	while(val != 0 && val != 1) {
	    prompt("Please enter 0 or 1");
	    val = scan.nextInt();
	    scan.nextLine(); // Need to flush out the new line character.
	}

	return val;
    }

    public static void prompt(String message) {
	System.out.println(message);
    }

    // Creates n new lines.
    public static void flush(int n) {
	for(int i = 0; i < n; i++) {
	    System.out.println();
	}
    }    
}
