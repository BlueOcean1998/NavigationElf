package com.navigation.foxizz.activity.adapter;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.navigation.foxizz.R;
import com.navigation.foxizz.activity.fragment.MainFragment;
import com.navigation.foxizz.data.SearchDataHelper;
import com.navigation.foxizz.data.SearchItem;
import com.navigation.foxizz.mybaidumap.MySearch;
import com.navigation.foxizz.util.LayoutUtil;
import com.navigation.foxizz.util.NetworkUtil;
import com.navigation.foxizz.util.ToastUtil;

/**
 * 搜索到的信息列表的适配器
 */
public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    private final MainFragment mainFragment;
    public SearchAdapter(MainFragment mainFragment) {
        this.mainFragment = mainFragment;
    }

    private long clickTime = 0;

    //设置item中的View
    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardSearchInfo;
        TextView tvTargetName;
        TextView tvAddress;
        TextView tvDistance;
        Button btItem;
        TextView tvEnd;

        ViewHolder(View view) {
            super(view);
            cardSearchInfo = view.findViewById(R.id.card_search_info);
            tvTargetName = view.findViewById(R.id.tv_target_name);
            tvAddress = view.findViewById(R.id.tv_address);
            tvDistance = view.findViewById(R.id.tv_distance);
            btItem = view.findViewById(R.id.bt_item);
            tvEnd = view.findViewById(R.id.include_tv_end_search).findViewById(R.id.tv_end);
        }
    }

    /**
     * 获取item数量
     *
     * @return int
     */
    @Override
    public int getItemCount() {
        return mainFragment.searchList.size();
    }

    /**
     * 更新列表
     */
    public void updateList() {
        mainFragment.requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    //获取SearchItem的数据
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SearchItem searchItem = mainFragment.searchList.get(position);
        holder.tvTargetName.setText(searchItem.getTargetName());
        holder.tvAddress.setText(searchItem.getAddress());
        holder.tvDistance.setText(searchItem.getDistance() + "km");

        //加载更多搜索结果
        if (position == getItemCount() - 4
                && !mainFragment.mySearch.isSearching
                && !mainFragment.isHistorySearchResult
                && mainFragment.currentPage < mainFragment.totalPage)
            mainFragment.mySearch.startSearch(mainFragment.currentPage);

        //设置提示信息的内容
        if (!mainFragment.isHistorySearchResult
                && mainFragment.currentPage < mainFragment.totalPage)
            holder.tvEnd.setText(mainFragment.getString(R.string.loading));
        else holder.tvEnd.setText(mainFragment.getString(R.string.no_more));

        //只有底部显示提示信息
        if (position == mainFragment.searchList.size() - 1) {
            holder.tvEnd.setVisibility(View.VISIBLE);
        } else holder.tvEnd.setVisibility(View.GONE);
    }

    //为recyclerView的每一个item设置点击事件
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_search_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);

        //cardView的点击事件
        holder.cardSearchInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (unableToClick()) return;

                if (!NetworkUtil.isNetworkConnected()) {//没有网络连接
                    ToastUtil.showToast(R.string.network_error);
                    return;
                }

                if (NetworkUtil.isAirplaneModeOn()) {//没有关飞行模式
                    ToastUtil.showToast(R.string.close_airplane_mode);
                    return;
                }

                click(holder);

                LayoutUtil.expandLayout(mainFragment.llSearchInfoLayout, true);//展开详细信息布局

                mainFragment.btMiddle.setText(R.string.middle_button1);//设置按钮为路线
                mainFragment.infoFlag = true;//设置信息状态为详细信息

                //获取点击的item
                int position = holder.getAdapterPosition();
                SearchItem searchItem = mainFragment.searchList.get(position);

                //移动视角到指定位置
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(searchItem.getLatLng());
                MapStatusUpdate msu = MapStatusUpdateFactory.newLatLngBounds(builder.build());
                mainFragment.mBaiduMap.setMapStatus(msu);

                //清空地图上的所有标记点和绘制的路线
                mainFragment.mBaiduMap.clear();
                //构建Marker图标
                BitmapDescriptor bitmap = BitmapDescriptorFactory
                        .fromResource(R.drawable.ic_to_location);
                //构建MarkerOption，用于在地图上添加Marker
                OverlayOptions option = new MarkerOptions()
                        .position(searchItem.getLatLng())
                        .icon(bitmap);
                //在地图上添加Marker，并显示
                mainFragment.mBaiduMap.addOverlay(option);
            }
        });

        //btItem的点击事件
        holder.btItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (unableToClick()) return;

                if (!NetworkUtil.isNetworkConnected()) {//没有网络连接
                    ToastUtil.showToast(R.string.network_error);
                    return;
                }
                if (NetworkUtil.isAirplaneModeOn()) {//没有关飞行模式
                    ToastUtil.showToast(R.string.close_airplane_mode);
                    return;
                }

                click(holder);

                LayoutUtil.expandLayout(mainFragment.llSelectLayout, true);//展开选择布局
                mainFragment.btMiddle.setText(R.string.middle_button2);//设置按钮为详细信息
                mainFragment.infoFlag = false;//设置信息状态为交通选择

                mainFragment.myRoutePlanSearch.startRoutePlanSearch();//开始路线规划
            }
        });

        //cardView的长按事件
        holder.cardSearchInfo.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (unableToClick()) return false;

                final int position = holder.getAdapterPosition();
                final SearchItem searchItem = mainFragment.searchList.get(position);

                if (mainFragment.isHistorySearchResult) {//如果是搜索历史记录
                    showDeleteSearchDataDialog(searchItem);//显示删除搜索记录对话框
                } else {//如果不是
                    //在searchList中寻找这条记录
                    for (int i = 0; i < mainFragment.searchList.size(); i++) {
                        if (searchItem.getUid().equals(mainFragment.searchList.get(i).getUid())) {
                            mainFragment.searchList.remove(searchItem);//移除搜索列表的这条记录
                            notifyItemRemoved(i);//通知adapter移除这条记录
                        }
                    }
                }

                return false;
            }
        });

        return holder;
    }

    //cardView和itemButton共同的点击事件
    private void click(ViewHolder holder) {
        //获取点击的item
        int position = holder.getAdapterPosition();
        SearchItem searchItem = mainFragment.searchList.get(position);

        LayoutUtil.expandLayout(mainFragment.llSearchLayout, false);//收起搜索布局
        if (mainFragment.searchExpandFlag) {//如果状态为展开
            mainFragment.expandSearchDrawer(false);//收起展开的搜索抽屉
            mainFragment.searchExpandFlag = false;//设置状态为收起
        }
        LayoutUtil.expandLayout(mainFragment.llStartLayout, true);//展开开始导航布局

        //设置终点坐标
        mainFragment.endLocation = searchItem.getLatLng();

        //加载详细信息
        mainFragment.llSearchInfoLoading.setVisibility(View.VISIBLE);
        mainFragment.svSearchInfo.setVisibility(View.GONE);

        mainFragment.mySearch.searchType = MySearch.DETAIL_SEARCH;//设置为直接详细搜索
        mainFragment.mySearch.isFirstDetailSearch = true;//第一次详细信息搜索
        mainFragment.mPoiSearch.searchPoiDetail(//开始POI详细信息搜索
                (new PoiDetailSearchOption()).poiUids(searchItem.getUid()));

        mainFragment.takeBackKeyboard();//收回键盘
    }

    //显示删除搜索记录对话框
    private void showDeleteSearchDataDialog(final SearchItem searchItem) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mainFragment.requireActivity());
        builder.setTitle(R.string.hint);
        builder.setMessage("你确定要删除'" + searchItem.getTargetName() + "'吗？");

        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //在searchList中寻找这条记录
                for (int i = 0; i < mainFragment.searchList.size(); i++) {
                    if (searchItem.getUid().equals(mainFragment.searchList.get(i).getUid())) {
                        mainFragment.searchList.remove(searchItem);//移除搜索列表的这条记录
                        notifyItemRemoved(i);//通知adapter移除这条记录
                    }
                }

                SearchDataHelper.deleteSearchData(searchItem.getUid());//删除数据库中的搜索记录

                ToastUtil.showToast(R.string.has_deleted);
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                //do nothing
            }
        });

        builder.show();
    }

    //不允许同时点击多个item
    private boolean unableToClick() {
        if ((System.currentTimeMillis() - clickTime) > 1000) {
            clickTime = System.currentTimeMillis();
            return false;
        } else return true;
    }

}