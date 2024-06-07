package bauernhof.gui;

import sag.elements.GElement;
import sag.elements.shapes.GCircle;
import sag.events.MouseButtonEvent;
import sag.events.MouseEventListener;
import sag.events.MouseMotionEvent;
import sag.events.MouseWheelEvent;

import java.awt.*;

/**
 * @author  Nils Wüstefeld
 * The class for the sound button
 *
 */
public class Soundbutton extends GCircle {
    int volume;
    boolean mute;
    final int bufferedvolume;
    FarmPanel parent;
    public Soundbutton( FarmPanel parent, float radius, int x, int y){
        super(radius);
        this.parent = parent;
        volume = parent.volume;
        bufferedvolume = volume;
        if(bufferedvolume == 0){
            setFill(Color.RED);
        }
        else{
        setFill(Color.GREEN);
        }
        this.setPosition(x, y);
        setMouseEventListener(new Soundbutton.SoundClickListener());
    }



    private class SoundClickListener implements MouseEventListener {
        @Override
        public void mouseClicked(MouseButtonEvent mouseButtonEvent, GElement gElement) {
            if(bufferedvolume != 0 && volume !=0) {
                volume = 0;
                parent.setVolume(volume);
               gElement.setFill(Color.RED);
               System.out.println("Der Ton ist aus");
               mute = true;
            } else if (bufferedvolume !=0 && mute) {
                parent.setVolume(bufferedvolume);
                volume = bufferedvolume;
                gElement.setFill(Color.GREEN);
                System.out.println("Der Ton ist aktiv mit Lautstärke "+ bufferedvolume);
                mute = false;
            }

        }

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
