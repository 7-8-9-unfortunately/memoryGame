package com.mark.memorygame.models

import com.mark.memorygame.utils.DEFAULT_ICONS

class MemoryGame(private val boardSize: BoardSize)
{

    val cards: List<MemoryCard>
    // When you start the game, no pairs found;
    var numPairsFound = 0

    private var numCardFlips = 0

    // Made 'null' because a new games has no SingleSelectedCard;
    private var indexOfSingleSelectedCard: Int? = null

    // Constructs a list of cards, based on board size, pick random images (chosenImage)...
    // ... and creating a memory card data class (MemoryCard(it)).
    init
    {
        // Takes images from ICON folder at random.
        val chosenImage = DEFAULT_ICONS.shuffled().take(boardSize.getNumPairs())
        // Two copies of each image.
        val randomizedImage = (chosenImage + chosenImage).shuffled()
        // Create list of memory cards.
        cards = randomizedImage.map { MemoryCard(it) }
    }

    fun flipCard(position: Int): Boolean
    {
        numCardFlips++
        val card = cards[position]
        // Three cases;
        // 0 cards flipped  => restore cards + flip over the selected card
        // 1 card flipped   => flip over the selected card + check if images match
        // 2 cards flipped  => restore cards + flip over the selected card
        var foundMatch = false
        if (indexOfSingleSelectedCard == null)
        {   // 0 or 2 cards previously flipped over;
            restoreCards()
            indexOfSingleSelectedCard = position
        }
        else
        { // Exactly 1 card previously flipped over
            foundMatch = checkForMatch(indexOfSingleSelectedCard!!, position)
            indexOfSingleSelectedCard = null
        }
        card.isFaceUp = !card.isFaceUp
        return foundMatch
    }

    private fun checkForMatch(position1: Int, position2: Int): Boolean
    {
        if (cards[position1].identifier != cards[position2].identifier)
        {
            return false
        }

        cards[position1].isMatched = true
        cards[position2].isMatched = true
        numPairsFound++
        return true
    }

    private fun restoreCards()
    {
        for (card in cards)
        {
            if (!card.isMatched)
            {
                card.isFaceUp = false
            }
        }
    }

    fun haveWonGame(): Boolean
    {
        return numPairsFound == boardSize.getNumPairs()
    }

    fun isCardFaceUp(position: Int): Boolean
    {
        return cards[position].isFaceUp
    }

    fun getNumMoves(): Int {
        return numCardFlips / 2
    }


}