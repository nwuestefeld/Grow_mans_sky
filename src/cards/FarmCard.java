package cards;

import bauernhof.preset.card.Card;
import bauernhof.preset.card.CardColor;
import bauernhof.preset.card.Effect;
import java.awt.*;
import sag.SAGInfo;
import java.util.HashSet;
import java.util.Set;

/**
 * @author cora
 *  * Die Klasse FarmCard repräsentiert eine Karte des Spiels Grow Mans Sky.
 *  * Sie implementiert das Card-Interface und enthält Attribute und Methoden,
 *  * um den Namen, die Farbe, den Basiswert, das Bild, den Blockierungsstatus und
 *  * die Effekte der Karte zu verwalten.
 */
public class FarmCard implements Card {

    private CardColor color;
    private String name;
    private int baseValue;

    private String image;

    private boolean blocked;

    private Set<Effect> effects;

    /**
     * Der Konstruktor initialisiert eine FarmCard mit den angegebenen Werten für
     * Namen, Farbe, Basiswert und Bild. Die Effekte werden als leeres Set initialisiert.
     *
     * @param n   der Name der Karte
     * @param c   die Farbe der Karte
     * @param bv  der Basiswert der Karte
     * @param img das Bild der Karte
     */

    public FarmCard(String n, CardColor c, int bv, String img) {
        name = n;
        color = c;
        baseValue = bv;
        image = img;
        effects = new HashSet<Effect>();
    }


    // Getter und Setter-Methoden
    public void setColor(CardColor color) {
        this.color = color;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBaseValue(int baseValue) {
        this.baseValue = baseValue;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setEffects(Set<Effect> effects) {
        this.effects = effects;
    }

    public void setFarmEffects(Set<FarmEffect> farmEffects) {
        Set<Effect> convertedEffects = new HashSet<>();
        for (FarmEffect farmEffect : farmEffects) {
            convertedEffects.add(farmEffect);
        }
        this.effects = convertedEffects;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getBaseValue() {
        return baseValue;
    }

    @Override
    public CardColor getColor() {
        return color;
    }

    @Override
    public String getImage() {
        return image;
    }

    @Override
    public Set<Effect> getEffects() {
        return effects;
    }

    //fügt Effekt aus Klasse FarmEffect zu dem Effekt-Set hinzu
    public void addEffect(FarmEffect effect) {
        effects.add(effect);
    }

    public int calculatePoints(Set<FarmCard> set, Set<FarmCard> all) {
        int sum = 0;
        for (Effect e : effects) {
            FarmEffect fe = (FarmEffect) e;

            //Bei fe.calculatePoints werden die Punkte durch den Effekt der Karte ausgerechnet
            int points = fe.calculatePoints(this, set, all);
            sum+=points;
        }
        //Zum (gegebenenfalls) Effekt-Wert kommt noch der Basis-Wert
        sum+=getBaseValue();
        if (isBlocked()) {
            return 0;
        }
        return sum;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public void evaluateBlockingStatus(Set<FarmCard> set, Set<FarmCard> allSet) {
        for (Effect e : effects) {
            FarmEffect fe = (FarmEffect) e;
            fe.calculatePoints(this, set, allSet); //Karten werden geblockt, punkte werden zwar gezählt, aber nicht gespeichert
        }
    }
}
