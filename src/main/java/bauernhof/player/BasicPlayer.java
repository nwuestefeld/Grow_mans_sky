package bauernhof.player;

import bauernhof.gameboard.GameBoard;
import bauernhof.preset.GameConfiguration;
import bauernhof.preset.ImmutableList;
import bauernhof.preset.Move;
import bauernhof.preset.card.Card;
import bauernhof.preset.Player;
import java.util.ArrayList;
import java.util.List;


/**
 * A super-class for {@link HumanPlayer} and {@link RandomAIPlayer}.
 * Implements all methods from {@link bauernhof.preset.Player} that the two classes have in common. Only the
 * PlayerType-specific {@link BasicPlayer#request()} methods needs an instance of {@link HumanPlayer} or {@link RandomAIPlayer}
 * to function properly.
 * @author Jonas Härtner
 */
public class BasicPlayer implements Player {

    /**
     * The name of the player.
     */
    private final String playername;

    /**
     * Flag to see if player has already been initialized.
     */
    private boolean initialized = false;

    /**
     * The ID of the player (1-4).
     */
    private int playerid;

    /**
     * The number of players in the game (2-4).
     */
    private int numplayers;

    /**
     * The {@link GameBoard} of the player used to keep track of the game logic and verify the game at the end.
     */
    private GameBoard playerboard;

    /**
     * A counter to ensure that update only gets called once per round per player.
     */
    private int updatedCounter = -1;


    /**
     * Constructor for BasicPlayer. The only variable accessible at all times is {@link BasicPlayer#playername}. The rest
     * will be made available by means of calling {@link BasicPlayer#init(GameConfiguration, ImmutableList, int, int)}.
     * @param playerName The name of the player, see also {@link BasicPlayer#playername}.
     */
    public BasicPlayer(String playerName) {
        this.playername = playerName;
    }

    /**
     * Getter for {@link BasicPlayer#initialized}.
     * @return true if player has been initialized, false otherwise.
     */
    protected boolean getInitialized() { return initialized; }

    /**
     * Getter for {@link BasicPlayer#numplayers}.
     * @return The number of players in the game.
     */
    protected int getNumplayers() { return numplayers; }

    /**
     * Getter for the player board {@link BasicPlayer#playerboard}.
     * @return The player's own GameBoard.
     */
    protected GameBoard getPlayerboard() { return playerboard; }

    /**
     * Getter for {@link BasicPlayer#playername}. Can be called at any time, even before
     * {@link BasicPlayer#init(GameConfiguration, ImmutableList, int, int)} is called.
     * @return The player's name.
     * @throws Exception If the player has not been given a name.
     */
    @Override
    public String getName() throws Exception {
        if ((playername == null) || playername.isEmpty()) {
            throw new Exception("Player Name not applicable");
        }
        return playername;
    }

    /**
     * Getter for {@link BasicPlayer#playerid}. Only accessible after
     * {@link BasicPlayer#init(GameConfiguration, ImmutableList, int, int)} was called and the player
     * was given an ID.
     * @return The ID of the player.
     * @throws UninitializedPlayerException If the player has not been initialized.
     */
    protected int getPlayerid() throws UninitializedPlayerException {
        if (!(this.getInitialized())) {
            throw new UninitializedPlayerException("Player needs to be initialized first");
        }
        return playerid;
    }

    /**
     * Moves cannot be requested from an instance of {@link BasicPlayer}!
     * @return Not applicable, will always throw an exception if called this way.
     * @throws Exception Operation is not supported.
     */
    @Override
    public Move request() throws Exception {
        throw new UnsupportedOperationException("Cannot request move from BasicPlayer, must be HumanPlayer or RandomAIPlayer");
    }

    /**
     * Updates the player's {@link BasicPlayer#playerboard} to include a move another player made. Can only be called
     * on another player's turn and if so only once.
     * @param opponentMove The move the opponent just made.
     * @throws Exception If the player has not been initialized, it's the player's own turn, or the player has already been
     * updated this turn.
     */
    @Override
    public void update(Move opponentMove) throws Exception {
        /*
        Since the game is cyclical in nature, the remainder of the turn count divided by the number of players will be the
        player ID of the player whose turn it is. Since the highest player ID is equal to the number of players, we need the
        remainder on the right side as well as 0 = numplayers (mod numplayers) but the logical operator "==" does not
        understand congruence classes.
         */
        if ((GameBoard.getTurnCount() % numplayers) == (playerid % numplayers)) {
            throw new UnsupportedOperationException("update() can only be called for players other than the current player");
        }

        if (!(this.getInitialized())) {
            throw new UninitializedPlayerException("Player needs to be initialized first");
        }

        /*
        Upon successfully updating the player's GameBoard, the update counter is set to the turn counter to signalize that
        the player has already been updated. Since only one update is allowed, an exception is thrown on the second try.
         */
        if (updatedCounter == GameBoard.getTurnCount()) {
            throw new UnsupportedOperationException("update() can only be called once per turn per player");
        }

        //The actual update.
        try {
            playerboard.makeMove(opponentMove);
            updatedCounter = GameBoard.getTurnCount();
        } catch (IllegalMoveException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Getter for the player's score in the game as held by their {@link BasicPlayer#playerboard}.
     * @return The player's score.
     * @throws Exception If the player has not been initialized.
     */
    @Override
    public int getScore() throws Exception {
        if (!(this.getInitialized())) {
            throw new UninitializedPlayerException("Player needs to be initialized first");
        }

        return playerboard.getPlayerScore(playerid);
    }

    /**
     * Used to verify the game scores upon completion of the game.
     * @param scores A list of the scores produced by another {@link GameBoard} to check against the scores stored in the
     *               player's own board.
     * @throws Exception If the player has not been initialized.
     */
    @Override
    public void verifyGame(ImmutableList<Integer> scores) throws ScoreDiscrepancyException, UninitializedPlayerException {
        if (!initialized) {
            throw new UninitializedPlayerException("Game can only be verified by an initialized player");
        }

        List<Integer> testscores = playerboard.getPlayerScoresAsList();
        for (int i = 0; i < scores.size(); i++) {
            if (scores.get(i).compareTo(testscores.get(i)) != 0) {
                throw new ScoreDiscrepancyException("Scores could not be verified by player: " + playername);
            }
        }
        System.out.println("Scores successfully verified by player: " + playername);
    }

    /**
     * Used to initialize the player. All other methods besides {@link BasicPlayer#getName()} can only be used after
     * this method has been successfully called.
     * @param config The {@link bauernhof.preset.GameConfiguration} used here to find out how many cards are in the player's hand.
     * @param initialDrawPile The list of {@link Card} after they have been shuffled by {@link bauernhof.main.Main#main(String[])}
     *                        from which each player draws their hand before the first actual turn.
     * @param numplayers The number of players in the game.
     * @param playerid The ID of the player.
     * @throws UnsupportedOperationException If the player has already been initialized. Can only be called once per player.
     */
    @Override
    public void init(GameConfiguration config, ImmutableList<Card> initialDrawPile, int numplayers, int playerid) throws UnsupportedOperationException {
        if (this.getInitialized()) {
            throw new UnsupportedOperationException("Player has already been initialized");
        }

        this.initialized = true;
        this.numplayers = numplayers;
        this.playerid = playerid;

        /*
        If every player draws numCardsPerPlayerHand many cards before the start of the first round, that means the player's
        GameBoard needs to start with a draw pile of size initialDrawPile.size() - numplayers * numCardsPerPlayerHand.
         */
        List<Card> drawPile = new ArrayList<>(initialDrawPile);
        int numCardsPerPlayerHand = config.getNumCardsPerPlayerHand();
        drawPile.subList(0, (numplayers * numCardsPerPlayerHand)).clear();

        playerboard = new GameBoard(numplayers, drawPile);

        /*
        Each player draws numCardsPerPlayerHand many cards from the draw pile in the beginning. Hence, each individual player's
        hand consists of the slice of the draw pile between numCardsPerPlayerHand times the number of players who have drawn
        before them.
         */
        for (int j = 0; j < numplayers; j++) {
            int offset = j * numCardsPerPlayerHand;
            List<Card> initialPlayerHand = new ArrayList<>();

            for (int i = offset; i < offset + numCardsPerPlayerHand; i++) {
                initialPlayerHand.add(initialDrawPile.get(i));
            }

            /*
            Setting each individual player's cards in every player's board, so everyone "knows" everyone else's cards to
            check if a move was legitimate.
             */
            playerboard.setPlayerCards(j + 1, initialPlayerHand);
        }

    }
}