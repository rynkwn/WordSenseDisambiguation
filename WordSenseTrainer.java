

public class WordSenseTrainer {

	// Constructor that does training.
	// Reading in the corpus and tokenizing it.
	// Doing random indexing, constructing the sentence mapping.

	
	// Retrieve list of sentences that use this word ranked by
	// closest word sense.
	public ArrayList<String> retrieve();

	
	// Score a sentence based on "similarity" with original sentence.
	public double score(String sent1, String sent2, String word);
}
