package com.streamaxia.opensdkdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChooserActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chooser);

        ButterKnife.bind(this);
    }

    @OnClick(R.id.liveButton)
    public void liveStreamingAction() {
        Intent intent = new Intent(this, StreamActivity.class);
        startActivity(intent);
        this.finish();
    }

    @OnClick(R.id.filesButton)
    public void fileStreamingAction() {
        Intent intent = new Intent(this, FileStreamingActivity.class);
        startActivity(intent);
        this.finish();
    }
}