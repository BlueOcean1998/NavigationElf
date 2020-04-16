package com.example.foxizz.navigation.searchdata;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
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
import com.example.foxizz.navigation.R;
import com.example.foxizz.navigation.activity.MainActivity;
import com.example.foxizz.navigation.util.MyPoiSearch;

import static com.example.foxizz.navigation.demo.Tools.isAirplaneModeOn;
import static com.example.foxizz.navigation.demo.Tools.isNetworkConnected;

/**
 * 搜索到的信息列表的适配器
 */
public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    private MainActivity mainActivity;

    //构造器
    public SearchAdapter(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    //设置item中的View
    static class ViewHolder extends RecyclerView.ViewHolder {
        View view;
        CardView cardView;
        TextView targetName;
        TextView address;
        TextView distance;
        Button itemButton;
        TextView endText;

        ViewHolder(View view) {
            super(view);
            cardView = view.findViewById(R.id.card_view);
            targetName = view.findViewById(R.id.target_name);
            address = view.findViewById(R.id.address);
            distance = view.findViewById(R.id.distance);
            itemButton = view.findViewById(R.id.item_button);
            endText = view.findViewById(R.id.end_text);
        }
    }

    //获取item数量
    @Override
    public int getItemCount() {
        return mainActivity.searchList.size();
    }

    //获取SearchItem的数据
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SearchItem searchItem = mainActivity.searchList.get(position);
        holder.targetName.setText(searchItem.getTargetName());
        holder.address.setText(searchItem.getAddress());
        holder.distance.setText(searchItem.getDistance() + "km");

        //底部显示提示信息
        if(position == mainActivity.searchList.size() - 1)
            holder.endText.setVisibility(View.VISIBLE);
        else
            holder.endText.setVisibility(View.GONE);
    }

    //为recyclerView的每一个item设置点击事件
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);

        //cardView的点击事件
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onClick(View view) {
                if(isNetworkConnected(mainActivity)) {
                    if(isAirplaneModeOn(mainActivity)) {
                        Toast.makeText(mainActivity, mainActivity.getString(R.string.close_airplane_mode), Toast.LENGTH_SHORT).show();
                    } else {
                        click(holder);

                        mainActivity.infoButton.setText(R.string.info_button1);//设置按钮为路线
                        mainActivity.expandInfoLayout(true);//展开详细信息布局
                        mainActivity.infoFlag = true;//设置信息状态为详细信息

                        //获取点击的item
                        int position = holder.getAdapterPosition();
                        SearchItem searchItem = mainActivity.searchList.get(position);

                        //移动视角到指定位置
                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        builder.include(searchItem.getLatLng());
                        MapStatusUpdate msu= MapStatusUpdateFactory.newLatLngBounds(builder.build());
                        mainActivity.mBaiduMap.setMapStatus(msu);

                        //清空地图上的所有标记点和绘制的路线
                        mainActivity.mBaiduMap.clear();
                        //构建Marker图标
                        BitmapDescriptor bitmap = BitmapDescriptorFactory
                                .fromResource(R.drawable.ic_to_location);
                        //构建MarkerOption，用于在地图上添加Marker
                        OverlayOptions option = new MarkerOptions()
                                .position(searchItem.getLatLng())
                                .icon(bitmap);
                        //在地图上添加Marker，并显示
                        mainActivity.mBaiduMap.addOverlay(option);
                    }
                } else {
                    Toast.makeText(mainActivity, mainActivity.getString(R.string.network_error), Toast.LENGTH_SHORT).show();
                }

            }
        });

        //itemButton的点击事件
        holder.itemButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onClick(View view) {
                if(isNetworkConnected(mainActivity)) {
                    if(isAirplaneModeOn(mainActivity)) {
                        Toast.makeText(mainActivity, mainActivity.getString(R.string.close_airplane_mode), Toast.LENGTH_SHORT).show();
                    } else {
                        click(holder);

                        mainActivity.infoButton.setText(R.string.info_button2);//设置按钮为详细信息
                        mainActivity.expandSelectLayout(true);//展开选择布局
                        mainActivity.infoFlag = false;//设置信息状态为交通选择

                        //重置交通类型为步行
                        mainActivity.routePlanSelect = MainActivity.WALKING;
                        mainActivity.selectButton1.setBackgroundResource(R.drawable.button_background_gray);
                        mainActivity.selectButton2.setBackgroundResource(R.drawable.button_background_black);
                        mainActivity.selectButton3.setBackgroundResource(R.drawable.button_background_gray);
                        mainActivity.selectButton4.setBackgroundResource(R.drawable.button_background_gray);

                        mainActivity.myRoutePlanSearch.startRoutePlanSearch();//开始路线规划
                    }
                } else {
                    Toast.makeText(mainActivity, mainActivity.getString(R.string.network_error), Toast.LENGTH_SHORT).show();
                }
            }
        });

        //cardView的长按事件
        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final int position = holder.getAdapterPosition();
                final SearchItem searchItem = mainActivity.searchList.get(position);

                if(mainActivity.isHistorySearchResult) {//如果是搜索历史记录
                    AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
                    builder.setTitle(mainActivity.getString(R.string.hint));
                    builder.setMessage("你确定要删除'" + searchItem.getTargetName() + "'吗？");

                    builder.setPositiveButton(mainActivity.getString(R.string.delete), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //在searchList中寻找这条记录
                            for(int i = 0; i < mainActivity.searchList.size(); i++) {
                                if(searchItem.getUid().equals(mainActivity.searchList.get(i).getUid())) {
                                    mainActivity.searchList.remove(searchItem);//移除搜索列表的这条记录
                                    notifyItemRemoved(i);//通知adapter移除这条记录
                                }
                            }

                            mainActivity.dbHelper.deleteSearchData(searchItem.getUid());//删除数据库中的搜索记录
                        }
                    });

                    builder.setNegativeButton(mainActivity.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //do nothing
                        }
                    });

                    builder.show();

                } else {//如果不是
                    //在searchList中寻找这条记录
                    for(int i = 0; i < mainActivity.searchList.size(); i++) {
                        if(searchItem.getUid().equals(mainActivity.searchList.get(i).getUid())) {
                            mainActivity.searchList.remove(searchItem);//移除搜索列表的这条记录
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
    @SuppressLint("SetTextI18n")
    private void click(ViewHolder holder) {
        //获取点击的item
        int position = holder.getAdapterPosition();
        SearchItem searchItem = mainActivity.searchList.get(position);

        mainActivity.expandSearchLayout(false);//收起搜索布局
        if(mainActivity.expandFlag) {
            mainActivity.expandSearchDrawer(false);//收起展开的搜索抽屉
            mainActivity.expandFlag = false;//设置状态为收起
        }
        mainActivity.expandStartLayout(true);//展开开始导航布局

        //设置终点坐标
        mainActivity.endLocation = searchItem.getLatLng();

        mainActivity.myPoiSearch.poiSearchType = MyPoiSearch.DETAIL_SEARCH;//设置为直接详细搜索
        mainActivity.mPoiSearch.searchPoiDetail(//进行详细信息搜索
                (new PoiDetailSearchOption()).poiUids(searchItem.getUid()));
    }

}