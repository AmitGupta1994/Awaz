package com.awaj;

import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.stetho.Stetho;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    DatabaseHelper databaseHelper;

    //Instance Variable And Constants Initialization/Declaration


    private static ImageView homeIV,listIV,settingsIV;
    private static ImageView recLogo;

    public static TextView decibelTV;   //To Show Average of the decibel value after each buffer
    private static TextView frequencyTV;
    private static TextView notesTV;
    public static TextView timerTV;

    private static Button rec;
    private static Button play;
    private static GraphFragment graphFragment = new GraphFragment();
    private static ListFragment listFragment = new ListFragment();

    private static AudioRecordClass audioRecordClass;
    private static AudioPlayClass audioPlayClass;

    public static final Timer timerStartObj = new Timer(3000000, 1000);


    private static int rec_btn_count = 0;
    private static int play_btn_count =0;   //To Know Button was Pressed
    private static boolean isRecording = false;    //To Know RECORDING Or STOPPED
    private static boolean isPlaying = false;//To Know PLAYING or STOPPED

    private static int playState = 0;   //TO Know RECORDING or PLAYING

    private final String TAG = MainActivity.class.getSimpleName();

    //START---Audio Record and Play Parameters-----
    // THE DEFINETIONS ARE DEFINED IN THE RESPECTIVE FUNCTION
    private static final int AUDIO_SOURCE = setAudioSource(MediaRecorder.AudioSource.MIC);
    private static final int SAMPLE_RATE_IN_HZ = setSampleRateInHz(22050);
    private static final int CHANNELS_CONFIGURATION = setChannelsConfiguration(AudioFormat.CHANNEL_IN_MONO);
    private static final int AUDIO_ENCODING = setAudioEncoding(AudioFormat.ENCODING_PCM_16BIT);

    private static final int NO_OF_SAMPLES = setNoOfSamples(8192);
    private static final float RESOLUTION = setResolution(SAMPLE_RATE_IN_HZ / NO_OF_SAMPLES);

    private static final int MIN_BUFFER_SIZE_BYTES = setMinBufferSizeInBytes(NO_OF_SAMPLES*2);
    //END---Audio Record and Play Parameters-----

    /**
     * Objects
     */
    Switch domainSwitch, visualizationSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //database part
        Stetho.initializeWithDefaults(this);
        databaseHelper = new DatabaseHelper(this);

        super.onCreate(savedInstanceState);

        try{
            databaseHelper.createDatabase();
        }catch (IOException e){
            throw new Error(e);
        }
        try {
            databaseHelper.openDatabase();
        }catch (SQLException e){
            throw e;
        }
        databaseHelper.getAllData();
        //frequency match test
        int match = databaseHelper.matchFreq(184.5);
        String note = databaseHelper.getNote(match);

        Log.d("VIVZ", "note="+note+" match="+match);

        //end of database part

        setContentView(R.layout.activity_main);

        /**START-Referencing UI Elements*/
            referenceToUIElements();
        /**End-Referencing UI Elements*/

        /**Start-GRAPH FRAGMENT*/
        FragmentManager myFragmentManager = getSupportFragmentManager();
        FragmentTransaction myFragmentTransaction = myFragmentManager.beginTransaction();
        myFragmentTransaction.add(R.id.graphFragmentLL, graphFragment, " ");
        myFragmentTransaction.commit();
        /**End-GRAPH FRAGMENT*/

        /**Switches*/
        domainSwitch = (Switch) findViewById(R.id.domainSwitch);
//        domainSwitch.setText("AMP");
//        visualizationSwitch = (Switch) findViewById(R.id.visualizationSwitch);
//        visualizationSwitch.setText("WAVE");

        /**HOME - LIST - SETTING Button*/
        if(listIV!=null) {
            listIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent toListTabs = new Intent(MainActivity.this, ListTabs.class);
                    startActivity(toListTabs);
                }
            });
        }

        //Setting To be made before Recording,Playing and Graph Plotting
        graphFragment.setMinBufferSizeInBytes(MIN_BUFFER_SIZE_BYTES);

        /**Start of Record Button*/
        record();
        /**End of Record Button*/

        /**Start of Play Button*/
        play();
        /**End of Play Button*/

    }//End-onCreate()


    // METHOD/FUNCTION DEFINITION SECTION

    // Returns the minimum buffer size required for the successful creation of an AudioRecord object, in byte units.
    public static  int setMinBufferSizeInBytes(int x){
        return x;
    }
    public static  int getMinBufferSizeInBytes(){
        return MIN_BUFFER_SIZE_BYTES;
    }

    public static void updateDecibel(float decibel){
        //The CALCULATED DECIBEL VALUE IN AudioPlayClass/AudioRecordClass is SENT to show in TEXTVIEW
        decibelTV.setText(String.valueOf(decibel));
    }

    public static void updateFrequncy(float frequency){
        //The CALCULATED FREQUNCY VALUE IN AudioPlayClass/AudioRecordClass is SENT to show in TEXTVIEW
        frequencyTV.setText(String.valueOf(frequency));
    }
    public static void updateNotes(String notes){
        //The CALCULATED FREQUNCY VALUE IN AudioPlayClass/AudioRecordClass i SENT to show in TEXTVIEW
        notesTV.setText(String.valueOf(notes));
    }

    public static  void updateRecordState(){
        rec.setText("RECORD");
        rec.setTextColor(Color.parseColor("#ffffff"));
        play.setEnabled(true);


        rec_btn_count =0;
    }

    public  static  void updatePlayState(){
        rec.setTextColor(Color.parseColor("#ffffff"));
        play.setText("play");
        play.setTextColor(Color.parseColor("#00b900"));
        rec.setEnabled(true);
        play_btn_count = 0;

        isPlaying=false;

        timerStartObj.cancel();
        timerStartObj.SS = 0L;
        timerStartObj.MM = 0L;
        timerStartObj.HH = 0L;
        timerStartObj.MS = 0L;
    }


    //START OF AUDIO RECORD-PLAY SECTION
    public static int setAudioSource(int x){
        return  x;
    };
    public static int getAudioSource(){
        return  AUDIO_SOURCE;
    }
    public static int setSampleRateInHz(int x){
        return  x;
    }
    public static int getSampleRateInHz(){
        return  SAMPLE_RATE_IN_HZ;
    }
    public static int setChannelsConfiguration(int x){
        return  x;
    }
    public static int getChannelsConfiguration(){
        return  CHANNELS_CONFIGURATION;
    }
    public static int setAudioEncoding(int x){
        return  x;
    }
    public static int getAudioEncoding(){
        return  AUDIO_ENCODING;
    }
    public static int setNoOfSamples(int x){
        return  x;
    }
    public static int getNoOfSamples(){
        return  NO_OF_SAMPLES;
    }
    public static float setResolution(float x){
        return  x;
    }
    public static float getResolution(){
        return  RESOLUTION;
    }

    public static boolean getValueOfisRecording(){
        return isRecording;
    }
    public static boolean getValueOfisPlaying(){
        return isPlaying;
    }

    //END OF AUDIO RECORD-PLAY SECTION


    //START OF Graph Fragment Section
    public static void plotGraph(float[] audioFloatsForAmp,float[] fftOutput){

        if (graphFragment.getGraphFragmentMode() == 0) {
            /**Amplitude Mode*/
            graphFragment.updateGraph(audioFloatsForAmp);
        } else if (graphFragment.getGraphFragmentMode() == 1) {
            /**Frequency Mode*/
            graphFragment.updateGraph(fftOutput);
        }
    }

    //END OF Graph Fragment Section

    //Start-Functions in MainActivity
    //Start-record()
    public void record(){
        if(rec !=null) {

            rec.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    audioRecordClass = new AudioRecordClass();


                    if (rec_btn_count == 0){

                        /**Code to handle click of "RECORD" button*/
                        playState = 0;
                        isRecording = true;
                        audioRecordClass.execute();
                        rec_btn_count = 1;

                        rec.setText("STOP");
                        rec.setTextColor(Color.parseColor("#ff0000"));
                        play.setEnabled(false);
                        play.setTextColor(Color.parseColor("#808080"));
                        Toast.makeText(getApplicationContext(), "Recording started", Toast.LENGTH_SHORT).show();

                        recLogo.setVisibility(View.VISIBLE);

                        timerTV.setText("00:00:00");
                        timerStartObj.start();

                    } else if (rec_btn_count == 1) {
                        /**Code to handle click of "Rec-STOP" button*/
                        isRecording = false;
                        play.setTextColor(Color.parseColor("#00ff00"));
                        recLogo.setVisibility(View.INVISIBLE);

                        timerStartObj.cancel();

                        timerStartObj.SS = 0L;
                        timerStartObj.MM = 0L;
                        timerStartObj.HH = 0L;
                        timerStartObj.MS = 0L;


                    }
                }
            });
        }
    }//End-record()

    //Start-play()
    public  void play(){

        if (play != null) {
            play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    audioPlayClass = new AudioPlayClass();


                    if (play_btn_count == 0) {


                        //PLAY Buttton
                        playState = 1;

                        play_btn_count = 1;
                        Log.d(TAG, "Clicked - play audio");
                        Toast.makeText(getApplicationContext(), "Playing audio", Toast.LENGTH_SHORT).show();
                        play.setText("Stop");
                        play.setTextColor(Color.parseColor("#ff0000"));
                        rec.setEnabled(false);

                        isPlaying = true;

                        audioPlayClass.execute();

                        play.setText("Stop");
                        play.setTextColor(Color.parseColor("#ff0000"));
                        rec.setEnabled(false);
                        rec.setTextColor(Color.parseColor("#808080"));
//                        Log.d("VIVZ", "Clicked - play audio");
                        Toast.makeText(getApplicationContext(), "Playing audio", Toast.LENGTH_SHORT).show();
                        timerTV.setText("00:00:00");
                        timerStartObj.start();
                    } else if (play_btn_count == 1) {
                        /**Code to pause/stop the playback*/
                        isPlaying = false;

                        timerStartObj.cancel();

                        timerStartObj.SS = 0L;
                        timerStartObj.MM = 0L;
                        timerStartObj.HH = 0L;
                        timerStartObj.MS = 0L;
                    }
                }
            });
        }
    }
    //End-play()


    public void referenceToUIElements(){
        //Start-To Show Decibels,Frequncy and Notes
        decibelTV = (TextView) findViewById(R.id.decibel);
        frequencyTV = (TextView) findViewById(R.id.frequencyTV);
        notesTV = (TextView) findViewById(R.id.notesTV);
        //End- To Show Decibels,Frequncy and Notes

        //Start-To Record and Play
        rec = (Button) findViewById(R.id.rec);
        play = (Button) findViewById(R.id.play);
        play.setTextColor(Color.parseColor("#808080"));
        //End-To Record and Play


        /**Start-Timer UI Setups*/
        timerTV = (TextView) findViewById(R.id.timerTV);
        timerTV.setText("00:00:00");
        /**End-Timer UI Setups*/

        /**Start-Recording Logo*/
        recLogo = (ImageView) findViewById(R.id.reclogo);
        recLogo.setVisibility(View.INVISIBLE);


        homeIV = (ImageView) findViewById(R.id.home);
        listIV = (ImageView) findViewById(R.id.list);
        /**End-Recording Logo*/
    }

}/**
 * End of MainActivity
 */