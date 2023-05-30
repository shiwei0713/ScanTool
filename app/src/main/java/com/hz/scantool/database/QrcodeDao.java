package com.hz.scantool.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface QrcodeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(QrcodeEntity qrcodeEntity);

    @Query("delete from QrcodeEntity ")
    void deleteAll();

    @Query("delete from QrcodeEntity where qrcode=:qrcode ")
    void deleteQrcode(String qrcode);

    @Query("select * from QrcodeEntity order by scantime desc")
    List<QrcodeEntity> getAll();

    @Query("select distinct docNo from QrcodeEntity")
    String getDcono();

    @Query("select count(*) from QrcodeEntity where qrcode=:qrcode")
    int getCount(String qrcode);

    @Query("select count(*) from QrcodeEntity")
    int getAllCount();
}
