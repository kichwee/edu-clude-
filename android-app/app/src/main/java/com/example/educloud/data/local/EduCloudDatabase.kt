package com.example.educloud.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.educloud.data.model.Interaction
import com.example.educloud.data.model.Streak
import com.example.educloud.data.model.Student

/**
 * EduCloud Room database.
 * Single source of truth for all offline student data.
 * Track A: 100% offline — all data lives here first; SyncWorker uploads when WiFi available.
 */
@Database(
    entities = [Student::class, Interaction::class, Streak::class, LessonSearchEntity::class, ContentPackStateEntity::class, RemediationLessonEntity::class],
    version = 5,
    exportSchema = true
)
abstract class EduCloudDatabase : RoomDatabase() {

    abstract fun studentDao(): StudentDao
    abstract fun interactionDao(): InteractionDao
    abstract fun streakDao(): StreakDao
    abstract fun lessonSearchDao(): LessonSearchDao
    abstract fun contentPackStateDao(): ContentPackStateDao
    abstract fun remediationLessonDao(): RemediationLessonDao

    companion object {
        const val DATABASE_NAME = "educloud.db"

        @Volatile
        private var INSTANCE: EduCloudDatabase? = null

        fun getDatabase(context: Context): EduCloudDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EduCloudDatabase::class.java,
                    DATABASE_NAME
                )
                    // Learner aliases and learning history are sensitive and must never be
                    // silently erased merely because an upgrade needs a migration.
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        internal val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE VIRTUAL TABLE IF NOT EXISTS `lesson_search` USING FTS4(" +
                        "`lesson_id` TEXT NOT NULL, `subject` TEXT NOT NULL, `grade` TEXT NOT NULL, " +
                        "`topic` TEXT NOT NULL, `searchable_text` TEXT NOT NULL)"
                )
            }
        }

        internal val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `content_pack_state` (`id` INTEGER NOT NULL, `version` TEXT NOT NULL, PRIMARY KEY(`id`))"
                )
            }
        }

        internal val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `interactions` ADD COLUMN `remediationSyncedAt` INTEGER")
                db.execSQL("ALTER TABLE `interactions` ADD COLUMN `learnerAnswer` TEXT")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `remediation_lessons` (" +
                        "`lessonId` TEXT NOT NULL, `studentId` INTEGER NOT NULL, `packId` TEXT NOT NULL, " +
                        "`contentVersion` TEXT NOT NULL, `reviewStatus` TEXT NOT NULL, `topic` TEXT NOT NULL, " +
                        "`source` TEXT NOT NULL, `keywordsJson` TEXT NOT NULL, `microLesson` TEXT NOT NULL, " +
                        "`teachingStepsJson` TEXT NOT NULL, `definition` TEXT NOT NULL, `practiceQuestionsJson` TEXT NOT NULL, " +
                        "PRIMARY KEY(`lessonId`))"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_remediation_lessons_studentId` ON `remediation_lessons` (`studentId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_remediation_lessons_packId` ON `remediation_lessons` (`packId`)")
            }
        }

        /** Replaces the learner-blocking review label with an automatic validation result. */
        internal val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `remediation_lessons_new` (" +
                        "`lessonId` TEXT NOT NULL, `studentId` INTEGER NOT NULL, `packId` TEXT NOT NULL, " +
                        "`contentVersion` TEXT NOT NULL, `validationStatus` TEXT NOT NULL, `topic` TEXT NOT NULL, " +
                        "`source` TEXT NOT NULL, `keywordsJson` TEXT NOT NULL, `microLesson` TEXT NOT NULL, " +
                        "`teachingStepsJson` TEXT NOT NULL, `definition` TEXT NOT NULL, `practiceQuestionsJson` TEXT NOT NULL, " +
                        "PRIMARY KEY(`lessonId`))"
                )
                db.execSQL(
                    "INSERT INTO `remediation_lessons_new` " +
                        "(`lessonId`, `studentId`, `packId`, `contentVersion`, `validationStatus`, `topic`, `source`, " +
                        "`keywordsJson`, `microLesson`, `teachingStepsJson`, `definition`, `practiceQuestionsJson`) " +
                        "SELECT `lessonId`, `studentId`, `packId`, `contentVersion`, 'automatic_validation_passed', " +
                        "`topic`, `source`, `keywordsJson`, `microLesson`, `teachingStepsJson`, `definition`, " +
                        "`practiceQuestionsJson` FROM `remediation_lessons`"
                )
                db.execSQL("DROP TABLE `remediation_lessons`")
                db.execSQL("ALTER TABLE `remediation_lessons_new` RENAME TO `remediation_lessons`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_remediation_lessons_studentId` ON `remediation_lessons` (`studentId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_remediation_lessons_packId` ON `remediation_lessons` (`packId`)")
            }
        }
    }
}
