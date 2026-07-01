package com.kirkware.blackjack.service;

import com.kirkware.blackjack.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Core game service handling all Blackjack game logic.
 * Manages in-memory game sessions stored by UUID.
 */
@Service
public class GameService {

    private final Map<UUID, BlackjackGame> gameStore = new ConcurrentHashMap<>();

    /**
     * Creates a new game session, deals initial cards, and checks for natural blackjack.
     */
    public BlackjackGame createGame() {
        UUID gameId = UUID.randomUUID();
        BlackjackGame game = new BlackjackGame(gameId);

        Deck deck = new Deck();
        deck.resetAndShuffle();
        game.setDeck(deck);

        Hand playerHand = new Hand();
        Hand dealerHand = new Hand();

        // Deal: player, dealer, player, dealer
        playerHand.addCard(deck.deal());
        dealerHand.addCard(deck.deal());
        playerHand.addCard(deck.deal());
        dealerHand.addCard(deck.deal());

        game.setPlayerHand(playerHand);
        game.setDealerHand(dealerHand);
        game.incrementRoundsPlayed();

        // Check for natural blackjack
        boolean playerBlackjack = playerHand.isNaturalBlackjack();
        boolean dealerBlackjack = dealerHand.isNaturalBlackjack();

        if (playerBlackjack && dealerBlackjack) {
            game.setStatus(GameStatus.ROUND_OVER);
            game.setLastOutcome(RoundOutcome.PUSH);
            game.setLastExplanation("Both you and the dealer have natural blackjack! It's a push.");
            game.incrementPushes();
        } else if (playerBlackjack) {
            game.setStatus(GameStatus.ROUND_OVER);
            game.setLastOutcome(RoundOutcome.PLAYER_WINS);
            game.setLastExplanation("Natural Blackjack! You win!");
            game.incrementWins();
        } else if (dealerBlackjack) {
            game.setStatus(GameStatus.ROUND_OVER);
            game.setLastOutcome(RoundOutcome.DEALER_WINS);
            game.setLastExplanation("Dealer has natural blackjack. You lose.");
            game.incrementLosses();
        } else {
            game.setStatus(GameStatus.PLAYER_TURN);
        }

        gameStore.put(gameId, game);
        return game;
    }

    /**
     * Gets the game by ID.
     */
    public Optional<BlackjackGame> getGame(UUID gameId) {
        return Optional.ofNullable(gameStore.get(gameId));
    }

    /**
     * Player takes one more card.
     */
    public BlackjackGame hit(UUID gameId) {
        BlackjackGame game = getGameOrThrow(gameId);
        validatePlayerTurn(game);

        Deck deck = game.getDeck();
        if (deck.isEmpty()) {
            deck.resetAndShuffle();
            game.setDeck(deck);
        }

        game.getPlayerHand().addCard(deck.deal());

        if (game.getPlayerHand().isBust()) {
            game.setStatus(GameStatus.ROUND_OVER);
            game.setLastOutcome(RoundOutcome.DEALER_WINS);
            game.setLastExplanation("You busted with " + game.getPlayerHand().value() + ". Dealer wins.");
            game.incrementLosses();
        }

        return game;
    }

    /**
     * Player stands. Dealer plays out their hand, then determine winner.
     */
    public BlackjackGame stand(UUID gameId) {
        BlackjackGame game = getGameOrThrow(gameId);
        validatePlayerTurn(game);

        game.setStatus(GameStatus.DEALER_TURN);
        dealerPlay(game);
        return game;
    }

    /**
     * Starts a new round in the existing game session, preserving stats.
     */
    public BlackjackGame newRound(UUID gameId) {
        BlackjackGame game = getGameOrThrow(gameId);

        if (game.getStatus() == GameStatus.GAME_ENDED) {
            throw new IllegalStateException("Game has been ended. Create a new game to continue.");
        }

        // Reset deck and hands
        Deck deck = new Deck();
        deck.resetAndShuffle();
        game.setDeck(deck);

        Hand playerHand = new Hand();
        Hand dealerHand = new Hand();

        playerHand.addCard(deck.deal());
        dealerHand.addCard(deck.deal());
        playerHand.addCard(deck.deal());
        dealerHand.addCard(deck.deal());

        game.setPlayerHand(playerHand);
        game.setDealerHand(dealerHand);
        game.setLastOutcome(null);
        game.setLastExplanation(null);
        game.incrementRoundsPlayed();

        // Check for natural blackjack
        boolean playerBlackjack = playerHand.isNaturalBlackjack();
        boolean dealerBlackjack = dealerHand.isNaturalBlackjack();

        if (playerBlackjack && dealerBlackjack) {
            game.setStatus(GameStatus.ROUND_OVER);
            game.setLastOutcome(RoundOutcome.PUSH);
            game.setLastExplanation("Both you and the dealer have natural blackjack! It's a push.");
            game.incrementPushes();
        } else if (playerBlackjack) {
            game.setStatus(GameStatus.ROUND_OVER);
            game.setLastOutcome(RoundOutcome.PLAYER_WINS);
            game.setLastExplanation("Natural Blackjack! You win!");
            game.incrementWins();
        } else if (dealerBlackjack) {
            game.setStatus(GameStatus.ROUND_OVER);
            game.setLastOutcome(RoundOutcome.DEALER_WINS);
            game.setLastExplanation("Dealer has natural blackjack. You lose.");
            game.incrementLosses();
        } else {
            game.setStatus(GameStatus.PLAYER_TURN);
        }

        return game;
    }

    /**
     * Ends the game session and removes it from storage.
     */
    public void deleteGame(UUID gameId) {
        BlackjackGame game = gameStore.remove(gameId);
        if (game != null) {
            game.setStatus(GameStatus.GAME_ENDED);
        }
    }

    // ---- Private helpers ----

    private BlackjackGame getGameOrThrow(UUID gameId) {
        BlackjackGame game = gameStore.get(gameId);
        if (game == null) {
            throw new NoSuchElementException("Game not found with ID: " + gameId);
        }
        return game;
    }

    private void validatePlayerTurn(BlackjackGame game) {
        if (game.getStatus() != GameStatus.PLAYER_TURN) {
            throw new IllegalStateException("Cannot perform action. Game status is: " + game.getStatus());
        }
    }

    /**
     * Dealer plays according to rules: hit on 16 or less, stand on 17 or higher (including soft 17).
     */
    private void dealerPlay(BlackjackGame game) {
        Deck deck = game.getDeck();
        Hand dealerHand = game.getDealerHand();

        // Dealer hits on 16 or less (hard or soft), stands on 17+ (hard or soft)
        while (dealerHand.value() < 17) {
            if (deck.isEmpty()) {
                deck.resetAndShuffle();
                game.setDeck(deck);
            }
            dealerHand.addCard(deck.deal());
        }

        game.setStatus(GameStatus.ROUND_OVER);

        int playerValue = game.getPlayerHand().value();
        int dealerValue = dealerHand.value();
        boolean playerBust = game.getPlayerHand().isBust();
        boolean dealerBust = dealerHand.isBust();

        if (playerBust) {
            // Already handled in hit(), but safety check
            game.setLastOutcome(RoundOutcome.DEALER_WINS);
            game.setLastExplanation("You busted with " + playerValue + ". Dealer wins.");
            game.incrementLosses();
        } else if (dealerBust) {
            game.setLastOutcome(RoundOutcome.PLAYER_WINS);
            game.setLastExplanation("Dealer busted with " + dealerValue + ". You win!");
            game.incrementWins();
        } else if (playerValue > dealerValue) {
            game.setLastOutcome(RoundOutcome.PLAYER_WINS);
            game.setLastExplanation("You win! " + playerValue + " beats " + dealerValue + ".");
            game.incrementWins();
        } else if (dealerValue > playerValue) {
            game.setLastOutcome(RoundOutcome.DEALER_WINS);
            game.setLastExplanation("Dealer wins. " + dealerValue + " beats " + playerValue + ".");
            game.incrementLosses();
        } else {
            game.setLastOutcome(RoundOutcome.PUSH);
            game.setLastExplanation("Push! Both hands total " + playerValue + ".");
            game.incrementPushes();
        }
    }
}
