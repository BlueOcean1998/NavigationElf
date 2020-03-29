package com.example.foxizz.navigation.searchdata;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.example.foxizz.navigation.R;
import com.example.foxizz.navigation.demo.Tools;
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
                mainActivity.expandSelectLayout(true);//展开选择布局
                mainActivity.expandSearchLayout(false);//收起搜索布局
                if(mainActivity.getExpandFlag())
                    mainActivity.expandSearchDrawer(true);//收起展开的搜索抽屉
                mainActivity.expandInfoLayout(true);//展开详细信息布局

                //计算详细信息布局原本的高度
                int infoLayoutHeight = (mainActivity.getInfoTargetName().getLayout().getHeight()
                        + mainActivity.getInfoAddress().getLayout().getHeight()
                        + mainActivity.getInfoDistance().getLayout().getHeight()
                        + mainActivity.getInfoOthers().getLayout().getHeight()
                        + mainActivity.getInfoButton1().getLayout().getHeight()) * 2;
                //展开动画
                Tools.getValueAnimator(mainActivity.getInfoLayout(), 0, infoLayoutHeight).start();

                int position = holder.getAdapterPosition();
                SearchItem searchItem = mSearchItemList.get(position);
                mainActivity.setSearchItemSelect(position);

                //获取定位坐标和目标坐标
                PlanNode startNode = PlanNode.withLocation(mainActivity.getLatLng());
                PlanNode endNode = PlanNode.withLocation(searchItem.getLatLng());

                switch(MainActivity.getRoutePlanSelect()) {
                    //驾车路线规划
                    case 0:
                        mainActivity.getMSearch().drivingSearch((new DrivingRoutePlanOption())
                                .from(startNode)
                                .to(endNode));
                        break;

                    //步行路线规划
                    case 1:
                        mainActivity.getMSearch().walkingSearch((new WalkingRoutePlanOption())
                                .from(startNode)
                                .to(endNode));
                        break;

                    //公交路线规划
                    case 2:
                        TransitRoutePlanOption transitRoutePlanOption = new TransitRoutePlanOption();
                        transitRoutePlanOption.city(mainActivity.getmCity());
                        transitRoutePlanOption.from(startNode);
                        transitRoutePlanOption.to(endNode);
                        mainActivity.getMSearch().transitSearch(transitRoutePlanOption);
                        break;
                }
            }
        });
        return holder;
    }

}