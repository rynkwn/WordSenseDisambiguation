import java.util.ArrayList;
import java.util.HashMap;

public class WordSenseTrainer {

    // Constructor that does training.
    // Reading in the corpus and tokenizing it.
    // Doing random indexing, constructing the sentence mapping.
    public WordSenseTrainer();
    
    
    // Retrieve list of sentences that use this word ranked by
    // closest word sense.
    public ArrayList<String> retrieve(String inputSentence, String word);
    
    
    // Score a sentence based on "similarity" with original sentence.
    public double score(String sentence, String inputSentence, String word);
}
