package com.navigation.foxizz.activity.adapter

import android.view.*
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.navigation.foxizz.R
import com.navigation.foxizz.activity.fragment.MainFragment
import com.navigation.foxizz.util.LayoutUtil

/**
 * 路线规划信息列表的适配器
 */
class SchemeAdapter(private val mainFragment: MainFragment) :
        RecyclerView.Adapter<SchemeAdapter.ViewHolder>() {
    private var clickTime: Long = 0

    //设置item中的View
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var cardSchemeInfo: CardView = view.findViewById(R.id.card_scheme_info)
        var tvSimpleInfo: TextView = view.findViewById(R.id.tv_simple_info)
        var llInfoDrawer: LinearLayout = view.findViewById(R.id.ll_info_drawer)
        var tvDetailInfo: TextView = view.findViewById(R.id.tv_detail_info)
        var ibSchemeExpand: ImageButton = view.findViewById(R.id.ib_scheme_expand)
        var tvEnd: View = view.findViewById<View>(R.id.include_tv_end_scheme).findViewById(R.id.tv_end)
    }

    /**
     * 获取item数量
     *
     * @return item数量
     */
    override fun getItemCount(): Int {
        return mainFragment.mSchemeList.size
    }

    /**
     * 更新列表
     */
    fun updateList() {
        mainFragment.requireActivity().runOnUiThread {
            notifyDataSetChanged()
        }
    }

    //获取SearchItem的数据
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val schemeItem = mainFragment.mSchemeList[position]
        holder.tvSimpleInfo.text = schemeItem.simpleInfo
        holder.tvDetailInfo.text = schemeItem.detailInfo

        //根据保存的展开状态设置信息抽屉的高度、旋转角度和最大行数
        if (schemeItem.expandFlag) {
            LayoutUtil.setViewHeight(holder.llInfoDrawer,
                    holder.tvDetailInfo.lineHeight * (holder.tvDetailInfo.lineCount + 1))
            holder.ibSchemeExpand.rotation = 180f
            holder.tvSimpleInfo.maxLines = 8
        } else {
            LayoutUtil.setViewHeight(holder.llInfoDrawer, 0)
            holder.ibSchemeExpand.rotation = 0f
            holder.tvSimpleInfo.maxLines = 1
        }

        //底部显示提示信息
        if (position == mainFragment.mSchemeList.size - 1)
            holder.tvEnd.visibility = View.VISIBLE
        else holder.tvEnd.visibility = View.GONE
    }

    //为recyclerView的每一个item设置点击事件
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.adapter_scheme_item, parent, false)
        val holder = ViewHolder(view)

        //cardView的点击事件
        holder.cardSchemeInfo.setOnClickListener {
            if (unableToClick()) return@setOnClickListener
            val position = holder.adapterPosition
            mainFragment.infoFlag = 2 //设置信息状态为交通选择
            mainFragment.btMiddle.setText(R.string.middle_button3) //设置按钮为交通选择
            mainFragment.mBaiduRoutePlan.startMassTransitRoutePlan(position)
            mainFragment.schemeExpandFlag = 2 //设置方案布局为单个方案
            LayoutUtil.expandLayout(mainFragment.llSchemeDrawer, false) //收起方案抽屉
            LayoutUtil.expandLayout(mainFragment.llSchemeInfoLayout, true) //展开方案信息布局
        }

        //伸展按钮的点击事件
        holder.ibSchemeExpand.setOnClickListener {
            val position = holder.adapterPosition
            val schemeItem = mainFragment.mSchemeList[position]
            if (schemeItem.expandFlag) { //收起
                holder.tvSimpleInfo.maxLines = 1
                LayoutUtil.expandLayout(holder.llInfoDrawer, false, holder.tvDetailInfo,
                        mainFragment.recyclerSchemeResult, position)
                LayoutUtil.rotateExpandIcon(holder.ibSchemeExpand, 180f, 0f) //旋转伸展按钮
                schemeItem.expandFlag = false
            } else { //展开
                holder.tvSimpleInfo.maxLines = 8
                LayoutUtil.expandLayout(holder.llInfoDrawer, true, holder.tvDetailInfo,
                        mainFragment.recyclerSchemeResult, position)
                LayoutUtil.rotateExpandIcon(holder.ibSchemeExpand, 0f, 180f) //旋转伸展按钮
                schemeItem.expandFlag = true
            }
        }
        return holder
    }

    //不允许同时点击多个item
    private fun unableToClick(): Boolean {
        return if (System.currentTimeMillis() - clickTime > 1000) {
            clickTime = System.currentTimeMillis()
            false
        } else true
    }
}