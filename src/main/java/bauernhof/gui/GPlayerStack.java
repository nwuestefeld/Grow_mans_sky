package bauernhof.gui;

import bauernhof.preset.card.Card;
import bauernhof.preset.card.GCard;
import sag.elements.GElement;
import sag.elements.GGroup;
import sag.elements.GText;
import sag.elements.shapes.GRect;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import sag.events.*;

/**
 * Represents the graphical representation of the current Player.
 * @author Corinna Liley
 */
public class GPlayerStack extends GGroup {
    private List<Card> cards;
    protected GGroup raw = new GGroup();
    protected GGroup detailsBox;
    private String playerName;
    private FarmPanel parent;
    private int score;
    private Color playerColor;


    /**
     * Creates a new player stack with the given farm cards.
     *
     * @param parent       the parent FarmPanel
     * @param playerName   the name of the player
     * @param cards        the player's hand cards
     * @param playerColor  the color associated with the player
     * @throws Exception   if an error occurs during initialization
     */
    public GPlayerStack(FarmPanel parent, String playerName, List<Card> cards, Color playerColor) throws Exception {
        super();
        this.parent = parent;
        this.cards = cards;
        this.playerName = playerName;
        this.playerColor = playerColor;
        score = 0;
        addChild(raw);

        initStack();

        setScale(0.75f);
        setPosition(120, -100);

    }


    /**
     * Initializes the stack of player cards with farm cards, player name, and index.
     *
     * @throws Exception if an error occurs during initialization
     */
    private void initStack() throws Exception {

        float startX = 20f;
        float startY = 85f;
        float spacingX = 160f; // slight overlap of cards
        float currentX = startX; // position for the next card

        //arranging cards and adding each to raw-group
        for (Card card : cards) {
            GCard gCard = new GCard(card);
            gCard.setMouseEventListener(new CardClickListener());
            gCard.setPosition(currentX, startY);
            raw.addChild(gCard);

            currentX += spacingX;
        }


        //details box with player color
        detailsBox = createDetailsBox();
        detailsBox.setScale(1.5f);
        detailsBox.setPosition(20f,-150f);
        raw.addChild(detailsBox);

        //Instructions
        GText instructions = new GText("Karten können durch Rechtsklick auf die Karte vergrößert werden.");
        instructions.setScale(0.75f);
        instructions.setPosition(200, -100);
        raw.addChild(instructions);


    }

    /**
     * Creates a details box with the player name and score.
     *
     * @return the created details box as a GGroup
     * @throws Exception if an error occurs during creation
     */
    private GGroup createDetailsBox() throws Exception {
        GGroup detailsBox = new GGroup();


        // Create background rectangle with player color
        GRect backgroundRect = new GRect(0, 0, 100f, 50f, false);
        backgroundRect.setFill(playerColor);
        detailsBox.addChild(backgroundRect);

        // Create player name text
        GText playerNameText = new GText(playerName);
        playerNameText.setAlignment(GText.TextAnchor.MIDDLE)
                .setFontSize(12f)
                .setFill(Color.BLACK)
                .move(50f, 20f);
        detailsBox.addChild(playerNameText);

        // Create score text
        GText scoreText = new GText("Score: " + score);
        scoreText.setAlignment(GText.TextAnchor.MIDDLE)
                .setFontSize(12f)
                .setFill(Color.BLACK)
                .move(50f, 40f);
        detailsBox.addChild(scoreText);

        return detailsBox;
    }

    /**
     * Updates the player stack with new cards, player name, score, and player color.
     *
     * @param cards        the updated list of cards
     * @param playerName   the updated player name
     * @param score        the updated player score
     * @param playerColor  the updated player color
     * @throws Exception   if an error occurs during updating
     */
    public void update(List<Card> cards, String playerName, int score, Color playerColor) throws Exception {
        this.cards = cards;
        this.playerName = playerName;
        this.score = score;
        this.playerColor = playerColor;
        removeAllGuiElements();
        initStack();
    }

    /**
     * Removes all GUI elements from the player stack before updating with new current Player.
     */
    private void removeAllGuiElements()
    {
        ArrayList<GElement> gElements = new ArrayList<>();
        for (GElement gElement : raw) {
            gElements.add(gElement);
        }
        for (GElement gElement : gElements) {
            raw.removeChild(gElement);
        }
    }

    /**
     * This class implements the `MouseEventListener` for clicking on a card.
     */
    private class CardClickListener implements MouseEventListener {
        /**
         * Handles the mouse click event on the screenshot button.
         *
         * @param mouseButtonEvent the mouse button event
         * @param gElement         the graphical element that was clicked
         */
        @Override
        public void mouseClicked(MouseButtonEvent mouseButtonEvent, GElement gElement) {
            MouseButton button = mouseButtonEvent.getMouseButton();
            // If the right mouse button is clicked, magnify the clicked element
            if (button == MouseButton.RIGHT){
                magnifyCard(gElement);
            }
            if (button == MouseButton.LEFT){
                // Only GCards can be drawn
                if (gElement instanceof GCard) {
                    parent.drawCardPlayerStack(((GCard) gElement).getCard());
                    //Only if taken and deposited are != null
                    if(!parent.isProcessingDeposited() && !parent.isProcessingTaken()){
                        synchronized (parent){
                            try{
                                parent.notify(); //Move is done
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
            }

        }
        /**
         * Magnifies or shrinks the card when right-clicked.
         *
         * @param self the clicked GElement
         */
        private void magnifyCard(GElement self){
            if (self instanceof GCard) {
                GCard pressedCard = (GCard) self;
                float scaleX = pressedCard.getScaleX();
                float scaleY = pressedCard.getScaleY();
                float originalScale = 1.0f;

                // Check if the card should be magnified or shrunk
                if (scaleX < 2.0f || scaleY < 2.0f) {
                    //Magnify
                    // If another card is magnified, shrink that card first
                    for (GElement element : raw) {
                        if (element instanceof GCard) {
                            GCard card = (GCard) element;
                            if (card.getScaleX() == 2.0f) {
                                card.setScale(originalScale, originalScale);
                            }
                        }
                    }
                    // Store the original position of the card
                    float originalPositionX = pressedCard.getPositionX();
                    float originalPositionY = pressedCard.getPositionY();

                    //Magnify the clicked Card
                    pressedCard.setScale(2.0f, 2.0f);

                    // Bring the card to the front
                    raw.removeChild(pressedCard);
                    raw.addChild(pressedCard);

                    // Set the card to its original position
                    pressedCard.setPosition(originalPositionX, originalPositionY);
                } else {
                    // Shrink
                    pressedCard.setScale(originalScale, originalScale);
                }
            }
        }

        // Other mouse event methods (not used)
        @Override
        public void mousePressed(MouseButtonEvent event, GElement self) {

        }

        @Override
        public void mouseReleased(MouseButtonEvent mouseButtonEvent, GElement gElement) {

        }

        @Override
        public void mouseEntered(MouseMotionEvent mouseMotionEvent, GElement gElement) {

        }

        @Override
        public void mouseExited(MouseMotionEvent mouseMotionEvent, GElement gElement) {

        }

        @Override
        public void mouseMoved(MouseMotionEvent mouseMotionEvent, GElement gElement) {

        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent, GElement gElement) {

        }


    }
}



