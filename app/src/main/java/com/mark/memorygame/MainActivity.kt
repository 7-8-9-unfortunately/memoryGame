package com.mark.memorygame

import android.animation.ArgbEvaluator
import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener
import com.google.android.material.snackbar.Snackbar
import com.mark.memorygame.databinding.ActivityMainBinding
import com.mark.memorygame.models.BoardSize
import com.mark.memorygame.models.MemoryCard
import com.mark.memorygame.models.MemoryGame
import com.mark.memorygame.utils.DEFAULT_ICONS
import com.mark.memorygame.utils.EXTRA_BOARD_SIZE

class MainActivity : AppCompatActivity()
{
    private lateinit var binding: ActivityMainBinding

    companion object
    {
        private const val TAG = "MainActivity"
        private const val CREATE_REQUEST_CODE = 221
    }

    private lateinit var clRoot: ConstraintLayout
    private lateinit var rvBoard: RecyclerView
    private lateinit var tvMoves: TextView
    private lateinit var tvPairs: TextView

    private lateinit var memoryGame: MemoryGame
    private lateinit var adapter: MemoryBoardAdapter
    private var boardSize = BoardSize.EASY

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // "R" Stands for 'Resources'.

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view: ConstraintLayout = binding.root
        setContentView(view)

        clRoot = findViewById(R.id.clRoot)
        rvBoard = findViewById(R.id.rvBoard)
        tvMoves = findViewById(R.id.tvMoves)
        tvPairs = findViewById(R.id.tvPairs)


       val intent = Intent(this, CreateActivity::class.java)
        intent.putExtra(EXTRA_BOARD_SIZE, BoardSize.EASY)
        startActivity(intent)

        setupBoard()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean
    {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        when (item.itemId)
        {
            R.id.mi_refresh ->
            {   // Set up game again;
                if (memoryGame.getNumMoves() > 0 && !memoryGame.haveWonGame())
                {
                    showAlertDialog("Quit your current game?", null,
                        View.OnClickListener { setupBoard() })
                }
                else
                {
                    setupBoard()
                }
                return true
            }
            R.id.mi_new_size ->
            {
                showNewSizeDialog()
                return true
            }
            R.id.mi_custom ->
            {
                showCreationDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showCreationDialog()
    {
        val boardSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size, null)
        val radioGroupSize = boardSizeView.findViewById<RadioGroup>(R.id.radioGroup)

        showAlertDialog("Create your own memory board", boardSizeView, View.OnClickListener
        {   // Set a new value for the board size;
            val desiredBoardSize = when (radioGroupSize.checkedRadioButtonId)
            {
                R.id.rbEasy -> BoardSize.EASY
                R.id.rbMedium -> BoardSize.MEDIUM
                else -> BoardSize.HARD
            }
            // Navigate user to new activity;
            val intent = Intent(this, CreateActivity::class.java)
            intent.putExtra(EXTRA_BOARD_SIZE, desiredBoardSize)
            startActivityForResult(intent, CREATE_REQUEST_CODE)
        })
    }

    private fun showNewSizeDialog()
    {
        val boardSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size, null)
        val radioGroupSize = boardSizeView.findViewById<RadioGroup>(R.id.radioGroup)

        when (boardSize)
        {
            BoardSize.EASY -> radioGroupSize.check(R.id.rbEasy)
            BoardSize.MEDIUM -> radioGroupSize.check(R.id.rbMedium)
            BoardSize.HARD -> radioGroupSize.check(R.id.rbHard)
        }

        showAlertDialog("Choose new size", boardSizeView, View.OnClickListener
        {   // Set a new value for the board size;
            boardSize = when (radioGroupSize.checkedRadioButtonId)
            {
                R.id.rbEasy -> BoardSize.EASY
                R.id.rbMedium -> BoardSize.MEDIUM
                else -> BoardSize.HARD
            }
            setupBoard()
        })
    }

    private fun showAlertDialog(title: String, view: View?, positiveButtonClickListener: View.OnClickListener)
    {
        AlertDialog.Builder(this )
            .setTitle(title)
            .setView(view)
            .setNegativeButton("Cancel", null) // null is used if user cancels.
            .setPositiveButton("OK"){_, _ -> positiveButtonClickListener.onClick(null)}.show()
    }

    private fun setupBoard()
    {
        when (boardSize)
        {
            BoardSize.EASY -> {
                tvMoves.text = "Easy: 4 x 2"
                tvPairs.text = "Pairs: 0 / 4"
            }
            BoardSize.MEDIUM -> {
                tvMoves.text = "Medium: 6 x 3"
                tvPairs.text = "Pairs: 0 / 9"
            }
            BoardSize.HARD -> {
                tvMoves.text = "Easy: 6 x 4"
                tvPairs.text = "Pairs: 0 / 12"
            }
        }

        tvPairs.setTextColor(ContextCompat.getColor(this, R.color.color_progress_none))

        // RecyclerView - Layout Manager;
        // Measures and positions item views.
        // RecyclerView - Adapter;
        // Provides a binding for the data set to the views of the RecyclerView.

        // Pre defined Layout Manager that comes with Android, that will produce a grid effect.
        // 'context: this' refers to 'MainActivity'.
        // 'spanCount' refers to Columns.

        // Constructs MemoryGame;
        memoryGame = MemoryGame(boardSize)

        adapter = MemoryBoardAdapter(this, boardSize, memoryGame.cards, object: MemoryBoardAdapter.CardClickListener {
            override fun onCardClicked(position: Int) {
                updateGameWithFlip(position)
            }
        })

        rvBoard.adapter = adapter
        // This sets the RecyclerView 'true' to whatever phones is using the app.
        rvBoard.setHasFixedSize(true)
        rvBoard.layoutManager = GridLayoutManager(this, boardSize.getWidth())
    }

    // Updates the game with a flip of the card at 'this position';
    private fun updateGameWithFlip(position: Int) {
        // Error checking;
        if (memoryGame.haveWonGame())
        { // Alert the user of invalid move using Snackbar;
            Snackbar.make(clRoot, "You already won", Snackbar.LENGTH_LONG).show()
            return
        }
        if (memoryGame.isCardFaceUp(position))
        { // Alert the user of invalid move using Snackbar;
            Snackbar.make(clRoot, "Invalid move", Snackbar.LENGTH_SHORT).show()
            return
        }

        // Flipping over card;
        if (memoryGame.flipCard(position))
        {
            Log.i(TAG, "Found a match! Num pairs found: ${memoryGame.numPairsFound}")
            val color = ArgbEvaluator().evaluate(
                memoryGame.numPairsFound.toFloat() / boardSize.getNumPairs(),
                ContextCompat.getColor(this, R.color.color_progress_none),
                ContextCompat.getColor(this, R.color.color_progress_full)
            ) as Int
            tvPairs.setTextColor(color)
            tvPairs.text = "Pairs: ${memoryGame.numPairsFound} / ${boardSize.getNumPairs()}"
            if (memoryGame.haveWonGame())
            {
                Snackbar.make(clRoot, "You Won! Congrats!", Snackbar.LENGTH_LONG).show()
            }
        }
        tvMoves.text = "Moves: ${memoryGame.getNumMoves()}"

        // Notifying RecyclerView of the card that's been flipped;
        adapter.notifyDataSetChanged()
    }
}