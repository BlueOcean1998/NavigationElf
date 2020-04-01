package com.example.foxizz.navigation.searchdata;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foxizz.navigation.R;
import com.example.foxizz.navigation.util.MainActivity;

import java.util.List;

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
            @Override
            public void onClick(View view) {
                click(holder);

                mainActivity.getInfoButton().setText(R.string.info_button1);//设置按钮为路线
                mainActivity.expandInfoLayout(true);//展开详细信息布局
                mainActivity.setInfoFlag(true);//设置信息状态为展开
            }
        });

        //itemButton1的点击事件
        holder.itemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                click(holder);

                mainActivity.getInfoButton().setText(R.string.info_button2);//设置按钮为详细信息
                mainActivity.expandSelectLayout(true);//展开选择布局
                mainActivity.setInfoFlag(false);//设置信息状态为收起

                mainActivity.startRoutePlanSearch();//开始路线规划
            }
        });

        //cardView的长按事件
        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {


                return false;
            }
        });

        return holder;
    }

    @SuppressLint("SetTextI18n")
    private void click(ViewHolder holder) {
        int position = holder.getAdapterPosition();
        SearchItem searchItem = mSearchItemList.get(position);

        mainActivity.setSearchItemSelect(position);//设置item选择

        //设置详细信息内容
        mainActivity.getInfoTargetName().setText(searchItem.getTargetName());
        mainActivity.getInfoAddress().setText(searchItem.getAddress());
        mainActivity.getInfoDistance().setText(searchItem.getDistance() + "km");
        mainActivity.getInfoOthers().setText(searchItem.getOtherInfo());

        mainActivity.expandSearchLayout(false);//收起搜索布局
        if(MainActivity.getExpandFlag()) {
            mainActivity.expandSearchDrawer(false);//收起展开的搜索抽屉
            MainActivity.setExpandFlag(false);//设置状态为收起
        }
        mainActivity.expandStartLayout(true);//展开开始导航布局
    }

}