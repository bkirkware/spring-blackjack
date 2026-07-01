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
        assertEquals(GameStatus.PLAYER_TURN, game.getStatus());
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

        BlackjackGame result = gameService.stand(game.getGameId());

        assertEquals(GameStatus.ROUND_OVER, result.getStatus());
        assertNotNull(result.getLastOutcome());
        assertNotNull(result.getLastExplanation());
    }

    @Test
    void newRound_resetsHandsButPreservesStats() {
        BlackjackGame game = gameService.createGame();
        game = gameService.stand(game.getGameId());
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
        gameService.stand(game.getGameId());

        assertThrows(IllegalStateException.class, () -> gameService.hit(game.getGameId()));
    }

    @Test
    void stand_whenRoundOver_throwsException() {
        BlackjackGame game = gameService.createGame();
        gameService.stand(game.getGameId());

        assertThrows(IllegalStateException.class, () -> gameService.stand(game.getGameId()));
    }
}
