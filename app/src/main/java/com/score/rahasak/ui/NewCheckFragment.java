package com.score.rahasak.ui;
/**
 * Created by Lakmal on 7/15/17.
 */

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.score.rahasak.R;
import com.score.rahasak.db.SenzorsDbSource;
import com.score.rahasak.enums.BlobType;
import com.score.rahasak.enums.DeliveryState;
import com.score.rahasak.exceptions.NoUserException;
import com.score.rahasak.pojo.BankUser;
import com.score.rahasak.pojo.Check;
import com.score.rahasak.pojo.Secret;
import com.score.rahasak.pojo.SecretUser;
import com.score.rahasak.utils.ImageUtils;
import com.score.rahasak.utils.PhoneBookUtil;
import com.score.rahasak.utils.PreferenceUtils;
import com.score.rahasak.utils.SenzUtils;
import com.score.senzc.enums.SenzTypeEnum;
import com.score.senzc.pojos.Senz;
import com.score.senzc.pojos.User;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class NewCheckFragment extends android.support.v4.app.Fragment  implements AdapterView.OnItemSelectedListener {
    private static final String TAG = NewCheckFragment.class.getName();

    // Ui elements
    private EditText fullName;
    private EditText amount;
    private Button generateBtn;
    private Button sendBtn;
    private Spinner addedUsersdropDown;

    private SenzorsDbSource dbSource;

    private ArrayList<SecretUser> friendList;
    private SecretUser selectedUser;
    private User owner;

    protected Typeface typefaceThin;
    private Long timestamp;

    private String previouslyGeneratedFullName;
    private String previouslyGeneratedAmount;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_new_check, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        dbSource = new SenzorsDbSource(getContext());
        friendList = dbSource.getSecretUserList();

        setupOwner();
        setupFonts();
        initUi();
        setupAddedUsersDropdown();
        setupBtnHandlers();
    }

    private void setupOwner(){
        try {
            owner = PreferenceUtils.getUser(getActivity());
        } catch (NoUserException e) {
            e.printStackTrace();
        }
    }

    private void setupAddedUsersDropdown(){
        addedUsersdropDown = (Spinner) getActivity().findViewById(R.id.spinner);
        addedUsersdropDown.setOnItemSelectedListener(this);
        addedUsersdropDown.getBackground().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);

        List<String> users = Arrays.asList(getUserNames(friendList));

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, users);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        addedUsersdropDown.setAdapter(dataAdapter);
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

    private void setupFonts() {
        typefaceThin = Typeface.createFromAsset(getActivity().getAssets(), "fonts/GeosansLight.ttf");
    }

    private void initUi() {
        fullName = (EditText) getActivity().findViewById(R.id.acc_full_name);
        fullName.setTypeface(typefaceThin, Typeface.NORMAL);
        fullName.getBackground().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);

        amount = (EditText) getActivity().findViewById(R.id.amount);
        amount.setTypeface(typefaceThin, Typeface.NORMAL);
        amount.getBackground().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);

        sendBtn = (Button) getActivity().findViewById(R.id.send_check_btn);
        sendBtn.setTypeface(typefaceThin, Typeface.BOLD);

        generateBtn = (Button) getActivity().findViewById(R.id.generate_check_btn);
        generateBtn.setTypeface(typefaceThin, Typeface.BOLD);
    }

    private void setupBtnHandlers(){
        generateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (!fullName.getText().toString().trim().isEmpty() && !amount.getText().toString().trim().isEmpty()) {
                        // Validity check
                        Double.parseDouble(amount.getText().toString().trim());

                        // Cache generated values
                        previouslyGeneratedFullName = fullName.getText().toString().trim();
                        previouslyGeneratedAmount = amount.getText().toString().trim();

                        Intent i = new Intent(getActivity(), CheckFullScreenActivity.class);
                        timestamp = System.currentTimeMillis();
                        i.putExtra("fullname", fullName.getText().toString().trim());
                        i.putExtra("amount", amount.getText().toString().trim());
                        i.putExtra("date", timestamp.toString());
                        getContext().startActivity(i);
                    } else {
                        Toast.makeText(getActivity(), "Some fields are missing, please complete the form", Toast.LENGTH_LONG).show();
                    }
                }catch(Exception ex){
                    ex.printStackTrace();
                    Toast.makeText(getActivity(), "You have entered invalid data", Toast.LENGTH_LONG).show();
                }
            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (!fullName.getText().toString().trim().isEmpty() && !amount.getText().toString().trim().isEmpty() && selectedUser != null) {
                        if (fullName.getText().toString().trim().equals(previouslyGeneratedFullName) && amount.getText().toString().trim().equals(previouslyGeneratedAmount)) {
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            ImageUtils.getImageBitmapFromInternalStorage("capturedCheck.jpg", getActivity()).compress(Bitmap.CompressFormat.JPEG, 100, stream);
                            byte[] byteArray = compress(stream.toByteArray());
                            ((DrawerActivity) getActivity()).sendPhotoCheckSenz(byteArray, dbSource.getSecretUser(selectedUser.getUsername()), getActivity(), new Check(new BankUser(
                                    "_id",
                                    selectedUser.getUsername(),
                                    fullName.getText().toString().trim()), "", timestamp, Long.parseLong(amount.getText().toString().trim()), null));
                            Toast.makeText(getActivity(), "Check Sent", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getActivity(), "You must regenerate the check before sending", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(getActivity(), "Some fields are missing, please complete the form", Toast.LENGTH_LONG).show();
                    }
                }catch(IOException ex){
                    ex.printStackTrace();
                }
            }
        });
    }

    public static byte[] compress(byte[] data) throws IOException {
        Deflater deflater = new Deflater();
        deflater.setInput(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        deflater.finish();
        byte[] buffer = new byte[1024];
        while (!deflater.finished()) {
            int count = deflater.deflate(buffer); // returns the generated code... index
            outputStream.write(buffer, 0, count);
        }
        outputStream.close();
        byte[] output = outputStream.toByteArray();
        Log.d(TAG, "Original: " + data.length / 1024 + " Kb");
        Log.d(TAG, "Compressed: " + output.length / 1024 + " Kb");
        return output;
    }

    private String[] getUserNames(ArrayList<SecretUser> users){
        final String[] userNamesArray = new String[users.size()+1];
        if(users.size() == 0){
            userNamesArray[0] = "You have no added users";
        }else {
            userNamesArray[0] = "Select a friend";
        }
        for (int i = 0; i < users.size(); i++) {
            userNamesArray[i+1] = PhoneBookUtil.getContactName(getActivity(), users.get(i).getPhone());
        }
        return userNamesArray;
    }
}
