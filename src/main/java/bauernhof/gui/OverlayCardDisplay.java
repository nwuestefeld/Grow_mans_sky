package bauernhof.gui;
import bauernhof.preset.card.Card;
import bauernhof.preset.card.GCard;
import sag.elements.GElement;
import sag.elements.GGroup;
import sag.elements.GText;
import sag.elements.shapes.GCircle;
import sag.elements.shapes.GRect;
import sag.events.MouseButtonEvent;
import sag.events.MouseEventListener;
import sag.events.MouseMotionEvent;
import sag.events.MouseWheelEvent;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the graphical representation of a card stack (player, discard or draw stack).
 * @author Corinna Liley
 */
public class OverlayCardDisplay extends GGroup {
    private float width;
    private float height;
    private FarmPanel parent;
    private List<Card> cards;
    private GCircle exitButton = new GCircle(5);
    private GGroup displayCards = new GGroup();
    private GGroup raw = new GGroup();

    /**
     * Creates a new overlay card display with the given width and height.
     *
     * @param parent the parent FarmPanel
     * @param width  the width of the display
     * @param height the height of the display
     */
    public OverlayCardDisplay(FarmPanel parent, float width, float height) {
        super();
        this.parent = parent;
        this.width = width;
        this.height = height;
        addChild(raw);

        //Background
        GElement mainBg = new GRect(0, 0, width, height, false, 5, 5)
                .setFill(Color.white);
        raw.addChild(mainBg);
        raw.scale(2.0f);
        setStroke(Color.BLACK);
        setStrokeWidth(1f);

        // Exit Button
        exitButton.setFill(Color.RED);
        exitButton.setPosition(width-10, 10);
        GText exit = new GText("x");
        exit.setPosition(width-15, 15);
        exit.setScale(0.5f);
        raw.addChild(exitButton);
        raw.addChild(exit);
        exitButton.setMouseEventListener(new OverlayClickListener());
    }
    /**
     * Updates the displayed cards with the given list of cards.
     *
     * @param cards the new list of cards to display
     */
    public void updateCards(List<Card> cards){
        removeAllCards();

        this.cards = cards;
        displayCards = arrangeCards();
        raw.addChild(displayCards);
    }

    /**
     * Arranges the cards on the display based on their count.
     *
     * @return the GGroup containing the arranged cards
     */
    private GGroup arrangeCards() {
        int count = cards.size();
        float cardsPerLine = 10.0f;
        // Number of lines with 10 cards each
        int lines = (int)Math.ceil(count / cardsPerLine);

        float cardWidth = (width - 20 )/ 10; // -20 as padding for 10 cards
        float cardHeight = (height - 20) / lines;

        GGroup displayCards = new GGroup();

        float startX = -30;

        //Different startX if there's only one or two cards
        if (count<3) {
            startX = 60;
        }

        for (int i = 0; i < count; i++) {
            Card card = cards.get(i);
            // Restart at the beginning after 10 cards
            float x = startX + i%10 * cardWidth;
            // i/10: Line number, padding of 10
            float y = i/10 * cardHeight + 10;

            GCard gCard = new GCard(card);
            gCard.setPosition(x, y);
            gCard.setScale(cardWidth / GCard.WIDTH * 0.9f);
            gCard.setMouseEventListener(new OverlayClickListener());
            displayCards.addChild(gCard);
        }

        displayCards.setPosition(cardWidth, cardHeight/2);
        return displayCards;
    }


    /**
     * Removes all card elements from the display before updating cards.
     */
    private void removeAllCards()
    {
        ArrayList<GElement> gElements = new ArrayList<>();
        //collect all gElements in displayCards (all cards on current stakc)
            for (GElement gElement : displayCards) {
                gElements.add(gElement);
            }
            //remove every GCard
            for (GElement gElement : gElements) {
                if (gElement instanceof GCard){
                    displayCards.removeChild(gElement);
                }
            }
            //
    }

    /**
     * This class implements the `MouseEventListener` for clicking on the overlay.
     */
    private class OverlayClickListener implements MouseEventListener {
        @Override
        public void mouseClicked(MouseButtonEvent mouseButtonEvent, GElement gElement) {
            if (gElement instanceof GCircle){
                // Exit button clicked
                parent.closeOverlayInfo();
            }
            if (gElement instanceof GCard){
                GCard gCard = (GCard)gElement;
                if (parent.isCardinDiscard(gCard.getCard())){
                    // Card in discard pile clicked
                    parent.drawCardDiscardStack(((GCard) gElement).getCard());
                    parent.closeOverlayInfo();
                }
            }
        }

        // Other mouse event methods (not used)
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
