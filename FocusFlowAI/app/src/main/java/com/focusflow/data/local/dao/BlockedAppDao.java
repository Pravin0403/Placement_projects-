package com.focusflow.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.focusflow.data.local.entity.BlockedAppEntity;

import java.util.List;

@Dao
public interface BlockedAppDao {

    @Insert
    long insert(BlockedAppEntity entity);

    @Query("DELETE FROM blocked_apps WHERE packageName = :packageName")
    void delete(String packageName);

    @Query("SELECT * FROM blocked_apps ORDER BY addedAtEpochMs DESC")
    LiveData<List<BlockedAppEntity>> observeAll();

    @Query("SELECT * FROM blocked_apps WHERE packageName = :packageName LIMIT 1")
    BlockedAppEntity findByPackageName(String packageName);

    @Query("SELECT EXISTS(SELECT 1 FROM blocked_apps WHERE packageName = :packageName)")
    boolean isBlocked(String packageName);
}