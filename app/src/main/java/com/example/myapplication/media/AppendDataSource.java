package com.example.myapplication.media;

import android.media.MediaDataSource;

import java.io.IOException;

public class AppendDataSource extends MediaDataSource {
    @Override
    public int readAt(long position, byte[] buffer, int offset, int size) throws IOException {
        return 0;
    }

    @Override
    public long getSize() throws IOException {
        return 0;
    }

    @Override
    public void close() throws IOException {

    }
}
