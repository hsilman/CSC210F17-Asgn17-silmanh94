package com.harrysilman.songquiz;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ThreadLocalRandom;


public class MainActivity extends Activity {


    final int NUMBER_OF_QUESTIONS = 10;

    private int questionNumber = 1;

    MusicPlayer player = new MusicPlayer();


    public int getQuestionNumber() {
        return questionNumber;
    }

    public void setQuestionNumber(int value) {
        questionNumber = value;
    }

    private boolean startQuiz = false;

    public void setStartQuiz(boolean value) {
        startQuiz = value;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // assign Buttons programmatically
        Button resetButton = findViewById(R.id.resetButton);
        Button startButton = findViewById(R.id.startButton);
        Button guessButtonOne = findViewById(R.id.guessOneButton);




        // configure listeners
        guessButtonOne.setOnClickListener(guessButtonOneListener);
        startButton.setOnClickListener(startButtonListener);
        resetButton.setOnClickListener(resetButtonListener);
    }

    // Start button listener
    private OnClickListener startButtonListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            // make start button disappear
            view.setVisibility(View.GONE);

            // get reset button and display it
            Button resetButton = findViewById(R.id.resetButton);
            resetButton.setVisibility(View.VISIBLE);
            resetButton.setOnClickListener(resetButtonListener);

            // get play-pause button and display it
            Button playButton = findViewById(R.id.playPauseButton);
            playButton.setVisibility(View.VISIBLE);
            playButton.setText("PLAY");

            newQuestion();
        }
    };

    private OnClickListener resetButtonListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            resetQuiz();
        }
    };

    private OnClickListener guessButtonOneListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            // get this button programmatically
            Button thisButton = findViewById(R.id.guessOneButton);

            if (startQuiz){
                //pause this button so it doesn't get clicked while loading next song
                thisButton.setEnabled(false);

                Toast toast = Toast.makeText(getApplicationContext(), "CORRECT!", Toast.LENGTH_SHORT );
                toast.show();
                setQuestionNumber(getQuestionNumber() + 1);
                newQuestion();
                thisButton.setEnabled(true);

                // set play/pause button to "Play"
                Button playPause = findViewById(R.id.playPauseButton);
                playPause.setText("Play");
                playPause.setOnClickListener(playPauseListener);
            }

        }
    };


    // create and update URL for API call
    private URL createURL(int randomOffset){
        String apiKey = getString(R.string.api_key);
        String baseURL = getString(R.string.web_service_url);

        // random offset so a random track is returned
        String offset = getString(R.string.offset) + randomOffset;

        try {
            // create URL with random offset
            String urlString = baseURL + apiKey + offset;
            return new URL(urlString);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null; // URL was malformed

    }

    // makes the REST web service call to get music data
    // saves the data to variables for invocation
    private class GetMusicData extends AsyncTask<URL, Void, JSONObject> {


        @Override
        protected JSONObject doInBackground(URL... params) {

            HttpURLConnection connection = null;

            try {
                connection = (HttpURLConnection) params[0].openConnection();
                int response = connection.getResponseCode();

                if (response == HttpURLConnection.HTTP_OK) {
                    StringBuilder builder = new StringBuilder();

                    // attempt to parse JSON response to JSONOBject
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()))) {

                        String line;

                        while ((line = reader.readLine()) != null) {
                            builder.append(line);
                        }
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                    return new JSONObject(builder.toString());
                }

                }
                catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                connection.disconnect(); // close the HttpURLConnection
            }
            return null;
        }

        // parse JSONObject to get strings
        @Override
        protected void onPostExecute(JSONObject musicData) {
            convertJSONtoStrings(musicData);

        }
    }

    // method to get song title for button and son url for mediaplayer
    private void convertJSONtoStrings(JSONObject musicData) {
        try {
            JSONArray tracksArray = musicData.getJSONArray("tracks");
            JSONObject tracksObject = tracksArray.getJSONObject(0);

            String songURL = (tracksObject.getString("previewURL"));
            String songName = (tracksObject.getString("name"));

            // create the music player and populate the button Name
            createMusicPlayer(songURL, songName);

        } catch (Exception e){
            e.printStackTrace();
        }

    }

    // method to generate random offset for song selection
    private int randomNum() {
        return ThreadLocalRandom.current().nextInt(0, 100);
    }

    private void createMusicPlayer(String songURL, String songName){

        player.getSource(songURL);

        // change first button text to song name
        Button buttonOne = findViewById(R.id.guessOneButton);
        buttonOne.setText(songName);

        // active play/pause button
        Button playPause = findViewById(R.id.playPauseButton);
        playPause.setText("Play");
        playPause.setOnClickListener(playPauseListener);

    }

    private OnClickListener playPauseListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            // get play-pause button to change text
            Button playButton = findViewById(R.id.playPauseButton);

            if(player.isPlaying()){
                playButton.setText("PLAY");
                player.pause();
            } else {
                player.start();
                playButton.setText("PAUSE");
            }
        }
    };

    // get a new song
    public void newQuestion(){

        if(questionNumber <= NUMBER_OF_QUESTIONS){
        // get quiz number textview and display current number
        TextView quizNumber = findViewById(R.id.quizNumberTextView);
        quizNumber.setText("Question " + questionNumber + " of " + NUMBER_OF_QUESTIONS);

        // generate random seed
        int randomOffset = randomNum();

        // get api URL
        URL sourceURL = createURL(randomOffset);

        // logcat statement to make sure URL is generated correctly
        Log.d("SOURCE", sourceURL.toString());

        // get JSON with data to populate quiz
        GetMusicData getMusicData = new GetMusicData();
        getMusicData.execute(sourceURL);

        setStartQuiz(true);
        } else if(questionNumber > NUMBER_OF_QUESTIONS)
        {
            Toast toast = Toast.makeText(getApplicationContext(), "FINISHED QUIZ, RESETTING", Toast.LENGTH_LONG );
            toast.show();
            resetQuiz();
        }
    }

    // reset the quiz
    public void resetQuiz() {
        if (player != null) {
            player.stop();
        }

        setQuestionNumber(1);
        setStartQuiz(false);

        // get start button and display it
        Button startButton = findViewById(R.id.startButton);
        startButton.setVisibility(View.VISIBLE);

        // get reset button and hide it
        Button resetButton = findViewById(R.id.resetButton);
        resetButton.setVisibility(View.INVISIBLE);

        // get play-pause button and hide it
        Button playButton = findViewById(R.id.playPauseButton);
        playButton.setVisibility(View.INVISIBLE);

        // get quizNumber and reset it to start message
        TextView quizNumber = findViewById(R.id.quizNumberTextView);
        quizNumber.setText(R.string.start_text);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        player.release();
    }

    @Override
    public void onPause() {
        super.onPause();

        player.release();

    }

}
