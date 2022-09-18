package com.streamaxia.opensdkdemo;

import android.content.res.AssetManager;
import android.media.MediaCodecInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.streamaxia.android.handlers.EncoderHandler;
import com.streamaxia.android.handlers.RecordHandler;
import com.streamaxia.android.handlers.RtmpHandler;
import com.streamaxia.android.streamer.StreamaxiaStreamer;
import com.streamaxia.opensdkdemo.filestreaming.IFrameCallback;
import com.streamaxia.opensdkdemo.filestreaming.MediaMoviePlayer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.nio.ByteBuffer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FileStreamingActivity extends AppCompatActivity implements RtmpHandler.RtmpListener, RecordHandler.RecordListener,
        EncoderHandler.EncodeListener {
    private final String TAG = FileStreamingActivity.class.getSimpleName();

    // Set default values for the streamer
    public final static String streamaxiaStreamName = "demo";

    @BindView(R.id.state_text)
    TextView stateTextView;
    @BindView(R.id.play_button)
    Button startStopButton;
    @BindView(R.id.chronometer)
    Chronometer mChronometer;

    private MediaMoviePlayer mPlayer;
    private StreamaxiaStreamer streamaxiaStreamer;

    private String inputPath;

    private byte[] mAudioOutTempBuf = new byte[1024];
    private byte[] mVideoOutTempBuf = new byte[1024];

    private final IFrameCallback mIFrameCallback = new IFrameCallback() {
        @Override
        public void onPrepared() {
            Log.d(TAG, "onPrepared");
            configureStreamer();
        }

        @Override
        public void onFinished() {
            mPlayer = null;
        }

        @Override
        public boolean onFrameAvailable(ByteBuffer buffer, long presentationTimeUs, boolean video) {
            if (video) {
                int size = buffer.limit();
                if (mVideoOutTempBuf.length < size) {
                    mVideoOutTempBuf = new byte[size];
                }
                buffer.position(0);
                buffer.get(mVideoOutTempBuf, 0, size);
                buffer.clear();

                streamaxiaStreamer.sendVideoFrames(mVideoOutTempBuf);
            } else {
                int size = buffer.limit();
                if (mAudioOutTempBuf.length < size) {
                    mAudioOutTempBuf = new byte[size];
                }
                buffer.position(0);
                buffer.get(mAudioOutTempBuf, 0, size);
                buffer.clear();

                streamaxiaStreamer.sendAudioFrames(mAudioOutTempBuf);
            }

            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_streaming);

        ButterKnife.bind(this);

        copyAssets();
        inputPath = getCacheDir().getAbsolutePath() + "/bunny.mp4";

        mPlayer = new MediaMoviePlayer(mIFrameCallback, true);
        mPlayer.prepare(inputPath);
    }

    @Override
    protected void onPause() {
        super.onPause();
        streamaxiaStreamer.stopPublish();
        streamaxiaStreamer.pauseRecord();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        streamaxiaStreamer.stopPublish();
        streamaxiaStreamer.stopRecord();
    }

    @OnClick(R.id.play_button)
    public void startStopStream() {
        if (startStopButton.getText().toString().toLowerCase().equals("start")) {
            startStopButton.setText("STOP");
            mChronometer.setBase(SystemClock.elapsedRealtime());
            mChronometer.start();
            streamaxiaStreamer.startPublish("rtmp://rtmp.streamaxia.com/streamaxia/" + streamaxiaStreamName);
            mPlayer.play();
        } else {
            if (mPlayer != null) {
                mPlayer.release();
                mPlayer = null;
            }

            startStopButton.setText("START");
            stopChronometer();
            streamaxiaStreamer.stopPublish();
        }
    }

    private void configureStreamer() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                streamaxiaStreamer = new StreamaxiaStreamer(FileStreamingActivity.this);
                streamaxiaStreamer.setEncoderHandler(new EncoderHandler(FileStreamingActivity.this));
                streamaxiaStreamer.setRtmpHandler(new RtmpHandler(FileStreamingActivity.this));
                streamaxiaStreamer.setRecordEventHandler(new RecordHandler(FileStreamingActivity.this));
                streamaxiaStreamer.setColorFormat(MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar);

                streamaxiaStreamer.setVideoOutputResolution(mPlayer.getWidth(), mPlayer.getHeight());
            }
        });
    }

    @Override
    public void onNetworkWeak() {

    }

    @Override
    public void onNetworkResume() {

    }

    @Override
    public void onEncodeIllegalArgumentException(IllegalArgumentException e) {
        handleException(e);
    }

    /*
     * RecordHandler implementation
     * */

    @Override
    public void onRecordPause() {

    }

    @Override
    public void onRecordResume() {

    }

    @Override
    public void onRecordStarted(String s) {

    }

    @Override
    public void onRecordFinished(String s) {

    }

    @Override
    public void onRecordIllegalArgumentException(IllegalArgumentException e) {
        handleException(e);
    }

    @Override
    public void onRecordIOException(IOException e) {
        handleException(e);
    }

    /*
     * RTMPListener implementation
     * */

    @Override
    public void onRtmpConnecting(String s) {
        setStatusMessage(s);
    }

    @Override
    public void onRtmpConnected(String s) {
        setStatusMessage(s);
        startStopButton.setText("STOP");
    }

    @Override
    public void onRtmpVideoStreaming() {

    }

    @Override
    public void onRtmpAudioStreaming() {

    }

    @Override
    public void onRtmpStopped() {
        setStatusMessage("STOPPED");
    }

    @Override
    public void onRtmpDisconnected() {
        setStatusMessage("Disconnected");
    }

    @Override
    public void onRtmpVideoFpsChanged(double v) {

    }

    @Override
    public void onRtmpVideoBitrateChanged(double v) {

    }

    @Override
    public void onRtmpAudioBitrateChanged(double v) {

    }

    @Override
    public void onRtmpBitrateChanged(double v) {

    }

    @Override
    public void onRtmpSocketException(SocketException e) {
        handleException(e);
    }

    @Override
    public void onRtmpIOException(IOException e) {
        handleException(e);
    }

    @Override
    public void onRtmpIllegalArgumentException(IllegalArgumentException e) {
        handleException(e);
    }

    @Override
    public void onRtmpIllegalStateException(IllegalStateException e) {
        handleException(e);
    }

    @Override
    public void onRtmpAuthenticationg(String s) {

    }

    private void stopChronometer() {
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.stop();
    }

    private void setStatusMessage(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stateTextView.setText("[" + msg + "]");
            }
        });
    }

    private void handleException(Exception e) {
        try {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            streamaxiaStreamer.stopPublish();
        } catch (Exception e1) {
            // Ignore
        }
    }

    private void copyAssets() {
        AssetManager assetManager = getAssets();
        String[] files = null;
        try {
            files = assetManager.list("videos");
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }

        for(String filename : files) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open("videos/" + filename);
                File outFile = new File(getCacheDir(), filename);
                if (!outFile.exists()) {
                    out = new FileOutputStream(outFile);
                    copyFile(in, out);
                    out.flush();
                    out.close();
                }
                in.close();
                in = null;
                out = null;
            } catch(IOException e) {
                Log.e("tag", "Failed to copy asset file: " + filename, e);
            }
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }
}