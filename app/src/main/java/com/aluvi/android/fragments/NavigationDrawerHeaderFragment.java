package com.aluvi.android.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aluvi.android.R;
import com.aluvi.android.fragments.base.BaseButterFragment;
import com.aluvi.android.managers.UserStateManager;
import com.aluvi.android.model.realm.Profile;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by usama on 7/31/15.
 */
public class NavigationDrawerHeaderFragment extends BaseButterFragment {
    public interface ProfileRequestedListener {
        void onProfileRequested();
    }

    @Bind(R.id.navigation_drawer_header_profile_picture_view) CircleImageView mProfilePictureImageView;
    @Bind(R.id.navigation_drawer_header_text_view_user_name) TextView mUserNameTextView;

    private ProfileRequestedListener mProfileRequestedListener;
    private boolean refreshUI = true;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mProfileRequestedListener = (ProfileRequestedListener) activity;
    }

    @Override
    public View getRootView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_navigation_drawer_header, container, false);
    }

    @Override
    public void initUI() {
        Profile profile = UserStateManager.getInstance().getProfile();
        if (profile != null) {
            String fullName = profile.getFirstName() + " " + profile.getLastName();
            mUserNameTextView.setText(fullName);

            String smallImageUrl = profile.getSmallImageUrl();

            if(smallImageUrl != null) {
                Picasso.with(getActivity()).load(profile.getSmallImageUrl())
                        .fit().centerCrop()
                        .placeholder(R.mipmap.profile_picture_placeholder)
                        .error(R.mipmap.profile_picture_placeholder)
                        .into(mProfilePictureImageView);
            }
        }

        refreshUI = false;
    }


    @Override
    public void onResume() {
        super.onResume();
        if (refreshUI)
            initUI();
        refreshUI = true;
    }

    @OnClick(R.id.navigation_drawer_header_root)
    public void onProfileClicked() {
        mProfileRequestedListener.onProfileRequested();
    }
}
