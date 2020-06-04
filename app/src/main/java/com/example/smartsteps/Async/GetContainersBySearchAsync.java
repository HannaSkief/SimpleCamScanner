package com.example.smartsteps.Async;

import android.content.Context;
import android.os.AsyncTask;

import com.example.smartsteps.R;
import com.example.smartsteps.Room.Connection;
import com.example.smartsteps.Room.Contianer;

import java.util.List;

public class GetContainersBySearchAsync extends AsyncTask<String,Void,String> {

    String name;
    Context context;
    AsyncTaskCallback<List<Contianer>> callback;
    Exception exception;
    List<Contianer> contianerList;

    public GetContainersBySearchAsync(String name, Context context, AsyncTaskCallback<List<Contianer>> callback) {
        this.name = name;
        this.context = context;
        this.callback = callback;
    }

    @Override
    protected String doInBackground(String... strings) {
        exception=null;
        name="%"+name+"%";
        contianerList= Connection.getInstance(this.context).getDatabase().getContainerDao().getAllFoldersAndListBySearch(this.name);
        if(contianerList==null||contianerList.isEmpty()){
            exception=new Exception(context.getString(R.string.no_folder_or_file_in_this_name));
        }
        return " ";
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        if(callback!=null){
            if (exception==null){
                callback.handleResponse(contianerList);
            }else{
                callback.handleFault(exception);
            }
        }



    }
}
