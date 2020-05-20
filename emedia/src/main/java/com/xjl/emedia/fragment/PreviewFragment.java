package com.xjl.emedia.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.xjl.emedia.R;
import com.xjl.emedia.activity.VideoRecordActivity;
import com.xjl.emedia.utils.IntentUtil;

import java.io.File;

/**
 * Created by 180933664 on 2019/6/12.
 */

public class PreviewFragment extends Fragment implements View.OnClickListener {

    private final String TAG = PreviewFragment.class.getSimpleName();

    protected ImageView playerBackground;
    protected ImageView player;
    protected ImageView cancelSelect;
    protected ImageView select;
    protected RelativeLayout playerLayout;
    private View rootView;

    private String filePath;
    private RequestOptions requestOptions;

    public static PreviewFragment getINSTANCE(String filePath) {
        PreviewFragment previewFragment = new PreviewFragment();
        Bundle bundle = new Bundle();
        bundle.putString("folderPath", filePath);
        previewFragment.setArguments(bundle);
        return previewFragment;
    }


    public PreviewFragment() {
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        requestOptions = new RequestOptions();
        requestOptions.skipMemoryCache(false);
        requestOptions.centerCrop();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_preview, null);
        filePath = getArguments().getString("folderPath");
        Log.e(TAG, "folderPath=" + filePath);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        playerBackground = (ImageView) rootView.findViewById(R.id.player_background);
        player = (ImageView) rootView.findViewById(R.id.player);
        player.setOnClickListener(PreviewFragment.this);
        cancelSelect = (ImageView) rootView.findViewById(R.id.cancel_select);
        cancelSelect.setOnClickListener(PreviewFragment.this);
        select = (ImageView) rootView.findViewById(R.id.select);
        select.setOnClickListener(PreviewFragment.this);
        playerLayout = (RelativeLayout) rootView.findViewById(R.id.player_layout);

        Glide.with(getActivity()).load("file://" + filePath)
                .apply(requestOptions)
                .into(playerBackground);

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.player) {
            IntentUtil.openLocalVideo(getActivity(), filePath);
        } else if (view.getId() == R.id.cancel_select) {
            File file = new File(filePath);
            file.delete();
            getActivity().setResult(VideoRecordActivity.RESULT_CODE_FOR_RECORD_VIDEO_CANCEL);
            getActivity().finish();
        } else if (view.getId() == R.id.select) {
            Intent intent = new Intent();
            intent.putExtra(VideoRecordActivity.INTENT_EXTRA_VIDEO_PATH, filePath);
            getActivity().setResult(Activity.RESULT_OK, intent);
            getActivity().finish();
        }
    }

    public void onKeyDown() {
        File file = new File(filePath);
        file.delete();
        getActivity().setResult(VideoRecordActivity.RESULT_CODE_FOR_RECORD_VIDEO_CANCEL);
        getActivity().finish();
    }

}
