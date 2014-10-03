package im.tox.antox.fragments

import android.os.Bundle
import android.support.v4.app.{Fragment, FragmentManager, FragmentPagerAdapter}
import android.support.v4.view.ViewPager
import android.view.{LayoutInflater, View, ViewGroup}
import com.astuetz.PagerSlidingTabStrip
import im.tox.antox.R
import im.tox.antox.activities.MainActivity

//remove if not needed

class LeftPaneFragment extends Fragment {

  private var main_act: MainActivity = _

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    main_act = getActivity.asInstanceOf[MainActivity]
    val rootView = inflater.inflate(R.layout.fragment_leftpane, container, false)
    val pager = rootView.findViewById(R.id.pager).asInstanceOf[ViewPager]
    pager.setAdapter(new LeftPagerAdapter(getFragmentManager))
    val tabs = rootView.findViewById(R.id.tabs).asInstanceOf[PagerSlidingTabStrip]
    tabs.setViewPager(pager)
    rootView
  }

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

}
