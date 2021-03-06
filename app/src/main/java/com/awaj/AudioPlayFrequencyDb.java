package com.awaj;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by anup on 8/12/16.
 */
public class AudioPlayFrequencyDb extends AudioPlayFrequency {

    AudioPlayFrequencyDbListener listener;



    public AudioPlayFrequencyDb(AudioPlayFrequencyDbListener listener) {
        super(listener);
        this.listener = listener;
    }

   /* public AudioPlayFrequencyDb(AudioPlayFrequencyDbListener listener) {
        // set null or default listener or accept as argument to constructor
        this.listener = listener;
    }*/

    @Override
    public void playRecord() {
        Log.d(TAG, "playRecord() BLAHBLAH");
        File folder = context.getExternalFilesDir("Awaj");
        File latest = super.getLatestModified();
        File filePcm;
        if (latest==null){
            Toast.makeText(MyApplication.getAppContext(), "Please Record something", Toast.LENGTH_SHORT).show();
            return;
        }
        else {
            filePcm = new File(folder,latest.getName());
        }

        //File filePcm = new File(Environment.getExternalStorageDirectory(), "Sound.pcm");

        //int minBufferSize = getPlayBufferSize();
        int minBufferSize = MainActivity.getMinBufferSizeInBytes();

        short[] audioData = new short[minBufferSize/2];
        int audioInt[] = new int[minBufferSize/2];
        float audioFloat[] = new float[minBufferSize/2];


        InputStream inputStream = null;
        BufferedInputStream bufferedInputStream = null;
        DataInputStream dataInputStream = null;
        AudioTrack audioTrack = null;
        try {
            inputStream = new FileInputStream(filePcm);
            bufferedInputStream = new BufferedInputStream(inputStream);
            dataInputStream = new DataInputStream(bufferedInputStream);

            audioTrack = new AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    44100,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBufferSize,
                    AudioTrack.MODE_STREAM);

            audioTrack.play();

            float audioFloatsForFFT[] = new float[audioData.length];
            float audioFloatsForAmp[] = new float[audioData.length];

            DecibelCalculation decibelCalculation = new DecibelCalculation();
            while (stateClass.getPlayingState() && dataInputStream.available() > 0) {
                int i = 0;
                while (dataInputStream.available() > 0 && i < audioData.length) {
                    audioData[i] = dataInputStream.readShort();

                    audioInt[i]=(int)audioData[i];
                    audioFloat[i]=(float) audioInt[i];

                    /**This one is for FFT*/
                    audioFloatsForFFT[i] = (float) audioInt[i];
                    /**This one is for Amplitude Visualization*/
                    //                audioFloatsForAmp[i]=(float)audioInt[i];
                    i++;
                }
                audioTrack.write(audioData, 0, audioData.length);

                float decibelValue = decibelCalculation.decibelCalculation(audioData);
                float[] fftOutput = FftOutput.callMainFft(audioFloatsForFFT);

                float frequency = FrequencyValue.getFundamentalFrequency(fftOutput);
                int match = databaseHelper.matchFreq(frequency);

                String note = databaseHelper.getNote(match);
                //MainActivity.plotGraph(audioFloatsForAmp,audioFloatsForFFT);

//                if(listener!=null)
//                    listener.onDataLoaded(decibelValue,frequency,note);

                publishProgress(String.valueOf(frequency),note,String.valueOf(decibelValue));
                //publishProgress(audioData[]);
            }
            audioTrack.pause();
            audioTrack.flush();
            audioTrack.stop();
            audioTrack.release();


            sucessfull=true;

            Log.d(TAG, "end of playrecord()");


        } catch (FileNotFoundException e) {
            e.printStackTrace();/**/
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (dataInputStream != null) {
                try {
                    dataInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bufferedInputStream != null) {
                try {
                    bufferedInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (audioTrack != null) {
                audioTrack.release();
            }
        }
    }

    @Override
    protected void onProgressUpdate(String... values) {
        listener.processExecuting(Float.valueOf(values[0]),values[1],Float.valueOf(values[2]));
    }
    @Override
    protected void onPostExecute(Boolean aVoid) {

        listener.processExecuted();

        Log.d(TAG, "onPostExecute");


    }
}


