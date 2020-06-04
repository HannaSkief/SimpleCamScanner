package com.example.smartsteps;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smartsteps.Adapter.FolderAdapter;
import com.example.smartsteps.Async.AsyncTaskCallback;
import com.example.smartsteps.Async.DeleteContainerCotentAsync;
import com.example.smartsteps.Async.GetAllContainerAsync;
import com.example.smartsteps.Async.GetParentTreeAsync;
import com.example.smartsteps.Async.InsertContainerAsync;
import com.example.smartsteps.Async.UpdateContainerAsync;
import com.example.smartsteps.Common.Common;
import com.example.smartsteps.Room.AppDatabase;
import com.example.smartsteps.Room.Connection;
import com.example.smartsteps.Room.Contianer;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity implements FolderAdapter.ContainerItemClick {

    private static final int SEARCH_REQUEST=10,IMPORT_DB=11,EXPORT_DB=12;
    LinearLayout backFolder;
    RecyclerView rvContainer;
    SharedPreferences preferences;

    private boolean isBigIcons;
    private  ArrayList<Contianer> folderList;

    private FloatingActionButton fab_main, fabFolder, fabFile;
    private Animation fab_open, fab_close, fab_clock, fab_anticlock;


    Boolean isOpen = false;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //floating action  button
        fab_main = findViewById(R.id.fab);
        fabFolder = findViewById(R.id.fabFolder);
        fabFile = findViewById(R.id.fabFiles);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_clock = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_rotate_clock);
        fab_anticlock = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_rotate_anticlock);

        ////


        backFolder=findViewById(R.id.backFolder);
        rvContainer=findViewById(R.id.rvContainer);
        rvContainer.setHasFixedSize(true);

        preferences= PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        isBigIcons=preferences.getBoolean("iconSize",false);
        switchIconSize(isBigIcons);

        backFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                back();
            }
        });

        //Hide the  back folder
        backFolder.setVisibility(View.GONE);

        // when rotate the device
        if(savedInstanceState!=null){
         folderList=savedInstanceState.getParcelableArrayList("folderList");
        }else{

        folderList=new ArrayList<>();
        //Add root container to folder list
        folderList.add(new Contianer(0, preferences.getString("rootFolderName", getString(R.string.home))));
        }




        refreshScene();

        initFabAnimation();

    }

    private void initFabAnimation(){
        fab_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isOpen) {


                    fabFile.startAnimation(fab_close);
                    fabFolder.startAnimation(fab_close);
                    fab_main.startAnimation(fab_anticlock);
                    fabFile.setClickable(false);
                    fabFolder.setClickable(false);
                    isOpen = false;
                } else {

                    fabFile.startAnimation(fab_open);
                    fabFolder.startAnimation(fab_open);
                    fab_main.startAnimation(fab_clock);
                    fabFile.setClickable(true);
                    fabFolder.setClickable(true);
                    isOpen = true;
                }

            }
        });


        fabFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                addNewFolderOrFile(false);

            }
        });

        fabFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewFolderOrFile(true);

            }
        });
    }



    private void addNewFolderOrFile(final boolean isFolder){

        final Date c= Calendar.getInstance().getTime();
        final SimpleDateFormat dateFormate=new SimpleDateFormat("dd/MM/yyyy");

        final EditText input = new EditText(MainActivity.this);
        input.setHint(isFolder?getString(R.string.folder_name):getString(R.string.file_name));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);

        AlertDialog.Builder dialog=new AlertDialog.Builder(MainActivity.this);
        dialog.setTitle(isFolder?getString(R.string.add_folder):getString(R.string.add_file));
      //  dialog.setMessage(getString(R.string.folder_name));
        dialog.setView(input);

        dialog.setPositiveButton(getString(R.string.save), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if(input.getText().toString().trim().isEmpty()){
                    Toast.makeText(MainActivity.this, getString(R.string.please_enter_the_name_field), Toast.LENGTH_LONG).show();
                    return;
                }

                Contianer contianer=new Contianer();
                contianer.setId(0);
                contianer.setName(input.getText().toString().trim());
                contianer.setType(isFolder?"folder":"file");
                contianer.setPriority(isFolder?1:2); // 1 for folder and 2 for file
                contianer.setParentId(folderList.get(folderList.size()-1).getId());
                contianer.setCreatedAt(dateFormate.format(c));

                new InsertContainerAsync(contianer, MainActivity.this, new AsyncTaskCallback<Contianer>() {
                    @Override
                    public void handleResponse(Contianer response) {
                        if(isFolder) {
                            refreshScene();
                        }
                        else{
                            openFile(response);
                        }
                    }

                    @Override
                    public void handleFault(Exception e) {
                        Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }).execute();
            }
        });

        dialog.setNegativeButton(getString(R.string.cancel),null);

        dialog.show();
    }

    private void openFolder(Contianer contianer){
        folderList.add(contianer);
        refreshScene();
    }
    private void openFile(Contianer contianer){

        Common.selected_file=contianer;
        startActivity(new Intent(MainActivity.this,FileActivity.class));
    }

    private void back(){
        if(folderList.size()>1){
            folderList.remove(folderList.size()-1);
        }
        refreshScene();
    }

    private void refreshScene(){
        rvContainer.setAdapter(null);

        Contianer currentContainer=folderList.get(folderList.size()-1);

        //change toolbar title
        this.setTitle(currentContainer.getName());

        backFolder.setVisibility((folderList.size()>1)?View.VISIBLE:View.GONE);

        new GetAllContainerAsync(currentContainer.getId(), MainActivity.this, new AsyncTaskCallback<List<Contianer>>() {
            @Override
            public void handleResponse(List<Contianer> response) {
                rvContainer.setAdapter(new FolderAdapter(response,getApplicationContext(),MainActivity.this));
            }

            @Override
            public void handleFault(Exception e) {
              //  rvContainer.setAdapter(new FolderAdapter(new ArrayList<Contianer>(),getApplicationContext(),MainActivity.this));
                Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).execute();
    }

    @Override
    public void onContainerItemClick(Contianer contianer) {

        if(contianer.getType().equals("folder")){
            openFolder(contianer);
        }else{
            openFile(contianer);
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.folder_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.bigItem:switchIconSize(true);return true;
            case R.id.smallItem:switchIconSize(false);return true;
            case R.id.renameFolder:renameFolder();return true;
            case R.id.search: search();return true;
            case R.id.deleteFolder:deleteFolder();return true;
            case R.id.exportDB:exportDB();return true;
            case R.id.importDB:importDB();return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void deleteFolder() {

        if(folderList.size()>1) {
            AlertDialog.Builder dialog=new AlertDialog.Builder(MainActivity.this);
            dialog.setTitle(getString(R.string.delete)+" ' "+folderList.get(folderList.size()-1).getName()+" '");
            dialog.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    new DeleteContainerCotentAsync(folderList.get(folderList.size() - 1), MainActivity.this, new DeleteContainerCotentAsync.deleteCallBack() {
                        @Override
                        public void afterContainerDeleted() {
                            folderList.remove(folderList.size() - 1);
                            refreshScene();
                        }
                    }).execute();

                }
            });
            dialog.setNegativeButton(getString(R.string.cancel),null);
            dialog.show();
        }else{
            Toast.makeText(this, getString(R.string.main_folder_cant_be_deleted), Toast.LENGTH_SHORT).show();
        }
    }

    private void search() {

        startActivityForResult(new Intent(MainActivity.this,SearchActivity.class),SEARCH_REQUEST);

    }

    private  void renameFolder(){
        final Contianer currentContainer=folderList.get(folderList.size()-1);
        final EditText input = new EditText(MainActivity.this);
        input.setHint(getString(R.string.new_name));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        input.setText(currentContainer.getName());
        input.setSelection(0,input.getText().toString().length());

        AlertDialog.Builder dialog=new AlertDialog.Builder(MainActivity.this);
        dialog.setTitle(getString(R.string.rename));
        dialog.setView(input);
        dialog.setPositiveButton(getString(R.string.save), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (input.getText().toString().trim().isEmpty()){
                    Toast.makeText(MainActivity.this, getString(R.string.please_enter_the_name_field), Toast.LENGTH_SHORT).show();
                    return;
                }

                //change root name
                if(currentContainer.getId()==0){
                    MainActivity.this.setTitle(input.getText().toString().trim());
                    currentContainer.setName(input.getText().toString().trim());
                    preferences.edit().putString("rootFolderName",input.getText().toString().trim()).apply();

                }else{

                    currentContainer.setName(input.getText().toString().trim());
                    new UpdateContainerAsync(currentContainer, MainActivity.this, new AsyncTaskCallback<String>() {
                        @Override
                        public void handleResponse(String response) {
                            MainActivity.this.setTitle(input.getText().toString().trim());
                        }

                        @Override
                        public void handleFault(Exception e) {
                            Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_LONG).show();

                        }
                    }).execute();

                }


            }
        });
        dialog.setNegativeButton(getString(R.string.cancel),null);
        dialog.show();


    }

    private void switchIconSize(boolean isBig){

        if(isBig){
            rvContainer.setLayoutManager(new GridLayoutManager(MainActivity.this,2));
            isBigIcons=true;
            preferences.edit().putBoolean("iconSize",isBigIcons).apply();
        }else{
            rvContainer.setLayoutManager(new LinearLayoutManager(MainActivity.this));
            isBigIcons=false;
            preferences.edit().putBoolean("iconSize",isBigIcons).apply();
        }


    }

    @Override
    public void onBackPressed() {
        if(folderList.size()>1)
            back();
        else
            super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshScene();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==SEARCH_REQUEST){

            if(resultCode==RESULT_OK){
                folderList.clear();
                folderList.add(new Contianer(0,preferences.getString("rootFolderName",getString(R.string.home))));
                folderList.addAll(Common.tree_list);
                refreshScene();
            }else{

            }
        }

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList("folderList",(ArrayList<? extends Parcelable>) folderList);
    }


    private void exportDB(){

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED) {
            Connection.getInstance(this).getDatabase().close();
            File db = getDatabasePath("documents_dp");
//        File dbShm = new File(db.getParent(), "documents_dp-shm");
//        File dbWal = new File(db.getParent(), "documents_dp-wal");

            File db2 = new File(Environment.getExternalStorageDirectory().getPath(), "documents_dp");
//        File dbShm2 = new File(db2.getParent(), "documents_dp-shm");
//        File dbWal2 = new File(db2.getParent(), "documents_dp-wal");

            try {
                FileUtils.copyFile(db, db2);
                Toast.makeText(this, getString(R.string.database_has_been_exported), Toast.LENGTH_SHORT).show();
//            FileUtils.copyFile(dbShm, dbShm2);
//            FileUtils.copyFile(dbWal, dbWal2);
            } catch (Exception e) {
                Log.e("SAVEDB", e.toString());
            }
        }else{
            //ask for permission
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, EXPORT_DB);
        }
    }

    private void importDB(){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED) {
            Connection.getInstance(this).getDatabase().close();
            File db = new File(Environment.getExternalStorageDirectory().getPath(), "documents_dp");
//        File dbShm = new File(db.getParent(), "documents_dp-shm");
//        File dbWal = new File(db.getParent(), "documents_dp-wal");

            File db2 = getDatabasePath("documents_dp");
//        File dbShm2 = new File(db2.getParent(), "documents_dp-shm");
//        File dbWal2 = new File(db2.getParent(), "documents_dp-wal");

            try {
                FileUtils.copyFile(db, db2);
                refreshScene();
                Toast.makeText(this, getString(R.string.database_has_been_imported), Toast.LENGTH_SHORT).show();
//            FileUtils.copyFile(dbShm, dbShm2);
//            FileUtils.copyFile(dbWal, dbWal2);
            } catch (Exception e) {
                Log.e("RESTOREDB", e.toString());
            }
        }else{
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, IMPORT_DB);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==EXPORT_DB){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                exportDB();
            }

        }
        else if(requestCode==IMPORT_DB){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                importDB();
            }
        }



    }
}
