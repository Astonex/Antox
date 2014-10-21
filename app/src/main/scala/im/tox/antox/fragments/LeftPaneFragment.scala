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
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import im.tox.antox.R
import im.tox.antox.activities.MainActivity
//remove if not needed
import scala.collection.JavaConversions._

class LeftPaneFragment extends Fragment {

  class LeftPagerAdapter(fm: FragmentManager) extends FragmentPagerAdapter(fm) {

    private val TITLES = Array(getString(R.string.titles_recent), getString(R.string.titles_contacts))

    override def getPageTitle(position: Int): CharSequence = TITLES(position)

    override def getItem(pos: Int): Fragment = pos match {
      case 0 => new RecentFragment()
      case 1 => new ContactsFragment()
      case _ => new ContactsFragment()
    }

    override def getCount(): Int = TITLES.length
  }

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    val thisActivity = this.getActivity.asInstanceOf[MainActivity]
    val actionBar = thisActivity.getActionBar()
    val rootView = inflater.inflate(R.layout.fragment_leftpane, container, false)
    val pager = rootView.findViewById(R.id.pager).asInstanceOf[ViewPager]

    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS)

    val tabListener = new ActionBar.TabListener() {
        def onTabSelected(tab: ActionBar.Tab, ft: FragmentTransaction) = {
          pager.setCurrentItem(tab.getPosition())
        }

        def onTabUnselected(tab: ActionBar.Tab, ft: FragmentTransaction) = {
        }

        def onTabReselected(tab: ActionBar.Tab, ft: FragmentTransaction) = {
        }
    }

    actionBar.addTab(
            actionBar.newTab()
              .setIcon(R.drawable.ic_action_recent_tab)
                    .setTabListener(tabListener))

    actionBar.addTab(
            actionBar.newTab()
              .setIcon(R.drawable.ic_action_contacts_tab)
                    .setTabListener(tabListener))

    pager.setAdapter(new LeftPagerAdapter(getFragmentManager))
    pager.setOnPageChangeListener(
            new ViewPager.SimpleOnPageChangeListener() {
                override def onPageSelected(position: Int) = {
                    thisActivity.getActionBar().setSelectedNavigationItem(position);
                }
            })

    rootView
  }
}
