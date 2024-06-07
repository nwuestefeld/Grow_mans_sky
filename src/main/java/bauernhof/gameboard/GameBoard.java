package bauernhof.gameboard;

import bauernhof.player.IllegalMoveException;
import bauernhof.preset.Move;
import bauernhof.preset.card.Card;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class representing the game board including the draw and discard pile and relevant logic
 *
 * @author Maxim Strzebkowski
 */
public class GameBoard {
    /**
     * That static turn counter for the game
     * (used to identify the current player)
     */
    private static int turnCount = 0;
    /**
     * The draw pile (Nachziehstapel)
     */
    private final DrawPile drawPile;
    /**
     * The discard Pile (Ablagestapel)
     */
    private final CardPile discardPile;
    /**
     * The number of players playing the game
     */
    private final int numOfPlayers;
    /**
     * A map storing the player cards corresponding to their id
     */
    private final Map<Integer, CardPile> playerCards;
    /**
     * A map storing the player scores corresponding to their id
     */
    private final Map<Integer, Integer> playerScores;
    /**
     * A calculator for calculating points of different card lists
     */
    private final PointsCalculator pointsCalculator = new PointsCalculator();

    /**
     * Constructor for the GameBoard where
     * - drawPile is initiated
     * - discard pile is initialised as empty
     * - number of players is set
     * - player cards are initialised as empty
     *
     * @param numberOfPlayers the number of players in the game
     * @param drawPile        the initial draw pile cards in form of a list
     */
    public GameBoard(int numberOfPlayers, List<Card> drawPile) {
        this.drawPile = new DrawPile(drawPile);
        this.discardPile = new CardPile(new ArrayList<>());

        this.numOfPlayers = numberOfPlayers;
        this.playerCards = new HashMap<>();
        this.playerScores = new HashMap<>();
        for (int i = 1; i <= numberOfPlayers; i++) {
            this.playerCards.put(i, new CardPile(new ArrayList<>()));
            this.playerScores.put(i, 0);
        }
    }

    /**
     * Gets the current turn count
     *
     * @return the turn count
     */
    public static int getTurnCount() {
        return turnCount;
    }

    /**
     * Increase the turn count by 1
     */
    public static void increaseTurnCount(){
        turnCount++;
    }

    /**
     * Calculate the current player id using the turn count
     *
     * @return the calculated id
     */
    public int calculateCurrentPlayerId() {
        int modValue = turnCount % this.numOfPlayers;
        return modValue == 0 ? numOfPlayers : modValue;    //player id is mod of turn count except for number 4 where mod = 0 but needs to equal 4
    }

    /**
     * Verifies a move; If move was illegal then the {@link IllegalMoveException} is thrown
     *
     * @param move the move that should be checked
     * @throws IllegalMoveException
     */
    public void verifyMove(Move move) throws IllegalMoveException {
        CardPile currentPlayerPile = playerCards.get(this.calculateCurrentPlayerId());
        if(!this.drawPile.isCardInPile(move.getTaken()) && !this.discardPile.isCardInPile(move.getTaken())){    //Check if the taken card was part of the discard or the draw pile
            throw new IllegalMoveException("The taken card was not in the discard or the draw pile");
        }
        if (!currentPlayerPile.isCardInPile(move.getDeposited()) && !move.getTaken().equals(move.getDeposited())) { //Check if deposited card exists in the player hand IF the taken card is not immediately deposited
            throw new IllegalMoveException("The player did not have the card on their hand when they discarded it");
        }
        if (this.drawPile.isCardInPile(move.getTaken()) && !move.getTaken().equals(this.drawPile.lookAt(0))) {  //Check if the card was the first card in the draw pile (if taken from the draw pile)
            throw new IllegalMoveException("The card taken from the draw pile was not the first card in the draw pile");
        }
        if(move.getTaken() == null) {   //Checks if no card was drawn
            throw new IllegalMoveException("No card was taken from any pile: every move a card has to be drawn");
        }
        if(move.getDeposited() == null){    //Checks if no card was deposited
            throw new IllegalMoveException("No card was deposited: every move a card has to be deposited");
        }
    }

    /**
     * Takes the move and adjusts all piles accordingly
     *
     * @param move         the move to be made on the board
     * @throws IllegalMoveException when an illegal move was made
     */
    public void makeMove(Move move) throws IllegalMoveException {
        this.verifyMove(move);  //Checks if illegal move was made
        int currentPlayerId = this.calculateCurrentPlayerId();
        CardPile currentPlayerPile = this.getPlayerPile(currentPlayerId);   //Gets the current player pile
        if (this.drawPile.isCardInPile(move.getTaken())) {    //If card was drawn from drawPile
            this.drawPile.drawCard();
        } else if (this.discardPile.isCardInPile(move.getTaken())) {   //if card was drawn from discardPile
            this.discardPile.drawCard(move.getTaken());
        }
        if(!move.getTaken().equals(move.getDeposited())){
            currentPlayerPile.drawCard(move.getDeposited());
            currentPlayerPile.addCard(move.getTaken());     //Add the taken card to the player hand
        }
        this.discardPile.addCard(move.getDeposited());  //Add the deposited card to the discard pile

        this.playerScores.put(currentPlayerId, this.pointsCalculator.calculatePoints(currentPlayerPile));
    }

    /**
     * gets the player scores as a list
     *
     * @return a list of player scores (IMPORTANT: the score positions are the playerIds - 1)
     */
    public List<Integer> getPlayerScoresAsList() {
        return new ArrayList<>(this.playerScores.values());
    }

    /**
     * Get the player score via the player id
     *
     * @param playerId the player id
     * @return the player score as int
     */
    public int getPlayerScore(Integer playerId) {
        return this.playerScores.get(playerId);
    }

    /**
     * Update all player scores with the respective player cards
     */
    public void updateAllPlayerScores(){
        for (Integer playerId : this.playerScores.keySet()) {
            this.playerScores.put(playerId, this.pointsCalculator.calculatePoints(this.playerCards.get(playerId)));
        }
    }

    /**
     * get the player pile as {@link CardPile} via the player id
     *
     * @param playerId the player id
     * @return the {@link CardPile} of the player
     */
    public CardPile getPlayerPile(Integer playerId) {
        return playerCards.get(playerId);
    }

    /**
     *
     * Setter for PlayerCards to be used upon player initialization
     *
     * author Jonas Härtner
     * @param playerId the player id
     * @param playerCards the initial hand of the player corresponding to the player id
     */
    public void setPlayerCards(Integer playerId, List<Card> playerCards) { this.playerCards.put(playerId, new CardPile(playerCards)); }


    /**
     * Get the drawPile
     *
     * @return the draw pile as {@link DrawPile}
     */
    public DrawPile getDrawPile() {
        return drawPile;
    }

    /**
     * Get the discard pile
     *
     * @return the discard pile as {@link CardPile}
     */
    public CardPile getDiscardPile() {
        return discardPile;
    }
}