package com.example.knowmequiz

data class Questions(
    val questionText: String="",
    val options: List<String> = listOf(),
    val correctIndex: Int = -1
)
