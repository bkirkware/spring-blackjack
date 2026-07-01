package com.kirkware.blackjack.dto;

import com.kirkware.blackjack.domain.GameStatus;
import com.kirkware.blackjack.domain.RoundOutcome;

import java.util.List;
import java.util.UUID;

/**
 * Response DTO for the full game state.
 */
public class GameStateResponse {

    private UUID gameId;
    private GameStatus status;
    private PlayerHandInfo playerHand;
    private DealerHandInfo dealerHand;
    private Stats stats;
    private RoundResult roundResult;
    private List<String> availableActions;

    public GameStateResponse() {
    }

    public GameStateResponse(UUID gameId, GameStatus status, PlayerHandInfo playerHand,
                             DealerHandInfo dealerHand, Stats stats, RoundResult roundResult,
                             List<String> availableActions) {
        this.gameId = gameId;
        this.status = status;
        this.playerHand = playerHand;
        this.dealerHand = dealerHand;
        this.stats = stats;
        this.roundResult = roundResult;
        this.availableActions = availableActions;
    }

    public UUID getGameId() {
        return gameId;
    }

    public void setGameId(UUID gameId) {
        this.gameId = gameId;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public PlayerHandInfo getPlayerHand() {
        return playerHand;
    }

    public void setPlayerHand(PlayerHandInfo playerHand) {
        this.playerHand = playerHand;
    }

    public DealerHandInfo getDealerHand() {
        return dealerHand;
    }

    public void setDealerHand(DealerHandInfo dealerHand) {
        this.dealerHand = dealerHand;
    }

    public Stats getStats() {
        return stats;
    }

    public void setStats(Stats stats) {
        this.stats = stats;
    }

    public RoundResult getRoundResult() {
        return roundResult;
    }

    public void setRoundResult(RoundResult roundResult) {
        this.roundResult = roundResult;
    }

    public List<String> getAvailableActions() {
        return availableActions;
    }

    public void setAvailableActions(List<String> availableActions) {
        this.availableActions = availableActions;
    }

    public static class PlayerHandInfo {
        private List<String> cards;
        private int value;
        private boolean isSoft;
        private boolean isBust;
        private boolean isBlackjack;

        public PlayerHandInfo() {
        }

        public List<String> getCards() {
            return cards;
        }

        public void setCards(List<String> cards) {
            this.cards = cards;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

        public boolean isSoft() {
            return isSoft;
        }

        public void setSoft(boolean soft) {
            isSoft = soft;
        }

        public boolean isBust() {
            return isBust;
        }

        public void setBust(boolean bust) {
            isBust = bust;
        }

        public boolean isBlackjack() {
            return isBlackjack;
        }

        public void setBlackjack(boolean blackjack) {
            isBlackjack = blackjack;
        }
    }

    public static class DealerHandInfo {
        private List<String> visibleCards;
        private int visibleValue;
        private List<String> fullCards;
        private int fullValue;
        private boolean isRevealed;

        public DealerHandInfo() {
        }

        public List<String> getVisibleCards() {
            return visibleCards;
        }

        public void setVisibleCards(List<String> visibleCards) {
            this.visibleCards = visibleCards;
        }

        public int getVisibleValue() {
            return visibleValue;
        }

        public void setVisibleValue(int visibleValue) {
            this.visibleValue = visibleValue;
        }

        public List<String> getFullCards() {
            return fullCards;
        }

        public void setFullCards(List<String> fullCards) {
            this.fullCards = fullCards;
        }

        public int getFullValue() {
            return fullValue;
        }

        public void setFullValue(int fullValue) {
            this.fullValue = fullValue;
        }

        public boolean isRevealed() {
            return isRevealed;
        }

        public void setRevealed(boolean revealed) {
            isRevealed = revealed;
        }
    }

    public static class Stats {
        private int wins;
        private int losses;
        private int pushes;
        private int roundsPlayed;

        public Stats() {
        }

        public int getWins() {
            return wins;
        }

        public void setWins(int wins) {
            this.wins = wins;
        }

        public int getLosses() {
            return losses;
        }

        public void setLosses(int losses) {
            this.losses = losses;
        }

        public int getPushes() {
            return pushes;
        }

        public void setPushes(int pushes) {
            this.pushes = pushes;
        }

        public int getRoundsPlayed() {
            return roundsPlayed;
        }

        public void setRoundsPlayed(int roundsPlayed) {
            this.roundsPlayed = roundsPlayed;
        }
    }

    public static class RoundResult {
        private RoundOutcome outcome;
        private String explanation;

        public RoundResult() {
        }

        public RoundOutcome getOutcome() {
            return outcome;
        }

        public void setOutcome(RoundOutcome outcome) {
            this.outcome = outcome;
        }

        public String getExplanation() {
            return explanation;
        }

        public void setExplanation(String explanation) {
            this.explanation = explanation;
        }
    }
}
