package bauernhof.gameconfig;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import bauernhof.preset.GameConfigurationException;
import bauernhof.preset.card.Card;
import bauernhof.preset.card.CardColor;

/**
 * This class enables the gamecofiguration necessary for playing the game.
 * It implements the interface bauernhof.preset.GameConfiguration.
 * @see bauernhof.preset.GameConfiguration
 * @author Nils Wüstefeld
 */


public class GameConfiguration implements bauernhof.preset.GameConfiguration {

    private String configDescription;
    private String rawConfiguration;
    private int numDepositionAreaSlots;
    private int numCardsPerPlayerHand;
    private Set<CardColor> colorCollection = new HashSet<>();
    private Set<Card> farmCards = new HashSet<>();

    //Configuration Description
    public String getConfigDescription(){
        return configDescription;
    }
    public void setConfigDescription( String configDesc){
        configDescription = configDesc ;
    }
    public void setRawConfiguration (String rawConfiguration){
        this.rawConfiguration = rawConfiguration;
    }
    public String getRawConfiguration(){
        return rawConfiguration;
    }

    public int getNumDepositionAreaSlots(){
        return numDepositionAreaSlots;
    }

    /**
     *
     * @param numCPPH: Integer of the number of cards per player hand
     * @throws GameConfigurationException if the number of player cards per hand is not between 2 and 10
     */
    public void setNumCardsPerPlayerHand (int numCPPH) throws  GameConfigurationException{
        numCardsPerPlayerHand = numCPPH;
        if (numCardsPerPlayerHand < 2 || numCardsPerPlayerHand >= 10) {
            throw new GameConfigurationException("numCardsPerPlayerHand muss zwischen 2 und 10 sein");
        }
    }

    /**
     * Setter for the card colors
     * @param farmCardColors a set of card colors
     * @throws GameConfigurationException if  card collection is empty
     */
    public void setFarmCardColors( Set<CardColor> farmCardColors) throws  GameConfigurationException{
        this.colorCollection = farmCardColors ;
        if (colorCollection == null){
            throw new GameConfigurationException("Leere Card Color Collection");
        }

    }


    public Set<CardColor> getCardColors(){
        return colorCollection;
    }


    public Set<Card> getCards(){
        return farmCards;
    }

    /**
     *
     * @param farmCards a set of all playing cards
     * @throws GameConfigurationException if cardstack is empty
     * or the number of cards is not at least the number of depostion area slots + number of cards per player hand times four
     * Cards {@literal >=} (M+H*4)
     */
    public void setCards( Set <Card> farmCards) throws GameConfigurationException{
        this.farmCards = farmCards;
        if(farmCards.isEmpty()){
            throw new GameConfigurationException("Kartenstapel ist leer");
        }
        if(farmCards.size() < (numDepositionAreaSlots+ numCardsPerPlayerHand*4) ){
            throw new GameConfigurationException("Zu wenige Karten");
        }
        if(!verifyCardColor(farmCards,this.colorCollection)){
            throw new GameConfigurationException("Es werden nicht alle Kartenfarben verwendent");
        }
    }


    /**
     *Setter for the size of the deposition pile
     * @param numDAP: number of cards in depostion area
     * @throws GameConfigurationException wird geworfen, wenn die Anzahl der Slots nicht zwischen 2 und 12 liegt
     */
    public void setNumDepositionAreaSlots(int numDAP) throws GameConfigurationException{
        numDepositionAreaSlots = numDAP;
        if (numDepositionAreaSlots < 2 || numDepositionAreaSlots >= 12) {
            throw new GameConfigurationException("Number of Deposition Slots must be between 2 und 12");
        }
    }


    public int getNumCardsPerPlayerHand(){
        return numCardsPerPlayerHand;
    }

    /**
     * Methode to get a card by its name
     * @param name String card name
     * @return Card
     */
    public Card getCardByName(String name){
        for (Card fc : farmCards) {
            if (Objects.equals(fc.getName(), name)) {
                return fc;
            }
        }
        return null;
    }


    /**
     * auxiliary method to check if all card colors are used in the card set
     * @param cards the playing cards
     * @param cardcolors a collection of all card colors obtained from the flag <colors>
     * @return boolean
     */
    private boolean verifyCardColor (Set<Card> cards, Set<CardColor> cardcolors ){
        Set<CardColor> usedColors = new HashSet<>();
        for(Card card : cards){
            usedColors.add(card.getColor());
        }
        return usedColors.size() == cardcolors.size();
    }




}
