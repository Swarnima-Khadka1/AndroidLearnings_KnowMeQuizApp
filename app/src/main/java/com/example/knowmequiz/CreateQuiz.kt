package com.example.knowmequiz

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.knowmequiz.databinding.ActivityCreateQuizBinding
import androidx.appcompat.app.AlertDialog
import com.google.firebase.database.FirebaseDatabase

class CreateQuiz : AppCompatActivity() {

    lateinit var binder: ActivityCreateQuizBinding
    private lateinit var adminUserName: String
    private var currentQuestionIndex = 0
    private var selectedAnswerIndex= -1

    //Firebase
    private val database= FirebaseDatabase.getInstance()
    private val quizRef= database.getReference("quizzes")

    private val questions = listOf(
        Questions(
            questionText = "What drink do I like the most?",
            options = listOf("Coffee", "Tea", "Butter Milk", "Hot lemon water")
        ),
        Questions(
            questionText = "What type of music do I like best?",
            options = listOf("Taylor Swift", "Hip-hop and rap", "90s retro Bollywood music", "K-pop")
        ),
        Questions(
            questionText = "What's my biggest pet peeve?",
            options = listOf("People chewing Loudly", "Being Late", "Messy Spaces", "Slow walkers")
        ),
        Questions(
            questionText = "If I could have any superpower, what would it be?",
            options = listOf("Reading minds", "Invisibility", "Teleportation", "Flying")
        ),
        Questions(
            questionText = "What am I most likely to splurge on?",
            options = listOf("Clothes and fashion", "Tech gadgets", "Food and restaurants", "Travel experiences")
        ),
        Questions(
            questionText = "What's my dream vacation destination?",
            options = listOf("Spain", "Paris, France", "Switzerland", "Japan")
        ),
        Questions(
            questionText = "What's my go-to thing to watch?",
            options = listOf("Netflix series and dramas", "Bollywood movies", "Anime", "Reality TV shows")
        ),
        Questions(
            questionText = "How do I handle stress?",
            options = listOf("Talk it out with friends", "Sleep it off", "Work out or stay active", "Meditate")
        ),
        Questions(
            questionText = "What's my hidden talent?",
            options = listOf("Singing", "Cooking", "Drawing", "Still Hidden")
        ),
        Questions(
            questionText = "What's my biggest fear?",
            options = listOf("Heights", "Claustrophobic/enclosed spaces", "insects", "Being alone")
        )
    )

    private val answeredQuestion = mutableMapOf<String, Questions>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binder= ActivityCreateQuizBinding.inflate(layoutInflater)
        setContentView(binder.root)

        ViewCompat.setOnApplyWindowInsetsListener(binder.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        adminUserName= intent.getStringExtra(AdminUi.KEY).toString()

        showQuestion()

        binder.btnAnswer.setOnClickListener {
            showAnswerDialog()
        }
        binder.btnNext.setOnClickListener {
            onNextClicked()
        }

    }

    private fun showQuestion(){

        val question= questions[currentQuestionIndex]
       binder.tvQuestion.text= question.questionText
        binder.tvAnswer.text= "NO answer selected"
        selectedAnswerIndex= -1
    }

    private fun showAnswerDialog() {
        val question = questions[currentQuestionIndex]
        val optionArray = question.options.toTypedArray()
        val alert = AlertDialog.Builder(this)
        alert.setTitle("Select Answer")
        alert.setSingleChoiceItems(optionArray, selectedAnswerIndex) { dialog, which ->
            selectedAnswerIndex = which
        }
        alert.setPositiveButton("Submit") { dialog, which ->
            if (selectedAnswerIndex != -1) {
                binder.tvAnswer.text = "Selected Answer: ${optionArray[selectedAnswerIndex]}"
            }
            dialog.dismiss()
        }
        alert.setNegativeButton("Cancel", null)
        alert.show()
    }

    private fun onNextClicked(){
         if (selectedAnswerIndex== -1){
             Toast.makeText(this,"Please select an answer", Toast.LENGTH_SHORT).show()
             return
         }
        val originalQuestion = questions[currentQuestionIndex]
        val savedQuestion = Questions(originalQuestion.questionText, options = originalQuestion.options, correctIndex = selectedAnswerIndex)
        //answeredQuestion[adminUserName] = savedQuestion
        answeredQuestion["Q ${currentQuestionIndex+1}"]= savedQuestion
        currentQuestionIndex++
        
        if(currentQuestionIndex<questions.size){
            showQuestion()
        }else {
            saveQuizToFirebase()
        }
   

    }
    
    private fun saveQuizToFirebase() {
        val quizId = quizRef.push().key ?: return
        
        val quizData = mapOf("adminUserName" to adminUserName, "questions" to answeredQuestion)
        quizRef.child(quizId).setValue(quizData).addOnSuccessListener {
            Toast.makeText(this, "Quiz created successfully", Toast.LENGTH_SHORT).show()
            val idAlertBuilder = AlertDialog.Builder(this)
            idAlertBuilder.setTitle("Quiz Code")
            idAlertBuilder.setMessage("Quiz Code: \n\n $quizId \n\n Share this code with your friends !")
            idAlertBuilder.setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            idAlertBuilder.setNeutralButton("Copy Code", null)
            idAlertBuilder.setCancelable(false)

            val dialog = idAlertBuilder.create()
            dialog.show()

            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("quizId", quizId)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "Quiz ID copied to clipboard", Toast.LENGTH_SHORT).show()
            }

        }.addOnFailureListener {
            Toast.makeText(this, "Failed to save quiz", Toast.LENGTH_SHORT).show()
        }

    }
}