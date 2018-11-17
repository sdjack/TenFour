package com.steve-jackson-studios.tenfour.Data;

import java.util.HashMap;
import java.util.UUID;

/**
 * Created by sjackson on 4/27/2018.
 *
 */

public class MapChatData {
    public final HashMap<String, ChatPostData> posts = new HashMap<>();
    public final HashMap<String, ChatPostData[]> stickers = new HashMap<>();
    public String uuid;

    public MapChatData() {
        uuid = UUID.randomUUID().toString();
    }

    public void load(String id, ChatPostData chatPostData) {
        if (chatPostData.isSticker == 0) {
            if (posts.get(id) == null) {
                uuid = UUID.randomUUID().toString();
                posts.put(id, chatPostData);
            } else {
                ChatPostData cpd = posts.get(id);
                if (!cpd.message.equals(chatPostData.message)) {
                    uuid = UUID.randomUUID().toString();
                    posts.put(id, chatPostData);
                }
            }
        }
    }

    public void loadStickers(String id, ChatPostData[] chatStickers) {
        if (stickers.get(id) == null) {
            uuid = UUID.randomUUID().toString();
            stickers.put(id, chatStickers);
        } else {
            ChatPostData[] cpds = stickers.get(id);
            boolean isUpdated = false;
            for (int i = 0; i < 4; i++) {
                if (chatStickers[i] != null && !chatStickers[i].image.equals(cpds[i].image)) {
                    isUpdated = true;
                }
            }
            if (isUpdated) {
                uuid = UUID.randomUUID().toString();
                stickers.put(id, chatStickers);
            }
        }
    }
}
