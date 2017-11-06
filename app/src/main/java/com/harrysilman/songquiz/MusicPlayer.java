package com.harrysilman.songquiz;

import android.media.AudioManager;
import android.media.MediaPlayer;

/**
 * Created by Silman on 11/5/2017.
 */

// a MusicPlayer Class/Object to handle playing the actual song clip

public class MusicPlayer extends MediaPlayer {

    // basic constructor
    public MusicPlayer(){
        new MediaPlayer();
    }

    public MusicPlayer getSource(String url) {
        try {
            if(isPlaying()){
                stop();
            }
            reset();
            setAudioStreamType(AudioManager.STREAM_MUSIC);
            setDataSource(url);
            prepare();
        } catch (Exception e) {
        }
        return null;
    }
}
