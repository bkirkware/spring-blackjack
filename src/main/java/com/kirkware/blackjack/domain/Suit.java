package com.kirkware.blackjack.domain;

/**
 * Suit enum representing the four suits in a standard deck.
 */
public enum Suit {
    HEARTS("♥"),
    DIAMONDS("♦"),
    CLUBS("♣"),
    SPADES("♠");

    private final String symbol;

    Suit(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    /**
     * Returns true for red suits (Hearts, Diamonds).
     */
    public boolean isRed() {
        return this == HEARTS || this == DIAMONDS;
    }
}
