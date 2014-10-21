/*
 * Copyright (c) 2014 Mark Winter (Astonex)
 * Author: Emil Suleymanov (suleymanovemil8@gmail.com)
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

package im.tox.antox.activities

import java.util.regex.Pattern

import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.app.ActionBarActivity
import android.text.util.Linkify
import android.view.MenuItem
import android.widget.TextView
import im.tox.antox.R

class About extends ActionBarActivity {

  protected override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.about)
    getSupportActionBar.setDisplayHomeAsUpEnabled(true)
    val tw = findViewById(R.id.textView).asInstanceOf[TextView]
    val tw10 = findViewById(R.id.textView10).asInstanceOf[TextView]
    val pattern = Pattern.compile("https://github.com/Astonex/Antox")
    Linkify.addLinks(tw10, pattern, "")
    var version = "-.-.-"
    try {
      version = getPackageManager.getPackageInfo(getPackageName, 0).versionName
    } catch {
      case e: PackageManager.NameNotFoundException => e.printStackTrace()
    }
    tw.setText(getString(R.string.ver) + " " + version)
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = item.getItemId match {
    case android.R.id.home =>
      finish()
      true

  }
}
