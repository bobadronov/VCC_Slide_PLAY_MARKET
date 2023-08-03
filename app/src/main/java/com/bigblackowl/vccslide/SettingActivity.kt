package com.bigblackowl.vccslide

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class SettingActivity : AppCompatActivity() {
    private lateinit var editTextKyiv: EditText
    private lateinit var editTextKhmelnytskyi: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        editTextKyiv = findViewById(R.id.editTextKyiv)
        editTextKhmelnytskyi = findViewById(R.id.editTextKhmelnytskyi)

        val buttonBack: ImageButton = findViewById(R.id.buttonBack)
        buttonBack.setOnClickListener { finish() }

        loadUrlsFromSharedPreferences() // Load URLs from SharedPreferences

        // Handle saving URLs when the activity is destroyed
        editTextKyiv.setOnEditorActionListener { _, _, _ ->
            saveUrlsToSharedPreferences()
            false
        }

        editTextKhmelnytskyi.setOnEditorActionListener { _, _, _ ->
            saveUrlsToSharedPreferences()
            false
        }
    }

    private fun saveUrlsToSharedPreferences() {
        val sharedPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("url_kyiv", editTextKyiv.text.toString())
        editor.putString("url_khmelnytskyi", editTextKhmelnytskyi.text.toString())
        editor.apply()
    }

    private fun loadUrlsFromSharedPreferences() {
        val sharedPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE)
        val urlKyiv = sharedPreferences.getString("url_kyiv", "")
        val urlKhmelnytskyi = sharedPreferences.getString("url_khmelnytskyi", "")

        editTextKyiv.setText(urlKyiv)
        editTextKhmelnytskyi.setText(urlKhmelnytskyi)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("finish()"))
    override fun onBackPressed() {
        finish()
    }
}
