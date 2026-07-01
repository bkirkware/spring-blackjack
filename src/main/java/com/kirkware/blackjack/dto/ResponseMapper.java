package com.kirkware.blackjack.dto;

import com.kirkware.blackjack.domain.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Maps domain objects to API response DTOs.
 * Handles hiding dealer's hole card during active play.
 */
public class ResponseMapper {

    private ResponseMapper() {
    }

    /**
     * Converts a BlackjackGame to a GameStateResponse.
     */
    public static GameStateResponse toResponse(BlackjackGame game) {
        GameStateResponse response = new GameStateResponse();
        response.setGameId(game.getGameId());
        response.setStatus(game.getStatus());

        // Player hand
        GameStateResponse.PlayerHandInfo playerHand = mapPlayerHand(game.getPlayerHand());
        response.setPlayerHand(playerHand);

        // Dealer hand (hide hole card during player turn)
        GameStateResponse.DealerHandInfo dealerHand = mapDealerHand(game);
        response.setDealerHand(dealerHand);

        // Stats
        GameStateResponse.Stats stats = new GameStateResponse.Stats();
        stats.setWins(game.getWins());
        stats.setLosses(game.getLosses());
        stats.setPushes(game.getPushes());
        stats.setRoundsPlayed(game.getRoundsPlayed());
        response.setStats(stats);

        // Round result
        GameStateResponse.RoundResult roundResult = new GameStateResponse.RoundResult();
        roundResult.setOutcome(game.getLastOutcome());
        roundResult.setExplanation(game.getLastExplanation());
        response.setRoundResult(roundResult);

        // Available actions
        response.setAvailableActions(getAvailableActions(game));

        return response;
    }

    private static GameStateResponse.PlayerHandInfo mapPlayerHand(Hand hand) {
        GameStateResponse.PlayerHandInfo info = new GameStateResponse.PlayerHandInfo();
        info.setCards(formatCards(hand.getCards()));
        info.setValue(hand.value());
        info.setSoft(hand.isSoft());
        info.setBust(hand.isBust());
        info.setBlackjack(hand.isNaturalBlackjack());
        return info;
    }

    private static GameStateResponse.DealerHandInfo mapDealerHand(BlackjackGame game) {
        GameStateResponse.DealerHandInfo info = new GameStateResponse.DealerHandInfo();
        Hand dealerHand = game.getDealerHand();

        boolean isRevealed = game.getStatus() == GameStatus.ROUND_OVER
                || game.getStatus() == GameStatus.DEALER_TURN
                || game.getStatus() == GameStatus.GAME_ENDED;
        info.setRevealed(isRevealed);

        if (isRevealed) {
            // Show all dealer cards
            List<String> allCards = formatCards(dealerHand.getCards());
            info.setVisibleCards(allCards);
            info.setVisibleValue(dealerHand.value());
            info.setFullCards(allCards);
            info.setFullValue(dealerHand.value());
        } else {
            // Hide the second card (hole card)
            List<Card> cards = dealerHand.getCards();
            if (cards.isEmpty()) {
                info.setVisibleCards(List.of());
                info.setVisibleValue(0);
                info.setFullCards(List.of());
                info.setFullValue(0);
            } else {
                // Visible: only first card
                info.setVisibleCards(List.of(cards.get(0).display()));
                Hand visibleHand = new Hand();
                visibleHand.addCard(cards.get(0));
                info.setVisibleValue(visibleHand.value());

                // Full: first card + placeholders for hidden cards
                List<String> hidden = new ArrayList<>();
                for (int i = 0; i < cards.size(); i++) {
                    if (i == 0) {
                        hidden.add(cards.get(i).display());
                    } else {
                        hidden.add(Card.hiddenDisplay());
                    }
                }
                info.setFullCards(hidden);
                info.setFullValue(0); // Unknown until revealed
            }
        }

        return info;
    }

    private static List<String> formatCards(List<Card> cards) {
        List<String> result = new ArrayList<>();
        for (Card card : cards) {
            result.add(card.display());
        }
        return result;
    }

    private static List<String> getAvailableActions(BlackjackGame game) {
        List<String> actions = new ArrayList<>();

        switch (game.getStatus()) {
            case PLAYER_TURN:
                actions.add("HIT");
                actions.add("STAND");
                break;
            case ROUND_OVER:
                actions.add("NEW_ROUND");
                break;
            case GAME_ENDED:
                // No actions available
                break;
            default:
                break;
        }

        return actions;
    }
}
