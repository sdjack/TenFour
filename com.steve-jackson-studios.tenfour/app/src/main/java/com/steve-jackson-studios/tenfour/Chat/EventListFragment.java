package com.steve-jackson-studios.tenfour.Chat;

import android.app.ListFragment;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.text.TextUtils;
import android.util.Log;
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

import com.steve-jackson-studios.tenfour.AppConstants;
import com.steve-jackson-studios.tenfour.Data.EventData;
import com.steve-jackson-studios.tenfour.Data.EventDataModel;
import com.steve-jackson-studios.tenfour.Observer.Dispatch;
import com.steve-jackson-studios.tenfour.Observer.ObservedEvents;
import com.steve-jackson-studios.tenfour.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sjackson on 8/2/2017.
 * EventListFragment
 */

public class EventListFragment extends ListFragment {

    private static final String TAG = "EventListFragment";

    private final Handler handler = new Handler();
    private int listPadding;
    private CallbackListener callbackListener;
    private LinearLayout progress;
    private RelativeLayout createButton;
    private RelativeLayout publicButton;
    private FetchActivity asyncLoader;

    EventListAdapter adapter;
    ListView listView;

    private static class ViewHolder {
        RelativeLayout container;
        ImageView category;
        TextView title;
    }

    private class EventListEntity {
        String id;
        String titleText;
        Drawable categoryIcon;

        EventListEntity(String id, String title, int resId) {
            this.id = id;
            this.titleText = (!TextUtils.isEmpty(title)) ? title : "";
            this.categoryIcon = ContextCompat.getDrawable(getActivity(), resId);
        }
    }


    /**
     * @param callbackListener the listener
     */
    public void setCallbackListener(CallbackListener callbackListener) {
        this.callbackListener = callbackListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        listPadding = (int) getActivity().getResources().getDimension(R.dimen.eight_px_margin);
        View view = inflater.inflate(R.layout.event_list_layout, container, false);
        createButton = (RelativeLayout) view.findViewById(R.id.event_create_button);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dispatch.triggerEvent(ObservedEvents.REQUEST_CREATE_EVENT);
            }
        });
        publicButton = (RelativeLayout) view.findViewById(R.id.event_public_button);
        publicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AppConstants.EVENT_ID != null && !AppConstants.EVENT_ID.equals(AppConstants.LOCATION_ID)) {
                    callbackListener.onEventSelected(AppConstants.LOCATION_ID, AppConstants.DEFAULT_CHAT_TITLE);
                }
            }
        });
        progress = (LinearLayout) view.findViewById(R.id.loading);
        listView = (ListView) view.findViewById(android.R.id.list);
        adapter = new EventListAdapter(getActivity(), R.layout.event_list_item, new ArrayList<EventListEntity>());
        listView.setAdapter(adapter);

        new FetchActivity().execute();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

    }

    public void refresh() {
        if (asyncLoader == null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    progress.setVisibility(View.VISIBLE);
                    if (AppConstants.EVENT_ID != null && AppConstants.LOCATION_ID != null && !AppConstants.EVENT_ID.equals(AppConstants.LOCATION_ID)) {
                        Log.d(TAG, "AppConstants.EVENT_ID = " + AppConstants.EVENT_ID + " && AppConstants.LOCATION_ID = " +  AppConstants.LOCATION_ID);
                        publicButton.setBackgroundResource(R.drawable.invisible);
                    } else {
                        publicButton.setBackgroundResource(R.drawable.event_button_bg);
                    }
                    publicButton.setPadding(listPadding, listPadding, listPadding, listPadding);
                    asyncLoader = new FetchActivity();
                    asyncLoader.execute();
                }
            });
        }
    }

    private void updateAdapterData(final ArrayList<EventListEntity> data) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                progress.setVisibility(View.GONE);
                adapter.clear();
                adapter.addAll(data);
                adapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * Interface definition for callbacks
     */
    public interface CallbackListener {
        void onEventSelected(String eventId, String titleText);
    }


    private class FetchActivity extends AsyncTask<Void, String, Boolean> {

        private final ArrayList<EventListEntity> feed = new ArrayList<>();

        @Override
        protected void onPostExecute(Boolean result) {
            asyncLoader = null;
            if (result)
                updateAdapterData(feed);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            HashMap<String, EventDataModel> eventList = EventData.getEventData();
            if (eventList != null) {
                for (Object o : eventList.entrySet()) {
                    Map.Entry entry = (Map.Entry) o;
                    EventDataModel model = (EventDataModel) entry.getValue();
                    //Log.d(TAG, "EventDataModel Added: " + model.getId());
                    feed.add(new EventListEntity(model.getId(), model.getTitle(), model.getResIcon()));
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

    private class EventListAdapter extends ArrayAdapter<EventListEntity> {

        public EventListAdapter(Context context, int resource, ArrayList<EventListEntity> data) {
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
                vi = inflater.inflate(R.layout.event_list_item, parent, false);
                holder = new ViewHolder();
                holder.container = (RelativeLayout) vi.findViewById(R.id.event_item_container);
                holder.category = (ImageView) vi.findViewById(R.id.event_category);
                holder.title = (TextView) vi.findViewById(R.id.event_title);
                vi.setTag(holder);
            } else {
                holder = (ViewHolder) vi.getTag();
            }

            final EventListEntity item = getItem(position);

            if (item != null) {
                holder.title.setText(item.titleText);
                holder.category.setImageDrawable(item.categoryIcon);
                if (AppConstants.EVENT_ID != null && item.id.equals(AppConstants.EVENT_ID)) {
                    holder.container.setBackgroundResource(R.drawable.event_button_bg);
                } else {
                    holder.container.setBackgroundResource(R.drawable.invisible);
                }
                holder.container.setPadding(listPadding, listPadding, listPadding, listPadding);
                holder.container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        callbackListener.onEventSelected(item.id, item.titleText);
                    }
                });
            } else {
                holder.title.setText("");
                holder.category.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.category, null));
                holder.container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
            }

            return vi;
        }
    }
}
