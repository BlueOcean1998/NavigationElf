package com.navigation.foxizz.activity.adapter

import android.annotation.SuppressLint
import android.view.*
import android.view.View.OnLongClickListener
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import base.foxizz.mlh
import base.foxizz.util.NetworkUtil
import base.foxizz.util.expandLayout
import base.foxizz.util.showToast
import com.baidu.mapapi.map.BitmapDescriptorFactory
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.baidu.mapapi.map.MarkerOptions
import com.baidu.mapapi.map.OverlayOptions
import com.baidu.mapapi.model.LatLngBounds
import com.baidu.mapapi.search.poi.PoiDetailSearchOption
import com.navigation.foxizz.R
import com.navigation.foxizz.activity.fragment.MainFragment
import com.navigation.foxizz.data.SearchDataHelper
import com.navigation.foxizz.data.SearchItem
import com.navigation.foxizz.mybaidumap.BaiduSearch
import com.navigation.foxizz.util.*
import kotlinx.android.synthetic.main.adapter_search_item.view.*
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.include_tv_end.view.*

/**
 * 搜索到的信息列表的适配器
 */
class SearchAdapter(private val mainFragment: MainFragment) : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {
    private var clickTime = 0L

    //设置item中的View
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardSearchInfo: CardView = view.card_search_info
        val tvTargetName: TextView = view.tv_target_name
        val tvAddress: TextView = view.tv_address
        val tvDistance: TextView = view.tv_distance
        val btItem: TextView = view.bt_item
        val tvEnd: TextView = view.include_tv_end_search.tv_end
    }

    /**
     * 获取item数量
     *
     * @return item数量
     */
    override fun getItemCount() = mainFragment.mBaiduSearch.mSearchList.size

    /**
     * 更新列表
     */
    fun updateList() = mlh.post { notifyDataSetChanged() }

    //获取SearchItem的数据
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.run {
            mainFragment.run {
                mBaiduSearch.run {
                    val searchItem = mSearchList[position]
                    tvTargetName.text = searchItem.targetName
                    tvAddress.text = searchItem.address
                    tvDistance.text = searchItem.distance.toString() + "km"

                    //加载更多搜索结果
                    if (position == itemCount - 4 && !isSearching
                            && !isHistorySearchResult && mCurrentPage < mTotalPage)
                        startSearch(mCurrentPage)

                    //设置提示信息的内容
                    if (!isHistorySearchResult && mCurrentPage < mTotalPage)
                        tvEnd.text = getString(R.string.loading)
                    else tvEnd.text = getString(R.string.no_more)

                    //只有底部显示提示信息
                    if (position == mSearchList.size - 1)
                        tvEnd.visibility = View.VISIBLE
                    else tvEnd.visibility = View.GONE
                }
            }
        }
    }

    //为recyclerView的每一个item设置点击事件
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.adapter_search_item, parent, false)
        val holder = ViewHolder(view)

        holder.run {
            mainFragment.run {
                mBaiduSearch.run {
                    //cardView的点击事件
                    cardSearchInfo.setOnClickListener {
                        if (unableToClick()) return@setOnClickListener
                        if (!NetworkUtil.isNetworkConnected) { //没有网络连接
                            showToast(R.string.network_error)
                            return@setOnClickListener
                        }
                        if (NetworkUtil.isAirplaneModeEnable) { //没有关飞行模式
                            showToast(R.string.close_airplane_mode)
                            return@setOnClickListener
                        }
                        click(holder)
                        ll_search_info_layout.expandLayout(true) //展开详细信息布局
                        infoFlag = 0 //设置信息状态为详细信息
                        bt_middle.setText(R.string.middle_button1) //设置按钮为路线

                        //获取点击的item
                        val searchItem = mSearchList[adapterPosition]

                        //移动视角到指定位置
                        val builder = LatLngBounds.Builder()
                        builder.include(searchItem.latLng)
                        val msu = MapStatusUpdateFactory.newLatLngBounds(builder.build())
                        mBaiduMap.setMapStatus(msu)

                        //清空地图上的所有标记点和绘制的路线
                        mBaiduMap.clear()
                        //构建Marker图标
                        val bitmap = BitmapDescriptorFactory
                                .fromResource(R.drawable.ic_to_location)
                        //构建MarkerOption，用于在地图上添加Marker
                        val option: OverlayOptions = MarkerOptions()
                                .position(searchItem.latLng)
                                .icon(bitmap)
                        //在地图上添加Marker，并显示
                        mBaiduMap.addOverlay(option)
                    }

                    //btItem的点击事件
                    btItem.setOnClickListener {
                        if (unableToClick()) return@setOnClickListener
                        if (!NetworkUtil.isNetworkConnected) { //没有网络连接
                            showToast(R.string.network_error)
                            return@setOnClickListener
                        }
                        if (NetworkUtil.isAirplaneModeEnable) { //没有关飞行模式
                            showToast(R.string.close_airplane_mode)
                            return@setOnClickListener
                        }
                        click(holder)
                        infoFlag = 1 //设置信息状态为交通选择
                        bt_middle.setText(R.string.middle_button2) //设置按钮为详细信息
                        ll_select_layout.expandLayout(true) //展开选择布局
                        mBaiduRoutePlan.startRoutePlanSearch() //开始路线规划
                    }

                    //cardView的长按事件
                    cardSearchInfo.setOnLongClickListener(OnLongClickListener {
                        if (unableToClick()) return@OnLongClickListener false
                        val searchItem = mSearchList[adapterPosition]
                        if (isHistorySearchResult) { //如果是搜索历史记录
                            showDeleteSearchDataDialog(searchItem, adapterPosition) //显示删除搜索记录对话框
                        } else { //如果不是
                            mSearchList.remove(searchItem) //移除搜索列表的这条记录
                            notifyItemRemoved(position) //通知adapter移除这条记录
                            SearchDataHelper.deleteSearchData(searchItem.uid) //删除数据库中的搜索记录
                        }
                        false
                    })
                }
            }
            return this
        }
    }

    //cardView和itemButton共同的点击事件
    private fun click(holder: ViewHolder) {
        mainFragment.run {
            mBaiduSearch.run {
                //获取点击的item
                val searchItem = mSearchList[holder.adapterPosition]
                searchLayoutFlag = false //设置搜索布局为收起
                ll_search_layout.expandLayout(false) //收起搜索布局
                if (searchExpandFlag) { //如果搜索抽屉展开
                    searchExpandFlag = false //设置搜索抽屉为收起
                    expandSearchDrawer(false) //收起搜索抽屉
                }
                ll_start_layout.expandLayout(true) //展开开始导航布局

                //设置终点坐标
                mBaiduRoutePlan.mEndLocation = searchItem.latLng

                //加载详细信息
                include_search_info_loading.visibility = View.VISIBLE
                sv_search_info.visibility = View.GONE
                mSearchType = BaiduSearch.DETAIL_SEARCH //设置为直接详细搜索
                isFirstDetailSearch = true //第一次详细信息搜索
                mPoiSearch.searchPoiDetail( //开始POI详细信息搜索
                        PoiDetailSearchOption().poiUids(searchItem.uid))
                takeBackKeyboard() //收回键盘
            }
        }
    }

    //显示删除搜索记录对话框
    private fun showDeleteSearchDataDialog(searchItem: SearchItem, position: Int) {
        AlertDialog.Builder(mainFragment.requireActivity())
                .setTitle(R.string.hint)
                .setMessage("你确定要删除'" + searchItem.targetName + "'吗？")
                .setPositiveButton(R.string.delete) { _, _ ->
                    mainFragment.mBaiduSearch.mSearchList.remove(searchItem) //移除搜索列表的这条记录
                    notifyItemRemoved(position) //通知adapter移除这条记录
                    SearchDataHelper.deleteSearchData(searchItem.uid) //删除数据库中的搜索记录
                    showToast(R.string.has_deleted)
                }
                .setNegativeButton(R.string.cancel) { _, _ ->
                    //do nothing
                }
                .show()
    }

    //不允许同时点击多个item
    private fun unableToClick() = if (System.currentTimeMillis() - clickTime > 1000) {
        clickTime = System.currentTimeMillis()
        false
    } else true
}