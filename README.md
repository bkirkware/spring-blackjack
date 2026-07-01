# Spring Blackjack

A complete 1-on-1 Blackjack game built with Spring Boot 4.x and Java 21.

Play against the dealer through a REST API — hit, stand, bust, and track your wins, losses, and pushes.

## Game Rules

See [docs/game-rules.md](docs/game-rules.md) for the full rule set. Highlights:

- Standard 52-card deck, shuffled each round
- Player gets 2 cards, dealer gets 2 (1 face-up, 1 hidden)
- **Hit**: draw a card; **Stand**: end your turn
- Dealer hits on 16 or less, stands on 17+ (hard or soft)
- Aces count as 11 or 1 (whichever keeps the hand alive)
- Natural blackjack (A + 10-value) beats any non-blackjack 21
- If both have blackjack → push
- No betting, splitting, doubling down, or insurance (v1)

## Quick Start

### Prerequisites
- Java 21+
- Maven 3.8+

### Run Locally
```bash
git clone https://github.com/bkirkware/spring-blackjack.git
cd spring-blackjack
git checkout agent-demo/blackjack-game

./mvnw spring-boot:run
```

The server starts at `http://localhost:8080`.

Health check: `curl http://localhost:8080/actuator/health`

### Run Tests
```bash
./mvnw test
```

## Deploy to Cloud Foundry

### Prerequisites
- [Cloud Foundry CLI](https://docs.cloudfoundry.org/cf-cli/) installed
- Access to a Cloud Foundry deployment (e.g., Pivotal Cloud Foundry, VMware Tanzu Application Service)
- Logged in via `cf login`

### Deploy

1. **Package the application** (if not already built):
   ```bash
   ./mvnw clean package -DskipTests
   ```

2. **Push to Cloud Foundry**:
   ```bash
   cf push
   ```

   The `manifest.yml` configures the app with:
   - A random route (change `random-route: false` and set a `hosts`/`domain` for a fixed URL)
   - 512 MB memory and 1 GB disk quota
   - HTTP health check on `/actuator/health`
   - 180-second startup timeout

3. **Verify the deployment**:
   ```bash
   cf apps
   cf logs spring-blackjack --recent
   ```

4. **Access the app**:
   ```bash
   cf app spring-blackjack
   ```
   Note the URL shown under `urls` and use it in place of `http://localhost:8080` for all API calls.

### Customizing the Manifest

Edit `manifest.yml` before pushing to adjust:

- **`memory`**: Increase if you encounter OutOfMemoryError (e.g., `1G`)
- **`disk_quota`**: Adjust for larger log retention or uploads
- **`instances`**: Scale horizontally (e.g., `instances: 2`) — note that game state is in-memory, so scaling beyond 1 instance may cause session inconsistencies
- **`env`**: Add environment variables, e.g., JVM options:
  ```yaml
  env:
    JBP_CONFIG_OPEN_JDK_JRE: '{ jre: { version: 21.+ } }'
  ```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/blackjack/games` | Start a new game session |
| `GET` | `/api/blackjack/games/{gameId}` | Get current game state |
| `POST` | `/api/blackjack/games/{gameId}/hit` | Player takes a card |
| `POST` | `/api/blackjack/games/{gameId}/stand` | Player stands; dealer plays |
| `POST` | `/api/blackjack/games/{gameId}/rounds` | New round (keeps stats) |
| `DELETE` | `/api/blackjack/games/{gameId}` | End game session |

## Example curl Commands

### 1. Start a new game
```bash
curl -X POST http://localhost:8080/api/blackjack/games | jq .
```

Save the `gameId` from the response.

### 2. Check game state
```bash
curl http://localhost:8080/api/blackjack/games/{gameId} | jq .
```

### 3. Hit (take a card)
```bash
curl -X POST http://localhost:8080/api/blackjack/games/{gameId}/hit | jq .
```

### 4. Stand (dealer plays)
```bash
curl -X POST http://localhost:8080/api/blackjack/games/{gameId}/stand | jq .
```

### 5. New round
```bash
curl -X POST http://localhost:8080/api/blackjack/games/{gameId}/rounds | jq .
```

### 6. End game
```bash
curl -X DELETE http://localhost:8080/api/blackjack/games/{gameId} | jq .
```

## Example Response

```json
{
  "gameId": "a1b2c3d4-...",
  "status": "ROUND_OVER",
  "playerHand": {
    "cards": ["♠A", "♥K"],
    "value": 21,
    "isSoft": false,
    "isBust": false,
    "isBlackjack": true
  },
  "dealerHand": {
    "visibleCards": ["♦Q", "♣7"],
    "visibleValue": 17,
    "fullCards": ["♦Q", "♣7"],
    "fullValue": 17,
    "isRevealed": true
  },
  "stats": {
    "wins": 1,
    "losses": 0,
    "pushes": 0,
    "roundsPlayed": 1
  },
  "roundResult": {
    "outcome": "PLAYER_WINS",
    "explanation": "Natural Blackjack! You win!"
  },
  "availableActions": ["NEW_ROUND"]
}
```

## Project Structure

```
src/main/java/com/kirkware/blackjack/
├── BlackjackApplication.java       # Spring Boot entry point
├── controller/
│   ├── BlackjackController.java    # REST API endpoints
│   └── GlobalExceptionHandler.java # Unified error responses
├── dto/
│   ├── GameStateResponse.java      # API response DTO
│   └── ResponseMapper.java         # Domain → DTO mapping
├── domain/
│   ├── Action.java                 # Available actions enum
│   ├── BlackjackGame.java          # Game session state
│   ├── Card.java                   # Playing card
│   ├── Deck.java                   # 52-card deck
│   ├── GameStatus.java             # Round status enum
│   ├── Hand.java                   # Hand with scoring
│   ├── Rank.java                   # Card rank enum
│   ├── RoundOutcome.java           # Win/loss/push enum
│   └── Suit.java                   # Card suit enum
└── service/
    └── GameService.java            # Core game logic
```

## Known Limitations

- In-memory state only (lost on restart)
- No betting or chip management
- No splitting, doubling down, or surrender
- Single player per game session
- No authentication or session persistence

## Future Enhancements

- Betting with chips and payout multipliers
- Splitting pairs
- Doubling down
- Insurance bets
- WebSocket for real-time play
- Persistent game storage (database)
- Leaderboard and player profiles
