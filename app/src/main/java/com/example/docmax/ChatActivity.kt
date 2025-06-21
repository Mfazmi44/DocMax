package com.example.docmax

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class ChatActivity : AppCompatActivity() {
    private lateinit var chatContainer: LinearLayout
    private lateinit var userInput: EditText
    private lateinit var sendButton: Button
    private lateinit var emotionAnalyzer: EmotionAnalyzer
    private var userName: String = "User"
    private var lastSentiment: String = "Belum terdeteksi"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        emotionAnalyzer = EmotionAnalyzer(this)

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

        try {
            val emotion = emotionAnalyzer.analyze(message)
            Toast.makeText(this, "Detected: $emotion", Toast.LENGTH_SHORT).show()

            val response = getResponseForEmotion(emotion, message)
            appendChat("DocMax", response)

            // ⏭ Next: here we’ll later call Google Search based on emotion/message
        } catch (e: Exception) {
            appendChat("DocMax", "Maaf, aku kesulitan memahami perasaanmu barusan 😞")
            e.printStackTrace()
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

        val detectedEmotion = emotionAnalyzer.analyze(message)
        Toast.makeText(this, "Detected emotion: $detectedEmotion", Toast.LENGTH_SHORT).show()
// You can map this to mood or use it directly in UI

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

    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = assets.openFd("model.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun getResponseForEmotion(emotion: String, message: String): String {
        return when (emotion) {
            "joy" -> "Senang sekali mendengarnya! Apa yang membuatmu bahagia hari ini?"
            "sadness" -> "Aku ikut sedih mendengarnya. Ingin bercerita lebih lanjut?"
            "anger" -> "Sepertinya kamu sedang kesal. Mau aku bantu cari solusi atau hanya ingin didengarkan?"
            "fear" -> "Kamu tidak sendiri. Ada hal yang membuatmu takut atau khawatir?"
            "neutral" -> {
                when {
                    message.contains("malas", ignoreCase = true) -> "Nggak apa-apa kok kalau lagi merasa malas. Mau coba aktivitas ringan?"
                    message.contains("capek", ignoreCase = true) -> "Kalau capek, istirahat sebentar juga penting lho. Mau aku carikan cara relaksasi?"
                    else -> "Terima kasih sudah berbagi. Ceritakan apa yang kamu rasakan saat ini."
                }
            }
            else -> "Aku di sini untuk mendengarkan, silakan ceritakan apa pun yang kamu rasakan."
        }
    }

    private fun testModelLoad() {
        val model = Interpreter(loadModelFile())
        Toast.makeText(this, "Model Loaded Successfully!", Toast.LENGTH_SHORT).show()
    }
}