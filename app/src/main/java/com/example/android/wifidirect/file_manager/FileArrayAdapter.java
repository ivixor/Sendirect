package com.example.android.wifidirect.file_manager;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.wifidirect.R;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;


public class FileArrayAdapter extends ArrayAdapter {
    private LayoutInflater inflater;
    private Context context;
    private int id;

    private List<File> items;
    private SparseBooleanArray selectedIDs;

    boolean isDir = false;

    public FileArrayAdapter(Context context, int textViewResourceId,
                            List<File> objects) {
        super(context, textViewResourceId, objects);
        this.context = context;
        id = textViewResourceId;
        items = objects;

        selectedIDs = new SparseBooleanArray();

        inflater = LayoutInflater.from(context);
    }

    public File getItem(int i)
    {
        return items.get(i);
    }

    private class ViewHolder {
        TextView textView1;
        TextView textView2;
        TextView textView3;
        ImageView itemImage;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        final ViewHolder holder;
        final File o = items.get(position);

        String data;

        if (o.isDirectory()) {
            File[] fbuf = o.listFiles();
            int buf = 0;
            if(fbuf != null){
                buf = fbuf.length;
            } else {
                buf = 0;
            }

            String num_item = String.valueOf(buf);
            if (buf == 0) {
                num_item = num_item + " item";
            } else {
                num_item = num_item + " items";
            }

            data = num_item;
        } else {
            data = o.length() + " Byte";
        }


        Date lastModDate = new Date(o.lastModified());
        DateFormat formater = DateFormat.getDateTimeInstance();
        String date_modify = formater.format(lastModDate);

        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.file_exploler, null);

            holder.textView1 = (TextView) view.findViewById(R.id.TextView01);
            holder.textView2 = (TextView) view.findViewById(R.id.TextView02);
            holder.textView3 = (TextView) view.findViewById(R.id.TextViewDate);
            holder.itemImage = (ImageView) view.findViewById(R.id.ImageView);

            String uri = "drawable/";

            if (o.isDirectory()) {
                uri += "directory_icon";
            } else {
                uri += "file_icon";
            }

            int imageResource = context.getResources().getIdentifier(uri, "drawable", context.getPackageName());
            Drawable image = context.getResources().getDrawable(imageResource);
            holder.itemImage.setImageDrawable(image);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        if (o != null) {
            if (holder.textView1 != null)
                holder.textView1.setText(o.getName());
            if (holder.textView2 != null)
                holder.textView2.setText(data);
            if (holder.textView3 != null)
                holder.textView3.setText(date_modify);
        }

        return view;
    }

    public void toggleSelection(int position, MenuItem menuItem) {
        selectView(position, !selectedIDs.get(position));

        isDir = false;

        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).isDirectory() && selectedIDs.get(i)) {
                isDir = true;
            }
        }

        if (isDir) {
            menuItem.setEnabled(false);
        } else {
            menuItem.setEnabled(true);
        }
    }

    public int getSelectedCount() {
        return selectedIDs.size();
    }

    public SparseBooleanArray getSelectedIDs() {
        return selectedIDs;
    }

    public void removeSelection() {
        selectedIDs = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    public void selectView(int position, boolean value) {
        if (value) {
            selectedIDs.put(position, value);
        } else {
            selectedIDs.delete(position);
        }

        notifyDataSetChanged();
    }
}
