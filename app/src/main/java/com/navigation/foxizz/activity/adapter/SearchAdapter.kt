package com.navigation.foxizz.activity.adapter

import android.annotation.SuppressLint
import android.view.*
import android.view.View.OnLongClickListener
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
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
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
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
    override fun getItemCount(): Int {
        return mainFragment.mBaiduSearch.mSearchList.size
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
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val searchItem = mainFragment.mBaiduSearch.mSearchList[position]
        holder.tvTargetName.text = searchItem.targetName
        holder.tvAddress.text = searchItem.address
        holder.tvDistance.text = searchItem.distance.toString() + "km"

        //加载更多搜索结果
        if (position == itemCount - 4 && !mainFragment.mBaiduSearch.isSearching
                && !mainFragment.isHistorySearchResult
                && mainFragment.mBaiduSearch.mCurrentPage < mainFragment.mBaiduSearch.mTotalPage)
            mainFragment.mBaiduSearch.startSearch(mainFragment.mBaiduSearch.mCurrentPage)

        //设置提示信息的内容
        if (!mainFragment.isHistorySearchResult
                && mainFragment.mBaiduSearch.mCurrentPage < mainFragment.mBaiduSearch.mTotalPage)
            holder.tvEnd.text = mainFragment.getString(R.string.loading)
        else holder.tvEnd.text = mainFragment.getString(R.string.no_more)

        //只有底部显示提示信息
        if (position == mainFragment.mBaiduSearch.mSearchList.size - 1) {
            holder.tvEnd.visibility = View.VISIBLE
        } else holder.tvEnd.visibility = View.GONE
    }

    //为recyclerView的每一个item设置点击事件
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.adapter_search_item, parent, false)
        val holder = ViewHolder(view)

        //cardView的点击事件
        holder.cardSearchInfo.setOnClickListener {
            if (unableToClick()) return@setOnClickListener
            if (!NetworkUtil.isNetworkConnected) { //没有网络连接
                R.string.network_error.showToast()
                return@setOnClickListener
            }
            if (NetworkUtil.isAirplaneModeOn) { //没有关飞行模式
                R.string.close_airplane_mode.showToast()
                return@setOnClickListener
            }
            click(holder)
            mainFragment.ll_search_info_layout.expandLayout(true) //展开详细信息布局
            mainFragment.infoFlag = 0 //设置信息状态为详细信息
            mainFragment.bt_middle.setText(R.string.middle_button1) //设置按钮为路线

            //获取点击的item
            val position = holder.adapterPosition
            val searchItem = mainFragment.mBaiduSearch.mSearchList[position]

            //移动视角到指定位置
            val builder = LatLngBounds.Builder()
            builder.include(searchItem.latLng)
            val msu = MapStatusUpdateFactory.newLatLngBounds(builder.build())
            mainFragment.mBaiduMap.setMapStatus(msu)

            //清空地图上的所有标记点和绘制的路线
            mainFragment.mBaiduMap.clear()
            //构建Marker图标
            val bitmap = BitmapDescriptorFactory
                    .fromResource(R.drawable.ic_to_location)
            //构建MarkerOption，用于在地图上添加Marker
            val option: OverlayOptions = MarkerOptions()
                    .position(searchItem.latLng)
                    .icon(bitmap)
            //在地图上添加Marker，并显示
            mainFragment.mBaiduMap.addOverlay(option)
        }

        //btItem的点击事件
        holder.btItem.setOnClickListener {
            if (unableToClick()) return@setOnClickListener
            if (!NetworkUtil.isNetworkConnected) { //没有网络连接
                R.string.network_error.showToast()
                return@setOnClickListener
            }
            if (NetworkUtil.isAirplaneModeOn) { //没有关飞行模式
                R.string.close_airplane_mode.showToast()
                return@setOnClickListener
            }
            click(holder)
            mainFragment.infoFlag = 1 //设置信息状态为交通选择
            mainFragment.bt_middle.setText(R.string.middle_button2) //设置按钮为详细信息
            mainFragment.ll_select_layout.expandLayout(true) //展开选择布局
            mainFragment.mBaiduRoutePlan.startRoutePlanSearch() //开始路线规划
        }

        //cardView的长按事件
        holder.cardSearchInfo.setOnLongClickListener(OnLongClickListener {
            if (unableToClick()) return@OnLongClickListener false
            val position = holder.adapterPosition
            val searchItem = mainFragment.mBaiduSearch.mSearchList[position]
            if (mainFragment.isHistorySearchResult) { //如果是搜索历史记录
                showDeleteSearchDataDialog(searchItem, position) //显示删除搜索记录对话框
            } else { //如果不是
                mainFragment.mBaiduSearch.mSearchList.remove(searchItem) //移除搜索列表的这条记录
                notifyItemRemoved(position) //通知adapter移除这条记录
                SearchDataHelper.deleteSearchData(searchItem.uid) //删除数据库中的搜索记录
            }
            false
        })
        return holder
    }

    //cardView和itemButton共同的点击事件
    private fun click(holder: ViewHolder) {
        //获取点击的item
        val position = holder.adapterPosition
        val searchItem = mainFragment.mBaiduSearch.mSearchList[position]
        mainFragment.searchLayoutFlag = false //设置搜索布局为收起
        mainFragment.ll_search_layout.expandLayout(false) //收起搜索布局
        if (mainFragment.searchExpandFlag) { //如果搜索抽屉展开
            mainFragment.searchExpandFlag = false //设置搜索抽屉为收起
            mainFragment.expandSearchDrawer(false) //收起搜索抽屉
        }
        mainFragment.ll_start_layout.expandLayout(true) //展开开始导航布局

        //设置终点坐标
        mainFragment.mBaiduRoutePlan.mEndLocation = searchItem.latLng

        //加载详细信息
        mainFragment.include_search_info_loading.visibility = View.VISIBLE
        mainFragment.sv_search_info.visibility = View.GONE
        mainFragment.mBaiduSearch.mSearchType = BaiduSearch.DETAIL_SEARCH //设置为直接详细搜索
        mainFragment.mBaiduSearch.isFirstDetailSearch = true //第一次详细信息搜索
        mainFragment.mBaiduSearch.mPoiSearch.searchPoiDetail( //开始POI详细信息搜索
                PoiDetailSearchOption().poiUids(searchItem.uid))
        mainFragment.takeBackKeyboard() //收回键盘
    }

    //显示删除搜索记录对话框
    private fun showDeleteSearchDataDialog(searchItem: SearchItem, position: Int) {
        val builder = AlertDialog.Builder(mainFragment.requireActivity())
        builder.setTitle(R.string.hint)
        builder.setMessage("你确定要删除'" + searchItem.targetName + "'吗？")
        builder.setPositiveButton(R.string.delete) { _, _ ->
            mainFragment.mBaiduSearch.mSearchList.remove(searchItem) //移除搜索列表的这条记录
            notifyItemRemoved(position) //通知adapter移除这条记录
            SearchDataHelper.deleteSearchData(searchItem.uid) //删除数据库中的搜索记录
            R.string.has_deleted.showToast()
        }
        builder.setNegativeButton(R.string.cancel) { _, _ ->
            //do nothing
        }
        builder.show()
    }

    //不允许同时点击多个item
    private fun unableToClick(): Boolean {
        return if (System.currentTimeMillis() - clickTime > 1000) {
            clickTime = System.currentTimeMillis()
            false
        } else true
    }
}