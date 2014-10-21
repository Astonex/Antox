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

import android.os.Bundle
import android.support.v7.app.ActionBarActivity
import android.view.MenuItem
import android.webkit.WebView
import im.tox.antox.R
//remove if not needed

class License extends ActionBarActivity {

  override def onOptionsItemSelected(item: MenuItem): Boolean = item.getItemId match {
    case android.R.id.home =>
      finish()
      true
  }

  override def onCreate(paramBundle: Bundle) {
    super.onCreate(paramBundle)
    setContentView(R.layout.license_menu)
    if (getSupportActionBar != null) getSupportActionBar.setDisplayHomeAsUpEnabled(true)
    val localWebView = findViewById(R.id.webView).asInstanceOf[WebView]
    val webSettings = localWebView.getSettings
    webSettings.setJavaScriptEnabled(false)
    localWebView.loadUrl("file:///android_res/raw/license.html")
  }
}
