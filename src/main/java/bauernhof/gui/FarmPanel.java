package bauernhof.gui;

import bauernhof.preset.*;
import bauernhof.gameboard.GameBoard;
import bauernhof.preset.card.Card;
import bauernhof.preset.card.GCard;
import sag.ChildNotFoundException;
import sag.SAGFrame;
import sag.SAGPanel;
import sag.elements.*;
import sag.elements.shapes.GRect;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static bauernhof.screenshot.Screenshot.playSound;

/**
 * Represents the main panel of the farm game (Grow Mans Sky) GUI.
 * @author Corinna Liley
 */
public class FarmPanel extends SAGPanel implements PlayerGUIAccess {
    private GGroup overlayGroup;
    private OverlayCardDisplay overlayArea;
    private GGroup raw;
    private boolean overlayVisible = false;
    private GameBoard gameBoard;
    private boolean processingTaken = false;
    private boolean processingDeposited = false;
    private Card taken = null;
    private Card deposited = null;

    private GDrawStack drawStack;
    private GPlayerStack playerStack;

    private List<GPlayer> otherPlayerStacks = new ArrayList<>();
    private GDiscardStack discardStack;
    private GGroup instructionBoard = new GGroup();
    private Player currentPLayer;
    private List<Player> players;
    private List<Color> playerColors;
    private SAGFrame sagFrame;

    private GCardDisplayGroup gCardDisplayGroup;

    private GCard gTaken;
    protected int volume;

    /**
     * Creates a new instance of FarmPanel.
     *
     * @param VIEWPORT_WIDTH  the width of the viewport
     * @param VIEWPORT_HEIGHT the height of the viewport
     * @param gameBoard       the game board
     * @param sagFrame        the SAG frame
     * @param volume          the volume level
     */
    public FarmPanel(int VIEWPORT_WIDTH, int VIEWPORT_HEIGHT, GameBoard gameBoard, SAGFrame sagFrame, int volume) {
        super(VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
        this.gameBoard = gameBoard;
        this.sagFrame = sagFrame;
        this.volume = volume;
    }

    /**
     * Sets the taken card.
     *
     * @param card the card to be set as taken
     */
    public void setTaken( Card card){
        this.taken = card;
    }

    /**
     * Initializes the panel with players and player colors.
     *
     * @param players       the list of players
     * @param playerColors  the list of player colors
     * @throws Exception if an error occurs during initialization
     */
    public void initPanel(List<Player> players, List<Color> playerColors) throws Exception {
        this.players = players;
        currentPLayer = players.get(gameBoard.calculateCurrentPlayerId() - 1);
        this.playerColors = playerColors;

        // Create the main group for main elements
        GGroup mainGroup = this.addLayer((float) 0.1, (float) 0.8);

        // Create the background picture
        GSVG background = new GSVG(new File("src/main/ressources/growmanssky.svg"));
        background.setScale(0.7f);
        background.setPosition(-250f,-750f);
        mainGroup.addChild(background);

        // Create the current player stack at the bottom of the panel
        int currentPlayerID = gameBoard.calculateCurrentPlayerId();
        playerStack = new GPlayerStack(this, currentPLayer.getName(), gameBoard.getPlayerPile(currentPlayerID).getList(), playerColors.get(currentPlayerID - 1));
        mainGroup.addChild(this.playerStack);

        // Create the upper player stacks with all players
        int playerID = 1;
        for (Player p : players) {
            GPlayer otherPlayerStack = new GPlayer(this, p.getName(), gameBoard.getPlayerPile(playerID).getList(), playerColors.get(playerID - 1));
            otherPlayerStack.setPosition(playerID * 300 - 500, -700);
            mainGroup.addChild(otherPlayerStack);
            otherPlayerStacks.add(otherPlayerStack);
            playerID++;
        }

        // Create the draw stack
        drawStack = new GDrawStack(this, gameBoard.getDrawPile().getList());
        mainGroup.addChild(drawStack);

        // Create the discard stack
        discardStack = new GDiscardStack(this, gameBoard.getDiscardPile().getList());
        mainGroup.addChild(discardStack);

        // Create the instruction board
        GRect instructionBg = new GRect(0, 0, 400, 40, false, 5, 5);
        instructionBg.setFill(Color.YELLOW);
        instructionBoard.addChild(instructionBg);
        setInstructions("Bitte warten Sie auf Anweisungen.");
        instructionBoard.setPosition(0, -700);
        mainGroup.addChild(instructionBoard);

        // Create the screenshot button
        ScreenShot screenShotButton = new ScreenShot(this, 20, sagFrame);
        mainGroup.addChild(screenShotButton);

        // Create the sound button
        Soundbutton soundbutton =  new Soundbutton(this, 20 , 1440, -600);
        GText SoundButtonText = new GText("Ton");
        SoundButtonText.setPosition(soundbutton.getPositionX(),soundbutton.getPositionY()-25 );
        SoundButtonText.setFill(Color.BLACK);
        SoundButtonText.setFontSize(20);
        SoundButtonText.setAlignment(GText.TextAnchor.MIDDLE);
        SoundButtonText.setBold(true);
        mainGroup.addChild(SoundButtonText);
        mainGroup.addChild(soundbutton);


        // Create a space for the taken card
        gCardDisplayGroup = new GCardDisplayGroup(this, 1000, -250, 300, 400);
        mainGroup.addChild(gCardDisplayGroup);


        // Create a layer for elements to iterate through
        raw = this.addLayer((float) 0, (float) 0);

        //Create a GGroup for the displaying cards overlay
        overlayGroup = new GGroup();
        raw.addChild(this.overlayGroup);


    }

    /**
     * Method requested by a HumanPlayer (requests mouse clicks from human Player).
     */
    public void doMove() {
        setInstructions("Bitte wählen Sie eine Karte vom Nachzieh- oder Ablagestapel.");
        setProcessingTaken(true);
    }

    /**
     * After being notified, the HumanPlayer requests this Method.
     *
     * @return the requested move
     */
    public Move requestMoveFromCurrentHumanPlayer() {
        return new Move(taken, deposited);
    }

    /**
     * Updates the GUI with the current player after every Move.
     *
     * @param currentPlayer the current player
     * @throws Exception if an error occurs during update
     */
    public void updateGUI(Player currentPlayer) throws Exception {
        this.currentPLayer = currentPlayer;
        int currentPlayerID = gameBoard.calculateCurrentPlayerId();

        //Update all stacks given by the game-board
        drawStack.update(gameBoard.getDrawPile().getList());
        drawStack.update(gameBoard.getDrawPile().getList());
        playerStack.update(gameBoard.getPlayerPile(currentPlayerID).getList(), currentPlayer.getName(), gameBoard.getPlayerScore(currentPlayerID), playerColors.get(currentPlayerID - 1));//
        discardStack.update(gameBoard.getDiscardPile().getList());
        discardStack.update(gameBoard.getDiscardPile().getList());
        for (int i = 0; i < otherPlayerStacks.size(); i++) {
            otherPlayerStacks.get(i).update( players.get(i).getName(), gameBoard.getPlayerPile(i+1).getList(), gameBoard.getPlayerScore(i+1), playerColors.get(i));
        }
    }
    /**
     * Creates an overlay displaying cards and opens/closes it.
     *
     * @param cards the cards to be displayed
     */
    public void createOverlayInfo(List<Card> cards) {
        if (!overlayVisible) {
            //Open overlay
            if (overlayArea == null) {
                //Create new OverlayCardDisplay element (only once)
                overlayArea = new OverlayCardDisplay(this, 820f, 420f);
            }
            overlayGroup.addChild(overlayArea);
            overlayVisible = true;
            bringToFront(overlayGroup);
            overlayArea.updateCards(cards);
        } else {
            //Close overlay
            overlayGroup.removeChild(overlayArea);
            overlayVisible = false;
        }
    }

    /**
     * Closes the overlay.
     */
    public void closeOverlayInfo() {
        overlayGroup.removeChild(overlayArea);
        overlayVisible = false;
    }


    /**
     * Brings the overlay (displayed cards) to the front by swapping the GGroup with the last Element in Raw.
     *
     * @param overlayGroup the overlay group
     */
    private void bringToFront(GGroup overlayGroup) {
        GElement lastElement = null;
        for (GElement gElement : raw) {
            lastElement = gElement;
        }
        try {
            if (lastElement != overlayGroup) {
                raw.swapChildren(lastElement, overlayGroup);
            }
        } catch (ChildNotFoundException e) {
        }
    }


    /**
     * After a card from the draw stack has been clicked.
     */
    public void drawCardDrawStack() {
        //Only if the current player requested a move
        if (processingTaken) {
            taken = gameBoard.getDrawPile().lookAt(0);
            setInstructions("Karte (" + taken.getName() + ")  wurde vom Nachziehstapel gezogen. Wähle eine Karte zum Ablegen aus der Hand oder die aufgenommene Karte.");
            //Display taken card
            gTaken = new GCard(taken);
            gCardDisplayGroup.updateCard(gTaken);
            processingTaken = false;
            processingDeposited = true;
        }

    }

    /**
     * Checks if the GUI is currently waiting for the human player to take a card.
     *
     * @return true if processing taken, false otherwise
     */
    public boolean isProcessingTaken() {
        return processingTaken;
    }
    /**
     * Checks if the GUI is currently waiting for the human player to deposit a card
     *
     * @return true if processing deposited, false otherwise
     */
    public boolean isProcessingDeposited() {
        return processingDeposited;
    }


    /**
     * After a card from the player stack has been clicked.
     *
     * @param c the card to be drawn
     */
    public void drawCardPlayerStack(Card c) {
        //Only if the current player requested a move
        if (processingDeposited) {
            deposited = c;
            setInstructions("Karte (" + c.getName() + ") wurde abgelegt.");
            processingDeposited = false;
            gCardDisplayGroup.removeChild((gCardDisplayGroup.card));
        }

    }

    /**
     * After a card from the discard stack has been clicked.
     *
     * @param c the card to be drawn
     */
    public void drawCardDiscardStack(Card c) {
        //Only if the current player requested a move
        if (processingTaken) {
            taken = c;
            setInstructions("Karte (" + c.getName() + ") wurde vom Ablagestapel gezogen. Wähle Karte zum Ablegen aus der Hand oder die aufgenommene Karte.");
            //Display taken card
            gTaken = new GCard(taken);
            gCardDisplayGroup.addChild(gTaken);
            gCardDisplayGroup.updateCard(gTaken);
            processingTaken = false;
            processingDeposited = true;

        }
    }

    /**
     * Sets the processing status for taken cards.
     *
     * @param b true if processing taken, false otherwise
     */
    public void setProcessingTaken(boolean b) {
        processingTaken = b;
    }

    /**
     * Sets the instructions on the instruction board.
     *
     * @param text the text of the instructions
     */
    public void setInstructions(String text) {
        // Remove existing text
        ArrayList<GElement> gElements = new ArrayList<>();
        for (GElement gElement : instructionBoard) {
            gElements.add(gElement);
        }
        for (GElement gElement : gElements) {
            instructionBoard.removeChild(gElement);
        }

        // Set new text
        GText instructions = new GText(text);
        instructions.setPosition(10, 20);
        instructions.setScale(0.75f);
        instructionBoard.addChild(instructions);

    }

    /**
     * Shows the winner overlay with the winning player's information.
     *
     * @param winnerName the name of the winning player
     * @param score the score of the winning player
     * @throws Exception if an error occurs during the operation
     */
    public void showWinner(String winnerName, int score) throws Exception {
        //this.updateGUI(currentPLayer);
        GSVG trophy = new GSVG(new File("src/main/ressources/win.svg"));
        playSound("src/main/ressources/applause.wav", volume);
        trophy.setScale(0.2f);
        trophy.setPosition(400f,-550f);

        GText winner = new GText("Winner:");
        winner.setPosition(600, -500);
        GText name = new GText(winnerName);
        name.setPosition(600, -450);
        GText winnerScore = new GText("Score: " + score);
        winnerScore.setPosition(600, -350);

        GGroup winnerOverlay = this.addLayer((float) 0.1, (float) 0.8);
        winnerOverlay.addChild(trophy);
        winnerOverlay.addChild(winner);
        winnerOverlay.addChild(name);
        winnerOverlay.addChild(winnerScore);
    }

    /**
     * Checks if a card is in the discard pile.
     *
     * @param c the card to check
     * @return true if the card is in the discard pile, false otherwise
     */
    public boolean isCardinDiscard(Card c) {
        return gameBoard.getDiscardPile().isCardInPile(c);
    }

    /**
     * Setter for the volume
     * author: Nils Wüstefeld
     * @param volume
     */
    public void setVolume(int volume){
        this.volume = volume;
    }

}
