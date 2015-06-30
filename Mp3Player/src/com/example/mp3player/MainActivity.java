package com.example.mp3player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.example.mp3player.Utilities;
import com.example.mp3player.ExternalStorage;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnCompletionListener, OnSeekBarChangeListener {

	String pi;
	//static int i = 0;
	static int currentindex=0;
	ImageButton btnRepeat, btnShuffle, btnPlaylist;
	Button btnPrevious, btnNext, btnBackward, btnForward, btnPlay;
	TextView songTitleLabel;
	ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
	String mp3Pattern = ".mp3";
	String MEDIA_PATH;
	TextView songCurrentDurationLabel, songTotalDurationLabel;
	SeekBar songProgressBar;
	// Media Player
	 static MediaPlayer mp = null ;
	// Handler to update UI timer, progress bar etc,.
	Handler mHandler = new Handler();
	Utilities utils;
	int seekForwardTime = 5000; // 5000 milliseconds
	int seekBackwardTime = 5000; // 5000 milliseconds
	static int currentSongIndex = 0;
	boolean isShuffle = false;
	boolean isRepeat = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		btnPrevious = (Button) findViewById(R.id.bprev);
		btnNext = (Button) findViewById(R.id.bnext);
		btnForward = (Button) findViewById(R.id.bforward);
		btnBackward = (Button) findViewById(R.id.bbackward);
		btnRepeat = (ImageButton) findViewById(R.id.brepeat);
		btnShuffle = (ImageButton) findViewById(R.id.bshuffle);
		btnPlay = (Button) findViewById(R.id.bmiddle);
		btnPlaylist = (ImageButton) findViewById(R.id.btnplaylist);
		songTitleLabel = (TextView) findViewById(R.id.et);
		songCurrentDurationLabel = (TextView) findViewById(R.id.tseekstart);
		songTotalDurationLabel = (TextView) findViewById(R.id.tseekend);
		songProgressBar = (SeekBar) findViewById(R.id.seekbar);
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
		
		if(mp==null)
		{
			mp = new MediaPlayer();
			btnPlay.setBackgroundResource(android.R.drawable.ic_media_play);
			currentSongIndex=0;
			currentindex=0;
		//playSong(0);
			try
			{
			mp.reset();
			mp.setDataSource(songsList.get(0).get("songPath"));
			mp.prepare();
			String songTitle = songsList.get(currentindex).get("songTitle");
			songTitleLabel.setText(songTitle);
			//mp.start();
			}catch(Exception e)
			{
				
			}
			
		}
		else
		{
			updateProgressBar();
			String songTitle = songsList.get(currentindex).get("songTitle");
			songTitleLabel.setText(songTitle);
			if(mp.isPlaying())
				btnPlay.setBackgroundResource(android.R.drawable.ic_media_pause);
			else
				btnPlay.setBackgroundResource(android.R.drawable.ic_media_play);
				
		}
		utils = new Utilities();
		// Listeners
				songProgressBar.setOnSeekBarChangeListener(this); // Important
				mp.setOnCompletionListener(this); // Important
		// By default play first song
			//playSong(0);
		btnPlay.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// check for already playing
				if (mp.isPlaying()) {
					if (mp != null) {
						mp.pause();
						// Changing button image to play button
						btnPlay.setBackgroundResource(android.R.drawable.ic_media_play);
					}
				} else {
					// Resume song
					if (mp != null) {
						mp.start();
						// Changing button image to pause button
						btnPlay.setBackgroundResource(android.R.drawable.ic_media_pause);
					}
				}

			}
		});

		btnForward.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// get current song position
				int currentPosition = mp.getCurrentPosition();
				// check if seekForward time is lesser than song duration
				if (currentPosition + seekForwardTime <= mp.getDuration()) {
					// forward song
					mp.seekTo(currentPosition + seekForwardTime);
				} else {
					// forward to end position
					mp.seekTo(mp.getDuration());
				}
			}
		});
		btnBackward.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// get current song position
				int currentPosition = mp.getCurrentPosition();
				// check if seekBackward time is greater than 0 sec
				if (currentPosition - seekBackwardTime >= 0) {
					// forward song
					mp.seekTo(currentPosition - seekBackwardTime);
				} else {
					// backward to starting position
					mp.seekTo(0);
				}

			}
		});
		btnNext.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// check if next song is there or not
				if (currentSongIndex < (songsList.size() - 1)) {
					playSong(currentSongIndex + 1);
					currentSongIndex = currentSongIndex + 1;
				} else {
					// play first song
					playSong(0);
					currentSongIndex = 0;
				}

			}
		});
		btnPrevious.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (currentSongIndex > 0) {
					playSong(currentSongIndex - 1);
					currentSongIndex = currentSongIndex - 1;
				} else {
					// play last song
					playSong(songsList.size() - 1);
					currentSongIndex = songsList.size() - 1;
				}

			}
		});

		btnRepeat.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (isRepeat) {
					isRepeat = false;
					Toast.makeText(getApplicationContext(), "Repeat is OFF", Toast.LENGTH_SHORT).show();
					 btnRepeat.setImageResource(R.drawable.btn_repeat);
				} else {
					// make repeat to true
					isRepeat = true;
					Toast.makeText(getApplicationContext(), "Repeat is ON", Toast.LENGTH_SHORT).show();
					// make shuffle to false
					isShuffle = false;
					 btnRepeat.setImageResource(R.drawable.btn_repeat_focused);
				 btnShuffle.setImageResource(R.drawable.btn_shuffle);
				}
			}
		});
		btnShuffle.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (isShuffle) {
					isShuffle = false;
					Toast.makeText(getApplicationContext(), "Shuffle is OFF", Toast.LENGTH_SHORT).show();
					 btnShuffle.setImageResource(R.drawable.btn_shuffle);
				} else {
					// make repeat to true
					isShuffle = true;
					Toast.makeText(getApplicationContext(), "Shuffle is ON", Toast.LENGTH_SHORT).show();
					// make shuffle to false
					isRepeat = false;
					 btnShuffle.setImageResource(R.drawable.btn_shuffle_focused);
					 btnRepeat.setImageResource(R.drawable.btn_repeat);
				}
			}
		});
		btnPlaylist.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(getApplicationContext(), PlayList.class);
				startActivityForResult(i, 100);
			}
		});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == 100) {
			currentSongIndex = data.getExtras().getInt("songIndex");
			// play selected song
			playSong(currentSongIndex);
		}

	}

	public void playSong(int songIndex) {
		// TODO Auto-generated method stub
		// Play song
		try {	
			mp.reset();
			mp.setDataSource(songsList.get(songIndex).get("songPath"));
			mp.prepare();
			mp.start();
			// Displaying Song title
			String songTitle = songsList.get(songIndex).get("songTitle");
			currentindex = songIndex;
			songTitleLabel.setText(songTitle);

			// Changing Button Image to pause image
			btnPlay.setBackgroundResource(android.R.drawable.ic_media_pause);

			// set Progress bar values
			songProgressBar.setProgress(0);
			songProgressBar.setMax(100);

			// Updating progress bar
			updateProgressBar();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void scanDirectory(File directory) {
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

	public void addSongToList(File song) {
		// TODO Auto-generated method stub
		if (song.getName().endsWith(mp3Pattern)) {
			// Toast.makeText(this, song.getAbsolutePath(), 1000).show();
			HashMap<String, String> songMap = new HashMap<String, String>();
			songMap.put("songTitle", song.getName().substring(0, (song.getName().length() - 4)));
			songMap.put("songPath", song.getPath());

			// Adding each song to SongList
			songsList.add(songMap);
		}
	}

	public void updateProgressBar() {
		mHandler.postDelayed(mUpdateTimeTask, 100);
	}

	private Runnable mUpdateTimeTask = new Runnable() {
		public void run() {
			long totalDuration = mp.getDuration();
			long currentDuration = mp.getCurrentPosition();

			// Displaying Total Duration time
			songTotalDurationLabel.setText("" + utils.milliSecondsToTimer(totalDuration));
			// Displaying time completed playing
			songCurrentDurationLabel.setText("" + utils.milliSecondsToTimer(currentDuration));

			// Updating progress bar
			int progress = (int) (utils.getProgressPercentage(currentDuration, totalDuration));
			// Log.d("Progress", ""+progress);
			songProgressBar.setProgress(progress);

			// Running this thread after 100 milliseconds
			mHandler.postDelayed(this, 100);
		}
	};

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {

	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// remove message Handler from updating progress bar
		mHandler.removeCallbacks(mUpdateTimeTask);
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		mHandler.removeCallbacks(mUpdateTimeTask);
		int totalDuration = mp.getDuration();
		int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);

		// forward or backward to certain seconds
		mp.seekTo(currentPosition);

		// update timer progress again
		updateProgressBar();
	}

	@Override
	public void onCompletion(MediaPlayer arg0) {

		// check for repeat is ON or OFF
		if (isRepeat) {
			// repeat is on play same song again
			playSong(currentSongIndex);
		} else if (isShuffle) {
			// shuffle is on - play a random song
			Random rand = new Random();
			currentSongIndex = rand.nextInt((songsList.size() - 1) - 0 + 1) + 0;
			playSong(currentSongIndex);
		} else {
			// no repeat or shuffle ON - play next song
			if (currentSongIndex < (songsList.size() - 1)) {
				playSong(currentSongIndex + 1);
				currentSongIndex = currentSongIndex + 1;
			} else {
				// play first song
				playSong(0);
				currentSongIndex = 0;
			}
		}
	}
}
