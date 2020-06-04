package com.example.smartsteps.Async;

import android.content.Context;
import android.os.AsyncTask;

import com.example.smartsteps.Room.Connection;
import com.example.smartsteps.Room.Images;

public class UpdateImageAsync extends AsyncTask<String,Void,String> {

    Images image;
    Context context;
    AsyncTaskCallback<Images> callback;
    Exception exception;

    public UpdateImageAsync(Images image, Context context, AsyncTaskCallback<Images> callback) {
        this.image = image;
        this.context = context;
        this.callback = callback;
    }

    @Override
    protected String doInBackground(String... strings) {
       exception=null;

        Connection.getInstance(context).getDatabase().getImagesDao().update(image);

        return " ";
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (callback!=null){
            if (exception==null){

                callback.handleResponse(image);
            }else {
                callback.handleFault(exception);
            }

        }

    }
}
