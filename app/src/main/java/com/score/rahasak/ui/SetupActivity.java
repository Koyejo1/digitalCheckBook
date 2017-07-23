package com.score.rahasak.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.score.rahasak.R;
import com.score.rahasak.exceptions.InvalidInputFieldsException;
import com.score.rahasak.exceptions.NoUserException;
import com.score.rahasak.utils.ActivityUtils;
import com.score.rahasak.utils.CheckUtils;
import com.score.rahasak.utils.ImageUtils;
import com.score.rahasak.utils.PreferenceUtils;
import com.score.senzc.pojos.User;

public class SetupActivity  extends BaseActivity {

    private static final String TAG = SetupActivity.class.getName();

    // ui controls
    private Button doneBtn;
    private Toolbar toolbar;
    private ImageView signImage;
    private Button getSignBtn;

    // saved digital sign
    private byte[] signImageByteArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        initUi();
        initToolbar();
        initActionBar();
    }

    private void initActionBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setCustomView(getLayoutInflater().inflate(R.layout.registration_header, null));
        getSupportActionBar().setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setCollapsible(false);
        toolbar.setOverScrollMode(Toolbar.OVER_SCROLL_NEVER);
        setSupportActionBar(toolbar);
    }

    private void initUi() {
        ((TextView) findViewById(R.id.welcome_message)).setTypeface(typefaceThin, Typeface.NORMAL);

        doneBtn = (Button) findViewById(R.id.register_btn);
        doneBtn.setTypeface(typefaceThin, Typeface.BOLD);
        doneBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onClickRegister();
            }
        });

        getSignBtn = (Button) findViewById(R.id.get_sign_btn);
        getSignBtn.setTypeface(typefaceThin, Typeface.BOLD);

        signImage = (ImageView) findViewById(R.id.acc_sign);
        signImage.setVisibility(View.GONE);

        signImage.setOnClickListener(onButtonClick);
        getSignBtn.setOnClickListener(onButtonClick);
    }

    /**
     * Sign-up button action,
     *  1. check if user has add the digital signature
     *  2. Navigate to home page
     */
    private void onClickRegister() {
            if(signImageByteArray != null) {
                String signatureFileName = saveSignature();
                PreferenceUtils.saveSignature(this, signatureFileName);
                navigateToHome();
            }else{
                Toast.makeText(this, "Signature is required", Toast.LENGTH_LONG).show();
            }
    }

    private String saveSignature(){
        return ImageUtils.saveImageToInternalStorage("registeredUserSign.png", signImageByteArray, this);
    }

    public void navigateToHome() {
        Intent intent = new Intent(this, DrawerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        SetupActivity.this.finish();
    }

    /**
     * Take user to full screen signing page
     */
    Button.OnClickListener onButtonClick = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent i = new Intent(SetupActivity.this, CaptureSignatureActivity.class);
            startActivityForResult(i, 0);
        }
    };

    /**
     * Return value from the CaptureSignatureActivity ondestroy
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 1) {
            signImageByteArray = data.getByteArrayExtra("byteArray");
            Bitmap b = BitmapFactory.decodeByteArray(
                    data.getByteArrayExtra("byteArray"), 0,
                    data.getByteArrayExtra("byteArray").length);
            signImage.setImageBitmap(b);
            signImage.setVisibility(View.VISIBLE);
        }
    }

}
