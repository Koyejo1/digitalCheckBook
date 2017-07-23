package com.score.rahasak.ui;
/**
 * Created by Lakmal on 7/15/17.
 */

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.score.rahasak.R;
import com.score.rahasak.enums.BlobType;
import com.score.rahasak.pojo.Check;
import com.score.rahasak.pojo.Secret;
import com.score.rahasak.utils.CheckUtils;
import com.score.rahasak.utils.ImageUtils;
import com.score.rahasak.utils.PhoneBookUtil;
import com.score.rahasak.utils.TimeUtils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;

class CheckListAdapter extends BaseAdapter {

    private static final String TAG = CheckListAdapter.class.getName();

    // Ui controls
    private Context context;
    private ArrayList<Check> checkList;
    private Typeface typeface;
    private NumberFormat nf;

    CheckListAdapter(Context _context, ArrayList<Check> secretList) {
        this.context = _context;
        this.checkList = secretList;
        this.typeface = Typeface.createFromAsset(context.getAssets(), "fonts/GeosansLight.ttf");
        this.nf = CheckUtils.getCheckDateFormater();
    }

    @Override
    public int getCount() {
        return checkList.size();
    }

    @Override
    public Object getItem(int position) {
        return checkList.get(position);
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
        final Check check = (Check) getItem(i);

        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.check_list_row_layout, viewGroup, false);

            holder = new ViewHolder();
            holder.message = (TextView) view.findViewById(R.id.message);
            holder.sender = (TextView) view.findViewById(R.id.sender);
            holder.sentTime = (TextView) view.findViewById(R.id.sent_time);

            holder.sender.setTypeface(typeface, Typeface.NORMAL);
            holder.message.setTypeface(typeface, Typeface.NORMAL);
            holder.sentTime.setTypeface(typeface, Typeface.NORMAL);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        setUpRow(check, holder);
        return view;
    }

    private void setUpRow(Check check, ViewHolder viewHolder) {
        // set username/name
        if (check.getIssuedFrom().getPhone() != null && !check.getIssuedFrom().getPhone().isEmpty()) {
            viewHolder.sender.setText("Check from: " + PhoneBookUtil.getContactName(context, check.getIssuedFrom().getPhone()));
        } else {
            viewHolder.sender.setText("Check from: @" + check.getIssuedFrom().getUsername());
        }
        // set username/name
        viewHolder.message.setText("Total: $"+ nf.format(check.getAmount()));
        if (check.getCreatedAt() != null) {
            viewHolder.sentTime.setText(TimeUtils.getTimeInWords(check.getCreatedAt()/1000));
        }
    }

    /**
     * Keep reference to children view to avoid unnecessary calls
     */
    private static class ViewHolder {
        TextView message;
        TextView sender;
        TextView sentTime;
    }
}
