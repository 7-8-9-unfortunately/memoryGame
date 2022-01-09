package com.mark.memorygame

import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mark.memorygame.models.BoardSize
import com.mark.memorygame.utils.EXTRA_BOARD_SIZE
import com.mark.memorygame.utils.BitmapScaler
import java.io.ByteArrayOutputStream

class CreateActivity : AppCompatActivity() {

    companion object
    {
        private const val TAG = "CreateActivity"
        private const val PICK_PHOTO_CODE = 222
    }

    private lateinit var rvImagePicker: RecyclerView
    private lateinit var etGame: EditText
    private lateinit var btnSave: Button

    private lateinit var imagePickerAdapter: ImagePickerAdapter
    private lateinit var boardSize: BoardSize
    private var numImagesRequired = -1
    private val chosenImageUris = mutableListOf<Uri>() // Where does a particular resource live

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)

        rvImagePicker = findViewById(R.id.rvImagePicker)
        etGame = findViewById(R.id.etGameName)
        btnSave = findViewById(R.id.btnSave)

        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Modifies to show a back button
        boardSize = intent.getSerializableExtra(EXTRA_BOARD_SIZE) as BoardSize
        numImagesRequired = boardSize.getNumPairs()
        supportActionBar?.title = "Choose pics (0 / $numImagesRequired)"

        btnSave.setOnClickListener { saveDataToFirebase() }
        etGame.filters = arrayOf(InputFilter.LengthFilter(14))
        etGame.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?)
            {
                btnSave.isEnabled = shouldEnableSaveButton()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int){}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        imagePickerAdapter = ImagePickerAdapter(this, chosenImageUris, boardSize, object: ImagePickerAdapter.ImageClickListener{

            override fun onPlaceholderClicker()
            {
                launchIntentForPhoto()
            }
        })
        rvImagePicker.adapter = imagePickerAdapter
        rvImagePicker.setHasFixedSize(true)
        rvImagePicker.layoutManager = GridLayoutManager(this, boardSize.getWidth())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home)
        {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != PICK_PHOTO_CODE || resultCode != Activity.RESULT_OK || data == null)
        {
            Log.w(TAG, "Did not get data back from the launched activity, user likely canceled flow")
            return
        }

        Log.i(TAG, "onActivityResult")
        val selectedUri = data.data
        val clipData = data.clipData
        if (clipData != null)
        {
            Log.i(TAG, "clipData numImages ${clipData.itemCount}: $clipData")
            for (i in 0 until clipData.itemCount) {
                val clipItem = clipData.getItemAt(i)
                if (chosenImageUris.size < numImagesRequired) {
                    chosenImageUris.add(clipItem.uri)
                }
            }
        }
        else if (selectedUri != null)
        {
            Log.i(TAG, "data: $selectedUri")
            chosenImageUris.add(selectedUri)
        }
        imagePickerAdapter.notifyDataSetChanged()
        supportActionBar?.title = "Choose pics (${chosenImageUris.size} / $numImagesRequired)"
        btnSave.isEnabled = shouldEnableSaveButton()
    }

    private fun saveDataToFirebase()
    {
        Log.i(TAG, "saveToFirebase")
        for ((index: Int, photoUri: Uri) in chosenImageUris.withIndex())
        {
            val imageByteArray = getImageByteArray(photoUri)
        }
    }

    private fun getImageByteArray(photoUri: Uri): ByteArray
    {
        val originalBitmap: Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
        {
            val source = ImageDecoder.createSource(contentResolver, photoUri)
            ImageDecoder.decodeBitmap(source)
        }
        else
        {
            MediaStore.Images.Media.getBitmap(contentResolver, photoUri)
        }
        Log.i(TAG, "Original width ${originalBitmap.width} and height ${originalBitmap.height}")
        val scaledBitmap = BitmapScaler.scaleToFitHeight(originalBitmap, 250)
        Log.i(TAG, "Scale width ${scaledBitmap.width} and height ${scaledBitmap.height}")
        val byteArrayOutputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

    private fun shouldEnableSaveButton(): Boolean
    { // Check if we should enable save button or not
        if(chosenImageUris.size != numImagesRequired)
        {
            return false
        }
        if(etGame.text.isBlank() || etGame.text.length < 3)
        {
            return false
        }
        return true
    }

    private fun launchIntentForPhoto()
    {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(Intent.createChooser(intent, "Choose pics"), PICK_PHOTO_CODE)
    }
}