package com.kirkware.blackjack.domain;

import java.util.Objects;

/**
 * Represents a single playing card with a suit and rank.
 */
public class Card {

    private final Suit suit;
    private final Rank rank;

    public Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
    }

    public Suit getSuit() {
        return suit;
    }

    public Rank getRank() {
        return rank;
    }

    /**
     * Returns a display string like "♠A", "♥10", "♦Q".
     */
    public String display() {
        return suit.getSymbol() + rank.displayRank();
    }

    /**
     * Returns a string representation for a hidden card.
     */
    public static String hiddenDisplay() {
        return "❓";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return suit == card.suit && rank == card.rank;
    }

    @Override
    public int hashCode() {
        return Objects.hash(suit, rank);
    }

    @Override
    public String toString() {
        return display();
    }
}
