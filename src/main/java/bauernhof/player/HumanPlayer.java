package bauernhof.player;

import bauernhof.gameboard.GameBoard;
import bauernhof.gui.FarmPanel;
import bauernhof.preset.GameConfiguration;
import bauernhof.preset.ImmutableList;
import bauernhof.preset.Move;
import bauernhof.preset.PlayerGUIAccess;

/**
 * A class corresponding to {@link bauernhof.preset.PlayerType#HUMAN} to make the game playable by humans by requesting
 * moves through the GUI.
 * @author Jonas Härtner
 */
public class HumanPlayer extends BasicPlayer {

    /**
     * The {@link PlayerGUIAccess} instance through which the human in front of the screen can enter moves.
     */
    private final PlayerGUIAccess guiAccess;

    /**
     * Constructor for {@link HumanPlayer}.
     * @param playername The name of the player.
     * @param guiAccess The GUI used to enter moves.
     */
    public HumanPlayer(String playername, PlayerGUIAccess guiAccess) {
        super(playername);
        this.guiAccess = guiAccess;
    }

    /**
     * Requests a move from the {@link HumanPlayer}.
     * @return The move that the player just made.
     * @throws Exception If the player has not been initialized (see also {@link BasicPlayer#init(GameConfiguration, ImmutableList, int, int)})
     * or if it is not the player's turn. Or there's a problem with requesting a move from the GUI.
     */
    @Override
    public Move request() throws Exception {
        if (!(getInitialized())) {
            throw new UninitializedPlayerException("request can only be called by an initialized player");
        }

        /*
        Same logic as in update in BasicPlayer. Just this time we want it to be the player's turn.
         */
        try {//getPlayerid() might throw an exception
            if ((GameBoard.getTurnCount() % getNumplayers()) != getPlayerid() % getNumplayers()) {
                throw new UnsupportedOperationException("request can only be called by the current player");
            }
            else {
                /*
                Synchronized block to ensure the GUI can get the move from the current player and notify the HumanPlayer
                again without interference from another thread.
                 */
                synchronized (guiAccess) {
                    try {
                        /*
                        Casting into a FarmPanel isn't very elegant here, but that is how the GUI is written, and it works.
                         */
                        ((FarmPanel) guiAccess).doMove();
                        //Waiting for the GUI to notify the HumanPlayer once a move was made.
                        guiAccess.wait();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        throw e;
                    }
                }
                //Getting the move that was just made from the GUI.
                Move myMove = guiAccess.requestMoveFromCurrentHumanPlayer();
                //Handing the move on to the player's GameBoard first, so it gets recorded before being returned.
                this.getPlayerboard().makeMove(myMove);
                return myMove;
            }
        } catch (UninitializedPlayerException e) {
            e.printStackTrace();
            throw e;
        }
    }
}
