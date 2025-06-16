package com.example.docmax

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // use your activity_main.xml layout

        val startButton = findViewById<Button>(R.id.btnStartChat)
        startButton.setOnClickListener {
            val intent = Intent(this, NameInputActivity::class.java)
            startActivity(intent)
        }
    }
}