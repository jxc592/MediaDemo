package com.example.myapplication.ui.main;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.IAudioPlayService;
import com.example.myapplication.R;
import com.example.myapplication.audio.AudioPlayActivity;
import com.example.myapplication.audio.AudioPlayService;
import com.example.myapplication.bean.MediaFileDescrtpter;
import com.example.myapplication.util.BitmapUtils;

import java.util.List;

public class AudioFragment extends PlaceholderFragment {
    
    private AudioAdapter mAdapter;

    ServiceConnection mServiceConnection;
    IAudioPlayService iAudioPlayService;
    @Override
    protected void init() {
        super.init();


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mListView.setLayoutManager(linearLayoutManager);
        mAdapter = new AudioAdapter();
        //Cursor audioCursor = getActivity().getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,null,null,null);
        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                iAudioPlayService = IAudioPlayService.Stub.asInterface(service);

                try {
                    List<String> listString = iAudioPlayService.getAudioList();

                    for (int i =0 ;i<listString.size();i++) {
                        MediaFileDescrtpter descrtpter = MediaFileDescrtpter.unmarshall(listString.get(i));
                        mDataList.add(descrtpter);
                    }
                    mListView.setAdapter(mAdapter);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                iAudioPlayService = null;
            }
        };
        Intent intent = new Intent(getActivity(), AudioPlayService.class);
        getActivity().bindService(intent,mServiceConnection, Activity.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(mServiceConnection != null && iAudioPlayService != null) {
            getActivity().unbindService(mServiceConnection);
        }
    }

    class AudidViewHolder extends RecyclerView.ViewHolder{

        View container;
        ImageView imageView;
        TextView title;
        TextView author;

        Bitmap mSouce;
        public AudidViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView;
            imageView = container.findViewById(R.id.iv_thumbuil);
            title = container.findViewById(R.id.tv_title);
            author = container.findViewById(R.id.tv_author);
        }

        void initData(final MediaFileDescrtpter mediaFileDescrtpter) {

            Bitmap source = BitmapUtils.getAlbumArt(getActivity(),mediaFileDescrtpter.getData());
            if(source != null) {
                imageView.setImageBitmap(source);
                mSouce = source;
            } else {
                imageView.setImageBitmap(null);
                mSouce = null;
            }
            String  strTitle = mediaFileDescrtpter.getTitle();

            if(strTitle !=null && strTitle.length() > 30) {
                strTitle = strTitle.substring(0,28)+"...";
            }
            title.setText(strTitle);
            author.setText(mediaFileDescrtpter.getArtist());

            title.setClickable(false);
            author.setClickable(false);
            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                    Intent intentService = new Intent(getActivity(), AudioPlayService.class);
                    intentService.putExtra("data",mediaFileDescrtpter);
                    getActivity().startForegroundService(intentService);


                    Intent intent = new Intent(getActivity(), AudioPlayActivity.class);
                    startActivity(intent);


                }
            });
        }


        void recycle(){
            if(mSouce != null) mSouce.recycle();
        }
    }

    class AudioAdapter extends RecyclerView.Adapter<AudidViewHolder> {


        @NonNull
        @Override
        public AudidViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_music,null);
            return new AudidViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AudidViewHolder holder, int position) {
            holder.initData(mDataList.get(position));
        }

        @Override
        public int getItemCount() {
            return mDataList.size();
        }

        @Override
        public void onViewRecycled(@NonNull AudidViewHolder holder) {
            super.onViewRecycled(holder);
            holder.recycle();

        }
    }
}
