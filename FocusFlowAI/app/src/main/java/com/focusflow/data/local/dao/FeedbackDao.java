package com.focusflow.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.focusflow.data.local.entity.FeedbackEntity;

@Dao
public interface FeedbackDao {

    @Insert
    long insert(FeedbackEntity entity);

    @Query("SELECT COUNT(*) FROM feedback WHERE helpful = 1")
    int helpfulCount();

    @Query("SELECT COUNT(*) FROM feedback WHERE helpful = 0")
    int unhelpfulCount();

    @Query("SELECT * FROM feedback ORDER BY createdAtEpochMs DESC LIMIT :limit")
    java.util.List<FeedbackEntity> recent(int limit);

    @Query("SELECT * FROM feedback WHERE synced = 0 ORDER BY createdAtEpochMs ASC LIMIT :limit")
    java.util.List<FeedbackEntity> unsynced(int limit);

    @Query("UPDATE feedback SET synced = 1 WHERE id IN (:ids)")
    void markSynced(java.util.List<Long> ids);
}
