package com.example.smartsteps.Room;

import android.content.Context;

import androidx.room.Room;

public class Connection {

    private  static Connection instance;
    private AppDatabase database;

    private  Connection(Context context){
        database= Room
                .databaseBuilder(context,AppDatabase.class,"documents_dp")
                .allowMainThreadQueries()
                .build();

    }

    public static Connection getInstance(Context context){

        if (instance==null || !instance.getDatabase().isOpen()){

            instance=new Connection(context);
        }
        return instance;
    }


    public AppDatabase getDatabase(){
        return database;
    }

}
