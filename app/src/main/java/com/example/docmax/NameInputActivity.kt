package com.example.docmax

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast

class NameInputActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_name_input) // Make sure this XML exists

        val nameInput = findViewById<EditText>(R.id.editTextName)
        val continueButton = findViewById<Button>(R.id.buttonSubmit)

        continueButton.setOnClickListener {
            val userName = nameInput.text.toString().trim()
            if (userName.isNotEmpty()) {
                val intent = Intent(this, ChatActivity::class.java)
                intent.putExtra("USERNAME", userName)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Tolong masukkan nama dulu", Toast.LENGTH_SHORT).show()
            }
        }
    }
}