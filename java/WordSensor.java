import java.util.Scanner;

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

	while(keepRunning) {
	    
	    // We read in the user's input.
	    prompt("Please enter a sentence.");
	    sentence = scan.nextLine();


	    // If the "sentence" is just a "q", we break.
	    if(sentence.equalsIgnoreCase("q")) {
		keepRunning = false;
		break;
	    }

	    prompt("Which word in that sentence is ambiguous?");
	    word = scan.nextLine();

	    flush(2);
	}
    }

    
    //////////////////////////////////
    //
    // Helper Methods
    //

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
