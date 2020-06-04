package com.example.smartsteps.Common;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.example.smartsteps.R;
import com.example.smartsteps.Room.Images;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class GeneratePdf extends AsyncTask<String,Void,String> {

    List<Images> imagesList;
    Context context;
    String fileName;
    IPdfCallBack callBack;
    ProgressDialog progressDialog;

    public GeneratePdf(List<Images> imagesList, Context context, String fileName,IPdfCallBack callBack) {
        this.imagesList = imagesList;
        this.context = context;
        this.fileName = fileName;
        this.callBack=callBack;
    }

    public  interface IPdfCallBack{
        void afterPdfCreated(String path);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog=new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(imagesList.size());
        progressDialog.setMessage(context.getString(R.string.please_wait));
        progressDialog.setCanceledOnTouchOutside(false);

        progressDialog.show();
    }

    @Override
    protected String doInBackground(String... strings) {
        // create pdf
        File pdfFolder = new File(context.getApplicationContext().getExternalFilesDir(
                Environment.DIRECTORY_DOCUMENTS), "smartStepsPdf");
        if (!pdfFolder.exists()) {
            pdfFolder.mkdir();
            Log.i("GeneratePdf", "Pdf Directory created");
        }

        Date date = new Date() ;
        String timeStamp = new SimpleDateFormat("yyyy_MM_dd").format(date);

        File myFile = new File(pdfFolder +"/"+fileName+"_"+ timeStamp + ".pdf");

        OutputStream output = null;
        try {
            output = new FileOutputStream(myFile);
        } catch (FileNotFoundException e) {
            Log.e("GeneratePdf",Log.getStackTraceString(e));
        }

        Document doc = new Document();
        try {
            PdfWriter.getInstance(doc, output);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        doc.open();

        //add images to pdf
        for(Images img:imagesList) {

            if ((new File(img.getSecondPath())).exists()) {
                try {
                    Image image = Image.getInstance(readImage(getImageBitmap(img.getSecondPath())));
                    image.scaleToFit(595, 842);

                    // doc.setPageSize(new Rectangle(image.getScaledWidth(), image.getScaledHeight()));
                    //  image.scaleAbsoluteWidth(doc.getPageSize().getWidth());
                    float x = (595 - image.getScaledWidth()) / 2;
                    float y = (842 - image.getScaledHeight()) / 2;
                    image.setAbsolutePosition(x, y);
                    doc.add(image);
                    doc.newPage();

                } catch (BadElementException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (DocumentException e) {
                    e.printStackTrace();
                }

            }
            progressDialog.setProgress(img.getPriority());
        }
        try {
            doc.add(new Paragraph(""));
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        doc.close();

        return myFile.getAbsolutePath();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        progressDialog.dismiss();
        callBack.afterPdfCreated(s);
    }

    private Bitmap getImageBitmap(String imgPath){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeFile(imgPath);
    }


    private byte[] readImage(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
        byte[] byteArray = stream.toByteArray();

        return byteArray;
    }




}
