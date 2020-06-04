package com.example.smartsteps.Async;

import android.content.Context;
import android.os.AsyncTask;

import com.example.smartsteps.Room.Connection;
import com.example.smartsteps.Room.Contianer;

import java.util.List;

public class GetParentTreeAsync extends AsyncTask<String,Void,String> {

    long id;
    Context context;
    AsyncTaskCallback<List<Contianer>> callback;
    Exception exception;
    List<Contianer> contianerList;

    public GetParentTreeAsync(long id, Context context, AsyncTaskCallback<List<Contianer>> callback) {
        this.id = id;
        this.context = context;
        this.callback = callback;
    }

    @Override
    protected String doInBackground(String... strings) {

        exception=null;
        contianerList= Connection.getInstance(this.context).getDatabase().getContainerDao().getTree(this.id);
        if(contianerList==null){
            exception=new Exception("some thing wrong");
        }


        return " ";
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        if(callback!=null){
            if(exception==null){
                callback.handleResponse(contianerList);
            }else{
                callback.handleFault(exception);
            }
        }



    }
}
