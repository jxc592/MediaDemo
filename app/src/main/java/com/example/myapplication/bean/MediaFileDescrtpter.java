package com.example.myapplication.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;

public class MediaFileDescrtpter implements Parcelable {
    //basic
    long id = -1 ;
    String data = "";
    String title ="";
    String album ="";
    String artist ="";
    String bookmart ="";
    String category ="";
    int height = 0;
    int width =0;
    String desctiption = "";
    long duration =0;
    String language ="";
    String resolution ="";
    //audio
    String year ="";
    long size = 0;
    long albumid = 0;

    @Deprecated
    String albumArtThumbil = "";


    public MediaFileDescrtpter() {
    }

    public MediaFileDescrtpter(long id, String data, String title, String album, String artist, String bookmart, String category, int height, int width, String desctiption, long duration, String language, String resolution) {
        this.id = id;
        this.data = data;
        this.title = title;
        this.album = album;
        this.artist = artist;
        this.bookmart = bookmart;
        this.category = category;
        this.height = height;
        this.width = width;
        this.desctiption = desctiption;
        this.duration = duration;
        this.language = language;
        this.resolution = resolution;
    }

    public MediaFileDescrtpter(long id, String data, String title, String album, String artist, String bookmart,String year,long size) {
        this.id = id;
        this.data =data;
        this.title = title;
        this.album = album;
        this.artist = artist;
        this.bookmart = bookmart;
        this.year = year;
        this.size = size;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getBookmart() {
        return bookmart;
    }

    public void setBookmart(String bookmart) {
        this.bookmart = bookmart;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public String getDesctiption() {
        return desctiption;
    }

    public void setDesctiption(String desctiption) {
        this.desctiption = desctiption;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
    public String getAlbumArtThumbil() {
        return albumArtThumbil;
    }

    public void setAlbumArtThumbil(String albumArtThumbil) {
        this.albumArtThumbil = albumArtThumbil;
    }

    public long getAlbumid() {
        return albumid;
    }

    public void setAlbumid(long albumid) {
        this.albumid = albumid;
    }


    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(this.id);
        parcel.writeString(this.data);
        parcel.writeString(this.title);
        parcel.writeString(this.album);
        parcel.writeString(this.artist);
        parcel.writeString(this.bookmart);
        parcel.writeString(this.category);
        parcel.writeInt(this.height);
        parcel.writeInt(this.width);
        parcel.writeString(this.desctiption);
        parcel.writeLong(this.duration);
        parcel.writeString(this.language);
        parcel.writeString(this.resolution);
        parcel.writeString(this.year);
        parcel.writeLong( this.size);
        parcel.writeLong(this.albumid);
        parcel.writeString(this.albumArtThumbil);
    }

    public String object2String() {
        // 1.序列化
        Parcel p = Parcel.obtain();
        this.writeToParcel(p, 0);
        byte[] bytes = p.marshall();
        p.recycle();

        // 2.编码
        String str = Base64.encodeToString(bytes, Base64.DEFAULT);
        return str;
    }

    public static Parcel unmarshall(byte[] bytes) {
        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(bytes, 0, bytes.length);
        parcel.setDataPosition(0); // this is extremely important!
        return parcel;
    }

    public static MediaFileDescrtpter unmarshall(String str) {
        // 1.解码
        byte[] bytes = Base64.decode(str, Base64.DEFAULT);
        // 2.反序列化
        Parcel parcel = unmarshall(bytes);
        return CREATOR.createFromParcel(parcel);
    }


    public static MediaFileDescrtpter read(Parcel parcel) {
        MediaFileDescrtpter descrtpter = new MediaFileDescrtpter();
        descrtpter.id = parcel.readLong();
        descrtpter.data =parcel.readString();
        descrtpter.title =parcel.readString();
        descrtpter.album =parcel.readString();
        descrtpter.artist =parcel.readString();
        descrtpter.bookmart =parcel.readString();
        descrtpter.category =parcel.readString();
        descrtpter.height =parcel.readInt();
        descrtpter.width =parcel.readInt();
        descrtpter.desctiption =parcel.readString();
        descrtpter.duration =parcel.readLong();
        descrtpter.language =parcel.readString();
        descrtpter.resolution =parcel.readString();
        descrtpter.year =parcel.readString();
        descrtpter.size =parcel.readLong( );
        descrtpter.albumid =parcel.readLong();
        descrtpter.albumArtThumbil =parcel.readString();
        return descrtpter;
    }

    public static final Parcelable.Creator<MediaFileDescrtpter> CREATOR = new Creator() {
        @Override
        public Object createFromParcel(Parcel parcel) {
            return read(parcel);
        }

        @Override
        public Object[] newArray(int i) {
            return new Object[0];
        }
    };

}
