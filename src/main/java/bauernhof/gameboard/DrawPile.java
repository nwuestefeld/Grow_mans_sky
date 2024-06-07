package bauernhof.gameboard;

import bauernhof.preset.card.Card;

import java.util.List;

/**
 * The draw pile with limited functionality concerning adding cards and drawing cards from a specific index
 * Implements {@link CardPile}
 *
 * @author Maxim Strzebkowski
 */
public class DrawPile extends CardPile {

    public DrawPile(List<Card> pile) {
        super(pile);
    }

    /**
     * Drawing from specific index is not possible in a draw pile
     *
     * @param index index of the card that should have been removed
     * @return Throws an {@link UnsupportedOperationException}
     */
    @Override
    public Card drawCard(int index) {
        throw new UnsupportedOperationException("Cannot draw specific Card from DrawPile");
    }

    /**
     * Adding cards to the draw pile is <b>not possible</b>.
     *
     * @param card the card that should have been added
     * @throws UnsupportedOperationException
     */
    @Override
    public void addCard(Card card) {
        throw new UnsupportedOperationException("Cannot add Card to DrawPile");
    }

    /**
     * Getting the index of a card in the draw pile is <b>not possible</b>
     *
     * @param card the card for which the index is needed
     * @throws UnsupportedOperationException
     * @return throws an {@link UnsupportedOperationException}
     */
    @Override
    public int getIndex(Card card) {
        throw new UnsupportedOperationException("Cannot get specific index of card in DrawPile");
    }

    /**
     * Drawing a specific card from the pile is <b>not possible</b>
     *
     * @param cardToRemove card to be taken from the pile
     * @throws UnsupportedOperationException
     * @return throws an {@link UnsupportedOperationException}
     */
    @Override
    public boolean drawCard(Card cardToRemove) {
        throw new UnsupportedOperationException("Cannot draw a specific card of the pile");
    }
}
