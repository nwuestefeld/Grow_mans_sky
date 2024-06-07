package bauernhof.player;

/**
 * A helper class to categorize exceptions thrown by {@link BasicPlayer} methods other than getName()
 * @author Jonas Härtner
 */
public class UninitializedPlayerException extends Exception {

    /**
     * Constructor for UninitializedPlayerException
     * @param s The exception message
     */
    UninitializedPlayerException(String s) {
        super(s);
    }

}
