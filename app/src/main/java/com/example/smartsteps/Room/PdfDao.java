package com.example.smartsteps.Room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface PdfDao {

    @Insert
    long insert(Pdf pdf);

    @Update
    void update(Pdf pdf);

    @Delete
    void delete(Pdf pdf);


    @Query("select * from pdf where fileId=:fileId")
    Pdf getPdf(long fileId);


}
