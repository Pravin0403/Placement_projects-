package com.focusflow.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.focusflow.data.local.entity.ViolationEntity;

import java.util.List;

@Dao
public interface ViolationDao {

    @Insert
    long insert(ViolationEntity entity);

    @Query("SELECT * FROM violations WHERE sessionId = :sessionId ORDER BY timestampEpochMs DESC")
    LiveData<List<ViolationEntity>> observeBySession(long sessionId);

    @Query("SELECT * FROM violations ORDER BY timestampEpochMs DESC LIMIT :limit")
    LiveData<List<ViolationEntity>> observeRecent(int limit);

    @Query("SELECT COUNT(*) FROM violations WHERE sessionId = :sessionId")
    int countForSession(long sessionId);
}