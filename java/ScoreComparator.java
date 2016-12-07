import java.util.HashMap;
import java.util.Comparator;

// A class used to sort our training sentences after they've been scored.
class ScoreComparator implements Comparator<String[]>{
    HashMap<String[], Double> sentenceScores;
    
    public ScoreComparator(HashMap<String[], Double> scores){
	sentenceScores = scores;
    }
    
    public int compare(String[] a, String[] b){
	return (int)(sentenceScores.get(a) - sentenceScores.get(b));
    }
}