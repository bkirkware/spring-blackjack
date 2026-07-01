package com.kirkware.blackjack.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a standard 52-card deck with shuffle and deal capabilities.
 */
public class Deck {

    private final List<Card> cards;

    public Deck() {
        this.cards = new ArrayList<>();
        initialize();
    }

    /**
     * Creates a new deck with all 52 cards and shuffles it.
     */
    public void resetAndShuffle() {
        initialize();
        shuffle();
    }

    private void initialize() {
        cards.clear();
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                cards.add(new Card(suit, rank));
            }
        }
    }

    /**
     * Shuffles the deck using Fisher-Yates algorithm via Collections.shuffle.
     */
    public void shuffle() {
        Collections.shuffle(cards);
    }

    /**
     * Deals the top card from the deck.
     * @throws IllegalStateException if the deck is empty
     */
    public Card deal() {
        if (cards.isEmpty()) {
            throw new IllegalStateException("Cannot deal from an empty deck. Reshuffle required.");
        }
        return cards.remove(cards.size() - 1);
    }

    /**
     * Returns the number of cards remaining in the deck.
     */
    public int remaining() {
        return cards.size();
    }

    /**
     * Returns true if the deck is empty.
     */
    public boolean isEmpty() {
        return cards.isEmpty();
    }
}
