package com.navigation.foxizz.activity.adapter

import android.view.*
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import base.foxizz.util.expandLayout
import base.foxizz.util.rotateExpandIcon
import base.foxizz.util.runOnUiThread
import base.foxizz.util.setHeight
import com.navigation.foxizz.R
import com.navigation.foxizz.activity.fragment.MainFragment
import kotlinx.android.synthetic.main.adapter_scheme_item.view.*
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.include_tv_end.view.*

/**
 * 路线规划信息列表的适配器
 *
 * @param mainFragment 地图页
 */
class SchemeAdapter(private val mainFragment: MainFragment) :
    RecyclerView.Adapter<SchemeAdapter.ViewHolder>() {
    private var clickTime = 0L

    //设置item中的View
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardSchemeInfo: CardView = view.card_scheme_info
        val tvSimpleInfo: TextView = view.tv_simple_info
        val llInfoDrawer: LinearLayout = view.ll_info_drawer
        val tvDetailInfo: TextView = view.tv_detail_info
        val ibSchemeExpand: ImageButton = view.ib_scheme_expand
        val tvEnd: TextView = view.include_tv_end_scheme.tv_end
    }

    /**
     * 获取item数量
     */
    override fun getItemCount() = mainFragment.mBaiduRoutePlan.mSchemeList.size

    /**
     * 更新列表
     */
    fun updateList() = runOnUiThread { notifyDataSetChanged() }

    //获取SearchItem的数据
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val schemeItem = mainFragment.mBaiduRoutePlan.mSchemeList[position]
        holder.run {
            tvSimpleInfo.text = schemeItem.simpleInfo
            tvDetailInfo.text = schemeItem.detailInfo

            //根据保存的展开状态设置信息抽屉的高度、旋转角度和最大行数
            tvSimpleInfo.maxLines = if (schemeItem.expandFlag) {
                llInfoDrawer.setHeight(tvDetailInfo.lineHeight * (tvDetailInfo.lineCount + 1))
                ibSchemeExpand.rotation = 180f
                8
            } else {
                llInfoDrawer.setHeight(0)
                ibSchemeExpand.rotation = 0f
                1
            }

            //底部显示提示信息
            tvEnd.visibility = if (position == mainFragment.mBaiduRoutePlan.mSchemeList.size - 1)
                View.VISIBLE else View.GONE
        }
    }

    //为recyclerView的每一个item设置点击事件
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.adapter_scheme_item, parent, false)
        return ViewHolder(view).apply {
            mainFragment.run {
                //cardView的点击事件
                cardSchemeInfo.setOnClickListener {
                    if (unableToClick) return@setOnClickListener
                    infoFlag = 2 //设置信息状态为交通选择
                    bt_middle.setText(R.string.middle_button3) //设置按钮为交通选择
                    mBaiduRoutePlan.startMassTransitRoutePlan(bindingAdapterPosition)
                    schemeExpandFlag = 2 //设置方案布局为单个方案
                    ll_scheme_drawer.expandLayout(false) //收起方案抽屉
                    ll_scheme_info_layout.expandLayout(true) //展开方案信息布局
                }

                //伸展按钮的点击事件
                ibSchemeExpand.setOnClickListener {
                    val position = bindingAdapterPosition
                    val schemeItem = mBaiduRoutePlan.mSchemeList[position]
                    if (schemeItem.expandFlag) { //收起
                        tvSimpleInfo.maxLines = 1
                        llInfoDrawer.expandLayout(
                            false, tvDetailInfo, recycler_scheme_result, position
                        )
                        ibSchemeExpand.rotateExpandIcon(180f, 0f) //旋转伸展按钮
                        schemeItem.expandFlag = false
                    } else { //展开
                        tvSimpleInfo.maxLines = 8
                        llInfoDrawer.expandLayout(
                            true, tvDetailInfo, recycler_scheme_result, position
                        )
                        ibSchemeExpand.rotateExpandIcon(0f, 180f) //旋转伸展按钮
                        schemeItem.expandFlag = true
                    }
                }
            }
        }
    }

    //不允许同时点击多个item
    private val unableToClick
        get() = if (System.currentTimeMillis() - clickTime > 1000) {
            clickTime = System.currentTimeMillis()
            false
        } else true
}