package com.example.test;

import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.test.Model.UploadSong;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ShowSongsActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    ProgressBar progressBar;

    List<UploadSong> mUpload;
    //FirebaseStorage mStorage;
    DatabaseReference databaseReference;

    ValueEventListener valueEventListener;
    MediaPlayer mediaPlayer;
    SongsAdapter adapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_songs);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        progressBar = (ProgressBar) findViewById(R.id.progressBarShowSongs);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mUpload = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference("songs");
        adapter = new SongsAdapter(ShowSongsActivity.this,mUpload);
        recyclerView.setAdapter(adapter);
        valueEventListener= databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    mUpload.clear();
                    for (DataSnapshot dss:dataSnapshot.getChildren())
                    {
                        UploadSong uploadSong = dss.getValue(UploadSong.class);
                        uploadSong.setmKey(dss.getKey());
                        mUpload.add(uploadSong);
                    }
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(getApplicationContext(),""+databaseError.getMessage(),Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
            }
        });

    }

    protected void onDestroy(){
        super.onDestroy();
        databaseReference.removeEventListener(valueEventListener);

    }

    public void playSong(List<UploadSong> arrayListSongs, int adapterPosition) throws IOException {

        UploadSong uploadSong= arrayListSongs.get(adapterPosition);

        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();;
            mediaPlayer = null;
        }

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setDataSource(uploadSong.getSongLink());

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();;
            }
        });
        mediaPlayer.prepareAsync();


    }
}
