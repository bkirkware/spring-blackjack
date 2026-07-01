package com.kirkware.blackjack.controller;

import com.kirkware.blackjack.dto.GameStateResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kirkware.blackjack.service.GameService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BlackjackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GameService gameService;

    // Helper: create a game and return the game ID
    private UUID createGame() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/blackjack/games"))
                .andExpect(status().isOk())
                .andReturn();

        GameStateResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), GameStateResponse.class);
        return response.getGameId();
    }

    @Test
    void createGame_returnsGameState() throws Exception {
        mockMvc.perform(post("/api/blackjack/games"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").isNotEmpty())
                .andExpect(jsonPath("$.status").value("PLAYER_TURN"))
                .andExpect(jsonPath("$.playerHand.cards").isArray())
                .andExpect(jsonPath("$.playerHand.cards.length()").value(2))
                .andExpect(jsonPath("$.dealerHand.visibleCards").isArray())
                .andExpect(jsonPath("$.dealerHand.visibleCards.length()").value(1))
                .andExpect(jsonPath("$.dealerHand.isRevealed").value(false))
                .andExpect(jsonPath("$.stats.wins").value(0))
                .andExpect(jsonPath("$.stats.losses").value(0))
                .andExpect(jsonPath("$.stats.pushes").value(0))
                .andExpect(jsonPath("$.availableActions").isArray())
                .andExpect(jsonPath("$.availableActions.length()").value(2));
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
        assertThat(response.getStatus()).isEqualTo("ROUND_OVER");
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
        assertThat(response.getStatus()).isIn("PLAYER_TURN", "ROUND_OVER");
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
}
