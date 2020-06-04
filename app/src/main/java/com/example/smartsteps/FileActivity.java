package com.example.smartsteps;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import team.clevel.documentscanner.ImageCropActivity;
import team.clevel.documentscanner.helpers.ScannerConstants;

import android.Manifest;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smartsteps.Adapter.FileAdapter;
import com.example.smartsteps.Adapter.OrderAdapter;
import com.example.smartsteps.Async.AsyncTaskCallback;
import com.example.smartsteps.Async.DeleteContainerCotentAsync;
import com.example.smartsteps.Async.DeleteImageAsync;
import com.example.smartsteps.Async.GetAllImagesAsync;
import com.example.smartsteps.Async.GetPdfAsync;
import com.example.smartsteps.Async.InsertImageAsync;
import com.example.smartsteps.Async.InsertPdfAsync;
import com.example.smartsteps.Async.UpdateAllImageAsync;
import com.example.smartsteps.Async.UpdateContainerAsync;
import com.example.smartsteps.Async.UpdateImageAsync;
import com.example.smartsteps.Async.UpdatePdfAsync;
import com.example.smartsteps.Common.Common;
import com.example.smartsteps.Common.GeneratePdf;
import com.example.smartsteps.Room.Contianer;
import com.example.smartsteps.Room.Images;
import com.example.smartsteps.Room.Pdf;

import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickClick;
import com.vansuita.pickimage.listeners.IPickResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class FileActivity extends AppCompatActivity implements FileAdapter.ImageClicked {


    private static final int REQUEST_CROP = 10, REQUEST_EDIT = 11, GALLERY = 12, CAMERA = 13, OPEN_CAMERA = 14, OPEN_GALLERY = 15;

    ImageView imgPdf, imgSharePdf;
    TextView tvPdfName, tvPdfDate;

    RecyclerView rvImages;
    LinearLayout pdfContainer;
    Toolbar toolbar;

    List<Images> imagesList;

    Images newImage;
    SharedPreferences preferences;
    private Contianer currentFile;
    private Pdf pdf;
    private Uri cameraImageUri;
    private String cameraImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);

        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        imagesList = new ArrayList<>();
        currentFile = Common.selected_file;

        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(Common.selected_file.getName());
        toolbar.setNavigationOnClickListener(view -> finish());

        imgPdf = findViewById(R.id.imgPdf);
        imgSharePdf = findViewById(R.id.imgSharePdf);
        tvPdfName = findViewById(R.id.tvPdfName);
        tvPdfDate = findViewById(R.id.tvPdfDate);
        rvImages = findViewById(R.id.rvImages);
        pdfContainer = findViewById(R.id.pdfContainer);
        pdfContainer.setVisibility(View.GONE);

        imgPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPdf();
            }
        });

        imgSharePdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharePdf();
            }
        });

        rvImages.setLayoutManager(new GridLayoutManager(FileActivity.this, 2));
        rvImages.setHasFixedSize(true);
        showPdfIfExist();
        initScannerConstants();

        getImages();

    }

    private void sharePdf() {
        File file = new File(pdf.getPath());
        if (file.exists()) {
            Intent email = new Intent(Intent.ACTION_SEND);
            // email.putExtra(Intent.EXTRA_EMAIL,toEmail);
            // email.putExtra(Intent.EXTRA_TEXT, reportContent.toString());
            Uri uri = FileProvider.getUriForFile(FileActivity.this,
                    getApplicationContext().getPackageName() + ".provider", file);
            email.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            email.putExtra(Intent.EXTRA_STREAM, uri);
            email.setType("message/rfc822");
            startActivity(email);
        }
    }

    private void openPdf() {

        File file = new File(pdf.getPath());
        if (file.exists()) {
            Uri pdfURI = FileProvider.getUriForFile(FileActivity.this,
                    getApplicationContext().getPackageName() + ".provider", file);
            Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_VIEW);
            intent.setDataAndType(pdfURI, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        }
    }


    private void getImages() {

        new GetAllImagesAsync(currentFile.getId(), FileActivity.this, new AsyncTaskCallback<List<Images>>() {
            @Override
            public void handleResponse(List<Images> response) {
                imagesList = response;
                rvImages.setAdapter(new FileAdapter(response, FileActivity.this, FileActivity.this));
            }

            @Override
            public void handleFault(Exception e) {
                Toast.makeText(FileActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).execute();

    }

    private void initScannerConstants() {
        ScannerConstants.cropText = getString(R.string.crop);
        ScannerConstants.backText = getString(R.string.back);
        ScannerConstants.progressColor = "#2c3e50";
        ScannerConstants.cropColor = "#2c3e50";
        ScannerConstants.cropError = getString(R.string.image_cant_be_cropped);
        ScannerConstants.imageError = getString(R.string.error);
    }

    private void showPdfIfExist() {

        new GetPdfAsync(currentFile.getId(), FileActivity.this, new AsyncTaskCallback<Pdf>() {
            @Override
            public void handleResponse(Pdf response) {
                if((new File(response.getPath())).exists()) {
                    pdfContainer.setVisibility(View.VISIBLE);
                    pdf = response;
                    tvPdfName.setText(currentFile.getName());
                    tvPdfDate.setText(pdf.getCreatedAt());
                }
            }

            @Override
            public void handleFault(Exception e) {

            }
        }).execute();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.home:
                finish();
                return true;
            case R.id.addImage:
                addImage();
                return true;
            case R.id.order:
                orderImages();
                return true;
            case R.id.renameFile:
                renameFile();
                return true;
            case R.id.deleteFile:
                deleteFile();
                return true;

            case R.id.generatePdf:
                generatePdf();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void generatePdf() {

        if (imagesList == null || imagesList.isEmpty()) {
            Toast.makeText(this, getString(R.string.empty_file), Toast.LENGTH_SHORT).show();
            return;
        }

        new GeneratePdf(imagesList, FileActivity.this, currentFile.getName(), new GeneratePdf.IPdfCallBack() {
            @Override
            public void afterPdfCreated(String path) {
                if (pdf == null) {
                    pdf = new Pdf();
                    pdf.setId(0);
                    pdf.setFileId(currentFile.getId());
                    pdf.setPath(path);
                    pdf.setCreatedAt(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));

                    new InsertPdfAsync(pdf, FileActivity.this, new AsyncTaskCallback<Pdf>() {
                        @Override
                        public void handleResponse(Pdf response) {
                            pdf.setId(response.getId());
                        }

                        @Override
                        public void handleFault(Exception e) {

                        }
                    }).execute();

                }
                else
                {
                    if(!pdf.getPath().equals(path)) {
                        File file = new File(pdf.getPath());
                        if (file.exists())
                            file.delete();
                    }

                    pdf.setPath(path);
                    pdf.setCreatedAt(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
                    new UpdatePdfAsync(pdf, FileActivity.this, null).execute();
                }

                pdfContainer.setVisibility(View.VISIBLE);
                tvPdfName.setText(currentFile.getName());
                tvPdfDate.setText(pdf.getCreatedAt());
            }
        }).execute();

    }

    private void deleteFile() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(FileActivity.this);
        dialog.setTitle(getString(R.string.delete) + " ' " + currentFile.getName() + " '");
        dialog.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                new DeleteContainerCotentAsync(currentFile, FileActivity.this, new DeleteContainerCotentAsync.deleteCallBack() {
                    @Override
                    public void afterContainerDeleted() {
                        FileActivity.this.finish();
                    }
                }).execute();

            }
        });
        dialog.setNegativeButton(getString(R.string.cancel), null);
        dialog.show();
    }

    private void renameFile() {
        final EditText input = new EditText(FileActivity.this);
        input.setHint(getString(R.string.new_name));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        input.setText(currentFile.getName());
        input.setSelection(0, input.getText().toString().length());

        AlertDialog.Builder dialog = new AlertDialog.Builder(FileActivity.this);
        dialog.setTitle(getString(R.string.rename));
        dialog.setView(input);
        dialog.setPositiveButton(getString(R.string.save), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (input.getText().toString().trim().isEmpty()) {
                    Toast.makeText(FileActivity.this, getString(R.string.please_enter_the_name_field), Toast.LENGTH_SHORT).show();
                    return;
                }


                currentFile.setName(input.getText().toString().trim());
                new UpdateContainerAsync(currentFile, FileActivity.this, new AsyncTaskCallback<String>() {
                    @Override
                    public void handleResponse(String response) {
                        toolbar.setTitle(input.getText().toString().trim());
                        currentFile.setName(input.getText().toString().trim());
                    }

                    @Override
                    public void handleFault(Exception e) {
                        Toast.makeText(FileActivity.this, "" + e.getMessage(), Toast.LENGTH_LONG).show();

                    }
                }).execute();


            }
        });
        dialog.setNegativeButton(getString(R.string.cancel), null);
        dialog.show();


    }

    private void orderImages() {

        if (imagesList == null || imagesList.isEmpty()) {
            Toast.makeText(this, getString(R.string.empty_file_2), Toast.LENGTH_SHORT).show();
            return;
        }
        RecyclerView rvOrder;
        LinearLayout orderInstruction;
        TextView tvDontShowOrderInstruction;
        AlertDialog.Builder dialog = new AlertDialog.Builder(FileActivity.this);
        dialog.setTitle(getString(R.string.order));
        View view = LayoutInflater.from(FileActivity.this).inflate(R.layout.order_images_layout, null);
        orderInstruction = view.findViewById(R.id.orderInstruction);
        tvDontShowOrderInstruction = view.findViewById(R.id.tvDontShowOrderInstruction);
        rvOrder = view.findViewById(R.id.rvOrder);
        rvOrder.setHasFixedSize(true);
        boolean showOrderInstruction = preferences.getBoolean("showOrderInstruction", true);
        orderInstruction.setVisibility(showOrderInstruction ? View.VISIBLE : View.GONE);
        tvDontShowOrderInstruction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                orderInstruction.setVisibility(View.GONE);
                preferences.edit().putBoolean("showOrderInstruction", false).apply();
            }
        });

        rvOrder.setLayoutManager(new GridLayoutManager(this, 3));
        dialog.setPositiveButton(getString(R.string.save), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                rvImages.getAdapter().notifyDataSetChanged();
                new UpdateAllImageAsync(imagesList, FileActivity.this, null).execute();

            }
        });
        dialog.setNegativeButton(getString(R.string.cancel), null);

        rvOrder.setAdapter(new OrderAdapter(imagesList, FileActivity.this));
        dialog.setView(view);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(rvOrder);

        dialog.show();

    }

    // callback to order images
    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.DOWN |
            ItemTouchHelper.UP | ItemTouchHelper.START | ItemTouchHelper.END, ItemTouchHelper.DOWN |
            ItemTouchHelper.UP | ItemTouchHelper.START | ItemTouchHelper.END) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();

            imagesList.get(fromPosition).setPriority(toPosition + 1);
            imagesList.get(toPosition).setPriority(fromPosition + 1);

//            Toast.makeText(FileActivity.this, ""+fromPosition+" to "+toPosition, Toast.LENGTH_SHORT).show();

            Collections.swap(imagesList, fromPosition, toPosition);
            recyclerView.getAdapter().notifyItemMoved(fromPosition, toPosition);

            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

        }
    };


    @Override
    public void EditImage() {
        Images images = Common.selected_image;
        File imageFile = new File(images.getPath());
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = null;
        if (imageFile.exists()) {
            bitmap = BitmapFactory.decodeFile(images.getPath(), options);
        } else {
            bitmap = BitmapFactory.decodeFile(images.getSecondPath(), options);
        }

        cropImage(bitmap, REQUEST_EDIT);

    }

    @Override
    public void deleteImage() {
        Images image = Common.selected_image;

        AlertDialog.Builder dialog = new AlertDialog.Builder(FileActivity.this);
        dialog.setTitle(getString(R.string.delete_this_image));
        dialog.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                new DeleteImageAsync(image, FileActivity.this, new AsyncTaskCallback<Images>() {
                    @Override
                    public void handleResponse(Images response) {

                        getImages();
                    }

                    @Override
                    public void handleFault(Exception e) {

                    }
                }).execute();
            }
        });
        dialog.setNegativeButton(getString(R.string.cancel), null);
        dialog.show();
    }

    @Override
    public void openImage() {

        File file = new File(Common.selected_image.getSecondPath());
        Uri photoURI = FileProvider.getUriForFile(FileActivity.this,
                getApplicationContext().getPackageName() + ".provider", file);
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(photoURI, "image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }


    private void addImage() {
        PickSetup setup = new PickSetup()
                .setTitle(getString(R.string.choose))
                .setCameraButtonText(getString(R.string.camera))
                .setGalleryButtonText(getString(R.string.gallery))
                .setCancelText(getString(R.string.cancel))
                .setButtonOrientation(LinearLayoutCompat.HORIZONTAL);

        PickImageDialog dialog = PickImageDialog.build(setup);
        dialog.setOnClick(new IPickClick() {
            @Override
            public void onGalleryClick() {
                getImageFromGallery();
                dialog.dismiss();
            }

            @Override
            public void onCameraClick() {
                getImageFromCamera();
                dialog.dismiss();
            }
        }).show(this);
    }


    private void getImageFromCamera() {
        if (ActivityCompat.checkSelfPermission(FileActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(FileActivity.this, new String[]{Manifest.permission.CAMERA}, OPEN_CAMERA);
        } else {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                File photoFile = null;
                photoFile = createImageFile();
                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(FileActivity.this,
                            getApplicationContext().getPackageName() + ".provider", photoFile);
                    cameraImageUri = photoURI;
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, CAMERA);
                }
            }

        }


    }

    private File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = null;
        try {
            imageFile = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        cameraImagePath=imageFile.getAbsolutePath();
        // Save a file: path for use with ACTION_VIEW intents
        return imageFile;
    }

    private void getImageFromGallery() {

        if (ActivityCompat.checkSelfPermission(FileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(FileActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, OPEN_GALLERY);
        } else {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
           // galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            startActivityForResult(galleryIntent, GALLERY);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.file_menu, menu);
        return true;
    }


    private  void CreateImage(Bitmap bitmap, String path) {

        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormate = new SimpleDateFormat("dd/MM/yyyy");

        newImage = new Images();
        newImage.setId(0);
        newImage.setFileId(currentFile.getId());
        newImage.setPath(path);
        newImage.setPriority(imagesList.size() + 1);
        newImage.setSecondPath(path);
        newImage.setAddedAt(dateFormate.format(c));

        cropImage(bitmap, REQUEST_CROP);

    }


    private void cropImage(Bitmap bitmap, int requestCode) {

        ScannerConstants.selectedImageBitmap = bitmap;
        startActivityForResult(new Intent(FileActivity.this, ImageCropActivity.class), requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CROP) {
            if (resultCode == RESULT_OK) {
                if (ScannerConstants.selectedImageBitmap != null) {
                    try {
                        saveCroppedImage(ScannerConstants.selectedImageBitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else
                    Toast.makeText(FileActivity.this, "Something wen't wrong.", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == REQUEST_EDIT) {

            if (resultCode == RESULT_OK) {
                if (ScannerConstants.selectedImageBitmap != null) {
                    try {
                        updateImage(ScannerConstants.selectedImageBitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else
                    Toast.makeText(FileActivity.this, "Something wen't wrong.", Toast.LENGTH_LONG).show();
            }

        } else if (requestCode == GALLERY) {

            if (resultCode == RESULT_OK) {
//                ClipData clipData=data.getClipData(); // clipData return null id only one item selected
//                if(clipData!=null){
//                    for(int i=0;i<clipData.getItemCount();i++){
//                        Uri contentURI =clipData.getItemAt(i).getUri();
//                        try {
//                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);
//                            CreateImage(bitmap, contentURI.getPath());
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }else{
//                    Uri contentURI = data.getData();
//                    try {
//                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);
//                        CreateImage(bitmap, contentURI.getPath());
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }

                if (data != null) {
                    Uri contentURI = data.getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);
                        CreateImage(bitmap, contentURI.getPath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if (requestCode == CAMERA) {
            if (resultCode == RESULT_OK) {
                Uri contentURI = cameraImageUri;
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);
                    Bitmap rotatedBitmap=getRotatedImageBitmap(bitmap,cameraImagePath);
                    CreateImage(rotatedBitmap, contentURI.getPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

    }

    private Bitmap getRotatedImageBitmap(Bitmap bitmap, String path) {

        ExifInterface ei = null;
        try {
            ei = new ExifInterface(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        switch (orientation) {

            case ExifInterface.ORIENTATION_ROTATE_90:
                return  rotateImage(bitmap, 90);

            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(bitmap, 180);

            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(bitmap, 270);

            default:
                return bitmap;
        }

    }
    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == OPEN_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getImageFromCamera();
            }
        } else if (requestCode == OPEN_GALLERY) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getImageFromGallery();
            }
        }
    }

    private void updateImage(Bitmap bitmap) throws IOException {

        Images images = Common.selected_image;

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File file = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        OutputStream out = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        out.flush();
        out.close();

        File oldFile = new File(images.getSecondPath());
        if (oldFile.exists()) {
            boolean d = oldFile.delete();
        }

        images.setSecondPath(file.getAbsolutePath());

        new UpdateImageAsync(images, FileActivity.this, new AsyncTaskCallback<Images>() {
            @Override
            public void handleResponse(Images response) {
                getImages();
            }

            @Override
            public void handleFault(Exception e) {
                Toast.makeText(FileActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).execute();

    }


    private void saveCroppedImage(Bitmap bitmap) throws IOException {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File file = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        OutputStream out = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        out.flush();
        out.close();


        newImage.setSecondPath(file.getAbsolutePath());

        new InsertImageAsync(newImage, FileActivity.this, new AsyncTaskCallback<Images>() {
            @Override
            public void handleResponse(Images response) {
                imagesList.add(response);
                rvImages.getAdapter().notifyDataSetChanged();
            }

            @Override
            public void handleFault(Exception e) {

            }
        }).execute();


    }


}
