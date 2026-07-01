package com.kirkware.blackjack.controller;

import com.kirkware.blackjack.domain.BlackjackGame;
import com.kirkware.blackjack.dto.GameStateResponse;
import com.kirkware.blackjack.dto.ResponseMapper;
import com.kirkware.blackjack.service.GameService;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * REST controller for Blackjack game API.
 */
@RestController
@RequestMapping("/api/blackjack/games")
public class BlackjackController {

    private final GameService gameService;

    public BlackjackController(GameService gameService) {
        this.gameService = gameService;
    }

    /**
     * POST /api/blackjack/games
     * Creates a new game session and deals the initial cards.
     */
    @PostMapping
    public ResponseEntity<GameStateResponse> createGame() {
        BlackjackGame game = gameService.createGame();
        return ResponseEntity.ok(ResponseMapper.toResponse(game));
    }

    /**
     * GET /api/blackjack/games/{gameId}
     * Returns the current game state.
     */
    @GetMapping("/{gameId}")
    public ResponseEntity<?> getGame(@PathVariable UUID gameId) {
        return gameService.getGame(gameId)
                .map(game -> ResponseEntity.ok(ResponseMapper.toResponse(game)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/blackjack/games/{gameId}/hit
     * Player takes one more card.
     */
    @PostMapping("/{gameId}/hit")
    public ResponseEntity<?> hit(@PathVariable UUID gameId) {
        try {
            BlackjackGame game = gameService.hit(gameId);
            return ResponseEntity.ok(ResponseMapper.toResponse(game));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /api/blackjack/games/{gameId}/stand
     * Player stands and dealer completes their turn.
     */
    @PostMapping("/{gameId}/stand")
    public ResponseEntity<?> stand(@PathVariable UUID gameId) {
        try {
            BlackjackGame game = gameService.stand(gameId);
            return ResponseEntity.ok(ResponseMapper.toResponse(game));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /api/blackjack/games/{gameId}/rounds
     * Starts a new round in the same game session, preserving stats.
     */
    @PostMapping("/{gameId}/rounds")
    public ResponseEntity<?> newRound(@PathVariable UUID gameId) {
        try {
            BlackjackGame game = gameService.newRound(gameId);
            return ResponseEntity.ok(ResponseMapper.toResponse(game));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * DELETE /api/blackjack/games/{gameId}
     * Ends the game session.
     */
    @DeleteMapping("/{gameId}")
    public ResponseEntity<?> deleteGame(@PathVariable UUID gameId) {
        BlackjackGame game = gameService.getGame(gameId).orElse(null);
        if (game == null) {
            return ResponseEntity.notFound().build();
        }
        gameService.deleteGame(gameId);
        return ResponseEntity.ok(Map.of("message", "Game ended. Final stats: W" + game.getWins() + " L" + game.getLosses() + " P" + game.getPushes()));
    }
}
