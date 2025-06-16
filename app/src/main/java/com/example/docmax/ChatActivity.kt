package com.example.docmax

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class ChatActivity : AppCompatActivity() {
    private lateinit var chatContainer: LinearLayout
    private lateinit var userInput: EditText
    private lateinit var sendButton: Button
    private var userName: String = "User"
    private var lastSentiment: String = "Belum terdeteksi"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        userName = intent.getStringExtra("USERNAME") ?: "User"
        findViewById<TextView>(R.id.userNameTitle).text = userName

        chatContainer = findViewById(R.id.chatContainer)
        userInput = findViewById(R.id.inputText)
        sendButton = findViewById(R.id.btnSend)

        // Side menu button
        findViewById<ImageButton>(R.id.menuButton).setOnClickListener {
            showMoodDialog()
        }

        // Greeting
        appendChat("DocMax", "Halo $userName, bagaimana kabarmu hari ini?")

        sendButton.setOnClickListener {
            val message = userInput.text.toString().trim()
            if (message.isNotEmpty()) {
                appendChat(userName, message)
                respondToUser(message)
                userInput.text.clear()
            }
        }
    }

    private fun appendChat(sender: String, message: String) {
        val bubble = TextView(this)
        bubble.text = "$sender:\n$message"
        bubble.textSize = 16f
        bubble.setPadding(24, 16, 24, 16)
        bubble.setTextColor(ContextCompat.getColor(this, android.R.color.white))

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(0, 8, 0, 8)

        if (sender == userName) {
            bubble.setBackgroundResource(android.R.drawable.dialog_holo_light_frame)
            bubble.setTextColor(ContextCompat.getColor(this, android.R.color.black))
            bubble.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_END
            layoutParams.gravity = android.view.Gravity.END
        } else {
            bubble.setBackgroundResource(android.R.drawable.dialog_holo_dark_frame)
            bubble.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_START
            layoutParams.gravity = android.view.Gravity.START
        }

        bubble.layoutParams = layoutParams
        chatContainer.addView(bubble)

        // Auto-scroll
        findViewById<ScrollView>(R.id.chatScroll).post {
            findViewById<ScrollView>(R.id.chatScroll).fullScroll(ScrollView.FOCUS_DOWN)
        }
    }

    private fun respondToUser(message: String) {
        lastSentiment = when {
            message.contains("tidak", ignoreCase = true) -> "Negatif"
            message.contains("baik", ignoreCase = true) -> "Positif"
            else -> "Netral"
        }

        val mood = when (lastSentiment) {
            "Positif" -> "Senang / Bahagia"
            "Negatif" -> "Sedih / Stres / Depresi"
            "Netral" -> "Biasa Saja"
            else -> "Belum diketahui"
        }

        val response = when (lastSentiment) {
            "Negatif" -> "Maaf kau merasa seperti itu. Mau bicara lebih lanjut?"
            "Positif" -> "Senang mendengarnya!"
            else -> "Ceritakan lebih lanjut yuk."
        }

        appendChat("DocMax", response)
    }

    private fun showMoodDialog() {
        val mood = when (lastSentiment) {
            "Positif" -> "Senang / Bahagia"
            "Negatif" -> "Sedih / Stres / Depresi"
            "Netral" -> "Biasa Saja"
            else -> "Belum diketahui"
        }

        AlertDialog.Builder(this)
            .setTitle("Suasana Hati Kamu")
            .setMessage("Berdasarkan pesan terakhir:\n\nSentimen: $lastSentiment\nMood: $mood")
            .setPositiveButton("Tutup", null)
            .show()
    }
}