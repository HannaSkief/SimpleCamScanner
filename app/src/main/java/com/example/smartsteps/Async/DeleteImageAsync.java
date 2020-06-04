package com.example.smartsteps.Async;

import android.content.Context;
import android.os.AsyncTask;

import com.example.smartsteps.Room.Connection;
import com.example.smartsteps.Room.Images;

import java.io.File;

public class DeleteImageAsync extends AsyncTask<String,Void,String> {

    Images images;
    Context context;
    AsyncTaskCallback<Images> callback;
    Exception exception;

    public DeleteImageAsync(Images images, Context context, AsyncTaskCallback<Images> callback) {
        this.images = images;
        this.context = context;
        this.callback = callback;
    }

    @Override
    protected String doInBackground(String... strings) {
        exception=null;

        Connection.getInstance(this.context).getDatabase().getImagesDao().delete(images);
        Connection.getInstance(this.context).getDatabase().getImagesDao().updateImageOrder(images.getFileId(),images.getPriority());
       // File file=new File(images.getPath());
        File file2=new File(images.getSecondPath());
//        if(file.exists()){
//            file.delete();
//        }
        if(file2.exists()){
            file2.delete();
        }



        return " ";
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        if(callback!=null){
            if(exception==null){
                callback.handleResponse(images);
            }
            else{
                callback.handleFault(exception);
            }

        }

    }
}
