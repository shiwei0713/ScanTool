package com.hz.scantool.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {DeliveryOrderEntity.class},version = 1)
public abstract class HzDb extends RoomDatabase {

    public abstract DeliveryOrderDao deliveryOrderDao();
}
