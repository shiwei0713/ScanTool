package com.hz.scantool.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ProductEntity productEntity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertList(List<ProductEntity> productEntityList);

    @Query("delete from ProductEntity ")
    void deleteAll();

    @Query("select count(*) from ProductEntity")
    int getCount();

    @Query("select * from ProductEntity")
    List<ProductEntity> getAll();
}
