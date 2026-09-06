package com.focusflow.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.focusflow.data.local.entity.FocusZoneEntity;

import java.util.List;

@Dao
public interface FocusZoneDao {

    @Insert
    long insert(FocusZoneEntity entity);

    @Query("SELECT * FROM focus_zones ORDER BY calculatedAtEpochMs DESC LIMIT 1")
    LiveData<FocusZoneEntity> observeLatest();

    @Query("SELECT * FROM focus_zones WHERE dayOfWeek = :dayOfWeek ORDER BY hourOfDay")
    LiveData<List<FocusZoneEntity>> observeByDayOfWeek(int dayOfWeek);

    @Query("SELECT * FROM focus_zones WHERE isRecommended = 1 ORDER BY focusScore DESC")
    LiveData<List<FocusZoneEntity>> observeRecommendedZones();

    @Query("DELETE FROM focus_zones")
    void clearAll();
}