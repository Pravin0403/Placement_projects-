package com.focusflow.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.focusflow.data.local.entity.EmergencyContactEntity;

import java.util.List;

@Dao
public interface EmergencyContactDao {

    @Insert
    long insert(EmergencyContactEntity entity);

    @Update
    void update(EmergencyContactEntity entity);

    @Query("DELETE FROM emergency_contacts WHERE id = :id")
    void delete(long id);

    @Query("SELECT * FROM emergency_contacts ORDER BY isPriority DESC, name ASC")
    LiveData<List<EmergencyContactEntity>> observeAll();

    @Query("SELECT * FROM emergency_contacts WHERE isPriority = 1")
    LiveData<List<EmergencyContactEntity>> observePriorityContacts();
}