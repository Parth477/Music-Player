package com.example.test;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.test.Model.UploadSong;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.google.firebase.database.FirebaseDatabase.getInstance;

public class MainActivity extends AppCompatActivity {

       AppCompatEditText editTextTitle;
       TextView textViewImage;
       ProgressBar progressBar;
       Uri audioUri;
       DatabaseReference referenceSong;
       StorageReference mStorageReff;
       StorageTask mUploadTask;
       FirebaseStorage storage;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editTextTitle = (AppCompatEditText) findViewById(R.id.songTitle);
        textViewImage = (TextView) findViewById(R.id.txtViewSongFileSelected);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        referenceSong = getInstance().getReference().child("songs");
        storage = FirebaseStorage.getInstance();
        mStorageReff = storage.getReference().child("songs");

    }

    public  void openAudioFile(View view){
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("audio/*");
        startActivityForResult(i,101);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 101 && resultCode == RESULT_OK && data.getData() != null){
            audioUri = data.getData();
            String fileName = getFileName(audioUri);

            textViewImage.setText(fileName);
        }
    }

    private String getFileName(Uri audioUri) {
        String result = null;
        if(audioUri.getScheme().equals("content")){
            Cursor cursor = getContentResolver().query(audioUri,null,null,null,null);

            try{
                if(cursor != null && cursor.moveToFirst()){
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));

                }

            }finally {
                cursor.close();

            }

        }
        if(result == null){
            result = audioUri.getPath();
            int cut = result.lastIndexOf('/');
            if(cut != -1){
                result = result.substring(cut + 1);
            }

        }
        return result;
    }

    public void uploadAudioToFirebase(View view){
        if(textViewImage.getText().toString().equals("No file selected")) {
            Toast.makeText(getApplicationContext(), "Please Select an Image", Toast.LENGTH_LONG).show();
        }
        else{
            if(mUploadTask != null && mUploadTask.isInProgress()){
                Toast.makeText(getApplicationContext(),"Song upload is already in progress ",Toast.LENGTH_LONG).show();
            }else {
                uploadFile();
            }
        }
    }

    private void uploadFile() {
        if(audioUri != null){
            String durationTxt;
            Toast.makeText(getApplicationContext(),"Uploadding Please wait....",Toast.LENGTH_LONG).show();

            progressBar.setVisibility(View.VISIBLE);
            final StorageReference storageReference = mStorageReff.child(System.currentTimeMillis() + "." + getFileExtension(audioUri));
            int durationInMillis = findSongDuration(audioUri);

            if(durationInMillis == 0){
                durationTxt = "NA";
            }
            durationTxt = getDurationFromMilli(durationInMillis);

            //final String finalDurationTxt = durationTxt;
            final String finalDurationTxt = durationTxt;
            mUploadTask = storageReference.putFile(audioUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    UploadSong uploadSong = new UploadSong(editTextTitle.getText().toString(),
                                            finalDurationTxt,audioUri.toString());

                                    String uploadId = referenceSong.push().getKey();
                                    referenceSong.child(uploadId).setValue(uploadSong);

                                }
                            });


                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                          double progress = (100.0 *taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                          progressBar.setProgress((int) progress);
                        }
                    });


        }
        else{
            Toast.makeText(getApplicationContext(), "No file selected to Upload",Toast.LENGTH_LONG).show();

        }
    }

    private String getDurationFromMilli(int durationInMillis) {
        Date date =new Date(durationInMillis);
        SimpleDateFormat simple = new SimpleDateFormat("mm:ss", Locale.getDefault());
        String myTime = simple.format(date);
        return myTime;
    }

    private int findSongDuration(Uri audioUri) {
        int timeInMillis = 0;

        try{
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(this,audioUri);
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            timeInMillis = Integer.parseInt(time);

            retriever.release();
            return timeInMillis;

        }
        catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    private String getFileExtension(Uri audioUri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(audioUri));

    }

    public void openSongsActivity(View view) {
        Intent i = new Intent(MainActivity.this, ShowSongsActivity.class);
        startActivity(i);
    }
}
