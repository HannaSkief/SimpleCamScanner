package com.example.smartsteps.Async;

import android.content.Context;
import android.os.AsyncTask;

import com.example.smartsteps.Room.Connection;
import com.example.smartsteps.Room.Pdf;

public class UpdatePdfAsync extends AsyncTask<String,Void,String> {

    Pdf pdf;
    Context context;
    AsyncTaskCallback<Pdf> callback;
    Exception exception;

    public UpdatePdfAsync(Pdf pdf, Context context, AsyncTaskCallback<Pdf> callback) {
        this.pdf = pdf;
        this.context = context;
        this.callback = callback;
    }

    @Override
    protected String doInBackground(String... strings) {
       exception=null;

        Connection.getInstance(context).getDatabase().getPdfDao().update(pdf);

        return " ";
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (callback!=null){
            if(exception==null){
                callback.handleResponse(pdf);
            }else {
                callback.handleFault(exception);
            }
        }
    }
}
