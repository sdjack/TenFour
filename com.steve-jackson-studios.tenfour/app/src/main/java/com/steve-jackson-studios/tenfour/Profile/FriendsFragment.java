package com.steve-jackson-studios.tenfour.Profile;

import android.content.Context;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.steve-jackson-studios.tenfour.AppConstants;
import com.steve-jackson-studios.tenfour.AppResolver;
import com.steve-jackson-studios.tenfour.Data.FriendData;
import com.steve-jackson-studios.tenfour.Data.FriendEntity;
import com.steve-jackson-studios.tenfour.Media.GlideApp;
import com.steve-jackson-studios.tenfour.Misc.ResolverFragment;
import com.steve-jackson-studios.tenfour.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by sjackson on 4/19/2018.
 * 
 */

public class FriendsFragment extends ResolverFragment {

    private ListView mFriendsList;
    private FetchActivity asyncLoader;
    private boolean firstLoad = false;

    FriendsFeedAdapter adapter;

    private static class FriendHolder {
        LinearLayout aura;
        ImageView avatarImage;
        TextView avatarText;
        TextView name;
        TextView status;
        Button inviteAccept;
        Button inviteDeny;
    }

    public static FriendsFragment newInstance(AppResolver appResolver) {

        FriendsFragment instance = new FriendsFragment();
        instance.setResolver(appResolver);

        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Context context = getActivity();
        View view = inflater.inflate(R.layout.profile_friends_layout, container, false);
        mFriendsList = (ListView) view.findViewById(R.id.friends_listview);

        adapter = new FriendsFeedAdapter(context, R.layout.profile_friends_item, new ArrayList<FriendEntity>());
        mFriendsList.setAdapter(adapter);
        new FetchActivity().execute();
        return view;
    }



    @Override
    public void refresh() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (asyncLoader == null) {
                    asyncLoader = new FetchActivity();
                    asyncLoader.execute();
                }
            }
        });
    }

    private void updateAdapterData(final ArrayList<FriendEntity> requests, final ArrayList<FriendEntity> friends) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                adapter.clear();
                adapter.addAll(requests);
                adapter.addAll(friends);
                adapter.notifyDataSetChanged();

                if (!firstLoad) {
                    firstLoad = true;
                    mFriendsList.setSelection(0);
                }
            }
        });
    }

    private class FetchActivity extends AsyncTask<Void, String, Boolean> {

        private final ArrayList<FriendEntity> requests = new ArrayList<>();
        private final ArrayList<FriendEntity> friends = new ArrayList<>();

        @Override
        protected void onPostExecute(Boolean result) {
            asyncLoader = null;
            if (result)
                updateAdapterData(requests, friends);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            HashMap<String, FriendEntity> source = FriendData.getAll();
            if (!source.isEmpty()) {
                Set set = source.entrySet();
                Iterator iterator = set.iterator();
                while(iterator.hasNext()) {
                    Map.Entry mentry = (Map.Entry)iterator.next();
                    FriendEntity friend = (FriendEntity)mentry.getValue();
                    if (!friend.isBlocked()) {
                        if (friend.isRequest()) {
                            requests.add(friend);
                        } else {
                            friends.add(friend);
                        }
                    }
                }
                return true;
            }
            return false;
        }

        @Override
        protected void onCancelled() {
            asyncLoader = null;
        }
    }

    private class FriendsFeedAdapter extends ArrayAdapter<FriendEntity> {

        public FriendsFeedAdapter(Context context, int resource, ArrayList<FriendEntity> data) {
            super(context, resource, data);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {

            final Context context = getActivity();

            View vi = convertView;
            FriendHolder holder;
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                vi = inflater.inflate(R.layout.profile_friends_item, parent, false);
                holder = new FriendHolder();
                holder.aura = (LinearLayout) vi.findViewById(R.id.friend_avatar_aura);
                holder.avatarText = (TextView) vi.findViewById(R.id.friend_avatar_type0);
                holder.avatarImage = (ImageView) vi.findViewById(R.id.friend_avatar_type1);
                holder.name = (TextView) vi.findViewById(R.id.friend_username);
                holder.status = (TextView) vi.findViewById(R.id.friend_status);
                holder.inviteDeny = (Button) vi.findViewById(R.id.friend_deny_button);
                holder.inviteAccept = (Button) vi.findViewById(R.id.friend_accept_button);
                vi.setTag(holder);
            } else {
                holder = (FriendHolder) vi.getTag();
            }

            final FriendEntity item = getItem(position);

            if (item != null) {
                int colorIndex = (item.karmaScore > 74) ? 1 : (item.karmaScore < 26) ? 2 : 0;
                holder.aura.getBackground().setColorFilter(AppConstants.AURA_COLORS[colorIndex], PorterDuff.Mode.SRC_ATOP);
                holder.name.setText(item.userName);

                ColorMatrix matrix = new ColorMatrix();
                int sat = (item.online) ? 1 : 0;
                matrix.setSaturation(sat);
                ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
                if (item.avatarType == 1) {
                    holder.avatarText.setVisibility(View.GONE);
                    holder.avatarImage.setVisibility(View.VISIBLE);
                    holder.avatarImage.setColorFilter(filter);
                    GlideApp.with(context)
                            .load(Uri.parse(AppConstants.BLOB_USERAVATARS_URL + item.userName + ".png"))
                            .placeholder(R.drawable.icon_avatar_default)
                            .fitCenter()
                            .into(holder.avatarImage);
                } else {
                    holder.avatarImage.setVisibility(View.GONE);
                    holder.avatarText.setVisibility(View.VISIBLE);
                    if (item.online) {
                        holder.avatarText.getBackground().setColorFilter(item.avatarColor, PorterDuff.Mode.SRC_ATOP);
                    } else {
                        holder.avatarText.getBackground().setColorFilter(filter);
                    }
                    holder.avatarText.setText(item.displayInitials);
                }

                if (item.isRequest()) {
                    holder.status.setVisibility(View.GONE);
                    holder.inviteDeny.setVisibility(View.VISIBLE);
                    holder.inviteAccept.setVisibility(View.VISIBLE);
                    holder.inviteDeny.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            item.setBlocked(true);
                            resolver.updateFriendRequest(item.friendId, 0);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, "Friend Request Denied", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                    holder.inviteAccept.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            resolver.updateFriendRequest(item.friendId, 1);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, "Friend Request Accepted", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                } else {
                    holder.inviteDeny.setVisibility(View.GONE);
                    holder.inviteAccept.setVisibility(View.GONE);
                    holder.status.setVisibility(View.VISIBLE);
                    holder.status.setText(item.userStatus);
                }
            }

            return vi;
        }
    }
}
