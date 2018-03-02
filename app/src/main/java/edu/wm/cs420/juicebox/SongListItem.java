package edu.wm.cs420.juicebox;

/**
 * Created by mjcurcio on 3/1/18.
 */

public class SongListItem {

    private String songName;
    private String album;
    private String artist;
    private int lengthInSeconds;

    SongListItem(String name){
        name = songName;
    }

    SongListItem(String name, String album, String artist, int length){
        songName = name;
        this.album = album;
        this.artist = artist;
        lengthInSeconds = length;
    }

    public String getName(){
        return songName;
    }

    public String getAlbum(){
        return album;
    }

    public String getArtist(){
        return artist;
    }

    public int getLengthInSeconds(){
        return lengthInSeconds;
    }
}
