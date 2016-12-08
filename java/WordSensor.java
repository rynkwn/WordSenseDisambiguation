

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

	while(keepRunning) {
	    // We read in the user's input.

	    keepRunning = false;
	}
    }

    
    //////////////////////////////////
    //
    // Helper Methods
    //

    public static void error(String message) {
	System.out.println("____________________________________");
	System.out.println(message);
	System.out.println("____________________________________");
    }
}
