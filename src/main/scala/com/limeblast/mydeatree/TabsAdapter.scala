package com.limeblast.mydeatree

import android.support.v4.app.{FragmentTransaction, FragmentActivity, Fragment, FragmentPagerAdapter}
import com.actionbarsherlock.app.ActionBar
import android.support.v4.view.ViewPager
import android.os.Bundle
import com.actionbarsherlock.app.ActionBar.Tab
import java.util

class TabInfo(val _class: Class[_], val _args: Bundle)

class TabsAdapter(val activity: FragmentActivity, val bar: ActionBar, val pager: ViewPager)
  extends FragmentPagerAdapter(activity.getSupportFragmentManager)
  with ViewPager.OnPageChangeListener with ActionBar.TabListener{

  lazy private val mTabs = new util.ArrayList[TabInfo]()
  pager.setAdapter(this)
  pager.setOnPageChangeListener(this)

  def addTab(tab: ActionBar.Tab, clss: Class[_ <: Fragment], args: Bundle) {
    val info = new TabInfo(clss, args)
    tab.setTag(info)
    tab.setTabListener(this)
    mTabs.add(info)

    bar.addTab(tab)
    notifyDataSetChanged()

  }

  def getCount: Int = {
    mTabs.size()
  }

  def getItem(position: Int): Fragment = {
    val info = mTabs.get(position)
    Fragment.instantiate(activity, info._class.getName, info._args)
  }

  def onPageScrolled(p1: Int, p2: Float, p3: Int) {}

  def onPageSelected(position: Int) {
    bar.setSelectedNavigationItem(position)
  }

  def onPageScrollStateChanged(state: Int) {}

  def onTabSelected(tab: Tab, ft: FragmentTransaction) {
    val tag = tab.getTag
    for (i <- 0 to mTabs.size - 1){
      if (mTabs.get(i) == tag)
        pager.setCurrentItem(i)
    }
  }

  def onTabUnselected(tab: Tab, ft: FragmentTransaction) {}

  def onTabReselected(tab: Tab, ft: FragmentTransaction) {}
}

