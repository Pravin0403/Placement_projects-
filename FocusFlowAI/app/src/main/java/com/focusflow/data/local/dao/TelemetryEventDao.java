package com.focusflow.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.focusflow.data.local.entity.TelemetryEventEntity;

import java.util.List;

@Dao
public interface TelemetryEventDao {

    @Insert
    long insert(TelemetryEventEntity entity);

    @Query("SELECT * FROM telemetry_events WHERE sessionId = :sessionId AND timestampEpochMs BETWEEN :fromInclusive AND :toExclusive")
    List<TelemetryEventEntity> eventsInRange(long sessionId, long fromInclusive, long toExclusive);

    @Query("SELECT * FROM telemetry_events WHERE synced = 0 ORDER BY timestampEpochMs ASC LIMIT :limit")
    List<TelemetryEventEntity> unsynced(int limit);

    @Query("UPDATE telemetry_events SET synced = 1 WHERE eventId IN (:eventIds)")
    void markSynced(List<String> eventIds);

    @Query("SELECT COUNT(*) FROM telemetry_events WHERE sessionId = :sessionId")
    LiveData<Integer> observeEventCount(long sessionId);

    @Query("SELECT COUNT(*) FROM telemetry_events WHERE sessionId = :sessionId")
    int countForSession(long sessionId);

    @Query("SELECT COUNT(*) FROM telemetry_events WHERE sessionId = :sessionId AND type = :type AND timestampEpochMs > :afterEpochMs")
    int countTypeAfter(long sessionId, String type, long afterEpochMs);
}
