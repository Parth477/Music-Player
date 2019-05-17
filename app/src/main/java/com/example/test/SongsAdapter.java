package com.example.test;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.test.Model.UploadSong;

import java.io.IOException;
import java.util.List;

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.SongsAdapterHolder> {


    Context context;
    List<UploadSong> arrayListSongs;

    public SongsAdapter(Context context, List<UploadSong> arrayListSongs) {
        this.context = context;
        this.arrayListSongs = arrayListSongs;
    }

    @NonNull
    @Override
    public SongsAdapterHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.song_item,viewGroup,false);
        return new SongsAdapterHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongsAdapterHolder holder, int i) {

        UploadSong uploadSong = arrayListSongs.get(i);
        holder.titleTxt.setText(uploadSong.getSongTitle());
        holder.durationTxt.setText(uploadSong.getSongDuration());

    }

    @Override
    public int getItemCount() {

        return arrayListSongs.size();
    }

    public class SongsAdapterHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView titleTxt,durationTxt;
        public SongsAdapterHolder(@NonNull View itemView) {
            super(itemView);

            titleTxt = itemView.findViewById(R.id.song_title);
            durationTxt = itemView.findViewById(R.id.song_duration);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            try {
                ((ShowSongsActivity)context).playSong
                        (arrayListSongs,getAdapterPosition());
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }


}
