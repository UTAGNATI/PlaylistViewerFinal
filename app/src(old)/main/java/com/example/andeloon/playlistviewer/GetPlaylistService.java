package com.example.andeloon.playlistviewer;

import android.app.Application;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Locale;

public class GetPlaylistService extends IntentService {

    private static final String ACTION_PLAY = "com.example.andeloon.playlistviewer.action.Playlist";

    public GetPlaylistService() {
        super("GetPlaylistService");
    }

    public static void startActionPlaylist(Context context) {
        Intent intent = new Intent(context, GetPlaylistService.class);
        intent.setAction(ACTION_PLAY);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PLAY.equals(action)) {
                handleActionPlaylist();
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

    private void handleActionPlaylist() {
        Log.d("handleActionPlaylist", "thread service name=" + Thread.currentThread().getName());
        URL url = null;
        try {
            url = new URL("https://api.deezer.com/user/5906315/playlists");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            if (HttpURLConnection.HTTP_OK == conn.getResponseCode()){
                copyInputStreamToFile(conn.getInputStream(), new File(getCacheDir(), "playlist.json"));
                Log.d("handleActionPlaylist", "playlist json downloaded");

            }
        }catch (MalformedURLException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(MainActivity.PLAYLIST_UPDATE));
    }

}
