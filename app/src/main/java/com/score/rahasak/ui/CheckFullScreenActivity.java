package com.score.rahasak.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.score.rahasak.R;
import com.score.rahasak.utils.ActivityUtils;
import com.score.rahasak.utils.CheckUtils;
import com.score.rahasak.utils.ImageUtils;
import com.score.rahasak.utils.PreferenceUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class CheckFullScreenActivity extends AppCompatActivity {

    private static final String TAG = CheckFullScreenActivity.class.getName();

    // Check Attributes
    private TextView fullName;
    private TextView amount;
    private TextView amountInWords;
    private TextView date;
    private ImageView signature;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_full_screen);
        setupUi();
        setData(getIntent());
        setupUserSignature(getIntent().getExtras().getString("signatureUrl"));
    }

    private void setupUi() {
        fullName = (TextView) findViewById(R.id.fullName);
        amount = (TextView) findViewById(R.id.amount);
        amountInWords = (TextView) findViewById(R.id.amount_in_words);
        date = (TextView) findViewById(R.id.now_date);
        signature = (ImageView) findViewById(R.id.signature);
    }

    private void setData(Intent intent) {
        NumberFormat nf = CheckUtils.getCheckDateFormater();
        fullName.setText(intent.getExtras().getString("fullname"));
        String amountString = intent.getExtras().getString("amount");
        amount.setText(nf.format(Double.parseDouble(intent.getExtras().getString("amount"))).trim());
        amountInWords.setText(CheckUtils.convertNumberToWords(Long.parseLong(intent.getExtras().getString("amount"))));
        date.setText(CheckUtils.getDate(Long.parseLong(intent.getExtras().getString("date"))));
    }

    private void setupUserSignature(String signatureUrl) {
        Bitmap signatureBitmap = ImageUtils.getImageBitmapFromInternalStorage(signatureUrl, this);
        if (signatureBitmap != null)
            signature.setImageBitmap(signatureBitmap);
    }

    /**
     * This was the earlier implementation to capture screen shot of the check. Now the digital signature is been sent so no need to generate check
     */
    private void captureCheckScreenShot() {
        View u = findViewById(R.id.root_view_check);
        u.setDrawingCacheEnabled(true);
        View z = (View) findViewById(R.id.root_view_check);
        int totalHeight = z.getHeight();
        int totalWidth = z.getWidth();
        u.layout(0, 0, totalWidth, totalHeight);
        u.buildDrawingCache(true);
        Bitmap b = Bitmap.createBitmap(u.getDrawingCache());
        u.setDrawingCacheEnabled(false);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.JPEG, 10, stream);
        byte[] byteArray = stream.toByteArray();

        try {
            ImageUtils.saveImageToInternalStorage("capturedCheck.jpg", byteArray, this);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

}