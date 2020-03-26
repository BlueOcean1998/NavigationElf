package com.example.foxizz.navigation.util;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.example.foxizz.navigation.R;

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
        Button itemButton1;

        ViewHolder(View view) {
            super(view);
            cardView = view;
            targetName = view.findViewById(R.id.target_name);
            address = view.findViewById(R.id.address);
            distance = view.findViewById(R.id.distance);
            itemButton1 = view.findViewById(R.id.item_button1);
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

        //itemButton1的点击事件
        holder.itemButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //获取导航布局原本的高度
                int bodyHeight = mainActivity.getSearchButton1().getLayout().getHeight() * 2;
                //获取并开始动画
                mainActivity.getValueAnimator(mainActivity.getSelectLayout(), 0, bodyHeight).start();

                int position = holder.getAdapterPosition();
                SearchItem searchItem = mSearchItemList.get(position);

                //获取定位点和目标点并开始步行路线规划
                PlanNode startNode = PlanNode.withLocation(mainActivity.getLatLng());
                PlanNode endNode = PlanNode.withLocation(searchItem.getLatLng());
                mainActivity.getMSearch().walkingSearch((new WalkingRoutePlanOption())
                        .from(startNode)
                        .to(endNode));

            }
        });
        return holder;
    }

}