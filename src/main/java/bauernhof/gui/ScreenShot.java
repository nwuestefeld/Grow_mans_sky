package bauernhof.gui;
import sag.SAGFrame;
import sag.elements.GElement;
import sag.elements.GGroup;
import sag.elements.shapes.GCircle;
import sag.events.MouseButtonEvent;
import sag.events.MouseEventListener;
import sag.events.MouseMotionEvent;
import sag.events.MouseWheelEvent;
import sag.elements.GText;

import static bauernhof.screenshot.Screenshot.playSound;
import static bauernhof.screenshot.Screenshot.screenshot;

/**
 * The class for the screenshot button
 * @author Corinna Liley
 * @author Nils Wüstefeld
 */
public class ScreenShot extends GGroup {
    FarmPanel parent;
    SAGFrame frame;

    /**
     * Creates a new ScreenShot button with the given parent FarmPanel and radius.
     *
     * @param parent the parent FarmPanel
     * @param r      the radius of the button
     */
    public ScreenShot(FarmPanel parent, float r, SAGFrame frame) {
        super();
        this.parent = parent;
        this.frame = frame;

        // Create the screenshot button as a circle
        GCircle button = new GCircle(r);
        button.setPosition(1450, 100);
        button.setMouseEventListener(new ClickListener());
        addChild(button);

        // Create the text label for the button
        GText text = new GText("Screenshot");
        text.setPosition(1400, 50);
        text.setScale(0.5f);
        addChild(text);
    }
    /**
     * This class implements the `MouseEventListener` for clicking on the screenshot button.
     */
    private class ClickListener implements MouseEventListener {
        @Override
        public void mouseClicked(MouseButtonEvent mouseButtonEvent, GElement gElement) {
            //take screenshot, play sound and save
            screenshot(frame);
            playSound("src/main/ressources/click.wav", parent.volume);
            parent.setInstructions("Screenshot wurde aufgenommen.");
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
