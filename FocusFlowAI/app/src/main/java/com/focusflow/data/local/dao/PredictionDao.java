package com.focusflow.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.focusflow.data.local.entity.PredictionEntity;

import java.util.List;

@Dao
public interface PredictionDao {

    @Insert
    long insert(PredictionEntity entity);

    @Query("SELECT * FROM predictions ORDER BY createdAtEpochMs DESC LIMIT 1")
    PredictionEntity latest();

    @Query("SELECT * FROM predictions ORDER BY createdAtEpochMs DESC LIMIT 1")
    LiveData<PredictionEntity> observeLatest();

    @Query("SELECT * FROM predictions ORDER BY createdAtEpochMs DESC LIMIT :limit")
    LiveData<List<PredictionEntity>> observeRecent(int limit);

    @Query("SELECT * FROM predictions WHERE synced = 0 ORDER BY createdAtEpochMs ASC LIMIT :limit")
    List<PredictionEntity> unsynced(int limit);

    @Query("UPDATE predictions SET synced = 1 WHERE predictionId IN (:ids)")
    void markSynced(List<String> ids);
}
