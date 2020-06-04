package com.example.smartsteps.Async;

import android.content.Context;
import android.os.AsyncTask;

import com.example.smartsteps.R;
import com.example.smartsteps.Room.Connection;
import com.example.smartsteps.Room.Contianer;

public class InsertContainerAsync extends AsyncTask<String, Void, String> {

    private Contianer contianer;
    private Context context;
    private AsyncTaskCallback<Contianer> callback;
    private Exception exception;


    public InsertContainerAsync(Contianer contianer, Context context, AsyncTaskCallback<Contianer> callback) {
        this.contianer = contianer;
        this.context = context;
        this.callback = callback;
    }

    @Override
    protected String doInBackground(String... strings) {
      exception =null;

      Contianer c= Connection.getInstance(context).getDatabase()
              .getContainerDao()
              .getContainer(contianer.getName(),contianer.getType(),contianer.getParentId());

      if(c!=null){
          exception=new Exception(context.getString(R.string.name_already_used));
      }else{
          long id=Connection.getInstance(context).getDatabase().getContainerDao().insert(this.contianer);
            contianer.setId(id);
      }


        return "";
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    
        if(callback!=null){
            
            if(exception==null){
                callback.handleResponse(this.contianer);
            }
            else{
                callback.handleFault(exception);
            }
            
        }
    
    
    }
}
