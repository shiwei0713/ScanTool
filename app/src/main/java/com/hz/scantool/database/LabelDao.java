package com.hz.scantool.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface LabelDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(LabelEntity labelEntity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertList(List<LabelEntity> labelEntityList);

    @Query("delete from LabelEntity ")
    void deleteLabel();

    @Query("delete from LabelEntity where docNo=:docNo ")
    void deleteLabel(String docNo);

    @Query("select * from LabelEntity")
    List<LabelEntity> getAll();

    @Query("select * from LabelEntity where docNo=:docNo and productCode=:productCode and quantity=:quantity and status=:status limit 1")
    LabelEntity queryLabel(String docNo,String productCode,int quantity,String status);

    @Update
    void updateLabel(LabelEntity labelEntity);

    @Delete
    void deleteLabel(LabelEntity labelEntity);
}
