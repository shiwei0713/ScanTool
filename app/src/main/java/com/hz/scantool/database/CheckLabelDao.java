package com.hz.scantool.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CheckLabelDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CheckLabelEntity checkLabelEntity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertList(List<CheckLabelEntity> checkLabelEntity);

    @Query("delete from CheckLabelEntity ")
    void deleteLabel();

    @Query("select * from CheckLabelEntity")
    List<CheckLabelEntity> getAll();
}
