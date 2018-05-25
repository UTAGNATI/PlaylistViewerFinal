package com.example.andeloon.playlistviewer;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class GetTracksService extends IntentService {

    private static final String ACTION_TRACK = "com.example.andeloon.playlistviewer.action.track";

    public GetTracksService() {
        super("GetTrackService");
    }

    public static void startActionTrack(Context context, String url) {
        Intent intent = new Intent(context, GetTracksService.class);
        intent.setAction(ACTION_TRACK);
        Bundle b =  new Bundle();
        b.putString("url", url);
        intent.putExtras(b);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_TRACK.equals(action)) {
                String trackListUrl = intent.getExtras().getString("url");
                handleActionTrack(trackListUrl);
            }
        }
    }

    private void copyInputStreamToFile(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len=in.read(buf))>0) {
                out.write(buf, 0, len);
            }
            out.close();
            in.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void handleActionTrack(String trackUrl) {
        Log.d("handleActionTrack", "track URL=" + trackUrl);
        URL url = null;
        try {
            url = new URL(trackUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            if (HttpURLConnection.HTTP_OK == conn.getResponseCode()){
                copyInputStreamToFile(conn.getInputStream(), new File(getCacheDir(), "tracklist.json"));
                Log.d("handleActionTrack", "track json downloaded !");
            }

        }catch (MalformedURLException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(PlaylistActivity.TRACKLIST_UPDATE));
    }

}
