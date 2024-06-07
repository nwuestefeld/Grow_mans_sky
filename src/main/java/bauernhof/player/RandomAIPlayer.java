package bauernhof.player;

import bauernhof.preset.GameConfiguration;
import bauernhof.preset.ImmutableList;
import bauernhof.preset.Move;
import bauernhof.gameboard.*;
import bauernhof.preset.card.Card;

/**
 * A class corresponding to {@link bauernhof.preset.PlayerType#RANDOM_AI} to enable playing
 * agains a randomly drawing and discarding AI.
 */
public class RandomAIPlayer extends BasicPlayer{

    /**
     * The time (in milliseconds) the AI player should wait between each move, so the human
     * in front of the screen can follow along better. Handed over by the {@link bauernhof.preset.ArgumentParser} through
     * the -d flag.
     */
    private final long delay;

    /**
     * Constructor for the {@link RandomAIPlayer}.
     * @param playername The name of the player.
     * @param delay The time the AI should wait between drawing and discarding cards.
     */
    public RandomAIPlayer(String playername, long delay) {
        super(playername);
        this.delay = delay;
    }

    /**
     * Used to let the AI make a move at random.
     * @return The move the AI just made.
     * @throws Exception If the player has not yet been initialized (see also {@link BasicPlayer#init(GameConfiguration, ImmutableList, int, int)}).
     */
    @Override
    public Move request() throws Exception {
        if (!(getInitialized())) {
            throw new UninitializedPlayerException("request can only be called by an initialized player");
        }

        //Same as in HumanPlayer
        try {
            if ((GameBoard.getTurnCount() % getNumplayers()) != getPlayerid() % getNumplayers()) {
                throw new UnsupportedOperationException("request can only be called by the current player");
            } else {
                GameBoard playerboard = getPlayerboard();
                //Deciding whether to draw from discard or draw pile, given that there are cards in the discard pile.
                boolean drawFromDiscardPile = (Math.random() > 0.5 && playerboard.getDiscardPile().getSize() > 0);

                //First time waiting so the player realizes it's the AI's turn.
                Thread.sleep(delay);

                Card taken;
                if (drawFromDiscardPile) {
                    //Pick a random card from the discard pile and draw it.
                    int randDraw = (int) (playerboard.getDiscardPile().getSize() * Math.random());
                    taken = playerboard.getDiscardPile().lookAt(randDraw);
                }
                else {//Only the first card from the draw pile can be drawn.
                    taken = playerboard.getDrawPile().lookAt(0);
                }

                //Another short pause.
                Thread.sleep(delay);

                Card deposited;
                if (Math.random() < 0.1){ //Randomly discarding the card that was just drawn.
                    deposited = taken;
                }
                else { //Alternatively discarding a card from the hand.
                    int randDiscard = (int) (playerboard.getPlayerPile(getPlayerid()).getSize() * Math.random());
                    deposited = playerboard.getPlayerPile(getPlayerid()).lookAt(randDiscard);
                }

                //Making the move on the player board.
                Move myMove = new Move(taken, deposited);
                playerboard.makeMove(myMove);

                //Final pause.
                Thread.sleep(delay);

                return myMove;
            }
        } catch (UninitializedPlayerException e) {
            e.printStackTrace();
            throw e;
        }
    }
}
