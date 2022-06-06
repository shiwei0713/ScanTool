/**
*描述: 销售出货表操作SQL
*日期：2022/5/28
**/
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

    @Query("delete from DeliveryOrderEntity where docNo=:docNo")
    void deleteOrderDoc(String docNo);

    @Query("update DeliveryOrderEntity set qrCode=:qrCode,status=:status  where docNo=:docNo and id=:id")
    void updateOrderQrcode(String qrCode,String status,String docNo,long id);

    @Query("update DeliveryOrderEntity set saleQrCode=:saleQrCode,status=:status where qrCode=:qrCode")
    void updateOrderSalerCode(String saleQrCode,String status,String qrCode);

    @Query("select * from DeliveryOrderEntity")
    List<DeliveryOrderEntity> getAll();

    @Query("select * from DeliveryOrderEntity where docNo=:docNo")
    List<DeliveryOrderEntity> getOrderDoc(String docNo);

    @Query("select * from DeliveryOrderEntity where docNo=:docNo and productCode=:productCode and quantity=:quantity and status<>:status limit 1")
    DeliveryOrderEntity queryOrder(String docNo,String productCode,int quantity,String status);

    @Query("select * from DeliveryOrderEntity where qrCode=:qrCode and status=:status limit 1")
    DeliveryOrderEntity queryOrderQrcode(String qrCode,String status);

    @Query("select * from DeliveryOrderEntity where qrCode=:qrCode limit 1")
    DeliveryOrderEntity queryOrderResult(String qrCode);

    @Update
    void updateOrder(DeliveryOrderEntity deliveryOrderEntity);
}
