package com.app.runaddictedtrack.data.local

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "FitTracker.db"
        private const val DATABASE_VERSION = 1

        // Definizione delle tabelle
        const val TABLE_USER = "User"
        const val COLUMN_USER_ID = "id"
        const val COLUMN_USER_EMAIL = "email"
        const val COLUMN_USER_PASSWORD = "password"
        const val COLUMN_USER_NAME = "name"
        const val COLUMN_USER_REG_DATE = "registrationDate"

        const val TABLE_ACTIVITY = "Activity"
        const val COLUMN_ACTIVITY_ID = "id"
        const val COLUMN_ACTIVITY_USER_ID = "userId"
        const val COLUMN_ACTIVITY_START = "startTime"
        const val COLUMN_ACTIVITY_END = "endTime"
        const val COLUMN_ACTIVITY_DISTANCE = "distance"
        const val COLUMN_ACTIVITY_STEPS = "steps"
        const val COLUMN_ACTIVITY_COORDS = "coordsJson"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Qui andremo a creare le tabelle con i comandi SQL
        val createUserTable = """
            CREATE TABLE $TABLE_USER (
                $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_EMAIL TEXT NOT NULL UNIQUE,
                $COLUMN_USER_PASSWORD TEXT NOT NULL,
                $COLUMN_USER_NAME TEXT,
                $COLUMN_USER_REG_DATE DATETIME DEFAULT CURRENT_TIMESTAMP
            );
        """.trimIndent()

        val createActivityTable = """
            CREATE TABLE $TABLE_ACTIVITY (
                $COLUMN_ACTIVITY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_ACTIVITY_USER_ID INTEGER NOT NULL,
                $COLUMN_ACTIVITY_START DATETIME,
                $COLUMN_ACTIVITY_END DATETIME,
                $COLUMN_ACTIVITY_DISTANCE REAL,
                $COLUMN_ACTIVITY_STEPS INTEGER,
                $COLUMN_ACTIVITY_COORDS TEXT,
                FOREIGN KEY($COLUMN_ACTIVITY_USER_ID) REFERENCES $TABLE_USER($COLUMN_USER_ID)
            );
        """.trimIndent()

        db.execSQL(createUserTable)
        db.execSQL(createActivityTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // In caso di upgrade del DB (non necessario ora, ma per future versioni)
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ACTIVITY")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USER")
        onCreate(db)
    }

    fun addUser(email: String, password: String, name: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_EMAIL, email)
            put(COLUMN_USER_PASSWORD, password)
            put(COLUMN_USER_NAME, name)
        }
        return db.insert(TABLE_USER, null, values)
    }

    fun validateUser(email: String, password: String): Int {
        val db = readableDatabase
        val projection = arrayOf(COLUMN_USER_ID)
        val selection = "$COLUMN_USER_EMAIL = ? AND $COLUMN_USER_PASSWORD = ?"
        val selectionArgs = arrayOf(email, password)
        db.query(TABLE_USER, projection, selection, selectionArgs, null, null, null)
            .use { cursor ->
                return if (cursor.moveToFirst()) {
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID))
                } else {
                    -1
                }
            }
        }

    fun insertActivity(userId: Int, startTime: String, endTime: String, distance: Double, steps: Int, coordsJson: String): Long {
        val db = writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_ACTIVITY_USER_ID, userId)
            put(COLUMN_ACTIVITY_START, startTime)
            put(COLUMN_ACTIVITY_END, endTime)
            put(COLUMN_ACTIVITY_DISTANCE, distance)
            put(COLUMN_ACTIVITY_STEPS, steps)
            put(COLUMN_ACTIVITY_COORDS, coordsJson)
        }
        return db.insert(TABLE_ACTIVITY, null, values)
    }

   fun getActivitiesForUser(userId: Int): Cursor {
        val db = readableDatabase
        return db.query(
            TABLE_ACTIVITY,
            null,
            "$COLUMN_ACTIVITY_USER_ID = ?",
            arrayOf(userId.toString()),
            null,
            null,
            "$COLUMN_ACTIVITY_START DESC"
        )
    }

    fun getActivityById(activityId: Int): Cursor {
        val db = readableDatabase
        return db.query(
            TABLE_ACTIVITY,
            null,
            "$COLUMN_ACTIVITY_ID = ?",
            arrayOf(activityId.toString()),
            null,
            null,
            null)
    }
}
