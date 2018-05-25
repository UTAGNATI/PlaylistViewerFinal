package com.example.andeloon.playlistviewer;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
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

public class MainActivity extends AppCompatActivity implements RecyclerViewClickListener {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Dialog languageDialog;


    public class PlaylistUpdate extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(PLAYLIST_UPDATE)) {
                Log.d("PlaylistUpdate", "onReceive playlist update received");
                mAdapter = new PlaylistAdapter(getPlaylistFromFile());
                mRecyclerView.setAdapter(mAdapter);
            }
        }
    }

    public static final String PLAYLIST_UPDATE = "com.octip.cours.inf4042_11.PLAYLIST_UPDATE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MainActivity", "onCreate call");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IntentFilter intentFilter = new IntentFilter(PLAYLIST_UPDATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(new PlaylistUpdate(),intentFilter);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_playlist);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        GetPlaylistService.startActionPlaylist(MainActivity.this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d("MainActivity", "onCreateOptionsMenu call");
        super.onCreateOptionsMenu(menu);
        MenuInflater myMenuInflater = getMenuInflater();
        myMenuInflater.inflate(R.menu.menu,menu);
        return true;
    }

    // ---------------------------------------------
    // Handle selection in the option menu
    // ---------------------------------------------
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("onOptionsItemSelected", "call");
        switch (item.getItemId()) {
            case R.id.action_language:
                languageDialog = new Dialog(MainActivity.this);
                languageDialog.setContentView(R.layout.radiobuttons_template);
                languageDialog.setCancelable(true);
                languageDialog.show();

                // Check radio button corresponding to the current locale
                Locale current = getResources().getConfiguration().locale;
                RadioGroup radio_grp=languageDialog.findViewById(R.id.buttonGroup);
                if (current.getLanguage().equals("en")) {
                    radio_grp.check(R.id.button_en);
                } else {
                    radio_grp.check(R.id.button_fr);
                }
                break;

            case R.id.action_update:
                GetPlaylistService.startActionPlaylist(MainActivity.this);
                Toast.makeText(this, getResources().getString(R.string.download), Toast.LENGTH_LONG).show();
                break;

            case R.id.action_credits:
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                String colorTitle = getResources().getString(R.string.credits_title);
                colorTitle = "<font color='#FD6A02'>" + colorTitle + "</font>";
                alertDialog.setTitle(Html.fromHtml(colorTitle));
                alertDialog.setMessage(getResources().getString(R.string.credits_text));
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
                break;
        }
        return true;
    }

    public JSONArray getPlaylistFromFile() {
        Log.d("MainActivity", "getPlaylistFromFile call");
        try {
            InputStream is = new FileInputStream(getCacheDir() + "/" + "playlist.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            Log.d("MainActivity", "getPlaylistFromFile "+Arrays.toString(buffer));
            return new JSONObject(new String(buffer, "UTF-8")).getJSONArray("data");
        } catch (IOException e) {
            e.printStackTrace();
            return new JSONArray();
        } catch (JSONException e) {
            e.printStackTrace();
            return new JSONArray();
        }
    }

     @Override
    public void recyclerViewListClicked(View v, int position) {
    }

    public class PlaylistAdapter extends RecyclerView.Adapter<com.example.andeloon.playlistviewer.MainActivity.PlaylistAdapter.PlaylistHolder> {
        private JSONArray mDataset;
        private Context context;
        private RecyclerViewClickListener itemListener;

        public class PlaylistHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public TextView name;
            public ImageView pic;
            public Button button;

            public PlaylistHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);
                pic = itemView.findViewById(R.id.imgtest);
                name = itemView.findViewById(R.id.textview_name);
                button = itemView.findViewById(R.id.button);
                Log.d("PlaylistHolder", "name="+name);
            }

            @Override
            public void onClick(View v) {

            }
        }

        public PlaylistAdapter(JSONArray myDataset) {
            Log.d("PlaylistAdapter", "PlaylistAdapter call");
            this.context = context;
            this.itemListener = itemListener;
            mDataset = myDataset;
        }


        @Override
        public com.example.andeloon.playlistviewer.MainActivity.PlaylistAdapter.PlaylistHolder onCreateViewHolder(ViewGroup parent,
                                                                                                     int viewType) {
            Log.d("PlaylistAdapter", "onCreateViewHolder call");
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.playlist_template, parent, false);

            com.example.andeloon.playlistviewer.MainActivity.PlaylistAdapter.PlaylistHolder ph = new com.example.andeloon.playlistviewer.MainActivity.PlaylistAdapter.PlaylistHolder(v);
            return ph;
        }

        @Override
        public void onBindViewHolder(com.example.andeloon.playlistviewer.MainActivity.PlaylistAdapter.PlaylistHolder holder, int position) {

            Log.d("PlaylistAdapter", "onBindViewHolder call at position "+position);
            URL picUrl = null; //test
            try {
                Picasso.with(MainActivity.this).load(mDataset.getJSONObject(position).getString("picture_medium")).into(holder.pic); //permet l'import des images
                Log.d("picTag",mDataset.getJSONObject(position).getString("picture_medium"));
                holder.name.setText(mDataset.getJSONObject(position).getString("title"));
                Log.d("tag", mDataset.getJSONObject(position).getString("title"));

                // Store the URL for the track list
                String trackListUrl=mDataset.getJSONObject(position).getString("tracklist");
                holder.button.setOnClickListener(new playListListener(trackListUrl));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        @Override
        public int getItemCount() {
            return mDataset.length();
        }
    }
    // -----------------------------------------------------------------
    // Custom listener who keeps track of the position in the playlist
    // -----------------------------------------------------------------
    public class playListListener implements View.OnClickListener
    {
        String trackListUrl;
        public playListListener(String url) {
            this.trackListUrl = url;
        }

        @Override
        public void onClick(View v)
        {
            Log.d("playListListener", "onClick call with track url "+trackListUrl);
            Intent intent = new Intent(MainActivity.this, PlaylistActivity.class);
            // We use a bundle to pass the position in the playlist
            Bundle b =  new Bundle();
            b.putString("url", trackListUrl);
            intent.putExtras(b);
            startActivity(intent);
        }
    };

    // -----------------------------------------
    // Handle locale change (french or english)
    // -----------------------------------------
    public void onChangeLanguage(View view) {
        Log.d("MainActivity", "onChangeLanguage call");
        boolean checked = ((RadioButton) view).isChecked();
        switch(view.getId()) {
            case R.id.button_en:
                if (checked) {
                    setLocale("en");
                }
                break;
            case R.id.button_fr:
                if (checked) {
                    setLocale("fr");
                }
                break;
            default:
                setLocale("en");
                break;
        }
    }

    private Configuration config;
    public void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Log.d("MainActivity", "setLocale call with "+lang);
        config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }

    public void dismissLanguageWindow(View view) {
        languageDialog.dismiss();
        // Apply new locale to the application
        recreate();
    }
}
