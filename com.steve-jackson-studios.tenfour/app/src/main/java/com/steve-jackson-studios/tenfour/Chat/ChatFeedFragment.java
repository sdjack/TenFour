package com.steve-jackson-studios.tenfour.Chat;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.steve-jackson-studios.tenfour.AppConstants;
import com.steve-jackson-studios.tenfour.Data.ChatData;
import com.steve-jackson-studios.tenfour.Data.ChatPostData;
import com.steve-jackson-studios.tenfour.Data.UserData;
import com.steve-jackson-studios.tenfour.Gallery.SaveImageTask;
import com.steve-jackson-studios.tenfour.Media.GlideApp;
import com.steve-jackson-studios.tenfour.Navigation.NavigationFragmentCallback;
import com.steve-jackson-studios.tenfour.R;
import com.steve-jackson-studios.tenfour.Widgets.EnhancedEditText;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by sjackson on 7/27/2017.
 * ChatFeedFragment
 */

public class ChatFeedFragment extends ListFragment {

    private static final String TAG = "ChatFeedFragment";

    private final Handler handler = new Handler();

    private CallbackListener callbackListener;
    private FetchActivity asyncLoader;
    private RelativeLayout stickyContainer;
    private TextView chatTitle;
    private EnhancedEditText inputText;
    private TextView stickyMessage;
    private TextView stickyAvatarText;
    private ImageView stickyAvatarImage;
    private ImageView stickyMedia;
    private Button sendButton;
    private String pendingMessage;
    private boolean firstLoad = false;

    ChatFeedAdapter adapter;
    ListView listView;

    private static class ViewHolder {
        RelativeLayout content;
        RelativeLayout toolbar;
        TextView timestamp;
        TextView score;
        TextView totalReplies;
        TextView message;
        TextView avatarText;
        ImageView avatarImage;
        ImageView media;
        ImageView sticker;
        LinearLayout aura;
        LinearLayout actions;
        LinearLayout reply;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Context context = getActivity();

        final View view = inflater.inflate(R.layout.chat_layout, container, false);

        LinearLayout frame = (LinearLayout) view.findViewById(R.id.chat_frame);
        frame.getBackground().setAlpha(130);

        Button closeButton = (Button) view.findViewById(R.id.chat_close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle();
            }
        });

        sendButton = (Button) view.findViewById(R.id.input_send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        listView = (ListView) view.findViewById(android.R.id.list);
        chatTitle = (TextView) view.findViewById(R.id.chat_title);
        chatTitle.setText(AppConstants.CHAT_TITLE);
        stickyContainer = (RelativeLayout) view.findViewById(R.id.chat_sticky_container);
        stickyContainer.setVisibility(View.GONE);
        stickyMessage = (TextView) view.findViewById(R.id.sticky_chat_message);
        stickyAvatarText = (TextView) view.findViewById(R.id.sticky_avatar_type0);
        stickyAvatarImage = (ImageView) view.findViewById(R.id.sticky_avatar_type1);
        stickyMedia = (ImageView) view.findViewById(R.id.sticky_chat_media);

        inputText = (EnhancedEditText) view.findViewById(R.id.input_edittext);
        inputText.setImeOptions(EditorInfo.IME_ACTION_SEND);
        inputText.setRawInputType(InputType.TYPE_CLASS_TEXT);
        inputText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        });
        inputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                pendingMessage = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {
                pendingMessage = s.toString();
            }
        });
        inputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendMessage();
                    handled = true;
                }
                return handled;
            }
        });

//        listView.setOnScrollListener(new ListView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(AbsListView view, int scrollState) {
//                if (scrollState == SCROLL_STATE_IDLE) {
//                    resetChildInteractions();
//                } else {
//                    haltChildInteractions();
//                }
//            }
//
//            @Override
//            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//                centerItemIndex = getItemPositionFromRawYCoordinates(firstVisibleItem, totalItemCount);
//            }
//        });


        adapter = new ChatFeedAdapter(context, R.layout.chat_message_layout, new ArrayList<ChatPostData>());
        listView.setAdapter(adapter);
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
//                callbackListener.onImageSelected();
//                ChatPostData item = (ChatPostData) parent.getItemAtPosition(position);
//                String img = item.image;
//                Log.d(TAG, "OnItemClickListener " + img);
//                if (img != null && !img.equals("")) {
//                    imageClicked(img);
//                }
//            }
//        });

        new FetchActivity().execute();

        return view;
    }

    private void sendMessage() {
        //Log.d("Chat Input", pendingMessage);
        if (callbackListener.onInputSubmit(pendingMessage)) {
            pendingMessage = null;
            inputText.setText("");
        }
    }

    public void toggle() {
        if (AppConstants.replyStepOut()) {
            callbackListener.onChatClosed();
        } else {
            callbackListener.onChatReload();
        }
    }

    public void refresh() {
        if (asyncLoader == null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    asyncLoader = new FetchActivity();
                    asyncLoader.execute();
                }
            });
        }
    }

    private void updateStickyData() {
        handler.post(new Runnable() {
            @Override
            public void run() {

                if (AppConstants.REPLY_ENTITY != null) {
                    ChatPostData item = AppConstants.REPLY_ENTITY;
                    stickyContainer.setVisibility(View.VISIBLE);
                    Context context = getActivity();
                    if (item.avatarType == 1) {
                        stickyAvatarText.setVisibility(View.GONE);
                        stickyAvatarImage.setVisibility(View.VISIBLE);
                        if (item.isMe) {
                            GlideApp.with(context)
                                    .load(UserData.AVATAR_URI)
                                    .skipMemoryCache(true)
                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .placeholder(R.drawable.icon_avatar_default)
                                    .fitCenter()
                                    .into(stickyAvatarImage);
                        } else {
                            GlideApp.with(context)
                                    .load(Uri.parse(AppConstants.BLOB_USERAVATARS_URL + item.userName + ".png"))
                                    .placeholder(R.drawable.icon_avatar_default)
                                    .fitCenter()
                                    .into(stickyAvatarImage);
                        }
                    } else {
                        stickyAvatarImage.setVisibility(View.GONE);
                        stickyAvatarText.setVisibility(View.VISIBLE);
                        stickyAvatarText.getBackground().setColorFilter(item.avatarColor, PorterDuff.Mode.SRC_ATOP);
                        stickyAvatarText.setText(item.displayInitials);
                    }

                    if (item.image != null && !TextUtils.isEmpty(item.image)) {
                        int glideWidth = (int) context.getResources().getDimension(R.dimen.glide_image_width);
                        int glideHeight = (int) context.getResources().getDimension(R.dimen.action_button_size);
                        stickyMessage.setVisibility(View.GONE);
                        stickyMedia.setVisibility(View.VISIBLE);
                        GlideApp.with(getActivity().getApplicationContext())
                                .load(Uri.parse(item.image))
                                .diskCacheStrategy(DiskCacheStrategy.DATA)
                                .thumbnail(0.25f)
                                .override(glideWidth, glideHeight)
                                .centerCrop()
                                .dontAnimate()
                                .into(stickyMedia);
                    } else {
                        stickyMedia.setVisibility(View.GONE);
                        stickyMessage.setVisibility(View.VISIBLE);
                        stickyMessage.setText(item.message);
                    }
                } else {
                    stickyContainer.setVisibility(View.GONE);
                }
            }
        });
    }

    private void updateAdapterData(final ArrayList<ChatPostData> sysPosts, final ArrayList<ChatPostData> localPosts, final ArrayList<ChatPostData> otherPosts) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                chatTitle.setText(AppConstants.CHAT_TITLE);

                adapter.clear();
                adapter.addAll(sysPosts);
                adapter.addAll(localPosts);
                adapter.addAll(otherPosts);
                adapter.notifyDataSetChanged();

                if (!firstLoad) {
                    firstLoad = true;
                    listView.setSelection(0);
                }
            }
        });
    }

    /**
     * @param callbackListener the listener
     */
    public void setCallbackListener(CallbackListener callbackListener) {
        this.callbackListener = callbackListener;
    }

    void karmaClicked(final View view, final String id) {
        callbackListener.onKarmaClicked(view, id);
    }

    void avatarClicked(final View view, final ChatPostData userChatPostData) {
        callbackListener.onAvatarClicked(view, userChatPostData);
        //showChatUserMenu(view, userId);
    }

    void textClicked(final String content) {
        callbackListener.onClickWebLink(content);
        //showChatUserMenu(view, userId);
    }

    void imageClicked(final String fileName) {
        callbackListener.onImageClicked(fileName);
    }

    public void showLongClickDialog(final ChatPostData entity) {
        // build dialog
        List<String> dialogItems = new ArrayList<String>();
        dialogItems.add("Save");
        dialogItems.add("Share");
        if (entity.userName.equals(UserData.USERNAME)) {
            dialogItems.add("Delete");
        }

        final CharSequence[] items = dialogItems.toArray(new String[dialogItems.size()]);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose your action");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                switch(item) {
                    case 0:
                        new SaveImageTask(getActivity(), entity.image).execute();
                        break;
                    case 1:
                        showShareDialog(entity);
                        break;
                    case 2:
                        showShareDialog(entity);
                        break;
                    default:
                        break;
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void showShareDialog(ChatPostData message) {
        // shoot the intent
        // will default to "messaging / sms" if nothing else is installed
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        //Text seems to be necessary for Facebook and Twitter
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_TEXT, message.displayName + " " + message.image);
        startActivity(Intent.createChooser(sharingIntent,"Share using"));
    }

    /**
     * Interface definition for callbacks
     */
    public interface CallbackListener extends NavigationFragmentCallback {

        boolean onInputSubmit(final String inputText);

        void onKarmaClicked(final View view, final String id);

        void onAvatarClicked(final View view, final ChatPostData userChatPostData);

        void onImageClicked(String fileName);

        void onReplyClicked(ChatPostData replyChatPostData);

        void onClickWebLink(String link);

        void onChatClosed();

        void onChatReload();
    }

    private class FetchActivity extends AsyncTask<Void, String, Boolean> {

        private final ArrayList<ChatPostData> sysPosts = new ArrayList<>();
        private final ArrayList<ChatPostData> localPosts = new ArrayList<>();
        private final ArrayList<ChatPostData> otherPosts = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            updateStickyData();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            asyncLoader = null;
            if (result)
                updateAdapterData(sysPosts, localPosts, otherPosts);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            ArrayList<ChatPostData> sysMessages = ChatData.getSystemMessageData(AppConstants.LOCATION_ID);
            int count = sysMessages.size();
            if (count > 0) {
                for (int i = 0; i < count; i++) {
                    ChatPostData data = sysMessages.get(i);
                    sysPosts.add(data);
                }
            }
            Map source = ChatData.getLive();
            if (!source.isEmpty()) {
                Set set = source.entrySet();
                Iterator iterator = set.iterator();
                while(iterator.hasNext()) {
                    Map.Entry mentry = (Map.Entry)iterator.next();
                    ChatPostData data = (ChatPostData)mentry.getValue();
                    //Log.d("DEVDEBUG", " Active reply: " + AppConstants.REPLY_ID + ", post replyId: " + data.replyId + ", msg: " + data.message);
                    if ((AppConstants.REPLY_ID == null && data.replyId.equals("")) || (AppConstants.REPLY_ID != null && data.replyId.equals(AppConstants.REPLY_ID))) {
                        if (data.isNearby()) {
                            localPosts.add(data);
                        } else {
                            otherPosts.add(data);
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

    private class ChatFeedAdapter extends ArrayAdapter<ChatPostData> {


        private final RelativeLayout.LayoutParams publicAvatarParams;
        private final RelativeLayout.LayoutParams publicContentParams;
        private final RelativeLayout.LayoutParams publicTextParams;
        private final RelativeLayout.LayoutParams publicMediaParams;
        private final RelativeLayout.LayoutParams publicStickerParams;
        private final RelativeLayout.LayoutParams userAvatarParams;
        private final RelativeLayout.LayoutParams userContentParams;
        private final RelativeLayout.LayoutParams userTextParams;
        private final RelativeLayout.LayoutParams userMediaParams;
        private final RelativeLayout.LayoutParams userStickerParams;

        private final int textMargin;
        private final int mediaMargin;
        private final int toolbarPaddingStart;
        private final int toolbarPaddingEnd;
        private final int zeroDimen;

        public ChatFeedAdapter(Context context, int resource, ArrayList<ChatPostData> data) {
            super(context, resource, data);

            int avatarSize = (int) context.getResources().getDimension(R.dimen.chat_avatar_size);
            int commonMargin = (int) context.getResources().getDimension(R.dimen.four_px_margin);
            int avatarMargin = (int) context.getResources().getDimension(R.dimen.chat_avatar_margin);
            int contentMargin = (int) context.getResources().getDimension(R.dimen.chat_content_margin);

            textMargin = (int) context.getResources().getDimension(R.dimen.chat_text_anchor_margin);
            mediaMargin = (int) context.getResources().getDimension(R.dimen.chat_media_anchor_margin);
            toolbarPaddingStart = (int) context.getResources().getDimension(R.dimen.chat_toolbar_start_padding);
            toolbarPaddingEnd = (int) context.getResources().getDimension(R.dimen.chat_toolbar_end_padding);
            zeroDimen = (int) context.getResources().getDimension(R.dimen.zero_px_margin);

            publicAvatarParams = new RelativeLayout.LayoutParams(avatarSize, avatarSize);
            publicAvatarParams.addRule(RelativeLayout.ALIGN_PARENT_START);
            publicAvatarParams.setMargins(commonMargin, avatarMargin, commonMargin, zeroDimen);

            publicContentParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            publicContentParams.addRule(RelativeLayout.ALIGN_PARENT_START);
            publicContentParams.setMarginEnd(commonMargin);
            publicContentParams.setMarginStart(contentMargin);

            publicTextParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            publicTextParams.setMarginEnd(commonMargin);
            publicTextParams.setMarginStart(textMargin);

            publicMediaParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            publicMediaParams.setMarginEnd(zeroDimen);
            publicMediaParams.setMarginStart(mediaMargin);

            publicStickerParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            publicStickerParams.setMarginEnd(zeroDimen);
            publicStickerParams.setMarginStart(contentMargin);

            userAvatarParams = new RelativeLayout.LayoutParams(avatarSize, avatarSize);
            userAvatarParams.addRule(RelativeLayout.ALIGN_PARENT_END);
            userAvatarParams.setMargins(commonMargin, avatarMargin, commonMargin, zeroDimen);

            userContentParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            userContentParams.addRule(RelativeLayout.ALIGN_PARENT_END);
            userContentParams.setMarginEnd(contentMargin);
            userContentParams.setMarginStart(commonMargin);

            userTextParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            userTextParams.setMarginEnd(textMargin);
            userTextParams.setMarginStart(commonMargin);

            userMediaParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            userMediaParams.setMarginEnd(mediaMargin);
            userMediaParams.setMarginStart(zeroDimen);

            userStickerParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            userStickerParams.setMarginEnd(contentMargin);
            userStickerParams.setMarginStart(zeroDimen);
        }



        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {

            final Context context = getActivity();

            View vi = convertView;
            ViewHolder holder;
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                vi = inflater.inflate(R.layout.chat_message_layout, parent, false);
                holder = new ViewHolder();
                holder.content = (RelativeLayout) vi.findViewById(R.id.chat_content);
                holder.toolbar = (RelativeLayout) vi.findViewById(R.id.chat_toolbar);
                holder.score = (TextView) vi.findViewById(R.id.chat_score);
                holder.totalReplies = (TextView) vi.findViewById(R.id.replies_total);
                holder.timestamp = (TextView) vi.findViewById(R.id.chat_time);
                holder.media = (ImageView) vi.findViewById(R.id.chat_media);
                holder.sticker = (ImageView) vi.findViewById(R.id.chat_sticker);
                holder.message = (TextView) vi.findViewById(R.id.chat_message);
                holder.aura = (LinearLayout) vi.findViewById(R.id.chat_avatar_aura);
                holder.avatarText = (TextView) vi.findViewById(R.id.chat_avatar_type0);
                holder.avatarImage = (ImageView) vi.findViewById(R.id.chat_avatar_type1);
                holder.reply = (LinearLayout) vi.findViewById(R.id.chat_reply_container);
                holder.actions = (LinearLayout) vi.findViewById(R.id.chat_actions_container);

                holder.score.setText("0");
                holder.totalReplies.setText("0");
                holder.timestamp.setText("");
                holder.message.setText("");
                holder.message.setVisibility(View.GONE);
                holder.media.setVisibility(View.GONE);
                vi.setTag(holder);
            } else {
                holder = (ViewHolder) vi.getTag();
            }

            final ChatPostData item = getItem(position);

            if (item != null) {
                if (item.isMe) {
                    holder.aura.setLayoutParams(userAvatarParams);
                    holder.content.setLayoutParams(userContentParams);
                    holder.message.setLayoutParams(userTextParams);
                    holder.media.setLayoutParams(userMediaParams);
                    if (item.isSticker == 1) {
                        int randMargin = (int)((mediaMargin * 4) * Math.random());
                        userStickerParams.setMarginEnd(randMargin);
                        holder.sticker.setLayoutParams(userStickerParams);
                        holder.content.setBackgroundResource(R.drawable.invisible);
                        holder.toolbar.setVisibility(View.GONE);
                    } else {
                        holder.content.setBackgroundResource(R.drawable.chat_message_right_bg);
                        holder.toolbar.setVisibility(View.VISIBLE);
                    }
                    holder.toolbar.setPadding(toolbarPaddingEnd, zeroDimen, toolbarPaddingStart, zeroDimen);
                } else {
                    holder.aura.setLayoutParams(publicAvatarParams);
                    holder.content.setLayoutParams(publicContentParams);
                    holder.message.setLayoutParams(publicTextParams);
                    holder.media.setLayoutParams(publicMediaParams);
                    int randMargin = (int)((mediaMargin * 8) * Math.random());
                    publicStickerParams.setMarginStart(randMargin);
                    holder.sticker.setLayoutParams(publicStickerParams);
                    if (item.isSticker == 1) {
                        holder.content.setBackgroundResource(R.drawable.invisible);
                        holder.toolbar.setVisibility(View.GONE);
                    } else {
                        holder.content.setBackgroundResource(R.drawable.chat_message_bg);
                        holder.toolbar.setVisibility(View.VISIBLE);
                    }
                    holder.toolbar.setPadding(toolbarPaddingStart, zeroDimen, toolbarPaddingEnd, zeroDimen);
                }

                holder.totalReplies.setText(Integer.toString(item.replyCount));
                if (item.replyCount > 0) {
                    holder.totalReplies.setTextColor(Color.BLUE);
                } else {
                    holder.totalReplies.setTextColor(Color.WHITE);
                }

                holder.score.setText(String.valueOf(item.postScore));
                holder.content.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String img = item.image;
                        if (img != null && !img.equals("")) {
                            imageClicked(img);
                        } else {
                            textClicked(item.message);
                        }
                    }
                });
                holder.content.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        String img = item.image;
                        if (img != null && !img.equals("")) {
                            showLongClickDialog(item);
                        } else {
                            textClicked(item.message);
                        }
                        return true;
                    }
                });
                holder.reply.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!item.isMe) {
                            callbackListener.onReplyClicked(item);
                        }
                    }
                });
                holder.actions.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!item.isMe) {
                            String id = item.postId;
                            karmaClicked(v, id);
                        }
                    }
                });

                int glideWidth = (int) context.getResources().getDimension(R.dimen.glide_image_width);
                int glideHeight = (int) context.getResources().getDimension(R.dimen.glide_image_height);

                if (item.image != null && !item.image.equals("")) {
                    holder.sticker.setContentDescription(item.image);
                    holder.media.setContentDescription(item.image);
                    if (item.isSticker == 1) {
                        holder.media.setVisibility(View.GONE);
                        holder.sticker.setVisibility(View.VISIBLE);
                        float angle = (float)(Math.random() * 66.0f) - 33.0f;
                        holder.sticker.setRotation(angle);
                        GlideApp.with(context)
                                .asGif()
                                .load(Uri.parse(item.image))
                                .fitCenter()
                                .into(holder.sticker);
//                    } else if (item.image.endsWith("gif")) {
//                        holder.media.setVisibility(View.VISIBLE);
//                        holder.sticker.setVisibility(View.GONE);
//                        GlideApp.with(context)
//                                .asGif()
//                                .load(Uri.parse(item.image))
//                                .thumbnail(0.5f)
//                                .override(glideWidth, glideHeight)
//                                .centerCrop()
//                                .dontAnimate()
//                                .into(holder.media);
                    } else {
                        holder.media.setVisibility(View.VISIBLE);
                        holder.sticker.setVisibility(View.GONE);
                        GlideApp.with(context)
                                .asBitmap()
                                .load(Uri.parse(item.image))
                                .thumbnail(0.5f)
                                .override(glideWidth, glideHeight)
                                .centerCrop()
                                .dontAnimate()
                                .placeholder(R.drawable.placeholder_image)
                                .into(holder.media);
                    }
                } else {
                    holder.media.setVisibility(View.GONE);
                    holder.sticker.setVisibility(View.GONE);
                }
                if (item.message != null && !item.message.equals("")) {
                    holder.message.setText(item.message);
                    holder.message.setVisibility(View.VISIBLE);
                } else {
                    holder.message.setText("");
                    holder.message.setVisibility(View.GONE);
                }
                int colorIndex = (item.karmaScore > 100) ? 1 : (item.karmaScore < 10) ? 2 : 0;
                holder.aura.getBackground().setColorFilter(AppConstants.AURA_COLORS[colorIndex], PorterDuff.Mode.SRC_ATOP);
                holder.aura.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!item.isMe) {
                            avatarClicked(v, item);
                        }
                    }
                });
                if (item.avatarType == 1) {
                    holder.avatarText.setVisibility(View.GONE);
                    holder.avatarImage.setVisibility(View.VISIBLE);
                    if (item.isMe) {
                        GlideApp.with(context)
                                .load(UserData.AVATAR_URI)
                                .skipMemoryCache(true)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .placeholder(R.drawable.icon_avatar_default)
                                .fitCenter()
                                .into(holder.avatarImage);
                    } else {
                        GlideApp.with(context)
                                .load(Uri.parse(AppConstants.BLOB_USERAVATARS_URL + item.userName + ".png"))
                                .placeholder(R.drawable.icon_avatar_default)
                                .fitCenter()
                                .into(holder.avatarImage);
                    }
                } else {
                    holder.avatarImage.setVisibility(View.GONE);
                    holder.avatarText.setVisibility(View.VISIBLE);
                    holder.avatarText.getBackground().setColorFilter(item.avatarColor, PorterDuff.Mode.SRC_ATOP);
                    holder.avatarText.setText(item.displayInitials);
                }
            } else {
                holder.content.setBackgroundResource(R.drawable.chat_message_bg);
                holder.aura.setLayoutParams(publicAvatarParams);
                holder.content.setLayoutParams(publicContentParams);
                holder.message.setLayoutParams(publicTextParams);
                holder.media.setLayoutParams(publicMediaParams);
                holder.toolbar.setPadding(toolbarPaddingStart, zeroDimen, toolbarPaddingEnd, zeroDimen);
                holder.actions.setVisibility(View.VISIBLE);
            }

            return vi;
        }
    }
}
