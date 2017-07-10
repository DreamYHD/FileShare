package cn.edu.nuc.androidlab.fileshare

import android.app.Application
import kotlin.properties.Delegates

/**
 * Created by MurphySL on 2017/7/10.
 */
class MyApplication : Application(){

    companion object {
        val instance  : MyApplication by Delegates.notNull()
    }

    override fun onCreate() {
        super.onCreate()
    }

}