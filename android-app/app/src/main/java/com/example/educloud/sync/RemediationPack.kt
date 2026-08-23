package com.example.educloud.sync

import com.example.educloud.content.ContentLesson
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** The strictly versioned JSON patch returned by the autonomous teaching loop. */
@Serializable
data class RemediationPack(
    @SerialName("schema_version") val schemaVersion: String,
    @SerialName("pack_id") val packId: String,
    @SerialName("content_version") val contentVersion: String,
    val label: String,
    @SerialName("target_skill") val targetSkill: String,
    @SerialName("validation_status") val validationStatus: String,
    val provenance: String,
    val lessons: List<RemediationLesson>,
)

@Serializable
data class RemediationLesson(
    val id: String,
    val topic: String,
    val source: String,
    val keywords: List<String>,
    @SerialName("micro_lesson") val microLesson: String,
    @SerialName("teaching_steps") val teachingSteps: List<String>,
    val definition: String,
    @SerialName("practice_questions") val practiceQuestions: List<RemediationPracticeQuestion>,
)

@Serializable
data class RemediationPracticeQuestion(
    val minuend: Int,
    val subtrahend: Int,
    val answer: Int,
)

/** Reject malformed, out-of-scope, or mathematically invalid downloaded content. */
fun RemediationPack.isSafeOfflinePatch(): Boolean =
    schemaVersion == "1" &&
        targetSkill == "two_digit_subtraction_regrouping" &&
        validationStatus == "automatic_validation_passed" &&
        provenance.contains("automatically validated", ignoreCase = true) &&
        lessons.size == 1 &&
        lessons.all { lesson ->
            lesson.id.isNotBlank() &&
                lesson.source.contains("automatic checks passed", ignoreCase = true) &&
                lesson.teachingSteps.size == 3 &&
                lesson.practiceQuestions.size == 3 &&
                lesson.practiceQuestions.all { question ->
                    question.minuend in 10..99 &&
                        question.subtrahend in 1..98 &&
                        question.subtrahend < question.minuend &&
                        question.answer == question.minuend - question.subtrahend
                }
        }

/** Converts a downloaded patch into the same deterministic lesson type as bundled content. */
fun RemediationLesson.toContentLesson(contentVersion: String): ContentLesson {
    val firstPractice = practiceQuestions.first()
    val practiceText = practiceQuestions.joinToString(separator = "\n") { question ->
        "${question.minuend} - ${question.subtrahend} = ${question.answer}"
    }
    return ContentLesson(
        id = id,
        topic = topic,
        keywords = (keywords + "regroup" + "subtract").toSet(),
        source = source,
        version = contentVersion,
        passage = "$microLesson\nPractice:\n$practiceText",
        teachingSteps = teachingSteps,
        definition = definition,
        verifiedAnswer = "${firstPractice.minuend} - ${firstPractice.subtrahend} = ${firstPractice.answer}",
        verifiedAnswerExplanation = "Use the lesson steps to check the subtraction.",
        verificationTokens = setOf(firstPractice.minuend.toString(), firstPractice.subtrahend.toString()),
        practicePrompt = "Try ${firstPractice.minuend} - ${firstPractice.subtrahend}.",
    )
}
