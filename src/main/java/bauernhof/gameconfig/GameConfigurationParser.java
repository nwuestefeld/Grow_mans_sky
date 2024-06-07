package bauernhof.gameconfig;
import bauernhof.preset.GameConfigurationException;
import bauernhof.preset.card.Card;
import bauernhof.preset.card.CardColor;
import bauernhof.preset.card.Effect;
import bauernhof.preset.card.EffectType;
import bauernhof.card.FarmCard;
import bauernhof.card.FarmEffect;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static java.lang.Integer.valueOf;


/**
 * @author Nils Wüstefeld
 *  This class reads either the gameconfiguration xml file or string representation and creates a gameconfiguration object
 *  The class implements the interface bauernhof.preset.GameConfigurationParser.
 */
public class GameConfigurationParser implements bauernhof.preset.GameConfigurationParser {
    GameConfiguration gameConfiguration = new GameConfiguration();
    int numCardperPlayerHand;
    int numDepositionAreaSlots;
    Set <Effect> currentCardEffects = new HashSet<>();
    Set<Card> cards = new HashSet<>();
    Set<Card> cardtest = new HashSet<>();
    Set<FarmCard> refCards = new HashSet<>();
    Set<CardColor> colorCollection = new HashSet<>();
    FarmEffect currentEffect;
    FarmCard currentCard = null;
    boolean isCardColorsElement = false;
    boolean isCardElement = false;

    /**
     * Parser for the gameconfiguration
     * @param  filecontent : String containing the file content
     * @return GameConfigration: The gameconfiguration for the game
     * @throws GameConfigurationException: GameConfigurationException in case of format errors or violation of requirements
     * @link This method is used in @link networking
     */

    public GameConfiguration parse(String filecontent) throws GameConfigurationException {
        try {
            gameConfiguration.setRawConfiguration(filecontent);
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLEventReader eventReader = factory.createXMLEventReader(new StringReader(filecontent));
            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();
                switch (event.getEventType()) {
                    //xml start tag
                    case XMLStreamConstants.START_ELEMENT:{
                        StartElement startElement = event.asStartElement();
                        String elementName = startElement.getName().getLocalPart();
                        if (elementName.equals("BauernhofGameConfiguration")) {
                            //Version is required to be "1"
                            String version = startElement.getAttributeByName(new QName("version")).getValue();
                            if ("1".equals(version)) {
                                System.out.println("Gültige Version");
                            } else {
                                throw new GameConfigurationException("Falsche Version");
                            }
                        }
                        if (elementName.equals("Description")) {
                            String configDesc = eventReader.getElementText();
                            gameConfiguration.setConfigDescription(configDesc);
                        }
                        if (elementName.equals("NumCardsPerPlayerHand")) {
                            numCardperPlayerHand = valueOf(eventReader.getElementText());
                            gameConfiguration.setNumCardsPerPlayerHand(numCardperPlayerHand);
                        }
                        if (elementName.equals("NumDepositionAreaSlots")) {
                            numDepositionAreaSlots = valueOf(eventReader.getElementText());
                            gameConfiguration.setNumDepositionAreaSlots(numDepositionAreaSlots);
                        }

                        if (elementName.equals("CardColors")) {
                            isCardColorsElement = true;
                        }

                        //CardColor
                        if (isCardColorsElement && elementName.equals("CardColor")) {
                            String cardColor = startElement.getAttributeByName(new QName("color")).getValue();
                            String colorName = eventReader.getElementText();

                            boolean addCardColor = true;
                            for (CardColor c : colorCollection) {
                                if (c.getName().equals(colorName)) {
                                    addCardColor = false;
                                }
                            }
                            if (addCardColor) {
                                CardColor currentColor = new CardColor(colorName, CardColor.decodeColor(cardColor));
                                colorCollection.add(currentColor);
                            }

                        }

                        if (elementName.equals("Cards")) {
                            isCardElement = true;
                        }
                        // creates card reference set for the effects in the second run
                        if (isCardElement && elementName.equals("Card")) {
                            String cardName = startElement.getAttributeByName(new QName("name")).getValue();
                            String cardColorString = startElement.getAttributeByName(new QName("color")).getValue();
                            CardColor cardColor = getColorByName(cardColorString);
                            String cardImage = startElement.getAttributeByName(new QName("image")).getValue();
                            int cardBasevalue = Integer.parseInt(startElement.getAttributeByName(new QName("basevalue")).getValue());
                            FarmCard currentCard = new FarmCard(cardName, cardColor, cardBasevalue, cardImage);
                            refCards.add(currentCard);
                        }
                        break;
                    }
                    //xml end tag
                    case XMLStreamConstants.END_ELEMENT: {
                        EndElement endElement = event.asEndElement();
                        String endelementName = endElement.getName().getLocalPart();
                        if (endelementName.equals("CardColors")) {
                            isCardColorsElement = false;
                            gameConfiguration.setFarmCardColors(colorCollection);
                        }
                        if (endelementName.equals("Cards")) {
                            isCardElement = false;
                        }
                        break;
                    }
                }
            }
            eventReader.close();
            /* Second run to obtain the card-set.
            * This is necessary, since the effects within the cards refers to other cards,
            * that may not have been read yet.
            */
            XMLInputFactory factory1 = XMLInputFactory.newInstance();
            XMLEventReader eventReader1 = factory1.createXMLEventReader(new StringReader(filecontent));
            while(eventReader1.hasNext()){
                XMLEvent event1 = eventReader1.nextEvent();
                switch (event1.getEventType()) {
                    case XMLStreamConstants.START_ELEMENT: {
                        StartElement startElement1 = event1.asStartElement();
                        String elementName1 = startElement1.getName().getLocalPart();
                        if(elementName1.equals("Card")){
                            String cardName = startElement1.getAttributeByName(new QName("name")).getValue();
                            currentCard = getCardByName(cardName);
                            currentCardEffects = new HashSet<>();
                        }
                        if(elementName1.equals("Effect")){
                            Attribute typeAttribute = startElement1.getAttributeByName(new QName("type"));
                            int effectValue = Integer.parseInt(startElement1.getAttributeByName(new QName("effectvalue")).getValue());
                            currentEffect = new FarmEffect(convertToEffectType(typeAttribute.getValue()), effectValue);
                        }
                        if(elementName1.equals("CardRef")){
                            String cardRefString = eventReader1.getElementText();
                            FarmCard cardRef = getCardByName(cardRefString);
                            currentEffect.addSelection(cardRef);
                        }
                        if(elementName1.equals("CardColorRef")){
                            String cardColorRefString = eventReader1.getElementText();
                            CardColor cardColorRef = getColorByName(cardColorRefString);
                            currentEffect.addSelection(cardColorRef);
                        }
                        break;
                    }
                    case XMLStreamConstants.END_ELEMENT: {
                        EndElement endElement1 = event1.asEndElement();
                        String elementName1 = endElement1.getName().getLocalPart();
                        if(elementName1.equals("Effect")){
                            currentCardEffects.add(currentEffect);
                        }
                        if(elementName1.equals("Card")){
                            currentCard.setEffects(currentCardEffects);
                            cardtest.add(currentCard);
                            cards.add(currentCard);
                        }
                        if(elementName1.equals("Cards")){
                            currentCard.setEffects(currentCardEffects);
                            gameConfiguration.setCards(cards);
                        }
                        break;
                    }

                }
            }
            eventReader1.close();
        } catch (XMLStreamException e) {
            System.out.println("Die Datei liegt im falschen Format vor");
            e.printStackTrace();
        } catch (GameConfigurationException e) {
            System.out.println("Falsches Format");
            e.printStackTrace();
        }
        return gameConfiguration;
    }

    /**
     * Parser for the gameconfiguration
     * @param  file : XML configuration file
     * @return GameConfiguration : The configuration of the game
     * @throws GameConfigurationException : In case of format errors or violated requirements
     * @throws IOException: In case of improper data input
     */
    public GameConfiguration parse(File file) throws GameConfigurationException, IOException {
        try {
            String xmlString = Files.readString(file.toPath());
            GameConfiguration gameConfiguration = parse(xmlString);
        } catch (IOException e) {
            System.out.println("Datei liegt im falschen Format vor");
        } catch (GameConfigurationException e) {
            System.out.println("Fehler in dem Format des Dateiinhalts");
        }
        return gameConfiguration;
    }


    /**
     * Auxiliary methode to convert an effect type name to a EffectType object
     * @param type : String of the effect type
     * @return EffectType: The Effecttype of the effect
     * @throws GameConfigurationException: Invalid EffectType
     */

    private static EffectType convertToEffectType(String type) throws GameConfigurationException {
        EffectType effectType;
        switch (type) {
            case("POINTS_FOREACH"): {
                 effectType = EffectType.POINTS_FOREACH;
                 break; }
            case ("POINTS_SUM_BASEVALUES"): {
                effectType = EffectType.POINTS_SUM_BASEVALUES;
                break;
            }
            case ("POINTS_FLAT_DISJUNCTION"): {
                effectType = EffectType.POINTS_FLAT_DISJUNCTION;
                break;
            }
            case ("POINTS_FLAT_CONJUNCTION"): {
                effectType = EffectType.POINTS_FLAT_CONJUNCTION;
                break;
            }
            case ("BLOCKED_IF_WITH"):{
                effectType = EffectType.BLOCKED_IF_WITH;
                break;
            }
            case ("BLOCKED_IF_WITHOUT"):{
                effectType = EffectType.BLOCKED_IF_WITHOUT;
                break;
            }
            case ("BLOCKS_EVERY"):{
                effectType = EffectType.BLOCKS_EVERY;
                break;
            }
            default: {throw new GameConfigurationException("Invalid Effecttype" + type);}
        }
        return effectType;
    }


    /**
     * Auxiliary methode to convert a color name to a color object
     * @param name String of the color name
     * @return Cardcolor
     */

    private CardColor getColorByName (String name) {
        for (CardColor cc : colorCollection) {
            if (Objects.equals(cc.getName(), name)) {
                return cc;
            }
        }
        return null;
    }

    /**
     * Auxiliary methode to convert a card name to a card object
     * @param name String of the card name
     * @return a card or null if the name of the card do not exist in the reference card set.
     */
    private FarmCard getCardByName (String name) {
        for (FarmCard fc : refCards) {
            if (fc.getName().equals(name)) {
                return fc;
            }
        }
        return null;
    }
}
