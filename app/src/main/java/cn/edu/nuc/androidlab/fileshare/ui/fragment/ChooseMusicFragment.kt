package cn.edu.nuc.androidlab.fileshare.ui.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.edu.nuc.androidlab.fileshare.R

/**
 * ChooseAPKFragment
 * Created by MurphySL on 2017/7/10.
 */
class ChooseMusicFragment private constructor(): Fragment(){

    companion object {
        val instance : ChooseMusicFragment = ChooseMusicFragment()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater?.inflate(R.layout.fragment_choose_music, container, false)
        return rootView
    }
}