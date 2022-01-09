package com.mark.memorygame

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.mark.memorygame.models.BoardSize
import com.mark.memorygame.models.MemoryCard
import kotlin.math.min

class MemoryBoardAdapter(
    private val context: Context,
    private val boardSize: BoardSize,
    private val cards: List<MemoryCard>, // The 'Int' represents one of the drawable images.
    private val cardClickListener: CardClickListener
) :
    RecyclerView.Adapter<MemoryBoardAdapter.ViewHolder>() {

    // Defines the gap between the cards.
    companion object
    {
        private const val TAG = "MemoryBoardAdapter"
        private const val MARGIN_SIZE = 10
    }

    interface CardClickListener{
        fun onCardClicked(position: Int)
    }

    // Figures out how to create one view of RecyclerView.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val cardWidth : Int = parent.width / boardSize.getWidth() - (2 * MARGIN_SIZE)
        val cardHeight : Int = parent.height / boardSize.getHeight() - (2 * MARGIN_SIZE)
        val cardSideLength = min(cardWidth, cardHeight)
        val view = LayoutInflater.from(context).inflate(R.layout.memory_card, parent, false)
        val layoutParams = view.findViewById<CardView>(R.id.cardView).layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.width = cardSideLength
        layoutParams.height = cardSideLength
        layoutParams.setMargins(MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE)

        return ViewHolder(view)
    }

    // How many elements in the RecyclerView.
    override fun getItemCount() = boardSize.numCards
    // Takes data from 'position' and passes it to 'holder: ViewHolder'.
    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        holder.bind(position)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        private val imageButton = itemView.findViewById<ImageButton>(R.id.imageButton)

        fun bind(position: Int)
        {
            val memoryCard = cards[position]
            imageButton.setImageResource(if (memoryCard.isFaceUp) memoryCard.identifier else R.drawable.ic_launcher_background)
            
            imageButton.alpha = if (memoryCard.isMatched) .4f else 1f
            val colourStateList = if (memoryCard.isMatched) ContextCompat.getColorStateList(context, R.color.color_gray) else null
            ViewCompat.setBackgroundTintList(imageButton, colourStateList)

            imageButton.setOnClickListener {
                Log.i(TAG, "Click on position $position")
                cardClickListener.onCardClicked(position)
            }
        }
    }
}
