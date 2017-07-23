package com.score.rahasak.ui;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.score.rahasak.R;
import com.score.rahasak.application.IntentProvider;
import com.score.rahasak.application.SenzApplication;
import com.score.rahasak.db.SenzorsDbSource;
import com.score.rahasak.enums.IntentType;
import com.score.rahasak.exceptions.NoUserException;
import com.score.rahasak.pojo.Check;
import com.score.rahasak.pojo.SecretUser;
import com.score.rahasak.utils.CheckUtils;
import com.score.rahasak.utils.ImageUtils;
import com.score.rahasak.utils.PhoneBookUtil;
import com.score.rahasak.utils.PreferenceUtils;
import com.score.rahasak.utils.SenzUtils;
import com.score.senz.ISenzService;
import com.score.senzc.enums.SenzTypeEnum;
import com.score.senzc.pojos.Senz;
import com.score.senzc.pojos.User;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.zip.Deflater;

public class CheckDetailActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private static final String TAG = CheckDetailActivity.class.getName();

    // Ui elements
    private EditText senderName;
    private EditText amount;
    private Button verifyBtn;
    private Button previewBtn;
    private Toolbar toolbar;
    private ImageView btnBack;
    private Spinner addedUsersdropDown;

    private ArrayList<SecretUser> friendList;
    private SecretUser selectedUser;
    private SecretUser owner;
    private Check activeCheck;
    private NumberFormat nf;

    // Fonts
    private Typeface typeface;
    protected Typeface typefaceThin;

    // db access
    private SenzorsDbSource dbSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_check_detail);

        dbSource = new SenzorsDbSource(this);
        friendList = dbSource.getSecretUserList();
        activeCheck = dbSource.getCheck(getIntent().getExtras().getString("id"));
        nf = CheckUtils.getCheckDateFormater();

        setupToolbar();
        setupActionBar();
        setupOwner();
        setupFonts();
        initUi();
        setupBtnHandlers();
        setupAddedUsersDropdown();
    }


    private void setupAddedUsersDropdown(){
        addedUsersdropDown = (Spinner) findViewById(R.id.spinner);
        addedUsersdropDown.setOnItemSelectedListener(this);
        addedUsersdropDown.getBackground().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);

        List<String> users = Arrays.asList(getUserNames(friendList));

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, users);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        addedUsersdropDown.setAdapter(dataAdapter);
    }

    private String[] getUserNames(ArrayList<SecretUser> users){
        final String[] userNamesArray = new String[users.size()+1];
        if(users.size() == 0){
            userNamesArray[0] = "You have no added users";
        }else {
            userNamesArray[0] = "Select a friend";
        }
        for (int i = 0; i < users.size(); i++) {
            userNamesArray[i+1] = PhoneBookUtil.getContactName(this, users.get(i).getPhone());
        }
        return userNamesArray;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        if(position == 0){
            selectedUser = null;
        }else{
            selectedUser = friendList.get(position-1);
        }
    }

    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }


    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "Bind to senz service");
        bindToService();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // unbind from service
        if (isServiceBound) {
            Log.d(TAG, "Unbind to senz service");
            unbindService(senzServiceConnection);

            isServiceBound = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    protected void bindToService() {
        Intent intent = new Intent("com.score.rahasak.remote.SenzService");
        intent.setPackage(this.getPackageName());
        bindService(intent, senzServiceConnection, Context.BIND_AUTO_CREATE);
    }


    private void setupOwner(){
        try {
            owner = dbSource.getSecretUser(PreferenceUtils.getUser(this).getUsername());
        } catch (NoUserException e) {
            e.printStackTrace();
        }
    }

    private void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setCollapsible(false);
        //toolbar.setOverScrollMode(Toolbar.OVER_SCROLL_NEVER);
        setSupportActionBar(toolbar);
    }

    private void setupActionBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setCustomView(getLayoutInflater().inflate(R.layout.check_detail_header, null));
        getSupportActionBar().setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

        TextView header = ((TextView) findViewById(R.id.my_title));
        header.setTypeface(typeface, Typeface.NORMAL);

        btnBack = (ImageView) getSupportActionBar().getCustomView().findViewById(R.id.back_btn);
        btnBack.setOnClickListener(this);
    }


    private void setupFonts() {
        typeface = Typeface.createFromAsset(getAssets(), "fonts/GeosansLight.ttf");
        typefaceThin = Typeface.createFromAsset(getAssets(), "fonts/GeosansLight.ttf");
    }

    private void initUi() {
        senderName = (EditText) findViewById(R.id.sender_name);
        senderName.setTypeface(typefaceThin, Typeface.NORMAL);
        if (activeCheck.getIssuedFrom().getPhone() != null && !activeCheck.getIssuedFrom().getPhone().isEmpty()) {
            senderName.setText(PhoneBookUtil.getContactName(this, activeCheck.getIssuedFrom().getPhone()));
        } else {
            senderName.setText(activeCheck.getIssuedFrom().getUsername());
        }


        amount = (EditText) findViewById(R.id.amount);
        amount.setTypeface(typefaceThin, Typeface.NORMAL);
        amount.setText("$" + nf.format(activeCheck.getAmount()));

        verifyBtn = (Button) findViewById(R.id.verify_btn);
        verifyBtn.setTypeface(typefaceThin, Typeface.BOLD);

        previewBtn = (Button) findViewById(R.id.preview_btn);
        previewBtn.setTypeface(typefaceThin, Typeface.BOLD);
    }

    private void setupBtnHandlers(){

        previewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(CheckDetailActivity.this, CheckFullScreenActivity.class);
                i.putExtra("id", activeCheck.getCheckId());

                // Check Attributes
                i.putExtra("fullname", activeCheck.getFullName());
                i.putExtra("amount", activeCheck.getAmount().toString());
                i.putExtra("date", activeCheck.getCreatedAt().toString());
                i.putExtra("signatureUrl", activeCheck.getSignatureUrl());

                // Open preview
                CheckDetailActivity.this.startActivity(i);
            }
        });


        verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedUser != null) {
                    try {
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        ImageUtils.getImageBitmapFromInternalStorage(activeCheck.getSignatureUrl(), CheckDetailActivity.this).compress(Bitmap.CompressFormat.PNG, 100, stream);
                        byte[] byteArray = CheckUtils.compress(stream.toByteArray());
                        verifyCheckSenz(byteArray, selectedUser, CheckDetailActivity.this, activeCheck);
                        Toast.makeText(CheckDetailActivity.this, "Your check has been sent", Toast.LENGTH_LONG).show();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else{
                    Toast.makeText(CheckDetailActivity.this, "Please select your bank", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v == btnBack) {
            finish();
        }
    }

    /**
     * Util methods
     */
    public void verifyCheckSenz(final byte[] image, SecretUser secretUser, final Context context, Check check) {
        // compose senz
        Long timestamp = (System.currentTimeMillis() / 1000);
        String uid = SenzUtils.getUid(this, timestamp.toString());

        // stream on senz
        // stream content
        // stream off senz
        Senz startStreamSenz = getStartStreamSenz(uid, timestamp, secretUser);
        ArrayList<Senz> photoSenzList = getPhotoStreamSenz(image, context, uid, timestamp, secretUser);
        Senz stopStreamSenz = getStopStreamSenz(uid, timestamp, secretUser, check);

        // populate list
        ArrayList<Senz> senzList = new ArrayList<>();
        senzList.add(startStreamSenz);
        senzList.addAll(photoSenzList);
        senzList.add(stopStreamSenz);

        sendInOrder(senzList);
    }

    /**
     * Decompose image stream in to multiple data/stream senz's
     *
     * @param image   image content
     * @param context app context
     * @param uid     unique id
     * @return list of decomposed senz's
     */
    public ArrayList<Senz> getPhotoStreamSenz(byte[] image, Context context, String uid, Long timestamp, SecretUser secretUser) {
        String imageString = ImageUtils.encodeBitmap(image);

        ArrayList<Senz> senzList = new ArrayList<>();
        String[] packets = split(imageString, 1024);

        for (String packet : packets) {
            // new senz
            String id = "_ID";
            String signature = "_SIGNATURE";
            SenzTypeEnum senzType = SenzTypeEnum.STREAM;

            // create senz attributes
            HashMap<String, String> senzAttributes = new HashMap<>();
            senzAttributes.put("time", timestamp.toString());
            senzAttributes.put("cam", packet.trim());
            senzAttributes.put("uid", uid);

            Senz _senz = new Senz(id, signature, senzType, null, new User(secretUser.getId(), secretUser.getUsername()), senzAttributes);
            senzList.add(_senz);
        }

        return senzList;
    }

    /**
     * Create start stream senz
     *
     * @return senz
     */
    public Senz getStartStreamSenz(String uid, Long timestamp, SecretUser secretUser) {
        // create senz attributes
        HashMap<String, String> senzAttributes = new HashMap<>();
        senzAttributes.put("time", timestamp.toString());
        senzAttributes.put("cam", "on");

        senzAttributes.put("chk", "on");
        senzAttributes.put("chk_type_verify", "true");

        senzAttributes.put("uid", uid);
        // new senz
        String id = "_ID";
        String signature = "_SIGNATURE";
        SenzTypeEnum senzType = SenzTypeEnum.STREAM;

        return new Senz(id, signature, senzType, null, new User(secretUser.getId(), secretUser.getUsername()), senzAttributes);
    }

    /**
     * Create stop stream senz
     *
     * @return senz
     */
    public Senz getStopStreamSenz(String uid, Long timestamp, SecretUser secretUser, Check check) {
        // create senz attributes
        HashMap<String, String> senzAttributes = new HashMap<>();
        senzAttributes.put("time", timestamp.toString());
        senzAttributes.put("cam", "off");

        senzAttributes.put("chk", "off");
        senzAttributes.put("chk_type_verify", "true");

        senzAttributes.put("uid", uid);
        senzAttributes.put("fullname", check.getFullName());
        senzAttributes.put("amount", check.getAmount().toString());
        senzAttributes.put("createdAt", check.getCreatedAt().toString());

        // Setting original sender and receiver for check so not to get confused in handler
        senzAttributes.put("chk_receiver", check.getIssuedFrom().getUsername());
        senzAttributes.put("chk_sender", check.getIssuedFrom().getUsername());

        // new senz
        String id = "_ID";
        String signature = "_SIGNATURE";
        SenzTypeEnum senzType = SenzTypeEnum.STREAM;

        return new Senz(id, signature, senzType, null, new User(secretUser.getId(), secretUser.getUsername()), senzAttributes);
    }

    private String[] split(String src, int len) {
        String[] result = new String[(int) Math.ceil((double) src.length() / (double) len)];
        for (int i = 0; i < result.length; i++)
            result[i] = src.substring(i * len, Math.min(src.length(), (i + 1) * len));
        return result;
    }
}
