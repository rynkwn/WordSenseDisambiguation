import java.util.HashMap;
import java.util.Comparator;

// A class used to sort our training sentences after they've been scored.
class ScoreComparator implements Comparator<String[]>{
    HashMap<String[], Double> sentenceScores;
    
    public ScoreComparator(HashMap<String[], Double> scores){
	sentenceScores = scores;
    }
    
    public int compare(String[] a, String[] b){
	double diff = sentenceScores.get(a) - sentenceScores.get(b);
	if (diff < 0.0) return 1;
	else if (diff > 0.0) return -1;
	else return 0;
    }
}