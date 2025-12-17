package com.example.knowmequiz

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Button
import android.content.Intent



class MainActivity : AppCompatActivity() {




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
       val admin= findViewById<Button>(R.id.admin)
        val player= findViewById<Button>(R.id.player)
        admin.setOnClickListener {
            val intent = Intent(this, AdminUi::class.java)
            startActivity(intent)
        }

      player.setOnClickListener {
          val intent= Intent(this, PlayerUi::class.java)
          startActivity(intent)
      }
    }
}