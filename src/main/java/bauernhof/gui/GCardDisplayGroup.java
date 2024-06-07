package bauernhof.gui;

import bauernhof.preset.card.GCard;
import sag.elements.GElement;
import sag.elements.GGroup;
import sag.elements.GText;
import sag.elements.shapes.GRect;
import java.awt.*;
import sag.events.*;

/**
 * The class for the draw box in the gui
 * @author  Nils Wüstefeld
 *
 */
public class GCardDisplayGroup extends GGroup {
    private GRect cardDisplayArea;
    private final  FarmPanel parent;
    protected GCard card;
    private final  GText obenText;

    public GCardDisplayGroup(FarmPanel parent,float x, float y, float width, float height) {
        super();
        this.parent = parent;

        //CardDisplayArea
        cardDisplayArea = new GRect(x, y, width, height, false);
        cardDisplayArea.setFill(Color.WHITE);
        cardDisplayArea.setStroke(Color.BLACK);
        cardDisplayArea.setStrokeWidth(2.0f);
        cardDisplayArea.setOpacity(0.5f);
        addChild(cardDisplayArea);

        //Text
        obenText = new GText("Aufgenommene Karte");
        obenText.setAlignment(GText.TextAnchor.MIDDLE);
        obenText.setFontSize(20f);
        obenText.setFill(Color.BLACK);
        obenText.move(x+0.5f*width,  y+20);
        obenText.setBold(true);
        addChild(obenText);
    }

    /**
     * Method to update the displayed card
     * @param card a GUI card
     */
    public void updateCard(GCard card) {
        if (card != null) {
            //position within the box
            float cardX = cardDisplayArea.getPositionX() + cardDisplayArea.getWidth()/2 ;
            float cardY = cardDisplayArea.getPositionY() + (cardDisplayArea.getHeight() + GCard.HEIGHT/4) /2;
            card.setPosition(cardX, cardY);
            this.card = card;

            //Listener
            card.setMouseEventListener(new DisplayCardClickListener());
            addChild(card);
        }
    }
    private class DisplayCardClickListener implements MouseEventListener {
        @Override
        public void mouseClicked(MouseButtonEvent mouseButtonEvent, GElement gElement) {
            MouseButton button = mouseButtonEvent.getMouseButton();
            if (button == MouseButton.LEFT){
                if (gElement instanceof GCard) {
                    GCard gCard = (GCard) gElement;
                   //places card on the discard stack if clicked
                    parent.drawCardPlayerStack(gCard.getCard());
                    //checks if move is complete
                    if(!parent.isProcessingDeposited() && !parent.isProcessingTaken()){
                        synchronized (parent){
                            try{
                                parent.notify();
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }


                    }
                }
            }


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




