package com.kirkware.blackjack.domain;

/**
 * Represents the current status of a game/round.
 */
public enum GameStatus {
    /** Player can hit or stand */
    PLAYER_TURN,
    /** Dealer is playing out their hand */
    DEALER_TURN,
    /** Round is over, result determined */
    ROUND_OVER,
    /** Game session has been ended */
    GAME_ENDED
}
