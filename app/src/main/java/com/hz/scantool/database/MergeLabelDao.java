package com.hz.scantool.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MergeLabelDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MergeLabelEntity mergeLabelEntity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertList(List<MergeLabelEntity> mergeLabelEntityList);

    @Query("delete from MergeLabelEntity where planDocno=:planDocno and version=:version and planSeq=:planSeq and typeDesc=:typeDesc and moreOrLess=:moreOrLess")
    void deleteAllMergeLabel(String planDocno,int version,int planSeq,String typeDesc,int moreOrLess);

    @Query("delete from MergeLabelEntity")
    void deleteAll();

    @Query("update MergeLabelEntity set quantity=:quantity where qrcode=:qrcode")
    void update(String quantity,String qrcode);

    @Query("select * from MergeLabelEntity where planDocno=:planDocno and version=:version and planSeq=:planSeq and typeDesc=:typeDesc and moreOrLess=:moreOrLess")
    List<MergeLabelEntity> getAll(String planDocno,int version,int planSeq,String typeDesc,int moreOrLess);

    @Query("select * from MergeLabelEntity where typeDesc=:typeDesc")
    List<MergeLabelEntity> getAll(String typeDesc);

    @Query("select count(*) from MergeLabelEntity where planDocno=:planDocno and version=:version and planSeq=:planSeq and typeDesc=:typeDesc and moreOrLess=:moreOrLess")
    int getCount(String planDocno,int version,int planSeq,String typeDesc,int moreOrLess);

    @Query("select count(*) from MergeLabelEntity")
    int getCount();
}
