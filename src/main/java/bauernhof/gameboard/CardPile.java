package bauernhof.gameboard;

import bauernhof.preset.card.Card;

import java.util.Collections;
import java.util.List;

/**
 * Class containing basic structure and functionality for a card pile
 *
 * @author Maxim Strzebkowski
 */
public class CardPile {
    /**
     * The actual list of cards contained in the file
     */
    private final List<Card> list;

    /**
     * Constructor initializing the card pile
     *
     * @param list the initial card pile
     */
    public CardPile(List<Card> list) {
        this.list = list;
    }

    /**
     * Get the card pile list
     *
     * @return the list of cards
     */
    public List<Card> getList() {
        return this.list;
    }

    /**
     * Removes the first card from the pile and returns it
     *
     * @return first {@link Card} of the pile
     */
    public Card drawCard() {
        if (!this.list.isEmpty()) {
            return this.list.remove(0);
        }
        return null;
    }


    /**
     * Remove a card from a specific index from the pile
     *
     * @param index index of the card to be removed
     * @return the card at the index position
     */
    public Card drawCard(int index) {
        if (index < this.list.size() && index >= 0) {
            return this.list.remove(index);
        }
        throw new IndexOutOfBoundsException("Invalid index");
    }

    /**
     * Remove a specific card from the pile
     *
     * @param cardToRemove card to be taken from the pile
     * @return true if removed and false if not removed
     */
    public boolean drawCard(Card cardToRemove) {
        return this.list.remove(cardToRemove);
    }

    /**
     * Get a card from a specific index without removing it from the file
     *
     * @param index the index at which to look at the card
     * @return the card at the given index
     */
    public Card lookAt(int index) {
        return this.list.get(index);
    }

    /**
     * Gets the index of a given card;
     *
     * @param card the card for which the index is needed
     * @return the index of the card; If the card does not exist in the pile <b>returns -1</b>
     */
    public int getIndex(Card card) {
        return this.list.indexOf(card);
    }

    /**
     * Adds a card to the pile
     *
     * @param card the card to be added to the pile
     */
    public void addCard(Card card) {
        this.list.add(card);
    }

    /**
     * Shuffles all the cards in the pile using the {@link Collections} shuffle algorithm
     */
    public void shuffle() {
        Collections.shuffle(this.list);
    }

    /**
     * Gets the size of the pile
     *
     * @return the size of the pile
     */
    public int getSize() {
        return this.list.size();
    }

    /**
     * Checks if the given card is in the pile
     *
     * @param card the card to be looked for
     * @return true / false if card is in pile or not
     */
    public boolean isCardInPile(Card card) {
        return this.list.contains(card);
    }
}
