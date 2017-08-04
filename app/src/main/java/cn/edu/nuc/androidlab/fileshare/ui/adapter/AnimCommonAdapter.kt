package cn.edu.nuc.androidlab.fileshare.ui.adapter

import android.animation.ObjectAnimator
import android.content.Context
import android.view.View
import cn.edu.nuc.androidlab.fileshare.R
import com.zhy.adapter.recyclerview.CommonAdapter
import com.zhy.adapter.recyclerview.base.ViewHolder

/**
 * Adapter with animation for item_live_new
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
        //val scaleY = ObjectAnimator.ofFloat(card, "scaleY", 0.5f, 1f)
        //val set = AnimatorSet()
        //set.playTogether(translationY, scaleY)
        translationY.duration = 500
        translationY.start()
    }
}