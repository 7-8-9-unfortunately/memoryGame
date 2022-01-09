package com.mark.memorygame.models

enum class BoardSize(val numCards: Int)
{
    EASY(8),
    MEDIUM(18),
    HARD(24);

    // 'when' is like a switch statement. 'this' refers to 'BoardSize(val...'
    fun getWidth(): Int
    {
        return when (this)
        {
            EASY -> 2
            MEDIUM -> 3
            HARD -> 4
        }
    }

    fun getHeight(): Int
    {
        return numCards / getWidth()
    }

    fun getNumPairs(): Int
    {
        return numCards / 2
    }



}