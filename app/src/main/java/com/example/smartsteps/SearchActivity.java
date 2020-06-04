package com.example.smartsteps;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import com.example.smartsteps.Adapter.FolderAdapter;
import com.example.smartsteps.Async.AsyncTaskCallback;
import com.example.smartsteps.Async.GetContainersBySearchAsync;
import com.example.smartsteps.Async.GetParentTreeAsync;
import com.example.smartsteps.Common.Common;
import com.example.smartsteps.Room.Contianer;

import java.util.List;

public class SearchActivity extends AppCompatActivity implements FolderAdapter.ContainerItemClick {

    Toolbar toolbar;
    RecyclerView rvSearch;
    EditText etSearch;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.search));
        toolbar.setNavigationOnClickListener(view -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        etSearch=findViewById(R.id.etSearch);
        rvSearch=findViewById(R.id.rvSearch);
        rvSearch.setHasFixedSize(true);
        rvSearch.setLayoutManager(new LinearLayoutManager(SearchActivity.this));

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    new GetContainersBySearchAsync(charSequence.toString(), SearchActivity.this, new AsyncTaskCallback<List<Contianer>>() {
                        @Override
                        public void handleResponse(List<Contianer> response) {
                            rvSearch.setAdapter(new FolderAdapter(response,SearchActivity.this,SearchActivity.this));

                        }

                        @Override
                        public void handleFault(Exception e) {
                            rvSearch.setAdapter(null);
                        }
                    }).execute();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    @Override
    public void onContainerItemClick(Contianer contianer) {

     new GetParentTreeAsync(contianer.getId(), SearchActivity.this, new AsyncTaskCallback<List<Contianer>>() {
         @Override
         public void handleResponse(List<Contianer> response) {
             Common.tree_list.clear();
             Common.tree_list.addAll(response);
             if(contianer.getType().equals("file")){
                 Common.selected_file=contianer;
                 startActivity(new Intent(SearchActivity.this,FileActivity.class));
                 Common.tree_list.remove(Common.tree_list.size()-1);
             }
             setResult(RESULT_OK);
             finish();
         }

         @Override
         public void handleFault(Exception e) {

         }
     }).execute();

    }
}
