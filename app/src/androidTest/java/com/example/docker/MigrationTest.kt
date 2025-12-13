package com.example.docker

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.docker.data.AppDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class MigrationTest {
    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrate1To2() {
        // Create V1 database manually.
        // Since we don't have V1 schema file, we create the DB with version 1
        // and manually execute SQL to match V1 schema.
        // Note: createDatabase usually validates schema if JSON is present.
        // If 1.json is missing, it might just create it.
        // But to be safe and simulate V1, we'll execute SQL.

        val db = helper.createDatabase(TEST_DB, 1)

        // We need to ensure the table exists with V1 schema.
        // If createDatabase created it based on current entity (which is V2), that's wrong.
        // But createDatabase uses the schema file. If 1.json is missing, what does it do?
        // It probably creates an empty DB with version 1.
        // Let's check if table exists.
        // Actually, if 1.json is missing, it cannot create the tables for us.
        // So we must create them.

        db.execSQL("CREATE TABLE IF NOT EXISTS `service_templates` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`image` TEXT NOT NULL, " +
                "`ports` TEXT NOT NULL, " +
                "`volumes` TEXT NOT NULL, " +
                "`env_vars` TEXT NOT NULL, " +
                "`created_at` INTEGER NOT NULL)")

        // Insert data
        db.execSQL("INSERT INTO service_templates (name, image, ports, volumes, env_vars, created_at) VALUES ('nginx', 'nginx:latest', '80:80', '', '{}', 123456789)")
        db.close()

        // Run migration and validate against V2 schema (which we just generated)
        val db2 = helper.runMigrationsAndValidate(TEST_DB, 2, true, AppDatabase.MIGRATION_1_2)

        // Verify data
        val cursor = db2.query("SELECT * FROM service_templates WHERE name = 'nginx'")
        assertTrue("Row should exist", cursor.moveToFirst())

        val restartPolicyIndex = cursor.getColumnIndex("restart_policy")
        assertTrue("Column restart_policy should exist", restartPolicyIndex != -1)

        val restartPolicy = cursor.getString(restartPolicyIndex)
        assertEquals("Default value should be 'no'", "no", restartPolicy)

        cursor.close()
    }
}

