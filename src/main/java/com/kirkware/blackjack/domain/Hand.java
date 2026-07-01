package com.kirkware.blackjack.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a hand of cards with scoring logic for Blackjack.
 * Handles soft/hard values, ace flexibility, bust detection, and blackjack detection.
 */
public class Hand {

    private final List<Card> cards;

    public Hand() {
        this.cards = new ArrayList<>();
    }

    public Hand(List<Card> cards) {
        this.cards = new ArrayList<>(cards);
    }

    /**
     * Adds a card to the hand.
     */
    public void addCard(Card card) {
        cards.add(card);
    }

    /**
     * Returns an unmodifiable list of all cards in the hand.
     */
    public List<Card> getCards() {
        return Collections.unmodifiableList(cards);
    }

    /**
     * Returns the number of cards in the hand.
     */
    public int size() {
        return cards.size();
    }

    /**
     * Returns the best hand value (highest value <= 21, or lowest bust value).
     * Aces count as 11 unless that would bust the hand, then count as 1.
     */
    public int value() {
        int total = 0;
        int aces = 0;

        for (Card card : cards) {
            if (card.getRank().isAce()) {
                aces++;
                total += 11;
            } else {
                total += card.getRank().getValue();
            }
        }

        // Downgrade aces from 11 to 1 until we're not busting (or no more aces to downgrade)
        while (total > 21 && aces > 0) {
            total -= 10; // 11 -> 1 is a reduction of 10
            aces--;
        }

        return total;
    }

    /**
     * Returns true if the hand is "soft" - contains an ace counted as 11.
     * A soft hand can absorb an 11-point hit without immediately busting.
     */
    public boolean isSoft() {
        int total = 0;
        int aces = 0;

        for (Card card : cards) {
            if (card.getRank().isAce()) {
                aces++;
                total += 11;
            } else {
                total += card.getRank().getValue();
            }
        }

        // Downgrade aces from 11 to 1 until we're not busting
        while (total > 21 && aces > 0) {
            total -= 10;
            aces--;
        }

        // After the loop, aces represents how many aces are still counted as 11
        // If any aces remain at 11, the hand is soft
        return aces > 0;
    }

    /**
     * Returns true if the hand value exceeds 21.
     */
    public boolean isBust() {
        return value() > 21;
    }

    /**
     * Returns true if this hand is a natural blackjack (exactly 2 cards totaling 21).
     */
    public boolean isNaturalBlackjack() {
        return cards.size() == 2 && value() == 21;
    }

    /**
     * Convenience: returns true if hand value is 21.
     */
    public boolean isTwentyOne() {
        return value() == 21;
    }
}
