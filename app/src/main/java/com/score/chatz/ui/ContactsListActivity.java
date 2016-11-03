package com.score.chatz.ui;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.os.Build;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.os.Bundle;

import com.score.chatz.R;
import com.score.chatz.exceptions.NoUserException;
import com.score.chatz.utils.ActivityUtils;
import com.score.chatz.utils.PreferenceUtils;

import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.provider.ContactsContract;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

public class ContactsListActivity extends BaseActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        AdapterView.OnItemClickListener {

    private static final String TAG = ContactsListActivity.class.getName();

    // Ui Elements
    private EditText inputSearch;
    private Toolbar toolbar;
    private ListView mContactsList;

    private SimpleCursorAdapter mCursorAdapter;

    /*
     * Defines an array that contains column names to move from
     * the Cursor to the ListView.
     */
    private final static String[] FROM_COLUMNS = {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? ContactsContract.Contacts.DISPLAY_NAME_PRIMARY : ContactsContract.Contacts.DISPLAY_NAME
    };
    /*
     * Defines an array that contains resource ids for the layout views
     * that get the Cursor column contents. The id is pre-defined in
     * the Android framework, so it is prefaced with "android.R.id"
     */
    private final static int[] TO_IDS = {
            android.R.id.text1
    };

    // Defines the text expression
    private static final String SELECTION = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " LIKE ?" : ContactsContract.Contacts.DISPLAY_NAME + " LIKE ?";

    // Defines a variable for the search string, by default list loads nothing, thus empty string
    private String mSearchString = "";
    private String[] mSelectionArgs = {mSearchString};

    // Content loader identifier
    private static final Integer CONTENT_LOADER_ID = 0;

    // Sort list by SORT_ORDER
    private static final String SORT_ORDER = ContactsContract.Contacts.DISPLAY_NAME + " ASC ";

    // Define what you want to get out from provider
    private static final String[] PROJECTION =
            {
                    ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.LOOKUP_KEY,
                    Build.VERSION.SDK_INT
                            >= Build.VERSION_CODES.HONEYCOMB ?
                            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY :
                            ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.Contacts.HAS_PHONE_NUMBER

            };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts_list);

        setupUiForSearch();
        setupToolbar();
        setupActionBar();
        setupBackBtn();
        setupContactsList();
        setupContactsListAdapter();

        // Initializes the loader
        getSupportLoaderManager().initLoader(CONTENT_LOADER_ID, null, this);
    }

    private void setupContactsList() {
        // Gets the ListView from the View list of the parent activity
        mContactsList =
                (ListView) this.findViewById(R.id.contacts_list);
        // Set the item click listener to be the current fragment.
        mContactsList.setOnItemClickListener(this);
    }

    private void setupContactsListAdapter() {
        // Gets a CursorAdapter
        mCursorAdapter = new SimpleCursorAdapter(
                this,
                R.layout.contacts_list_item,
                null,
                FROM_COLUMNS, TO_IDS,
                0);
        // Sets the adapter for the ListView
        mContactsList.setAdapter(mCursorAdapter);
    }

    private void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setCollapsible(false);
        toolbar.setOverScrollMode(Toolbar.OVER_SCROLL_NEVER);
        setSupportActionBar(toolbar);
    }

    private void setupActionBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setCustomView(getLayoutInflater().inflate(R.layout.search_contacts_header, null));
        getSupportActionBar().setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
    }

    private void setupBackBtn() {
        ImageView backBtn = (ImageView) findViewById(R.id.back_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setupUiForSearch() {
        inputSearch = (EditText) findViewById(R.id.inputSearch);
        inputSearch.getBackground().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);

        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
                mSelectionArgs[0] = "%" + cs + "%";
                getSupportLoaderManager().restartLoader(CONTENT_LOADER_ID, null, ContactsListActivity.this);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
                // Nothing to do in this case
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // Nothing to do in this case
            }
        });
    }

    @Override
    public void onItemClick(
            AdapterView<?> parent, View item, int position, long rowID) {

        Cursor cursor = (Cursor) parent.getItemAtPosition(position);
        int item_ID = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts._ID));
        String item_DisplayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        int item_HasPhoneNumber = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
        boolean hasPhoneNumber;

        if (item_HasPhoneNumber > 0) {
            hasPhoneNumber = true;

        } else {
            hasPhoneNumber = false;
        }

        // Using the item_ID now we will get contact phone number
        Cursor cursorPhone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},

                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                        ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,

                new String[]{String.valueOf(item_ID)},
                null);

        if (hasPhoneNumber) {
            String mobileNumber = "";
            if (cursorPhone.moveToFirst()) {
                mobileNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            }

            String confirmationMessage = "<font size=10>Are you sure you want to share your username with </font> <font color=#F88F8C>" + "<b>" + item_DisplayName + "</b>" + "</font> (" + mobileNumber + "), via sms?";

            final String finalMobileNumber = mobileNumber;
            try {
                final String username = PreferenceUtils.getUser(this).getUsername();
                displayConfirmationMessageDialog(confirmationMessage, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendSMS(finalMobileNumber, "#Rahasak\nHi, I'm using Rahasak App.I have added you as a friend. #username " + username);
                    }
                });
            }catch (NoUserException ex){
                ex.printStackTrace();
            }
        } else {
            ActivityUtils.showCustomToastShort("This user has no mobile number", this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        // Starts the query
        return new CursorLoader(
                this,
                ContactsContract.Contacts.CONTENT_URI,
                PROJECTION,
                SELECTION,
                mSelectionArgs,
                SORT_ORDER
        );
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Put the result Cursor in the adapter for the ListView
        mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Delete the reference to the existing Cursor
        mCursorAdapter.swapCursor(null);
    }

    //---sends an SMS message to another device---
    private void sendSMS(String phoneNumber, String message)
    {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
                new Intent(SENT), 0);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
                new Intent(DELIVERED), 0);

        //---when the SMS has been sent---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        ActivityUtils.showCustomToastShort("SMS sent",
                                getBaseContext());
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        ActivityUtils.showCustomToastShort("Generic failure",
                                getBaseContext());
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        ActivityUtils.showCustomToastShort("No service",
                                getBaseContext());
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        ActivityUtils.showCustomToastShort("Null PDU",
                                getBaseContext());
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        ActivityUtils.showCustomToastShort("Radio off",
                                getBaseContext());
                        break;
                }
                ActivityUtils.cancelProgressDialog();
                unregisterReceiver(this);
            }
        }, new IntentFilter(SENT));

        //---when the SMS has been delivered---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        ActivityUtils.showCustomToastShort("SMS delivered",
                                getBaseContext());
                        break;
                    case Activity.RESULT_CANCELED:
                        ActivityUtils.showCustomToastShort("SMS not delivered",
                                getBaseContext());
                        break;
                }
                ActivityUtils.cancelProgressDialog();
                unregisterReceiver(this);
            }
        }, new IntentFilter(DELIVERED));

        // Show loader
        ActivityUtils.showProgressDialog(ContactsListActivity.this, "Please wait...");

        SmsManager sms = SmsManager.getDefault();
        Log.i(TAG, "SMS Body -> " + message);
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
    }


}