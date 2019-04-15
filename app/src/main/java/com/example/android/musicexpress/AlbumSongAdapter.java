package com.example.android.musicexpress;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;

public class AlbumSongAdapter extends ArrayAdapter<Song> {
    public AlbumSongAdapter(Activity context, ArrayList<Song> songDetail){
        super(context,0,songDetail);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String songName;
        String artistName;
        String songDuration;
        View songListItem = convertView;
        if(songListItem == null){
            songListItem = LayoutInflater.from(getContext()).inflate(R.layout.song_list_item,parent,false);
        }
        Song currentSong = getItem(position);
        TextView name = (TextView)songListItem.findViewById(R.id.song_name);
        TextView artist = (TextView)songListItem.findViewById(R.id.song_artist_name);
        TextView duration = (TextView)songListItem.findViewById(R.id.song_duration);
        name.setText(currentSong.getSongName());
        artist.setText(currentSong.getArtistName());
        duration.setText(currentSong.getSongDuration());
        return songListItem;
    }
}

