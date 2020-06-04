package com.example.smartsteps.Async;

import android.content.Context;
import android.os.AsyncTask;

import com.example.smartsteps.Room.Connection;
import com.example.smartsteps.Room.Images;

import java.util.List;

public class UpdateAllImageAsync extends AsyncTask<String,Void,String> {

    List<Images> imagesList;
    Context context;
    AsyncTaskCallback<List<Images>> callback;
    Exception exception;


    public UpdateAllImageAsync(List<Images> imagesList, Context context, AsyncTaskCallback<List<Images>> callback) {
        this.imagesList = imagesList;
        this.context = context;
        this.callback = callback;
    }

    @Override
    protected String doInBackground(String... strings) {

        exception=null;

        for(Images img:imagesList){
            Connection.getInstance(context).getDatabase().getImagesDao().update(img);
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
