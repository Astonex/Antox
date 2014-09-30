/*
 * Copyright (c) 2014 Emil Suleymanov
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */

package im.tox.antox.utils;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import im.tox.antox.R;

public class DrawerArrayAdapter extends ArrayAdapter<DrawerItem> {

    private Context mContext;
    private List<DrawerItem> mItems;

    public DrawerArrayAdapter(Context context, int resourceId,
                              List<DrawerItem> items) {
        super(context, resourceId, items);
        this.mContext = context;
        this.mItems = items;
    }

    public List<DrawerItem> getList() {
        return mItems;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        DrawerItem rowItem = getItem(position);

        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        convertView = inflater.inflate(R.layout.rowlayout_drawer, null);

        holder = new ViewHolder();

        holder.txtLabel = (TextView) convertView.findViewById(R.id.textView);
        holder.imageView = (ImageView) convertView.findViewById(R.id.imageView);

        holder.txtLabel.setText(rowItem.getLabel());
        holder.imageView.setBackgroundResource(rowItem.getResId());

        convertView.setTag(holder);

        return convertView;
    }

    /*private view holder class*/
    private class ViewHolder {
        TextView txtLabel;
        ImageView imageView;
    }
}