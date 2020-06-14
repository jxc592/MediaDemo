package com.example.myapplication.ui.main;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.bean.MediaFileDescrtpter;
import com.example.myapplication.util.BitmapUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlaceholderFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    private PageViewModel pageViewModel;


    protected RecyclerView mListView;

    protected List<MediaFileDescrtpter> mDataList = new ArrayList<>();
    protected boolean isContentViewSeted;
    protected float mActivityWidth;
    protected View rootView;

    public static PlaceholderFragment newInstance(int index) {
        PlaceholderFragment fragment;
        switch (index) {
            case 1:
                fragment = new VideoFragment();
                break;
            case 2:
                fragment = new AudioFragment();
                break;
            default:
                fragment = new PlaceholderFragment();
                break;
        }
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = ViewModelProviders.of(this).get(PageViewModel.class);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        pageViewModel.setIndex(index);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        rootView= inflater.inflate(R.layout.fragment_content_main, container, false);
//        final TextView textView = rootView.findViewById(R.id.section_label);
//        pageViewModel.getText().observe(this, new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });

        init();

        return rootView;
    }

    public <T extends View> T findViewById(@IdRes int id) {
        return rootView.findViewById(id);
    }

    protected void init(){

        mListView = findViewById(R.id.main_list_view);


    }



}