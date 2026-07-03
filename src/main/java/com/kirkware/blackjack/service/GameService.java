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
     * Player takes one more card on the currently active hand.
     */
    public BlackjackGame hit(UUID gameId) {
        BlackjackGame game = getGameOrThrow(gameId);
        validatePlayerTurn(game);

        Deck deck = game.getDeck();
        if (deck.isEmpty()) {
            deck.resetAndShuffle();
            game.setDeck(deck);
        }

        Hand activeHand = game.getPlayerHand();
        activeHand.addCard(deck.deal());

        if (activeHand.isBust()) {
            // Keep advancing until we find a non-busted hand or run out of hands
            while (game.advanceToNextHand()) {
                Hand nextHand = game.getPlayerHand();
                if (!nextHand.isBust()) {
                    break; // Found a playable hand, continue game
                }
                // Next hand is also busted, keep advancing
            }
            // Check if all remaining hands are busted
            boolean allBusted = game.getPlayerHands().stream().allMatch(Hand::isBust);
            if (allBusted) {
                game.setStatus(GameStatus.ROUND_OVER);
                game.setLastOutcome(RoundOutcome.DEALER_WINS);
                game.setLastExplanation("You busted on all hands. Dealer wins.");
                game.incrementLosses();
            } else {
                // Some hands are still alive, proceed to dealer
                game.setStatus(GameStatus.DEALER_TURN);
                dealerPlaySplit(game);
            }
        }

        return game;
    }

    /**
     * Player stands on the currently active hand. Moves to next split hand or
     * proceeds to dealer play if this was the last hand.
     */
    public BlackjackGame stand(UUID gameId) {
        BlackjackGame game = getGameOrThrow(gameId);
        validatePlayerTurn(game);

        // If there is a next split hand, advance to it
        if (game.hasSplit() && game.advanceToNextHand()) {
            // Keep advancing past busted hands
            while (game.getPlayerHand().isBust() && game.advanceToNextHand()) {
                // Continue to next hand
            }
            // Check if all remaining hands are busted
            boolean allBusted = game.getPlayerHands().stream().allMatch(Hand::isBust);
            if (allBusted) {
                game.setStatus(GameStatus.ROUND_OVER);
                game.setLastOutcome(RoundOutcome.DEALER_WINS);
                game.setLastExplanation("You busted on all hands. Dealer wins.");
                game.incrementLosses();
            } else {
                // There's a playable hand — continue player turn on it
                return game;
            }
        }

        // No more hands — dealer plays
        game.setStatus(GameStatus.DEALER_TURN);
        if (game.hasSplit()) {
            dealerPlaySplit(game);
        } else {
            dealerPlay(game);
        }
        return game;
    }

    /**
     * Player splits their hand (when eligible). Creates two separate hands and
     * deals one additional card to each.
     */
    public BlackjackGame split(UUID gameId) {
        BlackjackGame game = getGameOrThrow(gameId);
        validatePlayerTurn(game);

        if (!game.canSplit()) {
            throw new IllegalStateException("Cannot split. You need exactly two cards of the same rank and have not already split.");
        }

        Deck deck = game.getDeck();
        if (deck.isEmpty()) {
            deck.resetAndShuffle();
            game.setDeck(deck);
        }

        game.performSplit(deck);

        // After split, the active hand has 2 cards. If it's blackjack, check it.
        // But standard rules: split aces only get one card. For simplicity, we allow
        // normal play on each hand.

        // Check if the first split hand busted (unlikely with 2 cards unless dealt weirdly)
        Hand activeHand = game.getPlayerHand();
        if (activeHand.isBust()) {
            // Unlikely scenario but handle it
            if (!game.advanceToNextHand()) {
                boolean allBusted = game.getPlayerHands().stream().allMatch(Hand::isBust);
                if (allBusted) {
                    game.setStatus(GameStatus.ROUND_OVER);
                    game.setLastOutcome(RoundOutcome.DEALER_WINS);
                    game.setLastExplanation("You busted on all hands. Dealer wins.");
                    game.incrementLosses();
                } else {
                    game.setStatus(GameStatus.DEALER_TURN);
                    dealerPlaySplit(game);
                }
            }
        }

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

        // Reset deck, hands, and split state
        Deck deck = new Deck();
        deck.resetAndShuffle();
        game.setDeck(deck);
        game.resetSplitState();

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

    /**
     * Dealer plays and then evaluates against all split player hands.
     * Each hand is compared individually. If at least one hand wins,
     * the round result is PLAYER_WINS. If all lose, DEALER_WINS. Otherwise PUSH.
     */
    private void dealerPlaySplit(BlackjackGame game) {
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

        List<Hand> playerHands = game.getPlayerHands();
        int dealerValue = dealerHand.value();
        boolean dealerBust = dealerHand.isBust();

        int handWins = 0;
        int handLosses = 0;
        int handPushes = 0;

        for (Hand hand : playerHands) {
            int playerValue = hand.value();
            boolean playerBust = hand.isBust();

            if (playerBust) {
                handLosses++;
            } else if (dealerBust) {
                handWins++;
            } else if (playerValue > dealerValue) {
                handWins++;
            } else if (dealerValue > playerValue) {
                handLosses++;
            } else {
                handPushes++;
            }
        }

        // Determine overall result
        if (handWins > handLosses) {
            game.setLastOutcome(RoundOutcome.PLAYER_WINS);
            game.setLastExplanation(String.format("Split round: %d hand(s) won, %d lost, %d pushed (Dealer: %d).",
                    handWins, handLosses, handPushes, dealerValue));
            game.incrementWins();
        } else if (handLosses > handWins) {
            game.setLastOutcome(RoundOutcome.DEALER_WINS);
            game.setLastExplanation(String.format("Split round: %d hand(s) won, %d lost, %d pushed (Dealer: %d).",
                    handWins, handLosses, handPushes, dealerValue));
            game.incrementLosses();
        } else if (handWins > 0) {
            game.setLastOutcome(RoundOutcome.PUSH);
            game.setLastExplanation(String.format("Split round push: %d hand(s) won, %d lost, %d pushed (Dealer: %d).",
                    handWins, handLosses, handPushes, dealerValue));
            game.incrementPushes();
        } else {
            // All pushes
            game.setLastOutcome(RoundOutcome.PUSH);
            game.setLastExplanation(String.format("Split round: All %d hand(s) pushed (Dealer: %d).",
                    handPushes, dealerValue));
            game.incrementPushes();
        }
    }
}
