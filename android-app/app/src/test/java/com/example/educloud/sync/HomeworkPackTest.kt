package com.example.educloud.sync

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class HomeworkPackTest {

    @After
    fun clearAssignment() {
        TonightAssignment.clear()
    }

    @Test
    fun demoFixtureParsesWithoutPiiAndMatchesTheGrade3Bank() {
        val pack = demoHomeworkPack()
        assertEquals(DEMO_HOMEWORK_CLASS_CODE, pack.classCode)
        assertEquals(HOMEWORK_SKILL_ID, pack.skillId)
        assertEquals(HOMEWORK_ITEM_IDS, pack.itemIds)
        assertTrue(pack.assignmentId.isNotBlank())
    }

    @Test
    fun unknownSkillAndOversizedBodiesAreRejected() {
        val unknown = parseHomeworkPack(
            """{"schema_version":"1","assignment_id":"11111111-1111-4111-8111-111111111111","skill_id":"grade7-algebra","item_ids":["g3-regroup-45-29"],"issued_at":"2026-08-25T00:00:00Z","class_code":"G3-HOME"}""",
        )
        val oversized = parseHomeworkPack("""{"schema_version":"1","padding":"${"x".repeat(3000)}"}""")
        assertTrue(unknown.exceptionOrNull()?.message?.contains("skill_id") == true)
        assertTrue(oversized.exceptionOrNull()?.message?.contains("too large") == true)
    }

    @Test
    fun nameAndFaceFieldsAreRejected() {
        val named = parseHomeworkPack(
            """{"schema_version":"1","assignment_id":"11111111-1111-4111-8111-111111111111","skill_id":"two_digit_subtraction_regrouping","item_ids":["g3-regroup-45-29"],"issued_at":"2026-08-25T00:00:00Z","class_code":"G3-HOME","name":"Jane"}""",
        )
        val face = parseHomeworkPack(
            """{"schema_version":"1","assignment_id":"11111111-1111-4111-8111-111111111111","skill_id":"two_digit_subtraction_regrouping","item_ids":["g3-regroup-45-29"],"issued_at":"2026-08-25T00:00:00Z","class_code":"G3-HOME","face":"base64"}""",
        )
        assertTrue(named.exceptionOrNull()?.message?.contains("personal or biometric") == true)
        assertTrue(face.exceptionOrNull()?.message?.contains("personal or biometric") == true)
    }

    @Test
    fun tonightAssignmentHoldsTheAcceptedPack() {
        val pack = demoHomeworkPack()
        TonightAssignment.replace(pack)
        assertEquals(pack.assignmentId, TonightAssignment.current?.assignmentId)
    }

    @Test
    fun aHumanMeaningfulStringIsNeverTurnedIntoALearnerToken() {
        for (raw in listOf("Jane Doe", "ADM/2026/0143", "+254700000000")) {
            assertNull(HomeworkService.uuidOrNull(raw))
            val minted = LearnerToken.resolve(raw)
            assertNotEquals(UUID.nameUUIDFromBytes(raw.toByteArray(Charsets.UTF_8)).toString(), minted)
            assertEquals(4, UUID.fromString(minted).version())
        }
    }

    @Test
    fun theMintedTokenIsStableAcrossReadsAndAnAlreadyValidUuidIsKept() {
        val first = LearnerToken.resolve(null)
        assertEquals(first, LearnerToken.resolve(first))

        val supplied = "c0a80101-7a7c-4e10-9e2a-f4f1ef2d6a01"
        assertEquals(supplied, LearnerToken.resolve(supplied))
    }
}
