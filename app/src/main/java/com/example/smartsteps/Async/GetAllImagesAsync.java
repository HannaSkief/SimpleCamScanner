package com.example.smartsteps.Async;

import android.content.Context;
import android.os.AsyncTask;

import com.example.smartsteps.R;
import com.example.smartsteps.Room.Connection;
import com.example.smartsteps.Room.Images;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GetAllImagesAsync extends AsyncTask<String,Void,String> {

    long fileId;
    Context context;
    AsyncTaskCallback<List<Images>> callback;
    Exception exception;
    List<Images> imagesList;


    public GetAllImagesAsync(long fileId, Context context, AsyncTaskCallback<List<Images>> callback) {
        this.fileId = fileId;
        this.context = context;
        this.callback = callback;
    }

    @Override
    protected String doInBackground(String... strings) {
        exception=null;

       List<Images> imagesList2= Connection.getInstance(this.context).getDatabase()
                .getImagesDao()
                .getAllImages(fileId);
        if(imagesList2==null){
            exception=new Exception(context.getString(R.string.empty_file));
        }else{
            imagesList=new ArrayList<>();
            for(int i=0;i<imagesList2.size();i++){
                if(( new File(imagesList2.get(i).getSecondPath())).exists()){
                    imagesList.add(imagesList2.get(i));
                }
            }
        }


        return " ";
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        if(callback!=null){
            if(exception==null){

                callback.handleResponse(imagesList);
            }else{
                callback.handleFault(exception);
            }

        }

    }
}
