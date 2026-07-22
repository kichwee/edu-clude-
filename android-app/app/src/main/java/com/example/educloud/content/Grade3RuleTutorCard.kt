package com.example.educloud.content

/** A generated Grade 3 lesson used by both the Android tutor and USSD simulator. */
internal data class Grade3RuleTutorCard(
    val lesson: ContentLesson,
    val hint: String,
    val question: String,
    val options: List<String>,
    val correctOption: String,
    val correction: String,
)
