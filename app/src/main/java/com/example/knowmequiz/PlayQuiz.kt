package com.example.knowmequiz

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.knowmequiz.databinding.ActivityPlayQuizBinding
import com.google.firebase.database.FirebaseDatabase


class PlayQuiz : AppCompatActivity() {

    lateinit var binder: ActivityPlayQuizBinding

    private var currentQuestionIndex= 0
    private var selectedAnswerIndex= -1

    private lateinit var playerName: String
    private lateinit var quizCode: String
    private val database= FirebaseDatabase.getInstance()
    private val quizRef= database.getReference("quizzes")

    private var questionList= mutableListOf<Questions>()
    private var playerAnswer= mutableMapOf<String, Int>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
         binder = ActivityPlayQuizBinding.inflate(layoutInflater)
        setContentView(binder.root)

        ViewCompat.setOnApplyWindowInsetsListener(binder.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        playerName= intent.getStringExtra("name").toString()
        quizCode= intent.getStringExtra("code").toString()

        fetchQuestions()

        binder.btnAnswer.setOnClickListener {
            showAnswerDialog()
        }
        binder.btnNext.setOnClickListener {
            onNextClicked()

        }
    }
    private fun fetchQuestions()
    {
       quizRef.child(quizCode).child("questions").get().addOnSuccessListener { snapshot ->

           if(snapshot.exists()){
               questionList.clear()
               for(questionSnapshot in snapshot.children) {
                   val question= questionSnapshot.getValue(Questions::class.java)
                   question?.let {
                       questionList.add(it)

               }
               }
               showQuestion()

           }else{
               Toast.makeText(this, "No questions found", Toast.LENGTH_SHORT).show()
          finish()
           }
       }.addOnFailureListener {
           Toast.makeText(this, "Error fetching Quiz", Toast.LENGTH_SHORT)
           }
       }
    private fun showQuestion(){
        if(currentQuestionIndex< questionList.size){
            val question= questionList[currentQuestionIndex]
            binder.tvQuestion.text= question.questionText
            binder.tvAnswer.text= "NO answer selected"
            selectedAnswerIndex= -1

        }
    }

    private fun showAnswerDialog()
    {
        val question = questionList[currentQuestionIndex]
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
    private fun onNextClicked()
    {
        if (selectedAnswerIndex== -1){
            Toast.makeText(this,"Please select an answer", Toast.LENGTH_SHORT).show()
            return
        }

        playerAnswer["Q${currentQuestionIndex+1}"]= selectedAnswerIndex
        currentQuestionIndex++

        if(currentQuestionIndex<questionList.size){
            showQuestion()
        }else {
            calculateScore()
        }
    }

    private fun calculateScore(){
        var score= 0
       for(i in questionList.indices){
           val correct= questionList[i].correctIndex
           val player= playerAnswer["Q${i+1}"]
           if(player==correct) score++
       }

        showResultDialog(score, questionList.size)
    }

    private fun showResultDialog(score: Int, total:Int)
    {
        // Store the score in Firebase
        quizRef.child(quizCode).child("scores").child(playerName).setValue(score)

        val message= when {

            score==total -> "Congratulations!🎉🎉 You got $score out of $total, You really know them well"
            score >=   total/2 -> "You got $score out of $total (Hmm 🤔, You know them a bit)"
            else -> "Oops ! You got $score out of $total (You should get closer 😑)"

        }
        AlertDialog.Builder(this).setTitle("Your Score: $score/$total")
            .setMessage(message)
            .setPositiveButton("OK"){_,_->
                finish()
            }.setCancelable(false).show()
    }
}