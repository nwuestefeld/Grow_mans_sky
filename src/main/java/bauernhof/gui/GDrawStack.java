package bauernhof.gui;


import bauernhof.preset.card.Card;
import bauernhof.preset.card.GCard;
import sag.elements.GElement;
import sag.elements.GGroup;
import sag.elements.GText;
import java.util.List;
import sag.events.MouseButtonEvent;
import sag.events.MouseEventListener;
import sag.events.MouseMotionEvent;
import sag.events.MouseWheelEvent;

/**
 * Represents the graphical representation of the draw stack.
 * @author Corinna Liley
 */
public class GDrawStack extends GGroup {

    private FarmPanel parent;
    private List<Card> cards;
    private GGroup drawButton;
    private GCard topCard = null;
    /**
     * Creates a new instance of GDrawStack.
     *
     * @param parentPanel the parent FarmPanel
     * @param cards       the list of cards in the draw stack
     */
    public GDrawStack(FarmPanel parentPanel, List<Card> cards) {
        super();
        this.parent = parentPanel;
        this.cards = cards;
        initDrawStack();

        setPosition(350f, -450f);
        setScale(0.75f);

    }
    /**
     * Initializes the draw stack by adding the top card, the label, and the button for displaying all cards in the stack.
     */
    private void initDrawStack() {
        //only if there's at least one card in the stack
        if (cards.size() != 0){
            // Add the top card
            Card card = cards.get(0);
            topCard = new GCard(card);
            topCard.setPosition(110,170);
            topCard.setMouseEventListener(new CardClickListener());
            addChild(topCard);
        }

        // Create and add the label
        GText label = new GText("Nachziehstapel");
        label.setPosition(10f, 10f);
        addChild(label);

        // GButton for displaying all cards
        drawButton = new GButton(parent, cards);
        drawButton.setPosition(-170, 0);
        addChild(drawButton);


    }

    /**
     * Updates the draw stack with the new list of cards.
     *
     * @param cards the updated list of cards in the draw stack
     */
    public void update(List<Card> cards) {
        removeChild(drawButton);
        removeChild(topCard);

        this.cards = cards;
        initDrawStack();

    }

    /**
     * The mouse event listener for the draw stack card click.
     */
    private class CardClickListener implements MouseEventListener {
        /**
         * Handles the mouse click event on the draw stack top card.
         *
         * @param mouseButtonEvent the mouse button event
         * @param gElement         the graphical element that was clicked
         */
        @Override
        public void mouseClicked(MouseButtonEvent mouseButtonEvent, GElement gElement) {
            parent.drawCardDrawStack();
        }

        //other mouse event methods (not used)
        @Override
        public void mousePressed(MouseButtonEvent mouseButtonEvent, GElement gElement) {

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

