package com.example.smartsteps.Async;

import android.content.Context;
import android.os.AsyncTask;

import com.example.smartsteps.R;
import com.example.smartsteps.Room.Connection;
import com.example.smartsteps.Room.Pdf;

public class GetPdfAsync extends AsyncTask<String, Void, String> {

    long fileId;
    Context context;
    AsyncTaskCallback<Pdf> callback;
    Exception exception;
    Pdf pdf;

    public GetPdfAsync(long fileId, Context context, AsyncTaskCallback<Pdf> callback) {
        this.fileId = fileId;
        this.context = context;
        this.callback = callback;
    }

    @Override
    protected String doInBackground(String... strings) {
        exception = null;

        pdf = Connection.getInstance(this.context).getDatabase().getPdfDao().getPdf(this.fileId);

        if (pdf == null) {
            exception = new Exception(context.getString(R.string.pdf_not_exist));
        }

        return " ";
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        if (callback != null) {

            if (exception == null) {

                callback.handleResponse(pdf);
            } else {

                callback.handleFault(exception);
            }

        }

    }
}
