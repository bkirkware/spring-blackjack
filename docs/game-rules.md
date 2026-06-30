# Blackjack Game Rules

## Objective
Get a hand value closer to 21 than the dealer without going over (busting).

## Card Values
| Card | Value |
|------|-------|
| Ace  | 11 (or 1 if 11 would bust) |
| 2–9  | Face value |
| 10, J, Q, K | 10 |

## Dealing
1. A standard 52-card deck is used.
2. The deck is shuffled at the start of each round.
3. Each player and the dealer receive two cards.
4. The dealer has one card face-up and one card face-down (hidden).

## Player Actions
- **Hit**: Draw one more card. Can hit multiple times until choosing to stand or busting.
- **Stand**: End your turn. Dealer then plays.
- **New Round**: Start a fresh round while keeping cumulative stats.

## Dealer Rules
- Dealer **hits** on **16 or less** (both hard and soft).
- Dealer **stands** on **17 or higher** (both hard and soft).
- Dealer plays automatically after the player stands or busts.

## Hand Types
- **Hard hand**: No ace, or ace counts as 1.
- **Soft hand**: Contains an ace counted as 11 (e.g., Ace + 6 = soft 17).
- **Natural Blackjack**: Ace + 10-value card dealt as the opening two cards.

## Outcomes
| Scenario | Result |
|----------|--------|
| Natural Blackjack (player only) | Player wins |
| Natural Blackjack (both) | Push |
| Player busts | Dealer wins immediately |
| Dealer busts | Player wins |
| Player hand > dealer hand | Player wins |
| Dealer hand > player hand | Dealer wins |
| Equal hand values | Push |

## Round Flow
1. **Deal**: Both player and dealer get two cards. Check for natural blackjack.
2. **Player turn**: Player hits or stands.
3. **Dealer turn**: If player didn't bust, dealer plays by their rules.
4. **Compare**: Determine winner based on hand values.
5. **Stats**: Track wins, losses, and pushes across rounds.

## Special Cases
- **Aces are flexible**: An ace counts as 11 unless that would cause the hand to bust (total > 21), in which case it counts as 1.
- **Best hand value**: The hand value is always the highest total ≤ 21. If all combinations bust, the hand value is the lowest bust total.
- **No side bets**: This implementation does not include insurance, splitting, or doubling down.
