

package bauernhof.card;

import bauernhof.preset.Either;
import bauernhof.preset.card.Card;
import bauernhof.preset.card.CardColor;
import bauernhof.preset.card.Effect;
import bauernhof.preset.card.EffectType;

import java.util.HashSet;
import java.util.Set;

/**
 * @author cora
 *  * Die Klasse FarmEffect repräsentiert einen Effekt einer Farm-Card des Spiels Grow Mans Sky.
 *  * Sie implementiert das Effect-Interface und enthält Attribute und Methoden, um den Typ,
 *  * den Effektwert und die Selektion des Effekts zu verwalten.
 */
public class FarmEffect implements Effect {

    private EffectType effectType;

    private int effectValue;

    private Set<Either<Card, CardColor>> selection;

    /**
     * Konstruktor für generierte Werte oder Blacklisting.
     *
     * @param type der Typ des Effekts
     */
    public FarmEffect(EffectType type) {
        effectType = type;
        effectValue = 0;
        selection = new HashSet<Either<Card, CardColor>>();
    }

    /**
     * Konstruktor für den Effekt mit einem Wert.
     *
     * @param type  der Typ des Effekts
     * @param value der Wert des Effekts
     */
    public FarmEffect(EffectType type, int value) {
        effectType = type;
        effectValue = value;
        selection = new HashSet<Either<Card, CardColor>>();
    }

    //Getter und Setter

    public void setEffectType(EffectType effectType) {
        this.effectType = effectType;
    }

    public void setEffectValue(int effectValue) {
        this.effectValue = effectValue;
    }

    public void setSelection(Set<Either<Card, CardColor>> selection) {
        this.selection = selection;
    }

    @Override
    public EffectType getType() {
        return effectType;
    }

    @Override
    public int getEffectValue() {
        return effectValue;
    }

    @Override
    public Set<Either<Card, CardColor>> getSelector() {
        return selection;
    }


    /**
     * Fügt der Selektion der FarmEffect-Karte eine FarmCard hinzu (Selektor).
     *
     * @param sel die hinzuzufügende einzigartige FarmCard
     */
    public void addSelection(FarmCard sel) {
        selection.add(new Either<Card, CardColor>(sel,null));
    }

    /**
     * Fügt der Selektion der FarmEffect-Karte eine CardColor hinzu.
     *
     * @param sel die hinzuzufügende CardColor, also gibt es mehrere Selektoren
     */
    public void addSelection(CardColor sel) {
        selection.add(new Either<Card, CardColor>(null,sel));
    }


    /**
     * Berechnet die Punktzahl des Effekts basierend auf der FarmCard,
     * der Selektion und den vorhandenen Karten.
     *
     * @param thisCard die FarmCard, auf die der Effekt angewendet wird
     * @param set      die Menge der FarmCards, die in Betracht gezogen werden sollen (Handkarten)
     * @param allSet   die Gesamtmenge der FarmCards im Spiel
     * @return die berechnete Punktzahl des Effekts
     */
    public int calculatePoints(FarmCard thisCard, Set<FarmCard> set, Set<FarmCard> allSet) {
        Set<FarmCard> currSelection = createSelection(thisCard, set);
        int points = 0;


        //einzelne Effekte und ihre Punktezählung
        switch (getType()) {
            case POINTS_FOREACH: //Der Effektwert wird mal die Anzahl an Karten vom Selektor die der Spieler auf der Hand hat gerechnet.
                for (FarmCard c : currSelection){
                    points += getEffectValue();
                }
                break;
            case BLOCKS_EVERY: //Alle Karten vom Selektor die der Spieler auf der Hand werden blockiert.
                for (FarmCard c : currSelection){
                c.setBlocked(true);
                }
                break;
            case BLOCKED_IF_WITH: //Die Karte ist blockiert falls der Spieler mindestens eine Karte vom Selektor auf der Hand hat.
                if (!(currSelection.isEmpty())){
                    thisCard.setBlocked(true);
                }
                break;
            case BLOCKED_IF_WITHOUT: //Die Karte ist blockiert falls der Spieler keine der Karten vom Selektor auf der Hand hat.
                if ((currSelection.isEmpty())){
                    thisCard.setBlocked(true);
                }
                break;
            case POINTS_SUM_BASEVALUES: //Die Summe aller Basiswerte der Karten vom Selektor die der Spieler auf der Hand hat.
                for (FarmCard c : currSelection){
                    points += c.getBaseValue();
                }
                break;
            case POINTS_FLAT_CONJUNCTION: //Der Effektwert falls der Spieler alle Karten vom Selektor auf der Hand hat, sonst 0.
                Set<FarmCard> selectionAllCards = createSelection(thisCard, allSet);
                if (selectionAllCards.size() == currSelection.size()){
                    points += getEffectValue();
                }
                break;
            case POINTS_FLAT_DISJUNCTION: //Der Effektwert falls der Spieler mindestens eine Karte vom Selektor auf der Hand hat, sonst 0.
                if (!(currSelection.isEmpty())){
                    points += getEffectValue();
                }
                break;
        }

        return points;
    }

    /**
     * Erstellt eine Auswahl von FarmCards basierend auf der FarmCard, der aktuellen Auswahl und der Gesamtmenge der FarmCards.
     *
     * @param thisCard die FarmCard, auf die der Effekt angewendet wird
     * @param set      die Menge der FarmCards, die in Betracht gezogen werden sollen (Handkarten oder alle Spielkarten)
     * @return die erstellte Auswahl von FarmCards
     */
    private Set<FarmCard> createSelection(FarmCard thisCard, Set<FarmCard> set) {
        Set<FarmCard> selection = new HashSet<>();

        for (FarmCard c : set) {
            // Die aktuelle Karte wird nicht mitgezählt, z. B. benötigt das Pferd ein anderes Pferd
            if (c.equals(thisCard)) {
                continue;
            }

            for (Either<Card, CardColor> ca_cc : getSelector()) {
                // ca_cc kann entweder eine Card oder eine CardColor sein
                if (ca_cc.isLeft()) {
                    // Hat einen Eintrag bei Card
                    if (c.equals(ca_cc.getLeft())) {
                        selection.add(c);
                    }
                } else {
                    // Hat einen Eintrag bei Color
                    if (ca_cc.getRight().equals(c.getColor())) {
                        selection.add(c);
                    }
                }
            }
        }

        return selection;
    }

}
