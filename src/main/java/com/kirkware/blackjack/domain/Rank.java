package com.kirkware.blackjack.domain;

/**
 * Rank enum representing card ranks from 2 through Ace.
 */
public enum Rank {
    TWO(2),
    THREE(3),
    FOUR(4),
    FIVE(5),
    SIX(6),
    SEVEN(7),
    EIGHT(8),
    NINE(9),
    TEN(10),
    JACK(10),
    QUEEN(10),
    KING(10),
    ACE(11);

    private final int value;

    Rank(int value) {
        this.value = value;
    }

    /**
     * Returns the numeric value for game scoring.
     * Face cards (J, Q, K) are worth 10. Ace is 11 (may be downgraded to 1).
     */
    public int getValue() {
        return value;
    }

    /**
     * Returns a display-friendly string for the card.
     * E.g., "10", "J", "Q", "K", "A".
     */
    public String displayRank() {
        return switch (this) {
            case JACK -> "J";
            case QUEEN -> "Q";
            case KING -> "K";
            case ACE -> "A";
            default -> String.valueOf(value);
        };
    }

    public boolean isTenValue() {
        return this == TEN || this == JACK || this == QUEEN || this == KING;
    }

    public boolean isAce() {
        return this == ACE;
    }
}
