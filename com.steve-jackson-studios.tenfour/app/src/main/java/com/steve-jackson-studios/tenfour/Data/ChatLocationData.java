package com.steve-jackson-studios.tenfour.Data;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by sjackson on 7/31/2017.
 * ChatLocationData
 */
public class ChatLocationData {
    public final String id;
    public final String subDivisionId;
    private final ChatPostData[] stickerPosts;
    private final TreeMap<Integer, ChatPostData> data;
    private ChatPostData tempPost;
    private boolean pending = false;
    private boolean unclean = false;

    public ChatLocationData(String id, String subDivisionId) {
        this.id = id;
        this.subDivisionId = subDivisionId;
        this.data = new TreeMap<>();
        this.stickerPosts = new ChatPostData[4];
    }

    public void loadTemp(ChatPostData data) {
        if (this.unclean) {
            this.tempPost = null;
            this.unclean = false;
        }
        tempPost = data;
    }

    public void load(int orderId, ChatPostData data) {
        if (this.unclean) {
            this.tempPost = null;
            this.unclean = false;
        }
        save(orderId, data);
    }

    private void save(int orderId, ChatPostData entity) {
        if (entity.isSticker == 0) {
            MapData.loadPopupData(id, entity);
        }
        this.pending = true;
        this.data.put(orderId, entity);
    }

    public int size() {
        return data.size();
    }

    public Map fetch() {
        return ChatData.sortByValues(data);
    }

    public ChatPostData[] stickers() {
        return stickerPosts;
    }

    public void collect(TreeMap<Integer, ChatPostData> source) {
        if (this.tempPost != null) {
            source.put(0, tempPost);
        }
        source.putAll(data);
    }

    public void lock() {
        if (this.tempPost != null) {
            this.unclean = true;
        }
    }

    public void unlock() {
        this.unclean = false;
        if (this.pending && !data.isEmpty()) {
            int i = 0;
            Set set = data.entrySet();
            Iterator iterator = set.iterator();
            while(iterator.hasNext() && i < 4) {
                Map.Entry mentry = (Map.Entry)iterator.next();
                ChatPostData entity = (ChatPostData)mentry.getValue();
                if (entity.isSticker == 1) {
                    stickerPosts[i] = (ChatPostData)mentry.getValue();
                    i++;
                }
            }
            MapData.loadStickerData(id, stickerPosts);
        }
        this.pending = false;
    }
}
