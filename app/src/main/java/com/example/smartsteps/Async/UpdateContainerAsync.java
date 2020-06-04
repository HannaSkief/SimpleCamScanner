package com.example.smartsteps.Async;

import android.content.Context;
import android.os.AsyncTask;

import com.example.smartsteps.R;
import com.example.smartsteps.Room.Connection;
import com.example.smartsteps.Room.Contianer;

public class UpdateContainerAsync extends AsyncTask<String,Void,String> {

    Contianer contianer;
    Context context;
    AsyncTaskCallback<String> callback;
    Exception exception;

    public UpdateContainerAsync(Contianer contianer, Context context, AsyncTaskCallback<String> callback) {
        this.contianer = contianer;
        this.context = context;
        this.callback = callback;
    }

    @Override
    protected String doInBackground(String... strings) {
        exception=null;

        Contianer c=Connection.getInstance(context)
                .getDatabase()
                .getContainerDao()
                .getContainerWithSameName(contianer.getName(),contianer.getType(),contianer.getParentId(),contianer.getId());

        if(c!=null){
            exception=new Exception(context.getString(R.string.name_already_used));
        }else {
            Connection.getInstance(context).getDatabase().getContainerDao().update(this.contianer);
        }
        return "";
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        if (callback!=null){

            if(exception==null){
                callback.handleResponse("Done");
            }else{
                callback.handleFault(exception);
            }

        }
    }
}
