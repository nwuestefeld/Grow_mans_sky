package bauernhof.gameboard;

import bauernhof.preset.Either;
import bauernhof.preset.card.Card;
import bauernhof.preset.card.CardColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A calculator for the points given a list of cards
 *
 * @author Maxim Strzebkowski
 */
public class PointsCalculator {

    /**
     * Checks if a given card is part of a selection
     *
     * @param card      the card to be checked
     * @param selection the selection used
     * @return true / false if it is part of the selection or not
     */
    private boolean isCardInSelection(Card card, Set<Either<Card, CardColor>> selection) {
        return selection.stream().anyMatch(selector ->
                isCardInSelector(card, selector));
    }

    /**
     * Checks if a given card is part of a singular selector
     *
     * @param card     the card to be checked
     * @param selector the selector used
     * @return true / false if it is part of the selector or not
     */
    private boolean isCardInSelector(Card card, Either<Card, CardColor> selector) {
        return selector.isLeft() ?
                selector.getLeft().equals(card) :
                selector.getRight().equals(card.getColor());
    }

    /**
     * Copies the given list and removes the blocked cards from it
     *
     * @param cards the cards of which the blocked cards should be removed
     * @return a new list containing none of the blocked cards
     */
    private List<Card> removeBlockedCards(List<Card> cards) {
        List<Card> cardsToCount = new ArrayList<>(cards);

        //Remove blocked cards
        cards.forEach(card -> card.getEffects().forEach(effect -> {
            Set<Either<Card, CardColor>> selection = effect.getSelector();
            boolean hasCard = cards.stream().anyMatch(cardToCheck -> this.isCardInSelection(cardToCheck, selection));
            switch (effect.getType()) {
                //Block every card that is inside the selection
                case BLOCKS_EVERY:
                    cardsToCount.removeIf(cardToCheck -> this.isCardInSelection(cardToCheck, selection));
                    break;
                //Block card itself if contains a card in selection
                case BLOCKED_IF_WITH:
                    if (hasCard) {
                        cardsToCount.remove(card);
                    }
                    break;
                //Only block card if it doesn't contain a card of the selection
                case BLOCKED_IF_WITHOUT:
                    if (!hasCard) {
                        cardsToCount.remove(card);
                    }
                    break;
                default:
                    break;
            }
        }));
        return cardsToCount;
    }

    /**
     * counts the points of the effects for a given list of cards depending on their effects
     *
     * @param cards the cards for which the effect points should be calculated
     * @return the points the effects give for the given cards
     */
    private int countEffectPoints(List<Card> cards) {
        AtomicInteger points = new AtomicInteger();
        cards.forEach(card -> card.getEffects().forEach(effect -> {
            Set<Either<Card, CardColor>> selection = effect.getSelector();
            switch (effect.getType()) {
                //If all selectors are met in the list => effect value
                case POINTS_FLAT_CONJUNCTION:
                    boolean allSelectorsMet = selection.stream().allMatch(selector ->
                            cards.stream().anyMatch(cardToCheck ->
                                    isCardInSelector(cardToCheck, selector)));
                    if (allSelectorsMet) {
                        points.addAndGet(effect.getEffectValue());
                    }
                    break;
                //If any card of the list is contained in the selector => effect value
                case POINTS_FLAT_DISJUNCTION:
                    boolean hasCard = cards.stream().anyMatch(cardToCheck -> this.isCardInSelection(cardToCheck, selection));
                    if (hasCard) {
                        points.addAndGet(effect.getEffectValue());
                    }
                    break;
                //Number of cards in the list contained in the selector => multplied by effect value
                case POINTS_FOREACH:
                    long countSelectedCards = cards.stream().filter(cardToCheck -> this.isCardInSelection(cardToCheck, selection)).count();
                    points.addAndGet((int) countSelectedCards * effect.getEffectValue());
                    break;
                //Sum of the base value of each card of the list contained in the selector
                case POINTS_SUM_BASEVALUES:
                    cards.forEach(cardToCheck -> {
                        if (this.isCardInSelection(cardToCheck, selection)) {
                            points.addAndGet(cardToCheck.getBaseValue());
                        }
                    });
                    break;
                default:
                    break;
            }
        }));
        return points.get();
    }

    /**
     * Counts the base points for all the given cards
     *
     * @param cards the cards for which the base points should be calculated
     * @return the cumulative base points for the cards
     */
    private int countBasePoints(List<Card> cards) {
        AtomicInteger points = new AtomicInteger();
        cards.forEach(card -> points.addAndGet(card.getBaseValue()));
        return points.get();
    }

    /**
     * Calculate the total points for a given list of cards; Takes all effects into account
     *
     * @param cards the list of cards for which the points should be calculated
     * @return the total cumulative cards of the points
     */
    public int calculatePoints(List<Card> cards) {
        int points = 0;
        List<Card> cardsToCount = this.removeBlockedCards(cards);
        points += this.countEffectPoints(cardsToCount);
        points += this.countBasePoints(cardsToCount);
        return points;
    }

    /**
     * Calculate the total points for a given list of cards; Takes all effects into account
     *
     * @param cardPile the {@link CardPile} of cards for which the points should be calculated
     * @return the total cumulative cards of the points
     */
    public int calculatePoints(CardPile cardPile) {
        return calculatePoints(cardPile.getList());
    }
}
