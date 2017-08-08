package cn.edu.nuc.androidlab.fileshare

import android.app.Application
import cn.edu.nuc.androidlab.fileshare.bean.FileInfo
import java.util.concurrent.Executors
import kotlin.properties.Delegates

/**
 * Created by MurphySL on 2017/7/10.
 */
class MyApplication : Application(){

    companion object {

        var instance  : MyApplication by Delegates.notNull()
    }

    val fileInfoMap = HashMap<String, FileInfo>()
    val mainExecutor = Executors.newFixedThreadPool(5)

    override fun onCreate() {
        super.onCreate()
        instance = MyApplication()
    }

    fun addFileInfo(fileInfo : FileInfo){
        fileInfoMap.put(fileInfo.path, fileInfo)
    }

    fun removeFileInfo(fileInfo : FileInfo){
        fileInfoMap.remove(fileInfo.path)
    }



}