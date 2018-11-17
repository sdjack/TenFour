package com.steve-jackson-studios.tenfour.Observer;

import java.util.Stack;

/**
 * Created by sjackson on 5/25/2017.
 * Dispatch
 */

public class Dispatch {
    private static final String TAG = "PlatformTransport";
    private static Stack<AbstractListener> REGISTRY = new Stack<AbstractListener>();
    private static Stack<AbstractListener> TEMP = new Stack<AbstractListener>();

    private static boolean DEBUGGING = false;

    /**
     * Register.
     *
     * @param listener the listener
     */
    public static void register(AbstractListener listener) {
        boolean exists = false;
        for (int i = 0; i < REGISTRY.size(); i++) {
            if (REGISTRY.get(i).getClass() == listener.getClass()) {
                exists = true;
            }
        }
        if (!exists) {
            REGISTRY.push(listener);
        }
    }

    /**
     * Unregister.
     *
     * @param listener the listener
     */
    @SuppressWarnings("unchecked")
    public static void unregister(AbstractListener listener) {
        TEMP.clear();
        for (int i = 0; i < REGISTRY.size(); i++) {
            AbstractListener l = REGISTRY.get(i);
            if (l.getClass() != listener.getClass()) {
                TEMP.push(l);
            }
        }
        REGISTRY.clear();
        REGISTRY.addAll(TEMP);
    }

    /**
     * Trigger event.
     *
     * @param eventID the event
     */
    public static void triggerEvent(final int eventID) {
//        if (eventID < 1000) {
//            String eventName = PlatformEvent.EVENT_NAME.get(eventID);
//            Gdx.app.log(TAG, " " + eventName);
//        }
        for (int i = 0; i < REGISTRY.size(); i++) {
            AbstractListener registrar = REGISTRY.get(i);
            if (DEBUGGING && eventID < 1000) {
                String registrarName = registrar.getClass().getSimpleName();
            }
            if (registrar instanceof Listener) {
                final Listener listener = (Listener) registrar;
                listener.onTransportNotification(eventID);
            } else if (registrar instanceof MessageListener) {
                final MessageListener listener = (MessageListener) registrar;
                listener.onTransportNotification(eventID, "null");
            }
        }
    }

    public static void triggerEvent(final int eventID, final String... args) {
        for (int i = 0; i < REGISTRY.size(); i++) {
            AbstractListener registrar = REGISTRY.get(i);
            if (DEBUGGING && eventID < 1000) {
                String registrarName = registrar.getClass().getSimpleName();
            }
            if (registrar instanceof MessageListener) {
                final MessageListener listener = (MessageListener) registrar;
                listener.onTransportNotification(eventID, args);
            }
        }
    }

    public static void triggerEvent(final int eventID, final Integer... args) {
        for (int i = 0; i < REGISTRY.size(); i++) {
            AbstractListener registrar = REGISTRY.get(i);
            if (DEBUGGING && eventID < 1000) {
                String registrarName = registrar.getClass().getSimpleName();
            }
            if (registrar instanceof IntegerListener) {
                final IntegerListener listener = (IntegerListener) registrar;
                listener.onTransportNotification(eventID, args);
            }
        }
    }

    /**
     * The listener interface for events only.
     */
    private interface AbstractListener {
    }

    /**
     * The listener interface for events only.
     */
    public interface Listener extends AbstractListener {
        /**
         * On transport notification.
         *
         * @param eventID the event
         */
        void onTransportNotification(int eventID);
    }

    /**
     * The listener interface for events with String args.
     */
    public interface MessageListener extends AbstractListener {
        /**
         * On transport notification.
         *
         * @param eventID the event
         */
        void onTransportNotification(int eventID, String... args);
    }

    /**
     * The listener interface for events with int args.
     */
    public interface IntegerListener extends AbstractListener {
        /**
         * On transport notification.
         *
         * @param eventID the event
         */
        void onTransportNotification(int eventID, Integer... args);
    }
}
