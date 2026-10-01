package com.example.educloud.ui.screens.home

import com.example.educloud.content.Grade3MathContent
import com.example.educloud.data.model.Interaction
import com.example.educloud.sync.HOMEWORK_SKILL_ID

enum class PathNodeState { Completed, InProgress, Locked }

enum class PathKind { Quiz, Tutor }

data class PathItem(
    val id: String,
    val title: String,
    val state: PathNodeState,
    val kind: PathKind,
    val tutorPrompt: String = "",
)

/**
 * Featured Grade 3 path: today's regrouping check plus the four reviewed rule cards.
 * Later catalogue topics stay off this chart so it never pretends Geometry is in the pack.
 */
object Grade3LearningPath {
    const val REGROUPING_ID = HOMEWORK_SKILL_ID

    fun from(interactions: List<Interaction>, lessonsPassed: Int): List<PathItem> {
        val regroupingDone = lessonsPassed > 0 ||
            interactions.any { it.strand == REGROUPING_ID && it.isCorrect == true }
        val skeleton = buildList {
            add(
                PathItem(
                    id = REGROUPING_ID,
                    title = "Two-digit regrouping",
                    state = PathNodeState.Locked,
                    kind = PathKind.Quiz,
                ),
            )
            Grade3MathContent.ruleCards.forEach { card ->
                add(
                    PathItem(
                        id = card.lesson.id,
                        title = card.lesson.topic,
                        state = PathNodeState.Locked,
                        kind = PathKind.Tutor,
                        tutorPrompt = card.lesson.practicePrompt
                            ?: "Help me with ${card.lesson.topic}",
                    ),
                )
            }
        }
        return assignStates(skeleton, interactions, regroupingDone)
    }

    fun nextOpenTitle(items: List<PathItem>): String? =
        items.firstOrNull { it.state == PathNodeState.InProgress }?.title
            ?: items.lastOrNull { it.state == PathNodeState.Completed }?.title

    internal fun assignStates(
        items: List<PathItem>,
        interactions: List<Interaction>,
        regroupingDone: Boolean,
    ): List<PathItem> {
        val completedIds = buildSet {
            if (regroupingDone) add(REGROUPING_ID)
            items.filter { it.kind == PathKind.Tutor }.forEach { item ->
                if (interactions.any { interaction -> mentions(interaction, item.id, item.title) }) {
                    add(item.id)
                }
            }
        }
        var offeredCurrent = false
        return items.map { item ->
            when {
                item.id in completedIds -> item.copy(state = PathNodeState.Completed)
                !offeredCurrent -> {
                    offeredCurrent = true
                    item.copy(state = PathNodeState.InProgress)
                }
                else -> item.copy(state = PathNodeState.Locked)
            }
        }
    }

    private fun mentions(interaction: Interaction, lessonId: String, topic: String): Boolean {
        val sources = interaction.ragSources.orEmpty()
        if (sources.contains(lessonId)) return true
        return interaction.question.contains(topic, ignoreCase = true)
    }
}
