import java.util.Scanner;
import java.util.ArrayList;

// The interface for WordSenseTrainer
public class WordSensor {

    public static void main(String[] args) {

	// FLAG: Set to false if we should stop.
	boolean keepRunning = true;

	// Use the first argument as the directory name.
	String dirName = args[0];

	// We build up our Word Sense magic
	WordSenseTrainer wordSense;

	try{
	    wordSense = new WordSenseTrainer(dirName);
	} catch(Exception e) {
	    e.printStackTrace();
	    error("File not found, or file not a directory!");
	    return;
	}


	// A scanner to read in user input.
	Scanner scan = new Scanner(System.in);

	String sentence;
	String word;
	ArrayList<String[]> results;

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

	    results = wordSense.retrieve(sentence, word);
	    printResults(results, wordSense);

	    flush(2);
	}
    }

    
    //////////////////////////////////
    //
    // Helper Methods
    //

    public static void printResults(ArrayList<String[]> results, WordSenseTrainer wordSense) {

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

    public static void prompt(String message) {
	System.out.println(message);
    }

    // Creates n new lines.
    public static void flush(int n) {
	for(int i = 0; i < n; i++) {
	    System.out.println();
	}
    }

    public static void error(String message) {
	System.out.println("____________________________________");
	System.out.println("ERROR:");
	System.out.println("\t" + message);
	System.out.println("____________________________________");
    }
}
