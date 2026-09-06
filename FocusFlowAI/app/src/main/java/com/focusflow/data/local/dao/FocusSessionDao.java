package com.focusflow.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.focusflow.data.local.entity.FocusSessionEntity;

import java.util.List;

@Dao
public interface FocusSessionDao {

    @Query("SELECT * FROM focus_sessions ORDER BY startedAtEpochMs DESC")
    LiveData<List<FocusSessionEntity>> observeAll();

    @Query("SELECT * FROM focus_sessions WHERE endedAtEpochMs IS NULL ORDER BY startedAtEpochMs DESC LIMIT 1")
    LiveData<FocusSessionEntity> observeActive();

    @Query("SELECT * FROM focus_sessions WHERE endedAtEpochMs IS NULL ORDER BY startedAtEpochMs DESC LIMIT 1")
    FocusSessionEntity getActive();

    @Query("SELECT * FROM focus_sessions ORDER BY startedAtEpochMs DESC LIMIT 1")
    FocusSessionEntity getLatest();

    @Query("SELECT * FROM focus_sessions ORDER BY startedAtEpochMs DESC LIMIT :limit")
    List<FocusSessionEntity> recent(int limit);

    @Insert
    long insert(FocusSessionEntity entity);

    @Update
    void update(FocusSessionEntity entity);
}
