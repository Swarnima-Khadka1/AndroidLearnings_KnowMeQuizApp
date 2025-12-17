package com.example.knowmequiz

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.knowmequiz.databinding.ActivityPlayerUiBinding
import com.google.firebase.database.FirebaseDatabase
import android.widget.Toast
import android.content.Intent





class PlayerUi : AppCompatActivity() {

    lateinit var binder: ActivityPlayerUiBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binder= ActivityPlayerUiBinding.inflate(layoutInflater)
        setContentView(binder.root)

        ViewCompat.setOnApplyWindowInsetsListener(binder.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binder.btnPlay.setOnClickListener {
            val name= binder.etName.text.toString()
            val code= binder.etCode.text.toString()
            val database= FirebaseDatabase.getInstance()
            val quizRef= database.getReference("quizzes")

            if(name.isEmpty() || code.isEmpty()){
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            quizRef.child(code).get().addOnSuccessListener { snapshot ->

                if(snapshot.exists()){
                    val intent = Intent(this, PlayQuiz::class.java)
                    intent.putExtra("name", name)
                    intent.putExtra("code", code)
                    startActivity(intent)
                    finish()

                } else {
                    Toast.makeText(this, "Invalid Quiz Code", Toast.LENGTH_SHORT).show()
                }


                }.addOnFailureListener {
                    Toast.makeText(this, "Database error", Toast.LENGTH_SHORT).show()
            }

        }
    }
}