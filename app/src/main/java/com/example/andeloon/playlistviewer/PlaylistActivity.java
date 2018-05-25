package com.example.andeloon.playlistviewer;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Locale;

public class PlaylistActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    public class TrackUpdate extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(TRACKLIST_UPDATE)) {
                Log.d("TrackUpdate", "onReceive track update received");
                mAdapter = new TrackAdapter(getTracklistFromFile());
                mRecyclerView.setAdapter(mAdapter);
            }
        }
    }

    public static final String TRACKLIST_UPDATE = "com.octip.cours.inf4042_11.TRACK_UPDATE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlist_activity);
        String trackListUrl = getIntent().getExtras().getString("url");
        Log.d("PlaylistActivity", " onCreate call with track url "+trackListUrl);

        IntentFilter intentFilter = new IntentFilter(TRACKLIST_UPDATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(new TrackUpdate(), intentFilter);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_tracklist);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        GetTracksService.startActionTrack(PlaylistActivity.this, trackListUrl);


    }

    public JSONArray getTracklistFromFile() {
        Log.d("PlaylistActivity", "getTracklistFromFile call");
        try {
            InputStream is = new FileInputStream(getCacheDir() + "/" + "tracklist.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            Log.d("PlaylistActivity", "getTracklistFromFile "+Arrays.toString(buffer));
            return new JSONObject(new String(buffer, "UTF-8")).getJSONArray("data");
        } catch (IOException e) {
            e.printStackTrace();
            return new JSONArray();
        } catch (JSONException e) {
            e.printStackTrace();
            return new JSONArray();
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("onOptionsItemSelected", "call");
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return true;
    }

    public class TrackAdapter extends RecyclerView.Adapter<com.example.andeloon.playlistviewer.PlaylistActivity.TrackAdapter.TrackHolder> {
        private JSONArray trackDataset;
        private Context context;

        public class TrackHolder extends RecyclerView.ViewHolder {
            public TextView trackName;
            public TextView artistName;
            public TextView albumName;
            public ImageView pic;
            public int position;
            public ImageButton button;

            public TrackHolder(View itemView) {
                super(itemView);
                pic = itemView.findViewById(R.id.track_img);
                trackName = itemView.findViewById(R.id.track_name);
                artistName = itemView.findViewById(R.id.album_name);
                albumName = itemView.findViewById(R.id.artist_name);
                button = itemView.findViewById(R.id.play_button);
                Log.d("TrackHolder", "trackname="+trackName);
            }
        }

        public TrackAdapter(JSONArray trackDataset) {
            Log.d("TrackAdapter", "TrackAdapter call");
            this.context = context;
            this.trackDataset = trackDataset;
        }

        public class trackListener implements View.OnClickListener
        {
            String trackPreviewUrl;
            public trackListener(String url) {
                this.trackPreviewUrl = url;
            }

            @Override
            public void onClick(View v)
            {
                if(!trackPreviewUrl.equals("")) {
                    Log.d("trackListener", "onClick call with preview url "+trackPreviewUrl);
                    Uri uriUrl = Uri.parse(trackPreviewUrl);
                    Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                    startActivity(launchBrowser);
                }
                else {
                    Log.d("tracjListener","onClick call with preview url empty");
                    AlertDialog alertDialog = new AlertDialog.Builder(PlaylistActivity.this).create();
                    String colorTitle = getResources().getString(R.string.err_preview_title);
                    colorTitle = "<font color='#FD6A02'>" + colorTitle + "</font>";
                    alertDialog.setTitle(Html.fromHtml(colorTitle));
                    alertDialog.setMessage(getResources().getString(R.string.err_preview_text));
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }

            }
        };


        @Override
        public com.example.andeloon.playlistviewer.PlaylistActivity.TrackAdapter.TrackHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Log.d("TrackAdapter", "onCreateViewHolder call");
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.track_template, parent, false);

            com.example.andeloon.playlistviewer.PlaylistActivity.TrackAdapter.TrackHolder th = new com.example.andeloon.playlistviewer.PlaylistActivity.TrackAdapter.TrackHolder(v);
            return th;
        }

        @Override
        public void onBindViewHolder(com.example.andeloon.playlistviewer.PlaylistActivity.TrackAdapter.TrackHolder holder, int position) {
            //Log.d("TrackAdapter", "onBindViewHolder call at position "+position);
            URL picUrl = null; //test
            try {
                if (trackDataset.getJSONObject(position).getJSONObject("album").has("cover")) {
                    Picasso.with(PlaylistActivity.this).load(trackDataset.getJSONObject(position).getJSONObject("album").getString("cover")).into(holder.pic); //permet l'import des images
                }
                //Log.d("picTag",trackDataset.getJSONObject(position).getJSONObject("album").getString("cover"));
                //Log.d("tag", trackDataset.getJSONObject(position).getString("title"));
                holder.trackName.setText(trackDataset.getJSONObject(position).getString("title"));
                holder.artistName.setText(trackDataset.getJSONObject(position).getJSONObject("artist").getString("name"));
                holder.albumName.setText(trackDataset.getJSONObject(position).getJSONObject("album").getString("title"));
                holder.position=position;
                String previewUrl=trackDataset.getJSONObject(position).getString("preview");
                holder.button.setOnClickListener(new trackListener(previewUrl));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return trackDataset.length();
        }
    }


}
