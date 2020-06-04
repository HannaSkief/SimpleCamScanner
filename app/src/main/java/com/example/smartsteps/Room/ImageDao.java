package com.example.smartsteps.Room;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface ImageDao {

    @Insert
    long  insert(Images image);

    @Update
    void update(Images images);

    @Delete
    void delete(Images images);

    @Query("select * from images where fileId=:fileId order by priority")
    List<Images> getAllImages(long fileId);

    @Query("update images set priority=priority-1 where fileId=:fileId and priority>:position")
    void updateImageOrder(long fileId,long position);


}
