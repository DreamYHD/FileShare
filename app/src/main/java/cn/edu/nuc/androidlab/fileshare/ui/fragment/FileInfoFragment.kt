package cn.edu.nuc.androidlab.fileshare.ui.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import cn.edu.nuc.androidlab.fileshare.R
import cn.edu.nuc.androidlab.fileshare.bean.FileInfo
import cn.edu.nuc.androidlab.fileshare.ui.adapter.AnimCommonAdapter
import cn.edu.nuc.androidlab.fileshare.util.FileUtil
import com.bumptech.glide.Glide
import com.zhy.adapter.recyclerview.base.ViewHolder

/**
 * Created by MurphySL on 2017/8/4.
 */
class FileInfoFragment private constructor() : Fragment(){

    private var fileType : Int = 0x00
    private val file_list = ArrayList<FileInfo>()
    private lateinit var recyclerView : RecyclerView
    private var onSelectedListener : OnSelectedListener? = null

    private constructor(type : Int) : this() {
        fileType = type
    }

    companion object {
        @JvmStatic
        fun instance(type : Int) : FileInfoFragment = FileInfoFragment(type)
    }

    fun setOnSelectedListener(onSelectedListener : OnSelectedListener){
        this.onSelectedListener = onSelectedListener
    }


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater?.inflate(R.layout.fragment_file_info, container, false)
        recyclerView = rootView?.findViewById(R.id.recycler_view)!!

        initData()

        return rootView
    }



    private fun initData() {
        when(fileType){
            FileUtil.APK_CODE -> {
                FileUtil.getSpecificTypeFiles(context, arrayOf(FileUtil.APK))
                        .forEach {
                            it.bitmap = FileUtil.drawableToBitmap(FileUtil.getApkThumbnail(context, it.path))
                            file_list.add(it)
                        }
                recyclerView.layoutManager = GridLayoutManager(context, 4)
                recyclerView.adapter = object : AnimCommonAdapter<FileInfo>(context, R.layout.item_apk_info, file_list){
                    override fun convert(holder: ViewHolder?, t: FileInfo?, position: Int) {
                        t?.let {
                            holder?.setText(R.id.apk_name, it.name)
                            holder?.setImageBitmap(R.id.apk_cover, it.bitmap)
                            holder?.setText(R.id.apk_size, FileUtil.byte2MemorySize(it.size))
                            holder?.itemView?.setOnClickListener {
                                val isVisible = holder.isVisible(R.id.apk_selected)
                                holder.setVisible(R.id.apk_selected, !isVisible)
                                onSelectedListener?.changeSelected(t, !isVisible)
                            }
                        }
                    }
                }
            }
            FileUtil.IMG_CODE -> {
                FileUtil.getSpecificTypeFiles(context, arrayOf(FileUtil.JPEG, FileUtil.JPG)).forEach { file_list.add(it) }
                recyclerView.layoutManager = GridLayoutManager(context, 4)
                recyclerView.adapter = object : AnimCommonAdapter<FileInfo>(context, R.layout.item_img_info, file_list){
                    override fun convert(holder: ViewHolder?, t: FileInfo?, position: Int) {
                        t?.let {
                            holder?.setImageWithPicasso(R.id.img_cover, it.path)
                            /*holder?.itemView?.setOnClickListener {
                                holder.setVisible(R.id.apk_selected, true)
                            }*/
                        }
                    }
                }
            }
            FileUtil.MUSIC_CODE -> {
                FileUtil.getSpecificTypeFiles(context , arrayOf(FileUtil.MP3)).forEach { file_list.add(it) }
                recyclerView.layoutManager = LinearLayoutManager(context)
                recyclerView.adapter = object : AnimCommonAdapter<FileInfo>(context, R.layout.item_music_info, file_list){
                    override fun convert(holder: ViewHolder?, t: FileInfo?, position: Int) {
                        t?.let {
                            holder?.setText(R.id.music_name, it.name)
                            holder?.setText(R.id.music_size, FileUtil.byte2MemorySize(it.size))
                        }
                    }
                }
            }
            FileUtil.VIDEO_CODE -> {
                FileUtil.getSpecificTypeFiles(context, arrayOf(FileUtil.MP4)).forEach {
                    it.bitmap = FileUtil.getVideoThumbnail(context, it.path)
                    file_list.add(it)
                }
                recyclerView.layoutManager = LinearLayoutManager(context)
                recyclerView.adapter = object : AnimCommonAdapter<FileInfo>(context, R.layout.item_music_info, file_list){
                    override fun convert(holder: ViewHolder?, t: FileInfo?, position: Int) {
                        t?.let {
                            holder?.setText(R.id.music_name, it.name)
                            holder?.setImageBitmap(R.id.music_cover, it.bitmap)
                            holder?.setText(R.id.music_size, FileUtil.byte2MemorySize(it.size))
                        }
                    }
                }
            }
        }
    }

    interface OnSelectedListener{
        fun changeSelected(fileInfo : FileInfo, isSelect : Boolean)
    }
}