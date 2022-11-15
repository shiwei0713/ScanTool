package com.hz.scantool.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {DeliveryOrderEntity.class,LabelEntity.class,ProductEntity.class,MergeLabelEntity.class,QrcodeEntity.class},version = 2)
public abstract class HzDb extends RoomDatabase {

    public abstract DeliveryOrderDao deliveryOrderDao();
    public abstract LabelDao labelDao();
    public abstract ProductDao productDao();
    public abstract MergeLabelDao mergeLabelDao();
    public abstract QrcodeDao qrcodeDao();
}
