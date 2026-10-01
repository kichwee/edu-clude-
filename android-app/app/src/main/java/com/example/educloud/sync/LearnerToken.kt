package com.example.educloud.sync

import android.content.Context
import java.util.UUID

/**
 * The pseudonymous identifier that crosses the school-to-home bridge: a random v4 UUID minted
 * once per install and reused so distinct-learner counts stay correct. It is never derived from
 * a name, an admission number, a phone number, or any other human-meaningful string.
 */
object LearnerToken {
    private const val PREFS_NAME = "device"
    private const val KEY_LEARNER_TOKEN = "learner_token"

    fun current(context: Context): String {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val stored = prefs.getString(KEY_LEARNER_TOKEN, null)
        val token = resolve(stored)
        if (token != stored) {
            prefs.edit().putString(KEY_LEARNER_TOKEN, token).apply()
        }
        return token
    }

    fun resolve(stored: String?): String =
        stored?.let { HomeworkService.uuidOrNull(it) } ?: UUID.randomUUID().toString()
}
