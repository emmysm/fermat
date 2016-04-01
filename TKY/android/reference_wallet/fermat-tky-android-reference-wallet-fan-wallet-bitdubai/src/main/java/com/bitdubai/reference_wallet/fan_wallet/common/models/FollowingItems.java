package com.bitdubai.reference_wallet.fan_wallet.common.models;

/**
 * Created by Miguel Payarez on 16/03/16.
 */
public class FollowingItems {
    private String artist_url;
    private int imagen;
    private String artist_name;

    public FollowingItems(int imagen, String artist_url, String artist_name){

        this.imagen = imagen;
        this.artist_url = artist_url;
        this.artist_name=artist_name;

    }

    public int getImagen(){return imagen;}

    public String getArtist_url(){return artist_url;}

    public String getArtist_name(){return artist_name;}


}
