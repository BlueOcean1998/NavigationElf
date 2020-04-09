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
import androidx.recyclerview.widget.RecyclerView;

import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.example.foxizz.navigation.R;
import com.example.foxizz.navigation.activity.MainActivity;
import com.example.foxizz.navigation.util.MyRoutePlanSearch;

import java.util.List;

import static com.example.foxizz.navigation.demo.Tools.isAirplaneModeOn;
import static com.example.foxizz.navigation.demo.Tools.isNetworkConnected;

/**
 * 搜索到的信息列表的适配器
 */
public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    private MainActivity mainActivity;
    private List<SearchItem> mSearchItemList;

    //构造器
    public SearchAdapter(MainActivity mainActivity, List<SearchItem> searchItemList) {
        this.mainActivity = mainActivity;
        mSearchItemList = searchItemList;
    }

    //设置item中的View
    static class ViewHolder extends RecyclerView.ViewHolder {
        View cardView;
        TextView targetName;
        TextView address;
        TextView distance;
        Button itemButton;

        ViewHolder(View view) {
            super(view);
            cardView = view;
            targetName = view.findViewById(R.id.target_name);
            address = view.findViewById(R.id.address);
            distance = view.findViewById(R.id.distance);
            itemButton = view.findViewById(R.id.item_button);
        }
    }

    //获取item数量
    @Override
    public int getItemCount() {
        return mSearchItemList.size();
    }

    //获取SearchItem的数据
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SearchItem dataItem = mSearchItemList.get(position);
        holder.targetName.setText(dataItem.getTargetName());
        holder.address.setText(dataItem.getAddress());
        holder.distance.setText(dataItem.getDistance() + "km");
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
                        mainActivity.infoFlag = true;//设置信息状态为展开
                    }
                } else {
                    Toast.makeText(mainActivity, mainActivity.getString(R.string.network_error), Toast.LENGTH_SHORT).show();
                }

            }
        });

        //itemButton1的点击事件
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
                        mainActivity.infoFlag = false;//设置信息状态为收起

                        MyRoutePlanSearch myRoutePlanSearch = new MyRoutePlanSearch(mainActivity);
                        myRoutePlanSearch.startRoutePlanSearch();//开始路线规划
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
                final SearchItem searchItem = mSearchItemList.get(position);

                if(mainActivity.isHistorySearchResult) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
                    builder.setTitle("提示");
                    builder.setMessage("你确定要删除这条记录吗？");

                    builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mSearchItemList.remove(position);//移除搜索列表的这条记录
                            notifyItemRemoved(position);//通知adapter移除这条记录

                            mainActivity.dbHelper.deleteSearchData(searchItem.getUid());//删除数据库中的搜索记录
                        }
                    });

                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //do nothing
                        }
                    });

                    builder.show();
                } else {
                    mainActivity.searchList.remove(position);//移除搜索列表的这条记录
                    notifyItemRemoved(position);//通知adapter移除这条记录
                }

                return false;
            }
        });

        return holder;
    }

    //cardView和itemButton1共同的点击事件
    @SuppressLint("SetTextI18n")
    private void click(ViewHolder holder) {
        int position = holder.getAdapterPosition();

        mainActivity.searchItemSelect = position;//设置item选择

        mainActivity.expandSearchLayout(false);//收起搜索布局
        if(mainActivity.expandFlag) {
            mainActivity.expandSearchDrawer(false);//收起展开的搜索抽屉
            mainActivity.expandFlag = false;//设置状态为收起
        }
        mainActivity.expandStartLayout(true);//展开开始导航布局

        SearchItem searchItem = mSearchItemList.get(position);

        mainActivity.myPoiSearch.detailPoiSearch();//设置为直接详细搜索
        mainActivity.mPoiSearch.searchPoiDetail(//进行详细信息搜索
                (new PoiDetailSearchOption()).poiUids(searchItem.getUid()));
    }

}