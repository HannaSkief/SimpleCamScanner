package com.example.smartsteps.Async;

import android.content.Context;
import android.os.AsyncTask;

import com.example.smartsteps.R;
import com.example.smartsteps.Room.Connection;
import com.example.smartsteps.Room.Contianer;

import java.util.List;

public class GetAllContainerAsync extends AsyncTask<String,Void,String> {

    private long parentId;
    private Context context;
    private AsyncTaskCallback<List<Contianer>> callback;
    private Exception exception;
    private List<Contianer> contianerList;

    public GetAllContainerAsync(long parentId, Context context, AsyncTaskCallback<List<Contianer>> callback) {
        this.parentId = parentId;
        this.context = context;
        this.callback = callback;
    }

    @Override
    protected String doInBackground(String... strings) {
        exception=null;

        contianerList= Connection.getInstance(context).getDatabase().getContainerDao().getAllFoldersAndList(parentId);
        if(contianerList==null|| contianerList.isEmpty()){
            exception=new Exception(context.getString(R.string.the_folder_is_empty));
        }

        return "";
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
