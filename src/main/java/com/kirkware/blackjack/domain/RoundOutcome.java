package com.kirkware.blackjack.domain;

/**
 * Represents the outcome of a single round.
 */
public enum RoundOutcome {
    /** Player wins the round */
    PLAYER_WINS,
    /** Dealer wins the round */
    DEALER_WINS,
    /** Tie - neither wins */
    PUSH
}
