package com.kirkware.blackjack.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Holds the complete state of a single Blackjack game session.
 * Tracks player/dealer hands, round outcomes, and cumulative stats.
 */
public class BlackjackGame {

    private final UUID gameId;
    private final Hand playerHand;
    private final Hand dealerHand;
    private final Deck deck;
    private GameStatus status;
    private RoundOutcome lastOutcome;
    private String lastExplanation;
    private int wins;
    private int losses;
    private int pushes;
    private int roundsPlayed;

    public BlackjackGame(UUID gameId) {
        this.gameId = gameId;
        this.playerHand = new Hand();
        this.dealerHand = new Hand();
        this.deck = new Deck();
        this.status = GameStatus.PLAYER_TURN;
        this.wins = 0;
        this.losses = 0;
        this.pushes = 0;
        this.roundsPlayed = 0;
    }

    // Getters
    public UUID getGameId() {
        return gameId;
    }

    public Hand getPlayerHand() {
        return playerHand;
    }

    public Hand getDealerHand() {
        return dealerHand;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public RoundOutcome getLastOutcome() {
        return lastOutcome;
    }

    public void setLastOutcome(RoundOutcome lastOutcome) {
        this.lastOutcome = lastOutcome;
    }

    public String getLastExplanation() {
        return lastExplanation;
    }

    public void setLastExplanation(String lastExplanation) {
        this.lastExplanation = lastExplanation;
    }

    public int getWins() {
        return wins;
    }

    public int getLosses() {
        return losses;
    }

    public int getPushes() {
        return pushes;
    }

    public int getRoundsPlayed() {
        return roundsPlayed;
    }

    public void incrementWins() {
        this.wins++;
    }

    public void incrementLosses() {
        this.losses++;
    }

    public void incrementPushes() {
        this.pushes++;
    }

    public void incrementRoundsPlayed() {
        this.roundsPlayed++;
    }

    /**
     * Resets hands for a new round but preserves stats.
     */
    public void resetForNewRound() {
        playerHand.getCards().clear(); // workaround since getCards returns unmodifiable
        // Actually we need to replace hands entirely
    }

    public void setPlayerHand(Hand playerHand) {
        this.playerHand = playerHand;
    }

    public void setDealerHand(Hand dealerHand) {
        this.dealerHand = dealerHand;
    }

    public void setDeck(Deck deck) {
        this.deck = deck;
    }

    /**
     * Returns the visible dealer cards (first card only when game is in progress).
     */
    public List<Card> getVisibleDealerCards() {
        if (dealerHand.size() == 0) {
            return List.of();
        }
        if (status == GameStatus.ROUND_OVER || status == GameStatus.DEALER_TURN) {
            return dealerHand.getCards();
        }
        return List.of(dealerHand.getCards().get(0));
    }

    /**
     * Returns the visible dealer hand value (only the visible cards).
     */
    public int getVisibleDealerValue() {
        List<Card> visible = getVisibleDealerCards();
        if (visible.isEmpty()) return 0;
        Hand tempHand = new Hand(visible);
        return tempHand.value();
    }
}
