package com.focusflow.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.focusflow.data.local.entity.FocusSnapshotEntity;

import java.util.List;

@Dao
public interface FocusSnapshotDao {

    @Query("SELECT * FROM focus_snapshots ORDER BY createdAtEpochMs DESC LIMIT 1")
    LiveData<FocusSnapshotEntity> observeLatest();

    @Query("SELECT * FROM focus_snapshots ORDER BY createdAtEpochMs DESC LIMIT :limit")
    LiveData<List<FocusSnapshotEntity>> observeRecent(int limit);

    @Insert
    long insert(FocusSnapshotEntity entity);
}
