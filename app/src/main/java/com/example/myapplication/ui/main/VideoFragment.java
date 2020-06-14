package com.example.myapplication.ui.main;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.myapplication.BuildConfig;
import com.example.myapplication.IAudioPlayService;
import com.example.myapplication.R;
import com.example.myapplication.audio.AudioPlayService;
import com.example.myapplication.bean.MediaFileDescrtpter;
import com.example.myapplication.util.BitmapUtils;
import com.example.myapplication.video.VideoPlayActivity;

import java.io.File;
import java.util.List;

public class VideoFragment extends PlaceholderFragment {

    VideoAdapter mAdapter;


    @Override
    protected void init() {
        super.init();

        StaggeredGridLayoutManager gridLayoutManager = new StaggeredGridLayoutManager(2,RecyclerView.VERTICAL);
        gridLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mListView.setLayoutManager(gridLayoutManager);
        mAdapter = new VideoAdapter();
        Cursor videoCursor = getActivity().getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,null,null,null);

        if(videoCursor != null ) {
            while (videoCursor.moveToNext()) {
                int id =videoCursor.getInt(videoCursor.getColumnIndex(MediaStore.Video.VideoColumns._ID));
                String data =videoCursor.getString(videoCursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA));
                String title = videoCursor.getString(videoCursor.getColumnIndex(MediaStore.Video.VideoColumns.TITLE));
                String album = videoCursor.getString(videoCursor.getColumnIndex(MediaStore.Video.VideoColumns.ALBUM));
                String artist = videoCursor.getString(videoCursor.getColumnIndex(MediaStore.Video.VideoColumns.ARTIST));
                String bookmart = videoCursor.getString(videoCursor.getColumnIndex(MediaStore.Video.VideoColumns.BOOKMARK));
                String category = videoCursor.getString(videoCursor.getColumnIndex(MediaStore.Video.VideoColumns.CATEGORY));
                int height = videoCursor.getInt(videoCursor.getColumnIndex(MediaStore.Video.VideoColumns.HEIGHT));
                int width = videoCursor.getInt(videoCursor.getColumnIndex(MediaStore.Video.VideoColumns.WIDTH));
                String desctiption = videoCursor.getString(videoCursor.getColumnIndex(MediaStore.Video.VideoColumns.DESCRIPTION));
                long duration = videoCursor.getLong(videoCursor.getColumnIndex(MediaStore.Video.VideoColumns.DURATION));
                String language = videoCursor.getString(videoCursor.getColumnIndex(MediaStore.Video.VideoColumns.LANGUAGE));
                String resolution = videoCursor.getString(videoCursor.getColumnIndex(MediaStore.Video.VideoColumns.RESOLUTION));
                MediaFileDescrtpter mediaFileDescrtpter = new MediaFileDescrtpter( id,  data ,title,  album,  artist,  bookmart,  category,  height,  width,  desctiption,  duration,  language,  resolution);
                mDataList.add(mediaFileDescrtpter);

            }
            videoCursor.close();
            mListView.setAdapter(mAdapter);
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    class VideoViewHolder extends RecyclerView.ViewHolder{

        View container;
        ImageView imageView;
        TextView title;
        List<TextView> mContentList;

        Bitmap mSource;
        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView;
            imageView = container.findViewById(R.id.iv_thumbuil);
            title = container.findViewById(R.id.tv_title);
        }

        void initData(final MediaFileDescrtpter mediaFileDescrtpter) {
            Bitmap bitmap = MediaStore.Video.Thumbnails.getThumbnail(container.getContext().getContentResolver(),mediaFileDescrtpter.getId(),1,null);
            mActivityWidth = mListView.getMeasuredWidth()/2 -50 ;
            Log.d("jxc","activiy width " + mActivityWidth);

            float imageW = bitmap.getWidth();

            float imageH = bitmap.getHeight() * (mActivityWidth/imageW);


            Bitmap source = BitmapUtils.scaleBitmap(bitmap,(int)mActivityWidth, (int)imageH);
            mSource = source;
            imageView.setImageBitmap(source);
            title.setText(mediaFileDescrtpter.getTitle());

            imageView.setClickable(false);
            title.setClickable(false);
            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), VideoPlayActivity.class);
                    Uri uri = null;
                    if (mediaFileDescrtpter != null && mediaFileDescrtpter.getData() != null) {
                        File file = new File(mediaFileDescrtpter.getData());

                       /* if(Build.VERSION.SDK_INT>= 27) {

                        } else */

                       if (Build.VERSION.SDK_INT >= 24) {//android 7.0以上
                            uri = FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID.concat(".provider"), file);
                        } else {
                            uri = Uri.fromFile(file);
                        }
                    }

                    intent.setData(uri);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    if (Build.VERSION.SDK_INT >= 24) {
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                    intent.putExtra("data",mediaFileDescrtpter);
                    startActivity(intent);
                }
            });
        }

        void recycle(){
            if(mSource != null) mSource.recycle();
        }
    }

    class VideoAdapter extends RecyclerView.Adapter<VideoViewHolder> {


        @NonNull
        @Override
        public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_preview,null);
            return new VideoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
            holder.initData(mDataList.get(position));
        }

        @Override
        public int getItemCount() {
            return mDataList.size();
        }

        @Override
        public void onViewRecycled(@NonNull VideoViewHolder holder) {
            super.onViewRecycled(holder);
            holder.recycle();

        }
    }



}
