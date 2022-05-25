package com.hz.scantool.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DeliveryOrderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DeliveryOrderEntity deliveryOrderEntity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertList(List<DeliveryOrderEntity> deliveryOrderEntityList);

    @Query("delete from DeliveryOrderEntity where docNo=:docNo and id=:id")
    void deleteOrder(String docNo,long id);

    @Query("select * from DeliveryOrderEntity")
    List<DeliveryOrderEntity> getAll();

    @Query("select * from DeliveryOrderEntity where docNo=:docNo")
    DeliveryOrderEntity queryOrder(String docNo);

    @Update
    void updateOrder(DeliveryOrderEntity deliveryOrderEntity);
}
