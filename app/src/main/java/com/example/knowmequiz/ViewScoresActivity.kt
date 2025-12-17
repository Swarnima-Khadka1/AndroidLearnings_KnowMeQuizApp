package com.example.knowmequiz

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.example.knowmequiz.databinding.ActivityViewScoresBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ViewScoresActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewScoresBinding
    private lateinit var adminUserName: String
    private val quizzesRef = FirebaseDatabase.getInstance().getReference("quizzes")

    companion object {
        const val CHANNEL_ID = "new_score_notification_channel"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewScoresBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adminUserName = intent.getStringExtra(AdminUi.KEY).toString()

        createNotificationChannel()
        setupListeners()

        binding.btnCreateQuiz.setOnClickListener {
            val intent = Intent(this, CreateQuiz::class.java).also {
                it.putExtra(AdminUi.KEY, adminUserName)
            }
            startActivity(intent)
        }
    }

    private fun setupListeners() {
        quizzesRef.orderByChild("adminUserName").equalTo(adminUserName)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val scoresText = StringBuilder()
                    for (quizSnapshot in snapshot.children) {
                        val quizId = quizSnapshot.key
                        scoresText.append("Quiz ID: $quizId\n")
                        val scoresSnapshot = quizSnapshot.child("scores")
                        for (playerSnapshot in scoresSnapshot.children) {
                            val playerName = playerSnapshot.key
                            val score = playerSnapshot.getValue(Int::class.java)
                            scoresText.append("  - $playerName: $score\n")
                            showNotification(playerName ?: "A player", score ?: 0)
                        }
                        scoresText.append("\n")
                    }
                    binding.tvScores.text = scoresText.toString()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "New Score"
            val descriptionText = "Notifications for new quiz scores"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(playerName: String, score: Int) {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("New Score!")
            .setContentText("$playerName just scored $score in your quiz!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(playerName.hashCode(), builder.build())
    }
}