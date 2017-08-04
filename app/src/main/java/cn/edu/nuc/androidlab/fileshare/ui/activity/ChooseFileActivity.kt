package cn.edu.nuc.androidlab.fileshare.ui.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar

import cn.edu.nuc.androidlab.fileshare.R
import cn.edu.nuc.androidlab.fileshare.bean.FileInfo
import cn.edu.nuc.androidlab.fileshare.ui.fragment.*
import cn.edu.nuc.androidlab.fileshare.util.FileUtil

class ChooseFileActivity : AppCompatActivity() {

    private lateinit var tabLayout : TabLayout
    private lateinit var viewPager : ViewPager
    private lateinit var tabTitle : Array<String>
    private val fragments : ArrayList<Fragment> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_file)

        initFragment()
        initView()
    }

    private fun initFragment() {
        fragments.add(FileInfoFragment.instance(FileUtil.APK_CODE))
        fragments.add(FileInfoFragment.instance(FileUtil.IMG_CODE))
        fragments.add(FileInfoFragment.instance(FileUtil.MUSIC_CODE))
        fragments.add(FileInfoFragment.instance(FileUtil.VIDEO_CODE))
    }

    fun initView(){
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        toolbar.title = resources.getString(R.string.choose_file_title)

        tabLayout = findViewById(R.id.tablayout) as TabLayout
        viewPager = findViewById(R.id.viewpager) as ViewPager

        tabTitle = arrayOf(
                resources.getString(R.string.tab_apk),
                resources.getString(R.string.tab_pic),
                resources.getString(R.string.tab_music),
                resources.getString(R.string.tab_video))

        val adapter : MyFragmentPagerAdapter = MyFragmentPagerAdapter(supportFragmentManager, tabTitle, fragments)

        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)
        tabLayout.setTabsFromPagerAdapter(adapter)
    }

    class MyFragmentPagerAdapter(fm : FragmentManager,
                                 val title : Array<String>,
                                 val fragments : ArrayList<Fragment>) : FragmentPagerAdapter(fm){
        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        override fun getCount(): Int {
            return fragments.size
        }

        override fun getPageTitle(position: Int): CharSequence {
            return title[position]
        }

    }
}
