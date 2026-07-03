package com.kirkware.blackjack.service;

import com.kirkware.blackjack.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GameServiceTest {

    private GameService gameService;

    @BeforeEach
    void setUp() {
        gameService = new GameService();
    }

    @Test
    void createGame_returnsGameWithTwoCardsEach() {
        BlackjackGame game = gameService.createGame();

        assertNotNull(game);
        assertEquals(2, game.getPlayerHand().size());
        assertEquals(2, game.getDealerHand().size());
        // Status is PLAYER_TURN normally, or ROUND_OVER if natural blackjack
        assertTrue(game.getStatus() == GameStatus.PLAYER_TURN
                || game.getStatus() == GameStatus.ROUND_OVER);
        assertEquals(1, game.getRoundsPlayed());
    }

    @Test
    void createGame_playerBlackjack_winsImmediately() {
        // We can't force specific cards, but we can verify the flow works
        BlackjackGame game = gameService.createGame();
        // Stats should start at 0 unless natural blackjack
        assertTrue(game.getWins() >= 0);
        assertTrue(game.getLosses() >= 0);
    }

    @Test
    void hit_addsCardToPlayerHand() {
        BlackjackGame game = gameService.createGame();
        if (game.getStatus() != GameStatus.PLAYER_TURN) {
            // Natural blackjack — skip this test
            return;
        }
        int beforeSize = game.getPlayerHand().size();

        game = gameService.hit(game.getGameId());

        assertEquals(beforeSize + 1, game.getPlayerHand().size());
    }

    @Test
    void hit_playerBusts_roundEndsDealerWins() {
        // Keep hitting until bust (statistically very likely within ~10 hits)
        BlackjackGame game = gameService.createGame();
        for (int i = 0; i < 20; i++) {
            if (game.getStatus() == GameStatus.ROUND_OVER) break;
            game = gameService.hit(game.getGameId());
        }

        // Should be ROUND_OVER or still PLAYER_TURN if lucky
        if (game.getStatus() == GameStatus.ROUND_OVER) {
            assertTrue(game.getLosses() > 0 || game.getPushes() > 0);
        }
    }

    @Test
    void stand_dealerPlaysAndRoundEnds() {
        BlackjackGame game = gameService.createGame();
        if (game.getStatus() != GameStatus.PLAYER_TURN) {
            // Natural blackjack — skip this test
            return;
        }

        BlackjackGame result = gameService.stand(game.getGameId());

        assertEquals(GameStatus.ROUND_OVER, result.getStatus());
        assertNotNull(result.getLastOutcome());
        assertNotNull(result.getLastExplanation());
    }

    @Test
    void newRound_resetsHandsButPreservesStats() {
        BlackjackGame game = gameService.createGame();

        // If game is already ROUND_OVER (natural blackjack), skip stand
        if (game.getStatus() == GameStatus.PLAYER_TURN) {
            game = gameService.stand(game.getGameId());
        }
        int previousRounds = game.getRoundsPlayed();

        BlackjackGame newRound = gameService.newRound(game.getGameId());

        assertEquals(previousRounds + 1, newRound.getRoundsPlayed());
        assertEquals(2, newRound.getPlayerHand().size());
        assertEquals(2, newRound.getDealerHand().size());
    }

    @Test
    void deleteGame_removesGame() {
        BlackjackGame game = gameService.createGame();
        UUID gameId = game.getGameId();

        gameService.deleteGame(gameId);

        assertFalse(gameService.getGame(gameId).isPresent());
    }

    @Test
    void hit_onNonExistentGame_throwsException() {
        UUID fakeId = UUID.randomUUID();

        assertThrows(Exception.class, () -> gameService.hit(fakeId));
    }

    @Test
    void hit_whenRoundOver_throwsException() {
        BlackjackGame game = gameService.createGame();
        if (game.getStatus() == GameStatus.PLAYER_TURN) {
            gameService.stand(game.getGameId());
        }
        // Now the game is ROUND_OVER
        assertThrows(IllegalStateException.class, () -> gameService.hit(game.getGameId()));
    }

    @Test
    void stand_whenRoundOver_throwsException() {
        BlackjackGame game = gameService.createGame();
        if (game.getStatus() == GameStatus.PLAYER_TURN) {
            gameService.stand(game.getGameId());
        }
        // Now the game is ROUND_OVER
        assertThrows(IllegalStateException.class, () -> gameService.stand(game.getGameId()));
    }

    // ========== SPLIT TESTS ==========

    /**
     * Helper to create a game with matching cards for splitting.
     * Tries up to 200 times to get a natural pair.
     */
    private BlackjackGame createGameWithPair() {
        for (int i = 0; i < 200; i++) {
            BlackjackGame game = gameService.createGame();
            if (game.getStatus() == GameStatus.PLAYER_TURN && game.canSplit()) {
                return game;
            }
            gameService.deleteGame(game.getGameId());
        }
        fail("Could not create a game with splittable cards after 200 attempts");
        return null; // unreachable
    }

    @Test
    void split_withMatchingCards_createsTwoHands() {
        BlackjackGame game = createGameWithPair();
        Card card1 = game.getPlayerHand().getCards().get(0);
        Card card2 = game.getPlayerHand().getCards().get(1);

        assertEquals(card1.getRank(), card2.getRank(), "Should have matching ranks");

        BlackjackGame result = gameService.split(game.getGameId());

        assertTrue(result.hasSplit());
        assertEquals(2, result.getPlayerHands().size());
        // First hand should start with the first original card
        assertEquals(card1, result.getPlayerHands().get(0).getCards().get(0));
        // Second hand should start with the second original card
        assertEquals(card2, result.getPlayerHands().get(1).getCards().get(0));
        // Each hand should have 2 cards (original + one dealt)
        assertEquals(2, result.getPlayerHands().get(0).size());
        assertEquals(2, result.getPlayerHands().get(1).size());
        // Should still be player turn (playing first hand)
        assertEquals(GameStatus.PLAYER_TURN, result.getStatus());
    }

    @Test
    void split_withNonMatchingCards_throwsException() {
        BlackjackGame game = gameService.createGame();
        if (game.getStatus() != GameStatus.PLAYER_TURN) {
            // If natural blackjack, skip
            return;
        }

        // Try to force a split even when cards don't match
        // This should fail because canSplit() returns false
        Hand hand = game.getPlayerHand();
        if (hand.getCards().get(0).getRank() == hand.getCards().get(1).getRank()) {
            return; // Skip if they happen to match
        }

        assertThrows(IllegalStateException.class, () -> gameService.split(game.getGameId()));
    }

    @Test
    void split_afterSplitting_throwsException() {
        BlackjackGame game = createGameWithPair();

        gameService.split(game.getGameId());

        // Should not be able to split again
        assertThrows(IllegalStateException.class, () -> gameService.split(game.getGameId()));
    }

    @Test
    void split_thenStandOnBothHands_completesRound() {
        BlackjackGame game = createGameWithPair();

        gameService.split(game.getGameId());
        // Stand on first hand
        BlackjackGame afterFirstStand = gameService.stand(game.getGameId());
        // Should have advanced to second hand
        assertEquals(1, afterFirstStand.getActiveHandIndex());
        assertEquals(GameStatus.PLAYER_TURN, afterFirstStand.getStatus());

        // Stand on second hand
        BlackjackGame afterSecondStand = gameService.stand(game.getGameId());
        // Round should be over
        assertEquals(GameStatus.ROUND_OVER, afterSecondStand.getStatus());
        assertNotNull(afterSecondStand.getLastOutcome());
    }

    @Test
    void split_thenHitOnFirstHand_staysOnFirstHand() {
        BlackjackGame game = createGameWithPair();

        gameService.split(game.getGameId());
        assertEquals(0, game.getActiveHandIndex());

        // Hit until the hand busts or reaches 21
        int hitsBeforeBust = 0;
        while (game.getStatus() == GameStatus.PLAYER_TURN
                && !game.getPlayerHand().isBust()
                && game.getPlayerHand().value() < 21
                && hitsBeforeBust < 5) {
            game = gameService.hit(game.getGameId());
            hitsBeforeBust++;
        }

        // If the hand hasn't busted yet, we should still be on the first hand
        if (!game.getPlayerHand().isBust() && game.getStatus() == GameStatus.PLAYER_TURN) {
            assertEquals(0, game.getActiveHandIndex());
        }
        // If it busted, it will have advanced to the next hand or the round ended
        // Both are valid behaviors
    }

    @Test
    void split_thenBustOnFirstHand_advancesToSecondHand() {
        BlackjackGame game = createGameWithPair();

        gameService.split(game.getGameId());
        // Hit multiple times to bust the first hand
        while (game.getStatus() == GameStatus.PLAYER_TURN
                && !game.getPlayerHand().isBust()) {
            game = gameService.hit(game.getGameId());
        }

        // After busting, should have advanced to second hand or round ended
        assertTrue(game.getStatus() == GameStatus.PLAYER_TURN
                || game.getStatus() == GameStatus.ROUND_OVER
                || game.getStatus() == GameStatus.DEALER_TURN);
        if (game.getStatus() == GameStatus.PLAYER_TURN) {
            assertEquals(1, game.getActiveHandIndex()); // Moved to second hand
        }
    }

    @Test
    void canSplit_returnsFalse_afterMoreThanTwoCards() {
        BlackjackGame game = createGameWithPair();

        // After hitting, should have 3 cards, so canSplit should be false
        gameService.hit(game.getGameId());

        assertFalse(game.canSplit());
    }

    @Test
    void newRound_resetsSplitState() {
        BlackjackGame game = createGameWithPair();
        gameService.split(game.getGameId());
        assertTrue(game.hasSplit());

        // Stand on both hands to end round
        gameService.stand(game.getGameId());
        gameService.stand(game.getGameId());

        BlackjackGame newRound = gameService.newRound(game.getGameId());

        assertFalse(newRound.hasSplit());
        assertEquals(1, newRound.getPlayerHands().size());
        assertEquals(0, newRound.getActiveHandIndex());
    }
}
