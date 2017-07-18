package com.score.rahasak.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.score.rahasak.R;
import com.score.rahasak.utils.ImageUtils;

public class CheckFullScreenPreviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_full_screen_preview);
        ((ImageView)findViewById(R.id.check_bg)).setImageBitmap(ImageUtils.getImageBitmapFromInternalStorage(getIntent().getExtras().getString("url"), this));
    }
}
