package cn.edu.nuc.androidlab.fileshare.ui.adapter

import android.animation.ObjectAnimator
import android.content.Context
import android.view.View
import android.widget.ImageView
import cn.edu.nuc.androidlab.fileshare.R
import com.bumptech.glide.Glide
import com.zhy.adapter.recyclerview.CommonAdapter
import com.zhy.adapter.recyclerview.base.ViewHolder

/**
 * Adapter with animation
 *
 * Created by MurphySL on 2017/7/27.
 */
abstract class AnimCommonAdapter<T>(val context : Context, layoutId : Int, val data : MutableList<T>) : CommonAdapter<T>(context, layoutId, data){

    private var oldPosition = 0

    override fun onViewAttachedToWindow(holder: ViewHolder?) {
        if(holder?.layoutPosition !! > oldPosition){
            addItemAnimation(holder.itemView.findViewById(R.id.file_info))
            oldPosition = holder.layoutPosition
        }
    }

    private fun addItemAnimation(view : View?){
        val translationY = ObjectAnimator.ofFloat(view, "translationY", 500f, 0f)
        translationY.duration = 500
        translationY.start()
    }

    fun ViewHolder.setImageWithPicasso(viewId : Int, url : String) : ViewHolder{
        val view : ImageView = getView(viewId)
        Glide.with(context)
                .load(url)
                .into(view)
        return this
    }

    fun ViewHolder.isVisible(viewId: Int) : Boolean{
        val view : View = getView(viewId)
        return view.visibility == View.VISIBLE
    }

}

