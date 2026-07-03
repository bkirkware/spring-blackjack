package com.kirkware.blackjack.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Holds the complete state of a single Blackjack game session.
 * Tracks player/dealer hands, round outcomes, and cumulative stats.
 * Supports splitting — multiple player hands tracked with an active-hand index.
 */
public class BlackjackGame {

    private final UUID gameId;
    private Hand playerHand;
    private Hand dealerHand;
    private Deck deck;
    private GameStatus status;
    private RoundOutcome lastOutcome;
    private String lastExplanation;
    private int wins;
    private int losses;
    private int pushes;
    private int roundsPlayed;

    // Splitting support
    private List<Hand> playerHands;
    private int activeHandIndex;
    private boolean hasSplit;

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
        this.playerHands = new ArrayList<>();
        this.activeHandIndex = 0;
        this.hasSplit = false;
    }

    // Getters
    public UUID getGameId() {
        return gameId;
    }

    /**
     * Returns the currently active player hand (used during splits).
     */
    public Hand getPlayerHand() {
        return getActivePlayerHand();
    }

    /**
     * Returns the hand at the active index from the player hands list.
     */
    private Hand getActivePlayerHand() {
        if (playerHands.isEmpty()) {
            return playerHand; // backward compat
        }
        return playerHands.get(activeHandIndex);
    }

    /**
     * Sets the player hand and also updates the hands list.
     */
    public void setPlayerHand(Hand playerHand) {
        this.playerHand = playerHand;
        if (playerHands.isEmpty()) {
            this.playerHands = new ArrayList<>();
            this.playerHands.add(playerHand);
        } else {
            this.playerHands.set(activeHandIndex, playerHand);
        }
    }

    public Hand getDealerHand() {
        return dealerHand;
    }

    public void setDealerHand(Hand dealerHand) {
        this.dealerHand = dealerHand;
    }

    public Deck getDeck() {
        return deck;
    }

    public void setDeck(Deck deck) {
        this.deck = deck;
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

    // ---- Splitting support ----

    /**
     * Returns all player hands (useful when splits are active).
     */
    public List<Hand> getPlayerHands() {
        if (!playerHands.isEmpty()) {
            return playerHands;
        }
        return List.of(playerHand);
    }

    /**
     * Returns the index of the currently active hand.
     */
    public int getActiveHandIndex() {
        return activeHandIndex;
    }

    /**
     * Moves to the next player hand. Returns true if there is another hand,
     * false if this was the last hand (round should proceed to dealer).
     */
    public boolean advanceToNextHand() {
        if (activeHandIndex < playerHands.size() - 1) {
            activeHandIndex++;
            return true;
        }
        return false;
    }

    /**
     * Returns true if the player can split: has exactly two cards in the active hand
     * with the same rank, has not already split this round, and is on the first hand.
     */
    public boolean canSplit() {
        if (hasSplit) {
            return false;
        }
        Hand activeHand = getActivePlayerHand();
        if (activeHand.size() != 2) {
            return false;
        }
        if (activeHandIndex != 0) {
            return false;
        }
        return activeHand.getCards().get(0).getRank() == activeHand.getCards().get(1).getRank();
    }

    /**
     * Performs a split: separates the two cards into two new hands, deals one additional
     * card to each, and sets up the game to play the first split hand.
     * @param deck the deck to deal from
     */
    public void performSplit(Deck deck) {
        Hand currentHand = getActivePlayerHand();
        Card card1 = currentHand.getCards().get(0);
        Card card2 = currentHand.getCards().get(1);

        Hand hand1 = new Hand();
        hand1.addCard(card1);
        hand1.addCard(deck.deal());

        Hand hand2 = new Hand();
        hand2.addCard(card2);
        hand2.addCard(deck.deal());

        playerHands = new ArrayList<>();
        playerHands.add(hand1);
        playerHands.add(hand2);
        activeHandIndex = 0;
        hasSplit = true;

        // Also keep playerHand reference in sync
        this.playerHand = hand1;
    }

    /**
     * Returns true if the player has already split this round.
     */
    public boolean hasSplit() {
        return hasSplit;
    }

    // ---- End splitting support ----

    /**
     * Resets hands for a new round but preserves stats.
     */
    public void resetForNewRound() {
        playerHand.getCards().clear(); // workaround since getCards returns unmodifiable
        // Actually we need to replace hands entirely
    }

    /**
     * Resets split state for a new round.
     */
    public void resetSplitState() {
        playerHands = new ArrayList<>();
        activeHandIndex = 0;
        hasSplit = false;
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
