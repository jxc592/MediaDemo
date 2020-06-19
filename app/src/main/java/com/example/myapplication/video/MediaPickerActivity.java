package com.example.myapplication.video;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.BuildConfig;
import com.example.myapplication.R;
import com.example.myapplication.bean.MediaFileDescrtpter;
import com.example.myapplication.image.ImageDisplayActivity;
import com.example.myapplication.util.BitmapUtils;
import com.example.myapplication.widget.NumberDrawable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MediaPickerActivity extends AppCompatActivity implements MenuItem.OnMenuItemClickListener{

    RecyclerView mListView;

    //数据
    List<MediaFileDescrtpter> mDataList;
    //逻辑
    HashMap<Integer,Boolean> mCheckStates;

    RecyclerView.Adapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_picker);

        getSupportActionBar().setTitle("video picker");
        mDataList = new ArrayList<>();
        mCheckStates = new HashMap<>();
        mListView = findViewById(R.id.main_list_view);
        initListView();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_done,menu);

        menu.findItem(R.id.menu_id_done).setOnMenuItemClickListener(this);
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_id_done:
                onSelectCompleted();
                break;
                default:break;
        }
        return true;
    }

    void onSelectCompleted(){


        ArrayList<String> resultList = new ArrayList<>();
        Iterator map1it=mCheckStates.entrySet().iterator();
        while(map1it.hasNext())
        {
            Map.Entry<Integer, Boolean> entry=(Map.Entry<Integer, Boolean>) map1it.next();
            Log.d("jxc","Key: "+entry.getKey()+" Value: "+entry.getValue());

            if(entry.getValue().booleanValue()) {
                resultList.add(mDataList.get(entry.getKey().intValue()).object2String());
            }
        }

        Intent intent = new Intent();
        intent.putStringArrayListExtra("result",resultList);
        setResult(RESULT_OK,intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }


    void initListView(){
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,4);
        gridLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mListView.setLayoutManager(gridLayoutManager);
        mAdapter = new VideoAdapter();
        Cursor videoCursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,null,null,null,"_ID desc");

        int i = 0;
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
                mCheckStates.put(i,false);
                i++;
            }
            videoCursor.close();
            mListView.setAdapter(mAdapter);
        }
    }



    class VideoViewHolder extends RecyclerView.ViewHolder{

        View container;
        ImageView imageView;
        CheckBox cb_pick;
        List<TextView> mContentList;

        Bitmap mSource;
        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView;
            imageView = container.findViewById(R.id.iv_thumbuil);
            cb_pick = container.findViewById(R.id.cb_pick);

        }

        void initData(final MediaFileDescrtpter mediaFileDescrtpter,int index) {
            Bitmap bitmap = MediaStore.Video.Thumbnails.getThumbnail(container.getContext().getContentResolver(),mediaFileDescrtpter.getId(),1,null);
            float a = mListView.getMeasuredWidth()/4 ;
            Log.d("jxc","target width " + a);
            Bitmap source = BitmapUtils.getBitmapForPicker(bitmap,a, a);
            mSource = source;
            imageView.setImageBitmap(source);
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) imageView.getLayoutParams();
            lp.width = (int)a;
            lp.height = (int)a;
            imageView.setLayoutParams(lp);

            //cb_pick.setBackground(new NumberDrawable(index,mCheckStates.get(index)));
            cb_pick.setChecked(mCheckStates.get(index));
            cb_pick.setTag(index);
            cb_pick.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int idx = (int) buttonView.getTag();
                    mCheckStates.put(idx,isChecked);
                }
            });
            //title.setText(mediaFileDescrtpter.getTitle());

            imageView.setClickable(false);
            //title.setClickable(false);
            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MediaPickerActivity.this, VideoPlayActivity.class);
                    Uri uri = null;
                    if (mediaFileDescrtpter != null && mediaFileDescrtpter.getData() != null) {
                        File file = new File(mediaFileDescrtpter.getData());

                       /* if(Build.VERSION.SDK_INT>= 27) {

                        } else */

                        if (Build.VERSION.SDK_INT >= 24) {//android 7.0以上
                            uri = FileProvider.getUriForFile(MediaPickerActivity.this, BuildConfig.APPLICATION_ID.concat(".provider"), file);
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_picker,null);
            return new VideoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
            holder.initData(mDataList.get(position), position);
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
