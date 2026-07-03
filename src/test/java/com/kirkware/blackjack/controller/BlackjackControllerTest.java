package com.kirkware.blackjack.controller;

import com.kirkware.blackjack.dto.GameStateResponse;
import com.kirkware.blackjack.domain.GameStatus;
import tools.jackson.databind.ObjectMapper;
import com.kirkware.blackjack.service.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class BlackjackControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GameService gameService;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    // Helper: create a game and return the game ID.
    // Retries if natural blackjack results in ROUND_OVER, since most tests expect PLAYER_TURN.
    private UUID createGame() throws Exception {
        int attempts = 0;
        while (attempts < 20) {
            MvcResult result = mockMvc.perform(post("/api/blackjack/games"))
                    .andExpect(status().isOk())
                    .andReturn();

            GameStateResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(), GameStateResponse.class);

            if (response.getStatus() == GameStatus.PLAYER_TURN) {
                return response.getGameId();
            }
            attempts++;
        }
        // Fallback: should be extremely rare
        return objectMapper.readValue(
                mockMvc.perform(post("/api/blackjack/games")).andReturn().getResponse().getContentAsString(),
                GameStateResponse.class).getGameId();
    }

    @Test
    void createGame_returnsGameState() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/blackjack/games"))
                .andExpect(status().isOk())
                .andReturn();

        GameStateResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), GameStateResponse.class);

        assertThat(response.getGameId()).isNotNull();
        assertThat(response.getStatus()).isIn(GameStatus.PLAYER_TURN, GameStatus.ROUND_OVER);
        assertThat(response.getPlayerHand().getCards()).hasSize(2);
        assertThat(response.getDealerHand().getVisibleCards()).isNotEmpty();
        assertThat(response.getStats().getWins()).isGreaterThanOrEqualTo(0);
        assertThat(response.getStats().getLosses()).isGreaterThanOrEqualTo(0);
        assertThat(response.getStats().getPushes()).isGreaterThanOrEqualTo(0);
        assertThat(response.getAvailableActions()).isNotEmpty();

        // If PLAYER_TURN, check additional details
        if (response.getStatus() == GameStatus.PLAYER_TURN) {
            assertThat(response.getAvailableActions()).contains("HIT", "STAND");
            assertThat(response.getDealerHand().isRevealed()).isFalse();
            assertThat(response.getDealerHand().getVisibleCards()).hasSize(1);
            // At minimum HIT and STAND; SPLIT is included when eligible
            assertThat(response.getAvailableActions()).hasSizeGreaterThanOrEqualTo(2);
        } else {
            // ROUND_OVER (natural blackjack) — only NEW_ROUND available
            assertThat(response.getAvailableActions()).contains("NEW_ROUND");
        }
    }

    @Test
    void getGame_withValidId_returnsGameState() throws Exception {
        UUID gameId = createGame();

        mockMvc.perform(get("/api/blackjack/games/{gameId}", gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(gameId.toString()));
    }

    @Test
    void getGame_withInvalidId_returnsNotFound() throws Exception {
        UUID fakeId = UUID.randomUUID();

        mockMvc.perform(get("/api/blackjack/games/{gameId}", fakeId))
                .andExpect(status().isNotFound());
    }

    @Test
    void hit_addsCardToPlayerHand() throws Exception {
        UUID gameId = createGame();

        MvcResult result = mockMvc.perform(post("/api/blackjack/games/{gameId}/hit", gameId))
                .andExpect(status().isOk())
                .andReturn();

        GameStateResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), GameStateResponse.class);
        assertThat(response.getPlayerHand().getCards()).hasSize(3);
    }

    @Test
    void hit_whenRoundOver_returnsBadRequest() throws Exception {
        UUID gameId = createGame();
        // End the round first
        mockMvc.perform(post("/api/blackjack/games/{gameId}/stand", gameId))
                .andExpect(status().isOk());

        // Try to hit after round is over
        mockMvc.perform(post("/api/blackjack/games/{gameId}/hit", gameId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void stand_dealerPlaysAndRevealsHand() throws Exception {
        UUID gameId = createGame();

        MvcResult result = mockMvc.perform(post("/api/blackjack/games/{gameId}/stand", gameId))
                .andExpect(status().isOk())
                .andReturn();

        GameStateResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), GameStateResponse.class);
        assertThat(response.getStatus()).isEqualTo(GameStatus.ROUND_OVER);
        assertThat(response.getDealerHand().isRevealed()).isTrue();
        assertThat(response.getRoundResult().getOutcome()).isNotNull();
        assertThat(response.getRoundResult().getExplanation()).isNotEmpty();
    }

    @Test
    void stand_withInvalidId_returnsNotFound() throws Exception {
        UUID fakeId = UUID.randomUUID();

        mockMvc.perform(post("/api/blackjack/games/{gameId}/stand", fakeId))
                .andExpect(status().isNotFound());
    }

    @Test
    void newRound_resetsHandsPreservesStats() throws Exception {
        UUID gameId = createGame();
        // Play and end a round
        mockMvc.perform(post("/api/blackjack/games/{gameId}/stand", gameId))
                .andExpect(status().isOk());

        // Start new round
        MvcResult result = mockMvc.perform(post("/api/blackjack/games/{gameId}/rounds", gameId))
                .andExpect(status().isOk())
                .andReturn();

        GameStateResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), GameStateResponse.class);
        assertThat(response.getPlayerHand().getCards()).hasSize(2);
        assertThat(response.getStats().getRoundsPlayed()).isEqualTo(2);
        assertThat(response.getStatus()).isIn(GameStatus.PLAYER_TURN, GameStatus.ROUND_OVER);
    }

    @Test
    void deleteGame_removesGame() throws Exception {
        UUID gameId = createGame();

        mockMvc.perform(delete("/api/blackjack/games/{gameId}", gameId))
                .andExpect(status().isOk());

        // Verify game is gone
        mockMvc.perform(get("/api/blackjack/games/{gameId}", gameId))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteGame_withInvalidId_returnsNotFound() throws Exception {
        UUID fakeId = UUID.randomUUID();

        mockMvc.perform(delete("/api/blackjack/games/{gameId}", fakeId))
                .andExpect(status().isNotFound());
    }

    @Test
    void hit_withInvalidId_returnsNotFound() throws Exception {
        UUID fakeId = UUID.randomUUID();

        mockMvc.perform(post("/api/blackjack/games/{gameId}/hit", fakeId))
                .andExpect(status().isNotFound());
    }

    // ========== SPLIT ENDPOINT TESTS ==========

    /**
     * Helper: create a game that is splittable (has matching-rank cards).
     */
    private UUID createSplittableGame() throws Exception {
        for (int i = 0; i < 200; i++) {
            MvcResult result = mockMvc.perform(post("/api/blackjack/games"))
                    .andExpect(status().isOk())
                    .andReturn();

            GameStateResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(), GameStateResponse.class);

            if (response.getStatus() == GameStatus.PLAYER_TURN
                    && response.getAvailableActions().contains("SPLIT")) {
                return response.getGameId();
            }
        }
        fail("Could not create a splittable game after 200 attempts");
        return null; // unreachable
    }

    @Test
    void split_withMatchingCards_succeeds() throws Exception {
        UUID gameId = createSplittableGame();

        MvcResult result = mockMvc.perform(post("/api/blackjack/games/{gameId}/split", gameId))
                .andExpect(status().isOk())
                .andReturn();

        GameStateResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), GameStateResponse.class);

        assertThat(response.getSplitHands()).isNotNull();
        assertThat(response.getSplitHands()).hasSize(1); // 2 total hands - 1 active = 1 split hand
        assertThat(response.getActiveHandIndex()).isEqualTo(0);
        assertThat(response.getStatus()).isEqualTo(GameStatus.PLAYER_TURN);
    }

    @Test
    void split_whenNotEligible_returnsBadRequest() throws Exception {
        UUID gameId = createGame();

        // Try to split when cards don't match (might rarely match, so just check it doesn't error)
        mockMvc.perform(post("/api/blackjack/games/{gameId}/split", gameId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void split_withInvalidId_returnsNotFound() throws Exception {
        UUID fakeId = UUID.randomUUID();

        mockMvc.perform(post("/api/blackjack/games/{gameId}/split", fakeId))
                .andExpect(status().isNotFound());
    }

    @Test
    void split_thenStandTwice_completesRound() throws Exception {
        UUID gameId = createSplittableGame();

        // Split
        mockMvc.perform(post("/api/blackjack/games/{gameId}/split", gameId))
                .andExpect(status().isOk());

        // Stand on first hand
        mockMvc.perform(post("/api/blackjack/games/{gameId}/stand", gameId))
                .andExpect(status().isOk());

        // Stand on second hand - should end round
        MvcResult result = mockMvc.perform(post("/api/blackjack/games/{gameId}/stand", gameId))
                .andExpect(status().isOk())
                .andReturn();

        GameStateResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), GameStateResponse.class);
        assertThat(response.getStatus()).isEqualTo(GameStatus.ROUND_OVER);
        assertThat(response.getRoundResult().getOutcome()).isNotNull();
    }

    @Test
    void getGame_afterSplit_showsSplitHands() throws Exception {
        UUID gameId = createSplittableGame();

        mockMvc.perform(post("/api/blackjack/games/{gameId}/split", gameId))
                .andExpect(status().isOk());

        MvcResult result = mockMvc.perform(get("/api/blackjack/games/{gameId}", gameId))
                .andExpect(status().isOk())
                .andReturn();

        GameStateResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), GameStateResponse.class);
        assertThat(response.getSplitHands()).isNotNull();
        assertThat(response.getSplitHands()).isNotEmpty();
    }

    @Test
    void availableActions_includesSplit_whenEligible() throws Exception {
        UUID gameId = createSplittableGame();

        MvcResult result = mockMvc.perform(get("/api/blackjack/games/{gameId}", gameId))
                .andExpect(status().isOk())
                .andReturn();

        GameStateResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), GameStateResponse.class);
        assertThat(response.getAvailableActions()).contains("SPLIT");
    }
}
