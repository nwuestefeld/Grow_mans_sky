package bauernhof.gui;

import bauernhof.preset.card.Card;
import bauernhof.preset.card.GCard;
import sag.elements.GElement;
import sag.elements.GGroup;
import sag.elements.GText;
import sag.events.*;
import java.util.List;

/**
 * Represents the graphical representation of the discard stack.
 * @author Corinna Liley
 */
public class GDiscardStack extends GGroup {
    private FarmPanel parent;
    private List<Card> cards;
    private GCard topCard = null;


    /**
     * Creates a new instance of GDiscardStack.
     *
     * @param parent the parent FarmPanel
     * @param cards  the list of cards in the discard stack
     */
    public GDiscardStack(FarmPanel parent, List<Card> cards) {
        super();
        this.parent = parent;
        this.cards = cards;
        initStack();
        setPosition(700f, -450f);
        setScale(0.75f);
    }

    /**
     * Initializes the discard stack by adding the top card and the label.
     */
    private void initStack() {
        //only if there's at least one card in the stack
        if (cards.size() != 0){
            // Add the top card
            Card card = cards.get(cards.size()-1);
            topCard = new GCard(card);
            topCard.setPosition(110,170);
            topCard.setMouseEventListener(new CardClickListener());
            addChild(topCard);
        }

        // Create and add the label
        GText label = new GText("Ablagestapel");
        label.setPosition(10f, 10f);
        addChild(label);

    }

    /**
     * Updates the discard stack with the new list of cards.
     *
     * @param cards the updated list of cards in the discard stack
     */
    public void update(List<Card> cards){
        if (topCard != null){
            removeChild(topCard);
        }

        this.cards = cards;
        initStack();
    }

    /**
     * The mouse event listener for the discard stack card click.
     */
    private class CardClickListener implements MouseEventListener {
        /**
         * Handles the mouse click event on the discard stack top card.
         *
         * @param mouseButtonEvent the mouse button event
         * @param gElement         the graphical element that was clicked (top card)
         */
        @Override
        public void mouseClicked(MouseButtonEvent mouseButtonEvent, GElement gElement) {
            //Create an overlay with not only the top card, but all cards in the stack
            parent.createOverlayInfo(cards);
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
