package com.kelevra.navi;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Created by sharlukovich on 28.05.2015.
 */
public class MenuAdapter extends BaseAdapter implements StickyListHeadersAdapter {

    private final Context context;

    static class ViewHolder {
        private TextView tvLabel;
        private ImageView ivIcon;
    }

    public MenuAdapter(Context context) {
        this.context = context;
    }

    @Override
    public long getHeaderId(int position) {
        return MainMenu.getGroupIdByPosition(position);
    }

    @Override
    public int getCount() {
        return MainMenu.getSize();
    }

    @Override
    public Object getItem(int position) {
        return MainMenu.getByPosition(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        ViewHolder viewHolder;
        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.drawer_list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.tvLabel = (TextView) rowView.findViewById(R.id.tvLabel);
            viewHolder.ivIcon = (ImageView) rowView.findViewById(R.id.ivIcon);
            rowView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) rowView.getTag();
        }

        MenuItems menuItem = MainMenu.getByPosition(position);
        int imageFile = menuItem.getIconFile();
        viewHolder.tvLabel.setText(menuItem.getName());
        viewHolder.ivIcon.setImageDrawable(context.getResources().getDrawable(imageFile));
        return rowView;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        ViewHolder viewHolder;
        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.drawer_list_header, null);
            viewHolder = new ViewHolder();
            viewHolder.tvLabel = (TextView) rowView.findViewById(R.id.tvLabel);
            viewHolder.ivIcon = null;
            rowView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) rowView.getTag();
        }

        int groupId = MainMenu.getGroupIdByPosition(position);
        viewHolder.tvLabel.setText(context.getString(groupId));
        return rowView;
    }
}
