package com.example.smartsteps.Room;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface ContainerDao {

    @Insert
    long insert(Contianer contianer);

    @Update
    void update(Contianer contianer);

    @Delete
    void delete(Contianer contianer);

    @Query("select * from container where name=:name and type=:type and parentId=:parentId limit 1")
    Contianer getContainer(String name,String type,long parentId);

    @Query("select * from container where name=:name and type=:type and parentId=:parentId and id!=:id limit 1")
    Contianer getContainerWithSameName(String name,String type,long parentId,long id );



    @Query("select * from container where parentId=:parentId order by priority,name COLLATE NOCASE")
    List<Contianer> getAllFoldersAndList(long parentId);

    @Query("select * from container where name like :name order by priority,name COLLATE NOCASE")
    List<Contianer> getAllFoldersAndListBySearch(String name);


    @Query("WITH RECURSIVE " +
            " folder_parent(n) AS (VALUES(:id) " +
            "union " +
            "select parentId from container,folder_parent " +
            "where container.id=folder_parent.n ) " +
            "select * from container where container.id in folder_parent ")
    List<Contianer> getTree(long id);

    @Query("WITH RECURSIVE " +
            " folder_parent(n) AS (VALUES(:id) " +
            "union " +
            "select id from container,folder_parent " +
            "where container.parentId=folder_parent.n ) " +
            "select secondPath from images where fileId in folder_parent " +
            "union all " +
            "select path from pdf where fileId in folder_parent ")
    List<String> getAllContainerContentPaths(long id);

    @Query("WITH RECURSIVE " +
            " folder_parent(n) AS (VALUES(:id) " +
            "union " +
            "select id from container,folder_parent " +
            "where container.parentId=folder_parent.n ) " +
            "delete  from images where fileId in folder_parent")
    void deleteContainerImages(long id);

    @Query("WITH RECURSIVE " +
            " folder_parent(n) AS (VALUES(:id) " +
            "union " +
            "select id from container,folder_parent " +
            "where container.parentId=folder_parent.n ) " +
            "delete  from pdf where fileId in folder_parent")
    void deleteContainerPdf(long id);

    @Query("WITH RECURSIVE " +
            " folder_parent(n) AS (VALUES(:id) " +
            "union " +
            "select id from container,folder_parent " +
            "where container.parentId=folder_parent.n ) " +
            "delete from container where id in folder_parent ")
    void deleteFolderContent(long id);
}
