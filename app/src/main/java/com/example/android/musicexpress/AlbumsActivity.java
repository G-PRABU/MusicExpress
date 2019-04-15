package com.example.android.musicexpress;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import java.util.ArrayList;

public class AlbumsActivity extends AppCompatActivity {

    GridView albumGridView;
    ArrayList<Album> albums;
    AlbumAdapter albumAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_albums);
        setTitle(getString(R.string.album_activity_title));
        albumGridView = findViewById(R.id.albums_grid_view);
        albums = new ArrayList<Album>();
        getAllAlbum();
    }

    public void getAllAlbum(){
        String albumName;
        int albumId;
        Uri albumUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        String[] albumProjection = {MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ALBUM_ID};
        String groupBy = MediaStore.Audio.Media.ALBUM;
        Cursor albumCursor = contentResolver.query(albumUri,albumProjection,null,null,groupBy);
        albumCursor.moveToFirst();
        albumName = albumCursor.getString(albumCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
        albumId = albumCursor.getInt(albumCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
        albums.add(new Album(albumName,albumId));
        do {
            int id = albumCursor.getInt(albumCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
            if(id != albumId) {
                albumName = albumCursor.getString(albumCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                albumId = albumCursor.getInt(albumCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                albums.add(new Album(albumName, albumId));
            }
        } while (albumCursor.moveToNext());
        albumAdapter = new AlbumAdapter(this,albums);
        albumGridView.setAdapter(albumAdapter);
        albumGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent albumSongsActivity = new Intent(AlbumsActivity.this,AlbumSongsActivity.class);
                albumSongsActivity.putExtra(getString(R.string.album_id_key),String.valueOf(albums.get(position).getAlbumId()));
                startActivity(albumSongsActivity);
            }
        });
    }
}
