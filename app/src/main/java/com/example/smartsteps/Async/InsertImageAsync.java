package com.example.smartsteps.Async;

import android.content.Context;
import android.os.AsyncTask;

import com.example.smartsteps.Room.Connection;
import com.example.smartsteps.Room.Images;

public class InsertImageAsync extends AsyncTask<String,Void,String> {

    Images images;
    Context context;
    AsyncTaskCallback<Images> callback;
    Exception exception;

    public InsertImageAsync(Images images, Context context, AsyncTaskCallback<Images> callback) {
        this.images = images;
        this.context = context;
        this.callback = callback;
    }

    @Override
    protected String doInBackground(String... strings) {

        exception=null;

        long imageId=Connection.getInstance(this.context).getDatabase().getImagesDao().insert(images);
        this.images.setId(imageId);

        return " ";
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);


        if (callback!=null){

            if (exception==null){
                callback.handleResponse(this.images);
            }else{
                callback.handleFault(exception);
            }

        }

    }
}
