package com.score.chatz.ui;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.score.chatz.R;
import com.score.chatz.application.IntentProvider;
import com.score.chatz.asyncTasks.BitmapWorkerTask;
import com.score.chatz.db.SenzorsDbSource;
import com.score.chatz.enums.IntentType;
import com.score.chatz.pojo.BitmapTaskParams;
import com.score.chatz.utils.ImageUtils;
import com.score.senzc.pojos.Senz;

public class PhotoFullScreenActivity extends AppCompatActivity {

    private static final String TAG = PhotoFullScreenActivity.class.getName();

    private View loadingView;
    private TextView selfieCallingText;
    private TextView usernameText;
    private ImageView imageView;
    private String imageData;
    private Typeface typeface;
    private ImageView waitingIcon;

    private static final int CLOSE_QUICK_VIEW_TIME = 2000;

    //Set the radius of the Blur. Supported range 0 < radius <= 25
    private static final float BLUR_RADIUS = 5f;

    // senz message
    private BroadcastReceiver senzReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Got message from Senz service");

            // extract senz
            if (intent.hasExtra("SENZ")) {
                Senz senz = intent.getExtras().getParcelable("SENZ");
                switch (senz.getSenzType()) {
                    case DATA:
                        onSenzReceived(senz);
                        break;
                    case STREAM:
                        onSenzStreamReceived(senz);
                        break;
                    default:
                        break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_full_screen);

        setUpFonts();
        initUi();
        initIntent();
    }

    private void setUpFonts() {
        typeface = Typeface.createFromAsset(getAssets(), "fonts/GeosansLight.ttf");
    }

    private void initUi() {
        waitingIcon = (ImageView) findViewById(R.id.selfie_image);
        imageView = (ImageView) findViewById(R.id.imageView);
        loadingView = findViewById(R.id.selfie_loading_view);
        selfieCallingText = (TextView) findViewById(R.id.selfie_calling_text);
        usernameText = (TextView) findViewById(R.id.selfie_username);

        selfieCallingText.setTypeface(typeface, Typeface.NORMAL);
        usernameText.setTypeface(typeface, Typeface.BOLD);
    }

    private void initIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra("IMAGE")) {
            loadingView.setVisibility(View.INVISIBLE);
            imageView.setVisibility(View.VISIBLE);
            imageData = intent.getStringExtra("IMAGE");
            imageView.setImageBitmap(new ImageUtils().decodeBitmap(imageData));
        } else {
            loadingView.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.INVISIBLE);
            setupUserImage(intent.getStringExtra("SENDER"));
            startAnimatingWaitingIcon();
        }

        if (intent.hasExtra("QUICK_PREVIEW")) {
            startCloseViewTimer();
        }
    }

    private void startAnimatingWaitingIcon() {
        AnimationDrawable anim = (AnimationDrawable) waitingIcon.getBackground();
        anim.start();
    }

    private void setupUserImage(String sender) {
        String userImage = new SenzorsDbSource(this).getSecretUser(sender).getImage();
        if (userImage != null) {
            Bitmap bitmap = new ImageUtils().decodeBitmap(userImage);
            ((ImageView) findViewById(R.id.user_profile_image)).setImageBitmap(new ImageUtils().blur(bitmap, BLUR_RADIUS, this));
        }

        usernameText.setText("@" + sender);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(senzReceiver, IntentProvider.getIntentFilter(IntentType.SENZ));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(senzReceiver);
    }

    private void loadBitmap(String data, ImageView imageView) {
        BitmapWorkerTask task = new BitmapWorkerTask(imageView);
        //task.execute(new BitmapTaskParams(data, 100, 100));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (new BitmapTaskParams(data, 1000, 1000)));
        else
            task.execute(new BitmapTaskParams(data, 1000, 1000));
    }

    private void startCloseViewTimer() {
        new CountDownTimer(CLOSE_QUICK_VIEW_TIME, CLOSE_QUICK_VIEW_TIME) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                PhotoFullScreenActivity.this.finish();
            }
        }.start();
    }

    private void onSenzReceived(Senz senz) {
        if (senz.getAttributes().containsKey("status")) {
            if (senz.getAttributes().get("status").equalsIgnoreCase("DELIVERED")) {
                // GET msg delivered
            } else if (senz.getAttributes().get("status").equalsIgnoreCase("801")) {
                // user busy
                displayInformationMessageDialog("info", "user busy");
            } else if (senz.getAttributes().get("status").equalsIgnoreCase("802")) {
                // camera error
                displayInformationMessageDialog("error", "cam error");
            } else if (senz.getAttributes().get("status").equalsIgnoreCase("offline")) {
                // camera error
                displayInformationMessageDialog("Offline", "User offline");
            }
        }
    }

    private void onSenzStreamReceived(Senz senz) {
        if (senz.getAttributes().containsKey("cam")) {
            // display stream
            loadingView.setVisibility(View.INVISIBLE);
            imageView.setVisibility(View.VISIBLE);
            //imageView.setImageBitmap(CameraUtils.getBitmapFromBytes(senz.getAttributes().get("cam").getBytes()));
            imageView.setImageBitmap(new ImageUtils().decodeBitmap(senz.getAttributes().get("cam")));
            startCloseViewTimer();
        }
    }

    public void displayInformationMessageDialog(String title, String message) {
        final Dialog dialog = new Dialog(this);

        //set layout for dialog
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.information_message_dialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(true);

        // set dialog texts
        TextView messageHeaderTextView = (TextView) dialog.findViewById(R.id.information_message_dialog_layout_message_header_text);
        TextView messageTextView = (TextView) dialog.findViewById(R.id.information_message_dialog_layout_message_text);
        messageHeaderTextView.setText(title);
        messageTextView.setText(Html.fromHtml(message));

        // set custom font
        messageHeaderTextView.setTypeface(typeface, Typeface.BOLD);
        messageTextView.setTypeface(typeface);

        //set ok button
        Button okButton = (Button) dialog.findViewById(R.id.information_message_dialog_layout_ok_button);
        okButton.setTypeface(typeface, Typeface.BOLD);
        okButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.cancel();
                PhotoFullScreenActivity.this.finish();
            }
        });

        dialog.show();
    }
}
