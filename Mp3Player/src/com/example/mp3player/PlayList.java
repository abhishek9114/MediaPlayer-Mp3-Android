package com.example.mp3player;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class PlayList extends ListActivity{
 
	ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
	String mp3Pattern = ".mp3";
	String MEDIA_PATH;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.playlist);
		ArrayList<HashMap<String, String>> songsListData = new ArrayList<HashMap<String, String>>();
		File root = Environment.getExternalStorageDirectory();
		Map<String, File> externalLocations = ExternalStorage.getAllStorageLocations();
		File sdCard = externalLocations.get(ExternalStorage.SD_CARD);
		File externalSdCard = externalLocations.get(ExternalStorage.EXTERNAL_SD_CARD);
		MEDIA_PATH = externalSdCard.getAbsolutePath() + "/";

		// System.out.println(MEDIA_PATH);
		if (MEDIA_PATH != null) {
			File home = new File(MEDIA_PATH);
			File[] listFiles = home.listFiles();
			if (listFiles != null && listFiles.length > 0) {
				for (File file : listFiles) {
					if (file.isDirectory()) {
						// Toast.makeText(this,file.getAbsolutePath() ,
						// 1000).show();
						scanDirectory(file);
					} else {
						addSongToList(file);
					}
				}
			}

		}
		for (int i = 0; i < songsList.size(); i++) {
			// creating new HashMap
			HashMap<String, String> song = songsList.get(i);

			// adding HashList to ArrayList
			songsListData.add(song);
		}

		// Adding menuItems to ListView
		ListAdapter adapter = new SimpleAdapter(this, songsListData,
				R.layout.playlist_item, new String[] { "songTitle" }, new int[] {
						R.id.songTitle });
		setListAdapter(adapter);
		// selecting single ListView item
		ListView lv = getListView();
		// listening to single listitem click
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// getting listitem index
				int songIndex = position;
				
				// Starting new intent
				Intent in = new Intent(getApplicationContext(),
						MainActivity.class);
				// Sending songIndex to PlayerActivity
				in.putExtra("songIndex", songIndex);
				setResult(100, in);
				// Closing PlayListView
				finish();
			}
		});

	}


	private void scanDirectory(File directory) {
		// TODO Auto-generated method stub
		if (directory != null) {
			File[] listFiles = directory.listFiles();
			if (listFiles != null && listFiles.length > 0) {
				for (File file : listFiles) {
					if (file.isDirectory()) {
						// Toast.makeText(this,file.getAbsolutePath() ,
						// 1000).show();
						scanDirectory(file);
					} else {
						addSongToList(file);
					}

				}
			}
		}
		
	}

	private void addSongToList(File song) {
		// TODO Auto-generated method stub
		if (song.getName().endsWith(mp3Pattern)) {
			//Toast.makeText(this, song.getAbsolutePath(), 1000).show();
			HashMap<String, String> songMap = new HashMap<String, String>();
			songMap.put("songTitle", song.getName().substring(0, (song.getName().length() - 4)));
			songMap.put("songPath", song.getPath());

			// Adding each song to SongList
			songsList.add(songMap);
		}
	}
}
