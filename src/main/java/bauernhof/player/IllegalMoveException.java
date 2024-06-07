package bauernhof.player;

/**
 * Helper class to categorize exceptions that correlate  to illegal moves.
 * @author Jonas Härtner
 */
public class IllegalMoveException extends Exception {
    /**
     * Constructor for IllegalMoveException.
     * @param s The exception message.
     */
    public IllegalMoveException(String s) { super(s); }

}
