package com.example.smartsteps.Room;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Contianer.class, Images.class,Pdf.class},version = 1,exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract ContainerDao getContainerDao();
    public abstract ImageDao getImagesDao();
    public abstract PdfDao getPdfDao();


}
