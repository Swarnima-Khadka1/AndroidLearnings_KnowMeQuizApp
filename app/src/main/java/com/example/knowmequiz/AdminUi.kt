package com.example.knowmequiz

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.knowmequiz.databinding.ActivityAdminUiBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AdminUi : AppCompatActivity() {

    lateinit var binder: ActivityAdminUiBinding
    lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binder = ActivityAdminUiBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binder.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binder.signUp.setOnClickListener {
            val userName = binder.etName.text.toString()
            val password = binder.etPassword.text.toString()

            if (userName.isEmpty()) {
                Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Stop the process
            }

            database = FirebaseDatabase.getInstance().getReference("Users")
            database.child(userName).get().addOnSuccessListener {
                if (it.exists()) {
                    Toast.makeText(this, "User already registered, please sign in.", Toast.LENGTH_SHORT).show()
                } else {
                    val user = User(userName, password)
                    database.child(userName).setValue(user).addOnSuccessListener {
                        Toast.makeText(this, "User registered successfully", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this, ViewScoresActivity::class.java).also {
                            it.putExtra(KEY, userName)
                        }
                        startActivity(intent)
                        finish()
                    }.addOnFailureListener {
                        Toast.makeText(this, "Failed to register user", Toast.LENGTH_SHORT).show()
                    }
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Database error", Toast.LENGTH_SHORT).show()
            }
        }

        binder.signIn.setOnClickListener {
            val userName = binder.etName.text.toString()
            val password = binder.etPassword.text.toString()
            if (userName.isNotEmpty() && password.isNotEmpty()) {
                readData(userName, password)
            } else {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun readData(userName: String, password: String) {
        database = FirebaseDatabase.getInstance().getReference("Users")
        database.child(userName).get().addOnSuccessListener {
            if (it.exists()) {
                val correctPass = it.child("password").value.toString()
                if (correctPass == password) {
                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, ViewScoresActivity::class.java).also {
                        it.putExtra(KEY, userName)
                    }
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Wrong Password", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "User doesn't exist. Please sign up first.", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Database error", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val KEY = "com.example.knowmequiz.AdminUi.KEY"
    }
}
