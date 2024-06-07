package bauernhof.player;

/**
 * A helper class to categorize exceptions thrown by {@link bauernhof.player.BasicPlayer#verifyGame}
 * @author Jonas Härtner
 */
public class ScoreDiscrepancyException extends Exception {

    /**
     * Constructor for ScoreDiscrepancyException
     * @param s The exception message
     */
    ScoreDiscrepancyException(String s) {
        super(s);
    }

}

