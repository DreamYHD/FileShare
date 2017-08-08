package cn.edu.nuc.androidlab.fileshare.ui.activity

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.util.Log
import cn.edu.nuc.androidlab.fileshare.MyApplication

import cn.edu.nuc.androidlab.fileshare.R
import cn.edu.nuc.androidlab.fileshare.bean.FileInfo
import cn.edu.nuc.androidlab.fileshare.ui.fragment.*
import cn.edu.nuc.androidlab.fileshare.util.FileUtil
import kotlinx.android.synthetic.main.activity_choose_file.*

class ChooseFileActivity : AppCompatActivity() {
    private val TAG : String = this.javaClass.simpleName

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

    private fun updateSelected(fileInfo: FileInfo, isSelect : Boolean){
        if(isSelect){
            MyApplication.instance.addFileInfo(fileInfo)
        }else{
            MyApplication.instance.removeFileInfo(fileInfo)
        }

        select.text = "已选(${MyApplication.instance.fileInfoMap.size})"
    }

    //待修改
    private fun initFragment() {
        val apk_fragment = FileInfoFragment.instance(FileUtil.APK_CODE)
        apk_fragment.setOnSelectedListener(object : FileInfoFragment.OnSelectedListener{
            override fun changeSelected(fileInfo: FileInfo, isSelect: Boolean) {
                Log.i(TAG, fileInfo.path)
                updateSelected(fileInfo, isSelect)
            }
        })
        val img_fragment = FileInfoFragment.instance(FileUtil.IMG_CODE)
        img_fragment.setOnSelectedListener(object : FileInfoFragment.OnSelectedListener{
            override fun changeSelected(fileInfo: FileInfo, isSelect: Boolean) {
                updateSelected(fileInfo, isSelect)
            }
        })
        val music_fragment = FileInfoFragment.instance(FileUtil.MUSIC_CODE)
        music_fragment.setOnSelectedListener(object : FileInfoFragment.OnSelectedListener{
            override fun changeSelected(fileInfo: FileInfo, isSelect: Boolean) {
                updateSelected(fileInfo, isSelect)
            }
        })
        val video_fragment = FileInfoFragment.instance(FileUtil.VIDEO_CODE)
        video_fragment.setOnSelectedListener(object : FileInfoFragment.OnSelectedListener{
            override fun changeSelected(fileInfo: FileInfo, isSelect: Boolean) {
                updateSelected(fileInfo, isSelect)
            }
        })
        fragments.add(apk_fragment)
        fragments.add(img_fragment)
        fragments.add(music_fragment)
        fragments.add(video_fragment)
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

        next.setOnClickListener {
            startActivity(Intent(ChooseFileActivity@this, ChooseReceiverActivity::class.java))
        }
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
