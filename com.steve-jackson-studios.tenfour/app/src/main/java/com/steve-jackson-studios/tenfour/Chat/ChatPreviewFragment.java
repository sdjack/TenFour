package com.steve-jackson-studios.tenfour.Chat;

import android.content.Context;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.steve-jackson-studios.tenfour.AppConstants;
import com.steve-jackson-studios.tenfour.AppResolver;
import com.steve-jackson-studios.tenfour.Data.ChatData;
import com.steve-jackson-studios.tenfour.Data.ChatLocationData;
import com.steve-jackson-studios.tenfour.Data.ChatSubdivisionData;
import com.steve-jackson-studios.tenfour.Data.ChatPostData;
import com.steve-jackson-studios.tenfour.Misc.ResolverListFragment;
import com.steve-jackson-studios.tenfour.Observer.Dispatch;
import com.steve-jackson-studios.tenfour.Observer.ObservedEvents;
import com.steve-jackson-studios.tenfour.R;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by sjackson on 2/28/2017.
 * ChatPreviewFragment
 */

public class ChatPreviewFragment extends ResolverListFragment {

    private static final String TAG = "ChatPreviewFragment";

    ChatPreviewAdapter adapter;
    RelativeLayout backdrop;
    ListView listView;

    private static class ViewHolder {
        TextView message;
        TextView avatar;
        ImageView media;
        LinearLayout aura;
    }

    public static ChatPreviewFragment newInstance(AppResolver appResolver) {

        ChatPreviewFragment instance = new ChatPreviewFragment();
        instance.setResolver(appResolver);

        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chat_preview_layout, container, false);
        listView = (ListView) view.findViewById(android.R.id.list);
        backdrop = (RelativeLayout) view.findViewById(R.id.preview_backdrop);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dispatch.triggerEvent(ObservedEvents.REQUEST_CLOSE_PREVIEW);
            }
        });

        adapter = new ChatPreviewAdapter(getActivity(), R.layout.chat_preview_item, new ArrayList<ChatPostData>());
        listView.setAdapter(adapter);

        new FetchActivity(adapter).execute();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

    }

    public void refresh() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                //backdrop.setVisibility(View.GONE);
                new FetchActivity(adapter).execute();
            }
        });
    }

    private static class FetchActivity extends AsyncTask<Void, String, Boolean> {

        private final ArrayList<ChatPostData> feed = new ArrayList<>();
        private final ChatPreviewAdapter cpa;

        FetchActivity(ChatPreviewAdapter a) {
            cpa = a;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result || feed.size() == 0) {
                Dispatch.triggerEvent(ObservedEvents.REQUEST_CLOSE_PREVIEW);
            } else {
                cpa.clear();
                cpa.addAll(feed);
                cpa.notifyDataSetChanged();
                Dispatch.triggerEvent(ObservedEvents.REQUEST_MAP_PREVIEW);
            }
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            Map source = ChatData.getPreview();
            if (source != null && !source.isEmpty()) {
                int count = 0;
                Set set = source.entrySet();
                Iterator iterator = set.iterator();
                while(iterator.hasNext() && count < 5) {
                    Map.Entry mentry = (Map.Entry)iterator.next();
                    ChatPostData data = (ChatPostData)mentry.getValue();
                    feed.add(data);
                    count++;
                }
                return true;
            }
            return false;
        }
    }

    private class ChatPreviewAdapter extends ArrayAdapter<ChatPostData> {

        ChatPreviewAdapter(Context context, int resource, ArrayList<ChatPostData> data) {
            super(context, resource, data);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {

            final Context context = getActivity();

            View vi = convertView;
            ViewHolder holder;
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                vi = inflater.inflate(R.layout.chat_preview_item, parent, false);
                holder = new ViewHolder();
                holder.media = (ImageView) vi.findViewById(R.id.preview_media);
                holder.message = (TextView) vi.findViewById(R.id.preview_message);
                holder.aura = (LinearLayout) vi.findViewById(R.id.preview_avatar_aura);
                holder.avatar = (TextView) vi.findViewById(R.id.preview_avatar);
                holder.message.setText("");
                holder.message.setVisibility(View.GONE);
                holder.media.setVisibility(View.GONE);
                vi.setTag(holder);
            } else
                holder = (ViewHolder) vi.getTag();

            final ChatPostData item = getItem(position);

            if (item != null) {
                int colorIndex = (item.karmaScore > 100) ? 1 : (item.karmaScore < 10) ? 2 : 0;
                holder.aura.getBackground().setColorFilter(AppConstants.AURA_COLORS[colorIndex], PorterDuff.Mode.SRC_ATOP);
                int cIndex = (int)(14 * Math.random());
                holder.avatar.getBackground().setColorFilter(AppConstants.CATEGORY_COLORS[cIndex], PorterDuff.Mode.SRC_ATOP);
                holder.avatar.setText(item.displayInitials);

                //int glideWidth = (int) context.getResources().getDimension(R.dimen.glide_image_width);
                //int glideHeight = (int) context.getResources().getDimension(R.dimen.glide_image_height);

                if (item.image != null && !item.image.equals("")) {
                    //backdrop.setVisibility(View.VISIBLE);
                    holder.message.setText("");
                    holder.message.setVisibility(View.GONE);
                    holder.media.setVisibility(View.VISIBLE);
                    holder.media.setContentDescription(item.image);
                    Glide.with(context)
                            .load(Uri.parse(item.image))
                            //.asBitmap()
                            .thumbnail(0.5f)
                            //.override(glideWidth, glideHeight)
                            //.centerCrop()
                            //.dontAnimate()
                            //.placeholder(R.drawable.placeholder_image)
                            .into(holder.media);
                } else if (item.message != null && !item.message.equals("")) {
                    //backdrop.setVisibility(View.VISIBLE);
                    holder.media.setVisibility(View.GONE);
                    holder.message.setVisibility(View.VISIBLE);
                    holder.message.setText(item.message);
                }

                //Glide.with(context)
                        //.load(Uri.parse(AppConstants.BLOB_USERAVATARS_URL + item.userName + ".png"))
                        //.asBitmap()
                        //.skipMemoryCache(true)
                        //.placeholder(R.drawable.icon_avatar_default)
                        //.fitCenter()
                        //.into(holder.avatar);
            }

            return vi;
        }
    }
}
