package bauernhof.screenshot;
import sag.SAGFrame;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * @author Nils Wüstefeld
 * This class implements the optional features SCREENSHOT and AUDIO
 */
public class Screenshot {
    /**
     * This method allows to take screenshots of the game
     * @param sagFrame the current frame of the gui
     */

    public static void screenshot(SAGFrame sagFrame){
        Rectangle rec = sagFrame.getBounds();
        BufferedImage bufferedImage = new BufferedImage(rec.width,rec.height,BufferedImage.TYPE_INT_RGB);
        sagFrame.paint(bufferedImage.getGraphics());

        //name formatting based on time and date
        LocalDate date = LocalDate.now();
        LocalTime time = LocalTime.now();
        DateTimeFormatter formatter_date = DateTimeFormatter.ofPattern("-yyyy-MM-dd");
        DateTimeFormatter formatter_time = DateTimeFormatter.ofPattern("_kk-mm-ss");
        String screenshotname = "screenshot";
        screenshotname = screenshotname + date.format(formatter_date);
        screenshotname = screenshotname + time.format(formatter_time);
        screenshotname = screenshotname + ".png";
       try {
            // creation of a file
            File f = new File(screenshotname);
            //creation of the image
            ImageIO.write(bufferedImage, "png", f);
            System.out.println("Screenshot saved at" + f.getAbsolutePath());

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

    }


    /**
     * A method to play sound
     * @param soundName name of the sound file
     * @param volume volume of the sound
     *
     */

    public static void playSound(String soundName, int volume)
   {

       try{
           AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(soundName).getAbsoluteFile());
           //clip allows pre-buffered audio
           Clip clip = AudioSystem.getClip();
           //open to assign the ressources
           clip.open(audioInputStream);


           //volume control
           FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
           //converting log db scale to linear scale
           float range = gainControl.getMaximum() - gainControl.getMinimum();
           float fvolume = (float) volume;
           float gain = (range * (fvolume/100)) + gainControl.getMinimum();
           gainControl.setValue(gain);
           clip.start();
       }
       catch(Exception ex){
           System.out.println("Error while playing sound.");
           ex.printStackTrace( );
       }
   }

}
