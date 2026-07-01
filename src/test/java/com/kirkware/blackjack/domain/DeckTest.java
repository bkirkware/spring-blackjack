package com.kirkware.blackjack.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DeckTest {

    @Test
    void newDeck_has52Cards() {
        Deck deck = new Deck();
        deck.resetAndShuffle();
        assertEquals(52, deck.remaining());
    }

    @Test
    void deal_removesTopCard() {
        Deck deck = new Deck();
        deck.resetAndShuffle();
        Card card = deck.deal();
        assertNotNull(card);
        assertEquals(51, deck.remaining());
    }

    @Test
    void deal_allCards() {
        Deck deck = new Deck();
        deck.resetAndShuffle();
        for (int i = 0; i < 52; i++) {
            deck.deal();
        }
        assertEquals(0, deck.remaining());
        assertTrue(deck.isEmpty());
    }

    @Test
    void deal_emptyDeck_throwsException() {
        Deck deck = new Deck();
        deck.resetAndShuffle();
        for (int i = 0; i < 52; i++) {
            deck.deal();
        }
        assertThrows(IllegalStateException.class, deck::deal);
    }

    @Test
    void resetAndShuffle_restoresAll52Cards() {
        Deck deck = new Deck();
        deck.resetAndShuffle();
        deck.deal();
        deck.deal();
        assertEquals(50, deck.remaining());
        deck.resetAndShuffle();
        assertEquals(52, deck.remaining());
    }

    @Test
    void card_hasCorrectDisplay() {
        Card aceOfSpades = new Card(Suit.SPADES, Rank.ACE);
        assertEquals("♠A", aceOfSpades.display());

        Card tenOfHearts = new Card(Suit.HEARTS, Rank.TEN);
        assertEquals("♥10", tenOfHearts.display());

        Card jackOfDiamonds = new Card(Suit.DIAMONDS, Rank.JACK);
        assertEquals("♦J", jackOfDiamonds.display());
    }

    @Test
    void card_hiddenDisplay() {
        assertEquals("❓", Card.hiddenDisplay());
    }
}
