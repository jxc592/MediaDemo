package com.example.myapplication.ui.main;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.myapplication.BuildConfig;
import com.example.myapplication.R;
import com.example.myapplication.bean.MediaFileDescrtpter;
import com.example.myapplication.util.BitmapUtils;
import com.example.myapplication.video.VideoPlayActivity;

import java.io.File;
import java.util.List;

public class ImageFragment extends PlaceholderFragment {

    VideoAdapter mAdapter;


    @Override
    protected void init() {
        super.init();

        StaggeredGridLayoutManager gridLayoutManager = new StaggeredGridLayoutManager(3,RecyclerView.VERTICAL);
        gridLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mListView.setLayoutManager(gridLayoutManager);
        mAdapter = new VideoAdapter();
        Cursor imageCursor = getActivity().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,null,null,null,"_ID desc");

        if(imageCursor != null ) {
            while (imageCursor.moveToNext()) {
                int id =imageCursor.getInt(imageCursor.getColumnIndex(MediaStore.Images.ImageColumns._ID));
                String data =imageCursor.getString(imageCursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
                String title = imageCursor.getString(imageCursor.getColumnIndex(MediaStore.Images.ImageColumns.TITLE));

                int height = imageCursor.getInt(imageCursor.getColumnIndex(MediaStore.Images.ImageColumns.HEIGHT));
                int width = imageCursor.getInt(imageCursor.getColumnIndex(MediaStore.Images.ImageColumns.WIDTH));
                String desctiption = imageCursor.getString(imageCursor.getColumnIndex(MediaStore.Images.ImageColumns.DESCRIPTION));
                long duration = imageCursor.getLong(imageCursor.getColumnIndex(MediaStore.Images.ImageColumns.DURATION));

                MediaFileDescrtpter mediaFileDescrtpter = new MediaFileDescrtpter( id,  data ,title,  "",  "",  "",  "",  height,  width,  desctiption,  duration,  "",  "");
                mDataList.add(mediaFileDescrtpter);

            }
            imageCursor.close();
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
            Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(container.getContext().getContentResolver(),mediaFileDescrtpter.getId(),1,null);
            mActivityWidth = mListView.getMeasuredWidth()/3 -20 ;
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
