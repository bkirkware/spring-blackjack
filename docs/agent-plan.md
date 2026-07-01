# Implementation Plan: Spring Boot Blackjack Game

## Overview
Build a complete 1-on-1 Blackjack game as a Spring Boot REST API application.

## Technology Stack
- **Java 21**
- **Spring Boot 4.x**
- **Maven**
- **In-memory state** (no database)
- **JUnit 5** for testing

## Architecture

```
src/main/java/com/kirkware/blackjack/
├── BlackjackApplication.java       # Spring Boot entry point
├── domain/
│   ├── Card.java                    # Playing card (suit + rank)
│   ├── Deck.java                    # 52-card deck with shuffle
│   ├── Hand.java                    # Hand with scoring logic
│   ├── Suit.java                    # ENUM: HEARTS, DIAMONDS, CLUBS, SPADES
│   ├── Rank.java                    # ENUM: ACE through 10, JACK, QUEEN, KING
│   ├── GameStatus.java              # ENUM: DEALING, PLAYER_TURN, DEALER_TURN, ROUND_OVER
│   ├── RoundOutcome.java            # ENUM: PLAYER_WINS, DEALER_WINS, PUSH
│   └── BlackjackGame.java           # Game state: player hand, dealer hand, stats
├── service/
│   └── GameService.java             # Core game logic (stateless helper + game store)
├── controller/
│   └── BlackjackController.java     # REST API endpoints
└── dto/
    ├── GameStateResponse.java       # Full game state response
    ├── NewGameResponse.java         # New game creation response
    └── RoundResultResponse.java     # Round end result response
```

## Implementation Phases

### Phase 1: Domain Model
1. `Suit` and `Rank` enums
2. `Card` class with `toString()` for display (e.g., "♠A", "♥10")
3. `Deck` class: 52 cards, shuffle method, deal method
4. `Hand` class: addCard(), value(), isSoft(), isBlackjack(), isBust()
5. `GameStatus` and `RoundOutcome` enums
6. `BlackjackGame` class: game state, player/dealer hands, win/loss/push counters

### Phase 2: Service Layer
1. `GameService` class:
   - In-memory store: `ConcurrentHashMap<UUID, BlackjackGame>`
   - `createGame()` → new game, deal initial cards, return game state
   - `hit(gameId)` → add card to player hand, check bust
   - `stand(gameId)` → dealer plays, determine outcome, update stats
   - `newRound(gameId)` → reset hands, deal new round, keep stats
   - `getGame(gameId)` → return current state
   - `deleteGame(gameId)` → remove game

### Phase 3: REST API
1. `BlackjackController` with endpoints:
   - `POST /api/blackjack/games` → create game
   - `GET /api/blackjack/games/{id}` → get state
   - `POST /api/blackjack/games/{id}/hit` → player hits
   - `POST /api/blackjack/games/{id}/stand` → player stands
   - `POST /api/blackjack/games/{id}/rounds` → new round
   - `DELETE /api/blackjack/games/{id}` → end game

### Phase 4: Response Design
- Hide dealer's hidden card until player stands/busts
- Include: playerHand, dealerVisibleCards, dealerFullHand (when revealed), 
  playerHandValue, dealerHandValue, gameStatus, availableActions, 
  roundOutcome, roundExplanation, stats (wins/losses/pushes)

### Phase 5: Testing
1. Domain tests:
   - Hand scoring (soft hands, aces, face cards)
   - Deck shuffling and dealing
   - Blackjack detection
2. Service tests:
   - Game creation and initial deal
   - Hit and stand logic
   - Dealer behavior (hit on 16, stand on 17)
   - Bust scenarios
   - Blackjack and push scenarios
3. Controller tests:
   - Full API flow with MockMvc
   - Error handling (invalid game ID, invalid actions)

### Phase 6: Documentation
1. `README.md` with overview, rules, run/test instructions, API docs, curl examples
2. `docs/agent-plan.md` (this file)
3. `docs/game-rules.md`

## Key Design Decisions

1. **Hand scoring**: Count aces as 11 by default. If total > 21, downgrade aces to 1 until ≤ 21 or no aces left.
2. **Soft hand detection**: Hand is "soft" if it contains an ace counted as 11.
3. **Dealer hidden card**: Stored in dealer hand but only exposed in response after round ends.
4. **Natural blackjack**: Detected after initial deal. If both have blackjack → push. If only one → immediate win.
5. **State management**: UUID-based game sessions stored in-memory. No persistence.
6. **Extensibility**: Service layer designed to support future betting, splitting, doubling down.

## Known Limitations (v1)
- No betting/chips
- No splitting, doubling down, or surrender
- Single player only
- In-memory state (lost on restart)
- No authentication or session management

## Future Enhancements
- Betting with chips and payout calculations
- Splitting pairs
- Doubling down
- Insurance
- Multiple simultaneous players
- WebSocket for real-time updates
- Persistent storage
- Leaderboard
