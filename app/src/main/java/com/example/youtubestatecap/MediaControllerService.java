package com.example.youtubestatecap;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.List;

public class MediaControllerService extends Service {

    private static final String TAG = "YoutubeStateCap";
    private static final String CHANNEL_ID = "1000";
    private static final int NOTIFICATION_ID = 1;
    private List<MediaController> mActiveSessions;
    private MediaController.Callback mSessionCallback;


    public MediaControllerService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = createNotifiction();
        initMediaSessionManager();
        startForeground(NOTIFICATION_ID, notification);
        Log.i(TAG, "starting service...");

        return START_STICKY;
    }

    private void initMediaSessionManager() {
        MediaSessionManager mediaSessionManager = (MediaSessionManager) getSystemService(MEDIA_SESSION_SERVICE);
        ComponentName localComponentName = new ComponentName(this, MediaControllerService.class);
        mediaSessionManager.addOnActiveSessionsChangedListener(new MediaSessionManager.OnActiveSessionsChangedListener() {
            @Override
            public void onActiveSessionsChanged(@Nullable List<MediaController> controllers) {
                for(MediaController mediaController : controllers) {
                    String packageName = mediaController.getPackageName();
                    Log.i(TAG, "onActiveSessionsChanged event occurs. Current package name: " + packageName);
                    synchronized (this) {
                        mActiveSessions = controllers;
                        registerSessionCallback();
                    }
                }
            }
        }, localComponentName);

        synchronized (this) {
            mActiveSessions = mediaSessionManager.getActiveSessions(localComponentName);
            registerSessionCallback();
        }

    }

    private void registerSessionCallback() {
        for(MediaController controller : mActiveSessions) {
            if(this.mSessionCallback == null) {
                this.mSessionCallback = new MediaController.Callback() {
                    @Override
                    public void onMetadataChanged(@Nullable MediaMetadata metadata) {
                        super.onMetadataChanged(metadata);
                        if(metadata != null) {
                            String title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE);
                            String artistName = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST);
                            String albumArtistName = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST);
                            String albumName = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM);
                            int duration = (int)metadata.getLong(MediaMetadata.METADATA_KEY_DURATION);
                            String uri = (String)metadata.getString(MediaMetadata.METADATA_KEY_MEDIA_URI);
                            String id = (String)metadata.getString(MediaMetadata.METADATA_KEY_MEDIA_ID);
                            String author = metadata.getString(MediaMetadata.METADATA_KEY_AUTHOR);
                            String date = metadata.getString(MediaMetadata.METADATA_KEY_DATE);
                            String artUri = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI);




                            Log.i(TAG, "Title: " + title);
                            Log.i(TAG, "Artist Name: " + artistName);
                            Log.i(TAG, "Album Artist: " + albumArtistName);
                            Log.i(TAG, "Album Name: " + albumName);
                            Log.i(TAG, "Duration: " + duration);
                            Log.i(TAG, "URI: " + uri);
                            Log.i(TAG, "ID: " + id);
                            Log.i(TAG, "Author: " + author);
                            Log.i(TAG, "Date: " + date);
                            Log.i(TAG, "Art URI: " + artUri);
                        }
                    }

                    @Override
                    public void onPlaybackStateChanged(@Nullable PlaybackState state) {
                        super.onPlaybackStateChanged(state);
                        if(state != null) {
                            boolean isPlaying = state.getState() == PlaybackState.STATE_PLAYING;
                            Log.i(TAG, "Playback State: " + state.getState());
                            Log.i(TAG, "MediaController.Callback onPlaybackStateChanged isPlaying: " + isPlaying);
                        }
                    }
                };
            }

            controller.registerCallback(mSessionCallback);
        }
    }

    private Notification createNotifiction() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Foreground Service")
                .setContentText("Running...")
                .setSmallIcon(R.drawable.ic_launcher_foreground);

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);

        return builder.build();
    }


}