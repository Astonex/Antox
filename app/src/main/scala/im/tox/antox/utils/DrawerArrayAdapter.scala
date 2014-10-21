/*
 * Copyright (c) 2014 Mark Winter (Astonex)
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

package im.tox.antox.utils

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import java.util.List
import im.tox.antox.R
//remove if not needed
import scala.collection.JavaConversions._

class DrawerArrayAdapter(context: Context, resourceId: Int, items: List[DrawerItem])
  extends ArrayAdapter[DrawerItem](context, resourceId, items) {

  private var mItems: List[DrawerItem] = items

  def getList(): List[DrawerItem] = mItems

  override def getView(position: Int, convertView: View, parent: ViewGroup): View = {
    var holder: ViewHolder = null
    val rowItem = getItem(position)
    val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE).asInstanceOf[LayoutInflater]
    val newConvertView = inflater.inflate(R.layout.rowlayout_drawer, null)
    holder = new ViewHolder()
    holder.txtLabel = newConvertView.findViewById(R.id.textView).asInstanceOf[TextView]
    holder.imageView = newConvertView.findViewById(R.id.imageView).asInstanceOf[ImageView]
    holder.txtLabel.setText(rowItem.getLabel)
    holder.imageView.setBackgroundResource(rowItem.getResId)
    newConvertView.setTag(holder)
    newConvertView
  }

  private class ViewHolder {

    var txtLabel: TextView = _

    var imageView: ImageView = _
  }
}

