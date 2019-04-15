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

public class AlbumAdapter extends ArrayAdapter<Album> {

    public AlbumAdapter(Activity context, ArrayList<Album> albumDetail){
        super(context,0,albumDetail);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View albumGridItem = convertView;
        if(albumGridItem == null){
            albumGridItem = LayoutInflater.from(getContext()).inflate(R.layout.album_grid_item,parent,false);
        }
        Album currentAlbum = getItem(position);
        TextView albumName = (TextView)albumGridItem.findViewById(R.id.album_name);
        albumName.setText(currentAlbum.getAlbumName());
        return albumGridItem;
    }
}
