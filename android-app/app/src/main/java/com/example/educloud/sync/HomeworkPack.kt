package com.example.educloud.sync

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import java.util.UUID

const val DEMO_HOMEWORK_CLASS_CODE = "G3-HOME"
const val HOMEWORK_SKILL_ID = "two_digit_subtraction_regrouping"
const val HOMEWORK_TUTOR_PROMPT = "Help me regroup when I subtract 45 minus 29"
const val MAX_HOMEWORK_PACK_BYTES = 2048

val HOMEWORK_ITEM_IDS = listOf(
    "g3-regroup-45-29",
    "g3-regroup-82-37",
    "g3-regroup-63-28",
)

private val FORBIDDEN_PACK_KEYS = setOf(
    "name", "names", "alias", "email", "phone", "photo", "photos",
    "face", "faces", "emotion", "emotions", "biometric", "national_id",
    "gps", "location", "roster", "student_name", "learner_name",
)

/** Bundled fixture so the home demo works without the Django host. */
val DEMO_HOMEWORK_PACK_JSON = """
{
  "schema_version": "1",
  "assignment_id": "11111111-1111-4111-8111-111111111111",
  "skill_id": "two_digit_subtraction_regrouping",
  "item_ids": ["g3-regroup-45-29", "g3-regroup-82-37", "g3-regroup-63-28"],
  "issued_at": "2026-08-25T00:00:00Z",
  "class_code": "G3-HOME",
  "label": "Tonight from class · Grade 3 regrouping (prototype)",
  "prototype": true,
  "not_athena_production": true,
  "not_facial_analysis": true
}
""".trimIndent()

@Serializable
data class HomeworkPack(
    @SerialName("schema_version") val schemaVersion: String,
    @SerialName("assignment_id") val assignmentId: String,
    @SerialName("skill_id") val skillId: String,
    @SerialName("item_ids") val itemIds: List<String>,
    @SerialName("issued_at") val issuedAt: String,
    @SerialName("class_code") val classCode: String,
    val label: String? = null,
)

data class HomeworkItemResult(
    val itemId: String,
    val correct: Boolean,
)

object TonightAssignment {
    @Volatile
    var current: HomeworkPack? = null
        private set

    fun replace(pack: HomeworkPack) {
        current = pack
    }

    fun clear() {
        current = null
    }
}

fun parseHomeworkPack(raw: String): Result<HomeworkPack> {
    if (raw.toByteArray(Charsets.UTF_8).size > MAX_HOMEWORK_PACK_BYTES) {
        return Result.failure(IllegalArgumentException("Homework pack is too large."))
    }
    return runCatching {
        val root = PACK_JSON.parseToJsonElement(raw)
        val obj = root as? JsonObject
            ?: throw IllegalArgumentException("Homework pack must be a JSON object.")
        val forbidden = obj.keys.map { it.lowercase() }.toSet().intersect(FORBIDDEN_PACK_KEYS)
        if (forbidden.isNotEmpty()) {
            throw IllegalArgumentException(
                "Homework packs cannot include personal or biometric fields: ${forbidden.sorted().joinToString()}.",
            )
        }
        val pack = PACK_JSON.decodeFromJsonElement(HomeworkPack.serializer(), obj)
        validateHomeworkPack(pack)
    }
}

fun demoHomeworkPack(): HomeworkPack = parseHomeworkPack(DEMO_HOMEWORK_PACK_JSON).getOrThrow()

fun normalizeClassCode(raw: String): String = raw.trim().uppercase()

private fun validateHomeworkPack(pack: HomeworkPack): HomeworkPack {
    if (pack.schemaVersion != "1") {
        throw IllegalArgumentException("Unsupported homework-pack schema_version.")
    }
    if (pack.skillId != HOMEWORK_SKILL_ID) {
        throw IllegalArgumentException("Unknown or unsupported skill_id.")
    }
    UUID.fromString(pack.assignmentId)
    if (pack.issuedAt.isBlank() || pack.issuedAt.length > 40) {
        throw IllegalArgumentException("issued_at must be a short ISO-8601 timestamp.")
    }
    val code = normalizeClassCode(pack.classCode)
    if (code.length !in 4..16 || code.any { it !in CLASS_CODE_CHARS }) {
        throw IllegalArgumentException("class_code must be a short token.")
    }
    if (pack.itemIds.isEmpty() || pack.itemIds.size > HOMEWORK_ITEM_IDS.size) {
        throw IllegalArgumentException("item_ids must be a non-empty supported list.")
    }
    if (pack.itemIds.toSet().size != pack.itemIds.size) {
        throw IllegalArgumentException("item_ids must be unique.")
    }
    if (pack.itemIds.any { it !in HOMEWORK_ITEM_IDS }) {
        throw IllegalArgumentException("Unknown or unsupported item_id.")
    }
    return pack.copy(classCode = code)
}

private const val CLASS_CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-"
private val PACK_JSON = Json { ignoreUnknownKeys = true }
