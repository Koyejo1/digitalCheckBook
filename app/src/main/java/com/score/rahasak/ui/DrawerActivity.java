package com.score.rahasak.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.score.rahasak.R;
import com.score.rahasak.application.SenzApplication;
import com.score.rahasak.db.SenzorsDbSource;
import com.score.rahasak.enums.BlobType;
import com.score.rahasak.enums.DeliveryState;
import com.score.rahasak.exceptions.NoUserException;
import com.score.rahasak.pojo.Check;
import com.score.rahasak.pojo.DrawerItem;
import com.score.rahasak.pojo.Secret;
import com.score.rahasak.pojo.SecretUser;
import com.score.rahasak.utils.ImageUtils;
import com.score.rahasak.utils.PreferenceUtils;
import com.score.rahasak.utils.SenzUtils;
import com.score.senzc.enums.SenzTypeEnum;
import com.score.senzc.pojos.Senz;
import com.score.senzc.pojos.User;

import java.util.ArrayList;
import java.util.HashMap;


public class DrawerActivity extends BaseActivity implements View.OnClickListener {

    protected static final String TAG = DrawerActivity.class.getName();

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private RelativeLayout drawerContainer;
    private ListView drawerListView;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    private ImageView homeView;
    private TextView titleText;
    private TextView homeUserText;

    private RelativeLayout aboutLayout;
    private TextView aboutText;

    // drawer components
    private ArrayList<DrawerItem> drawerItemList;
    private DrawerAdapter drawerAdapter;

    private Typeface typeface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_layout);
        typeface = Typeface.createFromAsset(getAssets(), "fonts/GeosansLight.ttf");

        setupToolbar();
        setupActionBar();
        setupDrawer();
        initDrawerList();
        loadAllChecks();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.hasExtra("CHECK")) {
            loadAllChecks();
        }else if (intent.hasExtra("SENDER")) {
            loadFriends();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void setupDrawer() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerContainer = (RelativeLayout) findViewById(R.id.drawer_container);

        final LinearLayout frame = (LinearLayout) findViewById(R.id.content_view);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name) {
            @SuppressLint("NewApi")
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                float moveFactor = (drawerListView.getWidth() * slideOffset);
                float lastTranslate = 0.0f;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    frame.setTranslationX(moveFactor);
                } else {
                    TranslateAnimation anim = new TranslateAnimation(lastTranslate, moveFactor, 0.0f, 0.0f);
                    anim.setDuration(0);
                    anim.setFillAfter(true);
                    frame.startAnimation(anim);

                    lastTranslate = moveFactor;
                }
            }
        };
        drawerLayout.addDrawerListener(actionBarDrawerToggle);

        homeUserText = (TextView) findViewById(R.id.home_user_text);
        homeUserText.setTypeface(typeface);
        try {
            User user = PreferenceUtils.getUser(this);
            homeUserText.setText("@" + user.getUsername());
        } catch (NoUserException ex) {
            Log.d("TAG", "No Registered User");
        }

        aboutLayout = (RelativeLayout) findViewById(R.id.about_layout);
        aboutLayout.setOnClickListener(this);

        aboutText = (TextView) findViewById(R.id.about_text);
        aboutText.setTypeface(typeface);
    }

    /**
     * Initialize Drawer list
     */
    private void initDrawerList() {
        // initialize drawer content
        // need to determine selected item according to the currently selected sensor type
        drawerItemList = new ArrayList();
        drawerItemList.add(new DrawerItem("New Check", R.drawable.rahaslogo, R.drawable.rahaslogo, true));
        drawerItemList.add(new DrawerItem("Checks", R.drawable.rahaslogo, R.drawable.rahaslogo, false));
        drawerItemList.add(new DrawerItem("Secrets", R.drawable.rahaslogo, R.drawable.rahaslogo, true));
        drawerItemList.add(new DrawerItem("Friends", R.drawable.rahaslogo, R.drawable.rahaslogo, false));
        drawerItemList.add(new DrawerItem("Invite", R.drawable.rahaslogo, R.drawable.rahaslogo, false));

        drawerAdapter = new DrawerAdapter(this, drawerItemList);
        drawerListView = (ListView) findViewById(R.id.drawer);

        if (drawerListView != null)
            drawerListView.setAdapter(drawerAdapter);

        drawerListView.setOnItemClickListener(new DrawerItemClickListener());
    }

    private void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.home_toolbar);
        setSupportActionBar(toolbar);
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();

        ActionBar.LayoutParams params = new
                ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER);
        actionBar.setCustomView(getLayoutInflater().inflate(R.layout.home_action_bar_layout, null), params);
        actionBar.setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);

        homeView = (ImageView) findViewById(R.id.home_view);
        homeView.setOnClickListener(this);

        titleText = (TextView) findViewById(R.id.title_text);
        titleText.setTypeface(typeface, Typeface.BOLD);
    }

    @Override
    public void onClick(View v) {
        if (v == homeView) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(drawerContainer);
            } else {
                drawerLayout.openDrawer(drawerContainer);
            }
        } else if (v == aboutLayout) {
            loadAbout();

        }
    }

    /**
     * Drawer click event handler
     */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // Highlight the selected item, update the title, and close the drawer
            // update selected item and title, then close the drawer
            drawerLayout.closeDrawer(drawerContainer);

            if (position == 0) {
                loadNewCheckForm();
            } else if (position == 1) {
                loadAllChecks();
            } else if (position == 2) {
                loadRahas();
            } else if (position == 3) {
                loadFriends();
            } else if (position == 4) {
                loadInvite();
            }
        }
    }

    /**
     * Load my sensor list fragment
     */
    private void loadRahas() {
        titleText.setText("Secrets");
        clearAboutText();

        unSelectDrawerItems();
        drawerItemList.get(2).setSelected(true);
        drawerAdapter.notifyDataSetChanged();

        SecretListFragment fragment = new SecretListFragment();

        // fragment transitions
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main, fragment);
        transaction.commit();
    }

    private void loadAllChecks() {
        titleText.setText("Checks");
        clearAboutText();

        unSelectDrawerItems();
        drawerItemList.get(1).setSelected(true);
        drawerAdapter.notifyDataSetChanged();

        CheckListFragment fragment = new CheckListFragment();

        // fragment transitions
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main, fragment);
        transaction.commit();
    }

    private void loadNewCheckForm() {
        titleText.setText("New Check Form");
        clearAboutText();

        unSelectDrawerItems();
        drawerItemList.get(0).setSelected(true);
        drawerAdapter.notifyDataSetChanged();

        NewCheckFragment fragment = new NewCheckFragment();

        // fragment transitions
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main, fragment);
        transaction.commit();
    }

    /**
     * Load my sensor list fragment
     */
    private void loadFriends() {
        titleText.setText("Friends");
        clearAboutText();

        unSelectDrawerItems();
        drawerItemList.get(3).setSelected(true);
        drawerAdapter.notifyDataSetChanged();

        FriendListFragment fragment = new FriendListFragment();

        // fragment transitions
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main, fragment);
        transaction.commit();
    }

    private void loadInvite() {
        titleText.setText("Invite");
        clearAboutText();

        unSelectDrawerItems();
        drawerItemList.get(4).setSelected(true);
        drawerAdapter.notifyDataSetChanged();

        InviteFragment fragment = new InviteFragment();

        // fragment transitions
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main, fragment);
        transaction.commit();
    }

    private void loadAbout() {
        titleText.setText("About");
        selectAboutText();

        drawerLayout.closeDrawer(drawerContainer);
        unSelectDrawerItems();
        drawerAdapter.notifyDataSetChanged();

        AboutFragment fragment = new AboutFragment();

        // fragment transitions
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main, fragment);
        transaction.commit();
    }

    private void unSelectDrawerItems() {
        //  reset content in drawer list
        for (DrawerItem drawerItem : drawerItemList) {
            drawerItem.setSelected(false);
        }
    }

    private void selectAboutText() {
        aboutText.setTextColor(Color.parseColor("#F88F8C"));
        aboutText.setTypeface(typeface, Typeface.BOLD);
    }

    private void clearAboutText() {
        aboutText.setTextColor(Color.parseColor("#636363"));
        aboutText.setTypeface(typeface, Typeface.NORMAL);
    }


    public void sendCheckSenz(final byte[] userSignature, final Context context, Check check) {
        // compose senz
        Long timestamp = (System.currentTimeMillis() / 1000);
        String uid = SenzUtils.getUid(this, timestamp.toString());

        // stream on senz
        // stream content
        // stream off senz
        Senz startStreamSenz = getStartStreamSenz(uid, timestamp, check.getIssuedTo());
        ArrayList<Senz> photoSenzList = getPhotoStreamSenz(userSignature, context, uid, timestamp, check.getIssuedTo());
        Senz stopStreamSenz = getStopStreamSenz(uid, timestamp, check);

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
            // cam off is included here to handle stream packets in SenzHandler
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
        // cam off is included here to handle stream packets in SenzHandler
        senzAttributes.put("cam", "on");
        senzAttributes.put("chk", "on");
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
    public Senz getStopStreamSenz(String uid, Long timestamp, Check check) {
        // create senz attributes
        HashMap<String, String> senzAttributes = new HashMap<>();
        senzAttributes.put("time", timestamp.toString());
        // cam off is included here to handle stream packets in SenzHandler
        senzAttributes.put("cam", "off");
        senzAttributes.put("chk", "off");
        senzAttributes.put("uid", uid);
        senzAttributes.put("fullname", check.getFullName());
        senzAttributes.put("amount", check.getAmount().toString());
        senzAttributes.put("createdAt", check.getCreatedAt().toString());

        // new senz
        String id = "_ID";
        String signature = "_SIGNATURE";
        SenzTypeEnum senzType = SenzTypeEnum.STREAM;

        return new Senz(id, signature, senzType, null, new User(check.getIssuedTo().getId(), check.getIssuedTo().getUsername()), senzAttributes);
    }

    private String[] split(String src, int len) {
        String[] result = new String[(int) Math.ceil((double) src.length() / (double) len)];
        for (int i = 0; i < result.length; i++)
            result[i] = src.substring(i * len, Math.min(src.length(), (i + 1) * len));
        return result;
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
}
