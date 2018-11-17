package com.steve-jackson-studios.tenfour.IO;

import android.util.Log;

import com.steve-jackson-studios.tenfour.AppConstants;
import com.steve-jackson-studios.tenfour.Observer.Dispatch;
import com.steve-jackson-studios.tenfour.Observer.ObservedEvents;
import com.steve-jackson-studios.tenfour.Data.UserData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by sjackson on 5/25/2017.
 * SocketIO
 */

public class SocketIO {
    private static final String TAG = "SocketIO";
    private static Socket SOCKET;
    private static boolean CONNECTING = false;
    private static boolean CONNECTED = false;
    private static boolean REQUEST_RECONNECT = false;
    private static HashMap<String, Emitter.Listener> LISTENERS = new HashMap<String, Emitter.Listener>();
    private static Stack<JSONObject> QUEUE = new Stack<JSONObject>();
    private static String SOCKET_ID = "";
    private static String CHANNEL_ID = "";

    private SocketIO() {
    }

    public static boolean connected() {
        return CONNECTED;
    }

    public static void addListener(String emitter, Emitter.Listener listener) {
        if (SOCKET == null) {
            LISTENERS.put(emitter, listener);
        } else {
            SOCKET.on(emitter, listener);
        }
    }

    public static void emit(String emitter, JSONObject object) {
        if (SOCKET != null && CONNECTED && !REQUEST_RECONNECT) {
            SOCKET.emit(emitter, object);
        } else {
            try {
                object.put("emitter", emitter);
                QUEUE.push(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (SOCKET == null || REQUEST_RECONNECT || !CONNECTING) {
                connectSocket();
            }
        }
    }

    public static JSONObject getResponseData(JSONObject data) throws JSONException {
        if (!data.isNull("data")) {
            final Object jsonObject = data.get("data");
            if (jsonObject instanceof JSONObject) {
                return (JSONObject)jsonObject;
            } else if (jsonObject instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray)jsonObject;
                if (jsonArray.length() > 0) {
                    return (JSONObject)jsonArray.get(0);
                }
            }
        }
        return null;
    }

    public static JSONArray getResponseArray(JSONObject data) throws JSONException {
        if (!data.isNull("data")) {
            final Object jsonObject = data.get("data");
            if (jsonObject instanceof JSONObject) {
                return ((JSONObject)jsonObject).getJSONArray("data");
            } else if (jsonObject instanceof JSONArray) {
                return (JSONArray)jsonObject;
            }
        }
        return null;
    }

    public static void connectSocket() {
        if (!SOCKET_ID.equals(AppConstants.SOCKET_ID)) {
            SOCKET_ID = AppConstants.SOCKET_ID;
            REQUEST_RECONNECT = true;
        }
        if ((REQUEST_RECONNECT || !CONNECTED) && UserData.ID != null) {
            if (SOCKET != null && CONNECTED) {
                SOCKET.disconnect();
                SOCKET.close();
                CONNECTED = false;
            }
            establishConnection();
        }
    }

    public static void joinChannel() {
        if (AppConstants.SUBDIVISION_ID != null && !CHANNEL_ID.equals(AppConstants.SUBDIVISION_ID)) {
            CHANNEL_ID = AppConstants.SUBDIVISION_ID;
            JSONObject row = new JSONObject();
            try {
                row.put("channelId", AppConstants.SUBDIVISION_ID);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            SOCKET.emit("join channel", row);
        }
    }

    public static void leaveChannel() {
        JSONObject row = new JSONObject();
        try {
            row.put("channelId", CHANNEL_ID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        SOCKET.emit("leave channel", row);
    }

    private static void establishConnection() {
        CONNECTING = true;

        //Log.d(TAG, "Connecting to " + SOCKET_ID + " ::::: PLEASE WAIT");

        try {
            final String socketUrl = AppConstants.SERVICE_URL + "/" + SOCKET_ID;
            SOCKET = IO.socket(socketUrl + "?userId=" + UserData.ID);
            SOCKET.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d(TAG, "Connected to " + socketUrl);
                    for (Object o : LISTENERS.entrySet()) {
                        Map.Entry entry = (Map.Entry) o;
                        String key = (String)entry.getKey();
                        Emitter.Listener listener = (Emitter.Listener)entry.getValue();
                        SOCKET.on(key, listener);
                    }
                    while (!QUEUE.empty()) {
                        try {
                            JSONObject object = QUEUE.pop();
                            String emitter = object.getString("emitter");
                            //Log.d(TAG, "Adding " + emitter);
                            SOCKET.emit(emitter, object);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    CONNECTED = true;
                    CONNECTING = false;
                    REQUEST_RECONNECT = false;
                    Dispatch.triggerEvent(ObservedEvents.SOCKET_AVAILABLE);
                }
            });
            SOCKET.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d(TAG, "Disconnected from " + socketUrl);
                    CONNECTED = false;
                    CONNECTING = false;
                    Dispatch.triggerEvent(ObservedEvents.SOCKET_UNAVAILABLE);
                }
            });
            SOCKET.on("action required", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    try {
                        JSONObject data = (JSONObject) args[0];
                        String originalType = data.getString("type");
                        String message = data.getString("message");
                        //Log.d(TAG, "Action Required :: from " + originalType + " :: " + message);
                        JSONObject jsonData = getResponseData(data);
                        if (null != jsonData && !jsonData.isNull("action") && !jsonData.isNull("event")) {
                            String emitter = jsonData.getString("action");
                            int id = jsonData.getInt("event");
                            if (!emitter.equals("create event")) {
                                JSONObject obj = new JSONObject();
                                obj.put("event", id);
                                obj.put("username", UserData.USERNAME);
                                Log.d(TAG, "Action Required (" + emitter + ") :: from " + originalType + " :: " + message);
                                SocketIO.emit(emitter, obj);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            SOCKET.on("failure", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    try {
                        JSONObject data = (JSONObject) args[0];
                        String originalType = data.getString("type");
                        String message = data.getString("message");
                        Log.d("SocketIO FAILURE", originalType + " :: " + message);
                        if (!data.isNull("data")) {
                            JSONArray jsonData = data.getJSONArray("data");
                            if (jsonData != null) {
                                Log.d("SocketIO FAILURE DATA", jsonData.toString());
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            SOCKET.on("channel left", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    CHANNEL_ID = "";
                    //JSONObject data = (JSONObject) args[0];
                    //Log.d("SocketIO channel left", data.toString());
                }
            });
//            SOCKET.on("user disconnect", new Emitter.Listener() {
//                @Override
//                public void call(Object... args) {
//                    try {
//                        JSONObject data = (JSONObject) args[0];
//                        String message = data.getString("message");
//                        Log.d(TAG, "Disconnect " + message);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//            });
            if (AppConstants.DEBUGGING) {
                SOCKET.on("error", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        try {
                            Log.d(TAG, "Error " + args[0]);
                            if (args[0].equals("Invalid namespace")) {
                                Dispatch.triggerEvent(ObservedEvents.NOTIFY_LOCATION_ID_INVALID);
                            }
                        } catch (ClassCastException e) {
                            e.printStackTrace();
                        }
                    }
                });
                SOCKET.on("debugging message", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        JSONObject data = (JSONObject) args[0];
                        final JSONArray jsonData;
                        try {
                            //Log.d("SocketIO EVENT CREATION", data.toString(2));
                            jsonData = data.getJSONArray("data");
                            if (jsonData != null) {
                                Log.d("SocketIO EVENT CREATION", jsonData.toString());
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            SOCKET.connect();
        } catch (Exception e) {
            CONNECTING = false;
            e.printStackTrace();
        }
    }
}
