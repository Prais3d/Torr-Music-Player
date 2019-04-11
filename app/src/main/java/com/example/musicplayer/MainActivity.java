package com.example.musicplayer;

import android.Manifest;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    EditText search;
    private ArrayAdapter myAdapter;

    ListView myListViewForSongs;
    String[] items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myListViewForSongs = (ListView) findViewById(R.id.mySongListView);
        search=(EditText)findViewById(R.id.searchFilter);

        runtimePermission();
    }

    public void runtimePermission(){
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {

                        display();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                        token.continuePermissionRequest();
                    }
                }).check();
    }

    public ArrayList<File> findSong(File file){

        ArrayList<File> arrayList=new ArrayList<>();
        File[] files = file.listFiles();

        for(File singleFile: files){

            if(singleFile.isDirectory() && !singleFile.isHidden()){

                arrayList.addAll(findSong(singleFile));
            }
            else{
                if(singleFile.getName().endsWith(".mp3") || singleFile.getName().endsWith(".wav")){
                   arrayList.add(singleFile);
                }
            }
        }
        Collections.sort(arrayList);
        return arrayList;
    }

    void display(){

        final ArrayList<File> mySongs = findSong(Environment.getExternalStorageDirectory());
        items = new String[mySongs.size()];

        for(int i=0; i<mySongs.size();i++){

            items[i] = mySongs.get(i).getName().toString().replace(".mp3", "").replace(".wav","");

        }
        myAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,items);
        myListViewForSongs.setAdapter(myAdapter);

        try {
            search.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    myAdapter.getFilter().filter(s);


                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }

        myListViewForSongs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long l) {

                Toast.makeText(MainActivity.this,myAdapter.getItem(i).toString(),Toast.LENGTH_LONG).show();
                String songName = myListViewForSongs.getItemAtPosition(i).toString();
                String itemName = (String) parent.getItemAtPosition(i);
                int position_fo_item = Arrays.asList(items).indexOf(itemName);

                startActivity(new Intent(getApplicationContext(),PlayerActivity.class)
                .putExtra("songs", mySongs).putExtra("songname",songName)
                .putExtra("pos",i));

            }
        });
    }

}
