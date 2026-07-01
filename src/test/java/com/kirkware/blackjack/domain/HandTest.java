package com.kirkware.blackjack.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HandTest {

    @Test
    void value_hardHand() {
        Hand hand = new Hand();
        hand.addCard(new Card(Suit.SPADES, Rank.TEN));
        hand.addCard(new Card(Suit.HEARTS, Rank.FIVE));
        assertEquals(15, hand.value());
    }

    @Test
    void value_softHand_withAceCountedAs11() {
        Hand hand = new Hand();
        hand.addCard(new Card(Suit.SPADES, Rank.ACE));
        hand.addCard(new Card(Suit.HEARTS, Rank.SIX));
        assertEquals(17, hand.value());
    }

    @Test
    void value_softHandDowngradesAce_whenWouldBust() {
        Hand hand = new Hand();
        hand.addCard(new Card(Suit.SPADES, Rank.ACE));
        hand.addCard(new Card(Suit.HEARTS, Rank.KING));
        hand.addCard(new Card(Suit.CLUBS, Rank.THREE));
        assertEquals(14, hand.value()); // A(1) + K(10) + 3 = 14
    }

    @Test
    void value_multipleAcesDowngradesCorrectly() {
        Hand hand = new Hand();
        hand.addCard(new Card(Suit.SPADES, Rank.ACE));
        hand.addCard(new Card(Suit.HEARTS, Rank.ACE));
        hand.addCard(new Card(Suit.CLUBS, Rank.NINE));
        assertEquals(21, hand.value()); // A(11) + A(1) + 9 = 21
    }

    @Test
    void isSoft_returnsTrue_whenAceCountedAs11() {
        Hand hand = new Hand();
        hand.addCard(new Card(Suit.SPADES, Rank.ACE));
        hand.addCard(new Card(Suit.HEARTS, Rank.SIX));
        assertTrue(hand.isSoft());
    }

    @Test
    void isSoft_returnsFalse_whenAceDowngradedTo1() {
        Hand hand = new Hand();
        hand.addCard(new Card(Suit.SPADES, Rank.ACE));
        hand.addCard(new Card(Suit.HEARTS, Rank.KING));
        hand.addCard(new Card(Suit.CLUBS, Rank.THREE));
        assertFalse(hand.isSoft()); // Ace must be 1, so hard 14
    }

    @Test
    void isSoft_returnsFalse_forNoAces() {
        Hand hand = new Hand();
        hand.addCard(new Card(Suit.SPADES, Rank.TEN));
        hand.addCard(new Card(Suit.HEARTS, Rank.FIVE));
        assertFalse(hand.isSoft());
    }

    @Test
    void isBust_returnsTrue_whenValueExceeds21() {
        Hand hand = new Hand();
        hand.addCard(new Card(Suit.SPADES, Rank.KING));
        hand.addCard(new Card(Suit.HEARTS, Rank.QUEEN));
        hand.addCard(new Card(Suit.CLUBS, Rank.THREE));
        assertTrue(hand.isBust()); // 10+10+3 = 23
    }

    @Test
    void isBust_returnsFalse_whenAcePreventsBust() {
        Hand hand = new Hand();
        hand.addCard(new Card(Suit.SPADES, Rank.ACE));
        hand.addCard(new Card(Suit.HEARTS, Rank.KING));
        hand.addCard(new Card(Suit.CLUBS, Rank.TEN));
        assertFalse(hand.isBust()); // A(1)+K(10)+10 = 21
    }

    @Test
    void isNaturalBlackjack_returnsTrue_forAcePlusTen() {
        Hand hand = new Hand();
        hand.addCard(new Card(Suit.SPADES, Rank.ACE));
        hand.addCard(new Card(Suit.HEARTS, Rank.TEN));
        assertTrue(hand.isNaturalBlackjack());
    }

    @Test
    void isNaturalBlackjack_returnsTrue_forAcePlusFace() {
        Hand hand = new Hand();
        hand.addCard(new Card(Suit.SPADES, Rank.ACE));
        hand.addCard(new Card(Suit.HEARTS, Rank.KING));
        assertTrue(hand.isNaturalBlackjack());
    }

    @Test
    void isNaturalBlackjack_returnsFalse_forThreeCards21() {
        Hand hand = new Hand();
        hand.addCard(new Card(Suit.SPADES, Rank.SEVEN));
        hand.addCard(new Card(Suit.HEARTS, Rank.SIX));
        hand.addCard(new Card(Suit.CLUBS, Rank.EIGHT));
        assertFalse(hand.isNaturalBlackjack());
    }

    @Test
    void isNaturalBlackjack_returnsFalse_forNonBlackjackTwoCards() {
        Hand hand = new Hand();
        hand.addCard(new Card(Suit.SPADES, Rank.TEN));
        hand.addCard(new Card(Suit.HEARTS, Rank.FIVE));
        assertFalse(hand.isNaturalBlackjack());
    }
}
