package com.score.rahasak.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.score.rahasak.R;
import com.score.rahasak.enums.BlobType;
import com.score.rahasak.pojo.Secret;
import com.score.rahasak.utils.ImageUtils;
import com.score.rahasak.utils.PhoneBookUtil;
import com.score.rahasak.utils.TimeUtils;

import java.util.ArrayList;

class SecretListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Secret> userSecretList;
    private Typeface typeface;

    SecretListAdapter(Context _context, ArrayList<Secret> secretList) {
        this.context = _context;
        this.userSecretList = secretList;

        typeface = Typeface.createFromAsset(context.getAssets(), "fonts/GeosansLight.ttf");
    }

    @Override
    public int getCount() {
        return userSecretList.size();
    }

    @Override
    public Object getItem(int position) {
        return userSecretList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Create list row view
     *
     * @param i         index
     * @param view      current list item view
     * @param viewGroup parent
     * @return view
     */
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        final ViewHolder holder;
        final Secret secret = (Secret) getItem(i);

        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.secret_list_row_layout, viewGroup, false);

            holder = new ViewHolder();
            holder.message = (TextView) view.findViewById(R.id.message);
            holder.sender = (TextView) view.findViewById(R.id.sender);
            holder.sentTime = (TextView) view.findViewById(R.id.sent_time);
            holder.userImage = (com.github.siyamed.shapeimageview.RoundedImageView) view.findViewById(R.id.user_image);
            holder.selected = (ImageView) view.findViewById(R.id.selected);

            holder.sender.setTypeface(typeface, Typeface.NORMAL);
            holder.message.setTypeface(typeface, Typeface.NORMAL);
            holder.sentTime.setTypeface(typeface, Typeface.NORMAL);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        setUpRow(secret, holder);
        return view;
    }

    private void setUpRow(Secret secret, ViewHolder viewHolder) {
        // set username/name
        if (secret.getUser().getPhone() != null && !secret.getUser().getPhone().isEmpty()) {
            viewHolder.sender.setText(PhoneBookUtil.getContactName(context, secret.getUser().getPhone()));
        } else {
            viewHolder.sender.setText("@" + secret.getUser().getUsername());
        }

        if (secret.getBlobType() == BlobType.IMAGE) {
            if (secret.isMissed()) {
                viewHolder.message.setText("Missed selfie");
            } else {
                viewHolder.message.setText("Selfie secret");
            }
        } else if (secret.getBlobType() == BlobType.SOUND) {
            viewHolder.message.setText("Audio secret");
        } else if (secret.getBlobType() == BlobType.TEXT) {
            viewHolder.message.setText(secret.getBlob());
        }

        if (secret.getTimeStamp() != null) {
            viewHolder.sentTime.setText(TimeUtils.getTimeInWords(secret.getTimeStamp()));
        }

        if (secret.getUser().getImage() != null) {
            viewHolder.userImage.setImageBitmap(ImageUtils.decodeBitmap(secret.getUser().getImage()));
        } else {
            viewHolder.userImage.setImageResource(R.drawable.default_user);
        }

        if (secret.isViewed()) {
            viewHolder.selected.setVisibility(View.VISIBLE);
        } else {
            viewHolder.selected.setVisibility(View.GONE);
        }
    }

    /**
     * Keep reference to children view to avoid unnecessary calls
     */
    private static class ViewHolder {
        TextView message;
        TextView sender;
        TextView sentTime;
        com.github.siyamed.shapeimageview.RoundedImageView userImage;
        ImageView selected;
    }
}