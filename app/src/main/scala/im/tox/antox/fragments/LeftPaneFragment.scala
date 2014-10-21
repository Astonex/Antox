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

package im.tox.antox.fragments

import android.app.ActionBar
import android.app.FragmentTransaction
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v4.view.PagerTabStrip
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ImageSpan
import android.text.style.DynamicDrawableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import im.tox.antox.R
import im.tox.antox.activities.MainActivity
//remove if not needed
import scala.collection.JavaConversions._

class LeftPaneFragment extends Fragment {

  class LeftPagerAdapter(fm: FragmentManager) extends FragmentPagerAdapter(fm) {

    override def getPageTitle(position: Int): CharSequence = {
      val drawableId = position match {
        case 0 => R.drawable.ic_action_recent_tab
        case _ => R.drawable.ic_action_contacts_tab
      }
      val drawable: Drawable = getResources.getDrawable(drawableId)
      val sb: SpannableStringBuilder = new SpannableStringBuilder("")
      drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight()); 
      val span: ImageSpan = new ImageSpan(drawable, DynamicDrawableSpan.ALIGN_BASELINE); 
      sb.setSpan(span, 0, 0, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); 
      return sb;
    }

    override def getItem(pos: Int): Fragment = pos match {
      case 0 => new RecentFragment()
      case _ => new ContactsFragment()
    }

    override def getCount(): Int = 2
  }

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    val thisActivity = this.getActivity.asInstanceOf[MainActivity]
    val actionBar = thisActivity.getActionBar()
    val rootView = inflater.inflate(R.layout.fragment_leftpane, container, false)
    val pager = rootView.findViewById(R.id.pager).asInstanceOf[ViewPager]

    val tabListener = new ActionBar.TabListener() {
        def onTabSelected(tab: ActionBar.Tab, ft: FragmentTransaction) = {
          pager.setCurrentItem(tab.getPosition())
        }

        def onTabUnselected(tab: ActionBar.Tab, ft: FragmentTransaction) = {
        }

        def onTabReselected(tab: ActionBar.Tab, ft: FragmentTransaction) = {
        }
    }
    val pagerTabStrip = rootView.findViewById(R.id.pager_tabs).asInstanceOf[PagerTabStrip]
    pagerTabStrip.setDrawFullUnderline(true)
    pagerTabStrip.setTabIndicatorColorResource(R.color.white_absolute)

    pager.setAdapter(new LeftPagerAdapter(getFragmentManager))

    rootView
  }
}
