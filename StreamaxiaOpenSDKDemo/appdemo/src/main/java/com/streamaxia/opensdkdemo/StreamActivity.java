package com.streamaxia.opensdkdemo;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.streamaxia.android.CameraPreview;
import com.streamaxia.android.StreamaxiaPublisher;
import com.streamaxia.android.handlers.EncoderHandler;
import com.streamaxia.android.handlers.RecordHandler;
import com.streamaxia.android.handlers.RtmpHandler;
import com.streamaxia.android.utils.Size;

import java.io.IOException;
import java.net.SocketException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

;

public class StreamActivity extends AppCompatActivity implements RtmpHandler.RtmpListener, RecordHandler.RecordListener,
        EncoderHandler.EncodeListener {

    private final String TAG = StreamActivity.class.getSimpleName();

    // Set default values for the streamer
    public final static String streamaxiaStreamName = "demo";
    public final static int bitrate = 500;
    public final static int width = 720;
    public final static int height = 1280;

    // The view that displays the camera feed
    @BindView(R.id.preview)
    CameraPreview mCameraView;
    @BindView(R.id.chronometer)
    Chronometer mChronometer;
    @BindView(R.id.start_stop)
    TextView startStopTextView;
    @BindView(R.id.state_text)
    TextView stateTextView;

    private StreamaxiaPublisher mPublisher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_stream);
        ButterKnife.bind(this);

        hideStatusBar();
        mPublisher = new StreamaxiaPublisher(mCameraView, this);

        mPublisher.setEncoderHandler(new EncoderHandler(this));
        mPublisher.setRtmpHandler(new RtmpHandler(this));
        mPublisher.setRecordEventHandler(new RecordHandler(this));
        mCameraView.startCamera();

        setStreamerDefaultValues();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            stopStreaming();
            stopChronometer();
            startStopTextView.setText("START");
        } else {
            Intent intent = new Intent(this, SplashscreenActivity.class);
            startActivity(intent);
            Toast.makeText(this, "You need to grant persmissions in order to begin streaming.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCameraView.stopCamera();
        mPublisher.pauseRecord();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPublisher.stopPublish();
        mPublisher.stopRecord();
    }

    @OnClick(R.id.start_stop)
    public void startStopStream() {
        if (startStopTextView.getText().toString().toLowerCase().equals("start")) {
            startStopTextView.setText("STOP");
            mChronometer.setBase(SystemClock.elapsedRealtime());
            mChronometer.start();
            mPublisher.startPublish("rtmp://rtmp.streamaxia.com/streamaxia/" + streamaxiaStreamName);
        } else {
            startStopTextView.setText("START");
            stopChronometer();
            mPublisher.stopPublish();
        }
    }

    private void stopStreaming() {
        mPublisher.stopPublish();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mPublisher.setScreenOrientation(newConfig.orientation);
    }

    private void hideStatusBar() {
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    private void setStreamerDefaultValues() {
        // Set one of the available resolutions
        List<Size> sizes = mPublisher.getSupportedPictureSizes(getResources().getConfiguration().orientation);
        Size resolution = sizes.get(0);
        mPublisher.setVideoOutputResolution(resolution.width, resolution.height, this.getResources().getConfiguration().orientation);
    }

    private void setStatusMessage(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stateTextView.setText("[" + msg + "]");
            }
        });
    }


    /*
    * EncoderHandler implementation
    * You can use these callbacks to get events from the streamer
    * */

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
        startStopTextView.setText("STOP");
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

    private void handleException(Exception e) {
        try {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            mPublisher.stopPublish();
        } catch (Exception e1) {
            // Ignore
        }
    }
}
