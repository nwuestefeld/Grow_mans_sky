package bauernhof.gui;

import bauernhof.preset.card.Card;
import sag.elements.GElement;
import sag.elements.GGroup;
import sag.elements.GText;
import sag.elements.shapes.GRect;
import java.util.ArrayList;
import java.util.List;
import java.awt.*;

/**
 * Represents the graphical representation of a player (one for every player in the game).
 * @author Corinna Liley
 */
public class GPlayer extends GGroup {
    private GGroup raw = new GGroup();
    private FarmPanel parent;

    private List<Card> cards;
    private int score;
    private String playerName;
    private Color color;

    /**
     * Creates a new instance of GPlayer.
     *
     * @param parent the parent FarmPanel
     * @param playerName  the name of the player
     * @param cards       the list of cards the player holds
     * @param color       the color associated with the player
     */
    public GPlayer(FarmPanel parent, String playerName, List<Card> cards, Color color) {
        super();
        this.parent = parent;
        this.playerName = playerName;
        this.cards = cards;
        this.color = color;
        score = 0;
        addChild(raw);
        initStack();


    }
    /**
     * Initializes the player's graphical elements, such as the background rectangle filled with their color,
     * player name text, score text, and the button for viewing cards.
     */
    private void initStack() {
        GGroup detailsBox = new GGroup();

        // Create background rectangle with player color
        GRect backgroundRect = new GRect(0, 0, 100f, 50f, false);
        backgroundRect.setFill(color);

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

        detailsBox.setPosition(100,100);
        raw.addChild(detailsBox);

        // Create button for viewing cards
        GButton button = new GButton(parent, cards);
        button.setPosition(200,100);
        button.setScale(0.5f);

        raw.addChild(button);
    }

    /**
     * Updates the player's graphical elements with new information after every Move (method used in updateGUI()).
     *
     * @param playerName the updated player name
     * @param cards      the updated list of cards the player holds
     * @param score      the updated player score
     * @param color      the updated color associated with the player
     * @throws Exception if an error occurs during the update process
     */
    public void update(String playerName, List<Card> cards, int score, Color color) throws Exception {
        removeAllGuiElements();
        this.cards = cards;
        this.score = score;
        this.playerName = playerName;
        this.color = color;
        initStack();
    }

    /**
     * Removes all graphical elements from the player's group before updating with new information.
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
}
