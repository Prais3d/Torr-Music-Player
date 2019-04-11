package com.example.musicplayer;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PlayerActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_DRAW_OVERLAY_PERMISSION = 5;
    private FloatViewManager mFloatViewManager;

    private List<Utility.Track> mTrackList;
    private Utility mUtility;

    private DatabaseHelper databaseHelper;

    Button btn_next, btn_prev, btn_pause, btn_shuffle,btn_repeat ;
    TextView songTextLabel, elapsedTimeLabel, totalTimeLabel;;
    SeekBar songSeekbar;

    static MediaPlayer myMediaPlayer;
    int position;
    String sname;
	static int c=0;

    ArrayList<File> mySongs;
    Thread updateseekBar;

    private Handler handler= new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        databaseHelper = new DatabaseHelper(PlayerActivity.this);

        btn_next= (Button) findViewById(R.id.next);
        btn_prev= (Button) findViewById(R.id.prev);
        btn_pause= (Button) findViewById(R.id.pause);
        songTextLabel= (TextView) findViewById(R.id.songLabel);
        songSeekbar = (SeekBar) findViewById(R.id.seekbar);
        btn_shuffle= (Button) findViewById(R.id.shuffle);
        btn_repeat= (Button) findViewById(R.id.repeat);
        mUtility = new Utility(PlayerActivity.this);
        elapsedTimeLabel= (TextView) findViewById(R.id.elapsedTimeLabel);
        totalTimeLabel= (TextView) findViewById(R.id.totalDuration);

        mFloatViewManager = new FloatViewManager(PlayerActivity.this);
        findViewById(R.id.createBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkDrawOverlayPermission()) {
                    mFloatViewManager.showFloatView();
                }
            }
        });

        getSupportActionBar().setTitle("Now Playing");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        updateseekBar = new Thread(){

            @Override
            public void run() {

                int totalDuration= myMediaPlayer.getDuration();
                int currentPosition= 0;
                while(currentPosition< totalDuration ){
                    try {
                        sleep(500);
                        currentPosition = myMediaPlayer.getCurrentPosition();
                        songSeekbar.setProgress(currentPosition);
                    }
                    catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        };

        if(myMediaPlayer!=null){
            myMediaPlayer.stop();
            myMediaPlayer.release();
        }
        Intent i = getIntent();
        Bundle bundle = i.getExtras();

        mySongs= (ArrayList) bundle.getParcelableArrayList("songs");

        sname = mySongs.get(position).getName().toString();

        String songName= i.getStringExtra("songname");

        songTextLabel.setText(songName);
        songTextLabel.setSelected(true);

        position= bundle.getInt("pos",0);

        Uri u = Uri.parse(mySongs.get(position).toString());

        myMediaPlayer = MediaPlayer.create(getApplicationContext(),u);
        databaseHelper.update(mTrackList);

        myMediaPlayer.start();

        mFloatViewManager.view =databaseHelper.getLyrics(songName);
        songSeekbar.setMax(myMediaPlayer.getDuration());
        updateseekBar.start();

        songSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                updateTimer();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                handler.removeCallbacks(mUpdateTimeTask);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                handler.removeCallbacks(mUpdateTimeTask);
                myMediaPlayer.seekTo(seekBar.getProgress());
                handler.post(mUpdateTimeTask);

            }
        });

        myMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                myMediaPlayer.release();
                position = (position+1)%mySongs.size();
                Uri u1 = Uri.parse(mySongs.get(position).toString());
                myMediaPlayer= MediaPlayer.create(getApplicationContext(),u1);
                sname= mySongs.get(position).getName().toString();
                songTextLabel.setText(sname);
                myMediaPlayer.start();

            }
        });
		
		btn_repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(c==0){
                    btn_repeat.setBackgroundResource(R.drawable.icon_repeatblack);
                    Toast.makeText(PlayerActivity.this,"Repeat On", Toast.LENGTH_SHORT ).show();
                    c++;
                }
                else{
                    btn_repeat.setBackgroundResource(R.drawable.icon_repeat);
                    Toast.makeText(PlayerActivity.this,"Repeat Off", Toast.LENGTH_SHORT ).show();
                    c=0;
                }
            }
        });
        btn_shuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(c==0){
                    btn_shuffle.setBackgroundResource(R.drawable.icon_shuffleblack);
                    Toast.makeText(PlayerActivity.this,"Shuffle On", Toast.LENGTH_SHORT ).show();
                    c++;
                }
                else{
                    btn_shuffle.setBackgroundResource(R.drawable.icon_shuffle);
                    Toast.makeText(PlayerActivity.this,"Shuffle Off", Toast.LENGTH_SHORT ).show();
                    c=0;
                }
            }
        });



        btn_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                songSeekbar.setMax(myMediaPlayer.getDuration());

                if(myMediaPlayer.isPlaying()){

                    btn_pause.setBackgroundResource(R.drawable.icon_play);
                    myMediaPlayer.pause();
                }
                else{
                    btn_pause.setBackgroundResource(R.drawable.icon_pause);
                    myMediaPlayer.start();
                }
            }
        });

        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myMediaPlayer.stop();
                myMediaPlayer.release();
                position = (position+1)%mySongs.size();

                Uri u = Uri.parse(mySongs.get(position).toString());

                myMediaPlayer= MediaPlayer.create(getApplicationContext(),u);

                sname= mySongs.get(position).getName().toString();
                songTextLabel.setText(sname);
                myMediaPlayer.start();
            }
        });

        btn_prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myMediaPlayer.stop();
                myMediaPlayer.release();

                position = ((position-1)<0)?(mySongs.size()-1):(position-1);
                Uri u = Uri.parse(mySongs.get(position).toString());
                myMediaPlayer = MediaPlayer.create(getApplicationContext(),u);

                sname= mySongs.get(position).getName().toString();
                songTextLabel.setText(sname);
                myMediaPlayer.start();

            }
        });
    }
    private Runnable mUpdateTimeTask= new Runnable() {
        @Override
        public void run() {
            updateTimer();
            if (myMediaPlayer.isPlaying()){
                handler.postDelayed(this,100);
            }

        }
    };

    private void updateTimer(){
        long totalDuration=myMediaPlayer.getDuration();
        long currentDuration=myMediaPlayer.getCurrentPosition();
        totalTimeLabel.setText(mUtility.millisecondsToTime(totalDuration));
        elapsedTimeLabel.setText(mUtility.millisecondsToTime(currentDuration));
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_DRAW_OVERLAY_PERMISSION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
                mFloatViewManager.showFloatView();
            }
        }
    }

    private boolean checkDrawOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_CODE_DRAW_OVERLAY_PERMISSION);
            return false;
        } else {
            return true;
        }
    }


}
