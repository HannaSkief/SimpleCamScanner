package com.example.smartsteps.Async;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.example.smartsteps.R;
import com.example.smartsteps.Room.Connection;
import com.example.smartsteps.Room.Contianer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DeleteContainerCotentAsync extends AsyncTask<String,Void,String> {

    Contianer contianer;
    Context context;
    deleteCallBack callBack;
    List<String> pathList;
    ProgressDialog progressDialog;

    public DeleteContainerCotentAsync(Contianer contianer, Context context, deleteCallBack callBack) {
        this.contianer=contianer;
        this.context = context;
        this.callBack = callBack;
    }

    public  interface deleteCallBack{
        void afterContainerDeleted();
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if(pathList==null)
            pathList=new ArrayList<>();

        pathList= Connection.getInstance(context).getDatabase().getContainerDao().getAllContainerContentPaths(contianer.getId());
        progressDialog=new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage(context.getString(R.string.please_wait));
        progressDialog.setMax(pathList.size());
        progressDialog.show();

    }

    @Override
    protected String doInBackground(String... strings) {


        int i=0;
       Connection.getInstance(context).getDatabase().getContainerDao().deleteContainerImages(contianer.getId());
       Connection.getInstance(context).getDatabase().getContainerDao().deleteContainerPdf(contianer.getId());
       Connection.getInstance(context).getDatabase().getContainerDao().delete(contianer);
       Connection.getInstance(context).getDatabase().getContainerDao().deleteFolderContent(contianer.getId());

        for (String path:pathList){
            File file=new File(path);
            if(file.exists())
                file.delete();
            progressDialog.setProgress(++i);
        }


        return " ";
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        progressDialog.dismiss();
        callBack.afterContainerDeleted();
    }
}
