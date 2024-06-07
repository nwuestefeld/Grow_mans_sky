package bauernhof.gui;

import bauernhof.preset.card.Card;
import sag.elements.GElement;
import sag.elements.GGroup;
import sag.elements.GText;
import sag.elements.shapes.GCircle;
import sag.events.MouseButtonEvent;
import sag.events.MouseEventListener;
import sag.events.MouseMotionEvent;
import sag.events.MouseWheelEvent;
import java.util.List;

import java.awt.*;

import static bauernhof.screenshot.Screenshot.playSound;
/**
 * A graphical button that allows the user to view a list of cards.
 * @author Corinna Liley
 */
public class GButton extends GGroup{
        private FarmPanel parent;
        private List<Card> cards;
    /**
     * Creates a new instance of GButton.
     *
     * @param parentPanel the parent FarmPanel
     * @param cards       the list of cards to be displayed after pressing the Button
     */
        public GButton(FarmPanel parentPanel, List<Card> cards) {
            super();
            this.parent = parentPanel;
            this.cards = cards;
            initButton();
        }
    /**
     * Initializes the button by creating the graphical elements and setting the MouseEventListeners.
     */
        private void initButton() {
            // Create the button circle
            GCircle button = new GCircle(50f);
            button.setFill(Color.BLACK);
            button.setPosition(90,60);
            button.setMouseEventListener(new CardClickListener());
            addChild(button);

            // Create the label text
            GText label = new GText("Karten ansehen");
            label.setPosition(40f, 0f);
            label.setScale(0.5f);
            addChild(label);
        }
    /**
     * The mouse event listener for the card button click.
     */
        private class CardClickListener implements MouseEventListener {
        /**
         * Handles the mouse click event on the card button.
         *
         * @param mouseButtonEvent the mouse button event
         * @param gElement         the graphical element that was clicked (Button-Circle)
         */
        @Override
            public void mouseClicked(MouseButtonEvent mouseButtonEvent, GElement gElement) {
                playSound("src/main/ressources/click.wav", parent.volume);
                //Display the given cards
                parent.createOverlayInfo(cards);
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




