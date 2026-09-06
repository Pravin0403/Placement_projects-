package com.focusflow.data.local;

import android.content.Context;

import androidx.annotation.NonNull;

import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.Database;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.focusflow.core.common.Constants;
import com.focusflow.data.local.dao.BlockedAppDao;
import com.focusflow.data.local.dao.EmergencyContactDao;
import com.focusflow.data.local.dao.FeedbackDao;
import com.focusflow.data.local.dao.FocusSessionDao;
import com.focusflow.data.local.dao.FocusSnapshotDao;
import com.focusflow.data.local.dao.FocusZoneDao;
import com.focusflow.data.local.dao.PredictionDao;
import com.focusflow.data.local.dao.TelemetryEventDao;
import com.focusflow.data.local.dao.UserPreferencesDao;
import com.focusflow.data.local.dao.ViolationDao;
import com.focusflow.data.local.entity.BlockedAppEntity;
import com.focusflow.data.local.entity.EmergencyContactEntity;
import com.focusflow.data.local.entity.FeedbackEntity;
import com.focusflow.data.local.entity.FocusSessionEntity;
import com.focusflow.data.local.entity.FocusSnapshotEntity;
import com.focusflow.data.local.entity.FocusZoneEntity;
import com.focusflow.data.local.entity.PredictionEntity;
import com.focusflow.data.local.entity.TelemetryEventEntity;
import com.focusflow.data.local.entity.UserPreferencesEntity;
import com.focusflow.data.local.entity.ViolationEntity;

@Database(
        entities = {
                UserPreferencesEntity.class,
                FocusSessionEntity.class,
                FocusSnapshotEntity.class,
                TelemetryEventEntity.class,
                PredictionEntity.class,
                FeedbackEntity.class,
                FocusZoneEntity.class,
                EmergencyContactEntity.class,
                ViolationEntity.class,
                BlockedAppEntity.class
        },
        version = 4,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE feedback ADD COLUMN synced INTEGER NOT NULL DEFAULT 0");
        }
    };

    private static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS focus_zones (id INTEGER PRIMARY KEY AUTOINCREMENT, hourOfDay INTEGER NOT NULL, dayOfWeek INTEGER NOT NULL, focusScore INTEGER NOT NULL, distractionRisk REAL NOT NULL, confidence REAL NOT NULL, recommendation TEXT, isRecommended INTEGER NOT NULL, calculatedAtEpochMs INTEGER NOT NULL)");
            database.execSQL("CREATE TABLE IF NOT EXISTS emergency_contacts (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, contactType TEXT NOT NULL, contactValue TEXT NOT NULL, isPriority INTEGER NOT NULL, createdAtEpochMs INTEGER NOT NULL)");
            database.execSQL("CREATE TABLE IF NOT EXISTS violations (id INTEGER PRIMARY KEY AUTOINCREMENT, sessionId INTEGER NOT NULL, violationType TEXT NOT NULL, description TEXT NOT NULL, timestampEpochMs INTEGER NOT NULL, packageName TEXT, wasBlocked INTEGER NOT NULL)");
            database.execSQL("CREATE TABLE IF NOT EXISTS blocked_apps (id INTEGER PRIMARY KEY AUTOINCREMENT, packageName TEXT NOT NULL, appName TEXT, distractionLevel TEXT, addedAtEpochMs INTEGER NOT NULL)");
        }
    };

    private static volatile AppDatabase instance;

    public abstract UserPreferencesDao userPreferencesDao();

    public abstract FocusSessionDao focusSessionDao();

    public abstract FocusSnapshotDao focusSnapshotDao();

    public abstract TelemetryEventDao telemetryEventDao();

    public abstract PredictionDao predictionDao();

    public abstract FeedbackDao feedbackDao();

    public abstract FocusZoneDao focusZoneDao();

    public abstract EmergencyContactDao emergencyContactDao();

    public abstract ViolationDao violationDao();

    public abstract BlockedAppDao blockedAppDao();

    @NonNull
    public static AppDatabase getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            Constants.DATABASE_NAME
                    ).addMigrations(MIGRATION_2_3, MIGRATION_3_4).build();
                }
            }
        }
        return instance;
    }
}
