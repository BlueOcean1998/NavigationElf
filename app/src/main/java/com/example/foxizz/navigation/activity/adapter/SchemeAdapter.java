package com.example.foxizz.navigation.activity.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.search.route.MassTransitRouteLine;
import com.example.foxizz.navigation.R;
import com.example.foxizz.navigation.activity.fragment.MainFragment;
import com.example.foxizz.navigation.data.SchemeItem;
import com.example.foxizz.navigation.mybaidumap.overlayutil.MassTransitRouteOverlay;

import java.util.List;

import static com.example.foxizz.navigation.mybaidumap.MyApplication.getContext;
import static com.example.foxizz.navigation.util.LayoutUtil.expandLayout;
import static com.example.foxizz.navigation.util.LayoutUtil.getValueAnimator;
import static com.example.foxizz.navigation.util.LayoutUtil.rotateExpandIcon;

/**
 * 路线规划信息列表的适配器
 */
public class SchemeAdapter extends RecyclerView.Adapter<SchemeAdapter.ViewHolder> {

    private MainFragment mainFragment;
    private long clickTime = 0;

    public SchemeAdapter(MainFragment mainFragment) {
        this.mainFragment = mainFragment;
    }

    //获取item数量
    @Override
    public int getItemCount() {
        return mainFragment.schemeList.size();
    }

    //获取SearchItem的数据
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SchemeItem schemeItem = mainFragment.schemeList.get(position);
        holder.simpleInfo.setText(schemeItem.getSimpleInfo());
        holder.detailInfo.setText(schemeItem.getDetailInfo());

        //根据保存的展开状态设置信息抽屉的高度和旋转角度
        if (schemeItem.getExpandFlag()) {
            holder.infoDrawer.getLayoutParams().height
                    = holder.detailInfo.getLineHeight() * (holder.detailInfo.getLineCount() + 1);
            holder.schemeExpand.setRotation(180);
        } else {
            holder.infoDrawer.getLayoutParams().height = 0;
            holder.schemeExpand.setRotation(0);
        }

        //底部显示提示信息
        if (position == mainFragment.schemeList.size() - 1)
            holder.endText.setVisibility(View.VISIBLE);
        else
            holder.endText.setVisibility(View.GONE);
    }

    //为recyclerView的每一个item设置点击事件
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.scheme_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);

        //cardView的点击事件
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                if (unableToClick()) return;

                int position = holder.getAdapterPosition();
                SchemeItem schemeItem = mainFragment.schemeList.get(position);

                expandLayout(mainFragment.schemeDrawer, false);//收起方案抽屉
                expandLayout(mainFragment.schemeInfoDrawer, true);//展开方案信息抽屉
                expandLayout(mainFragment.startLayout, true);//展开开始导航布局

                //调整方案布局的高度
                getValueAnimator(mainFragment.schemeLayout,
                        mainFragment.bodyLength / 2,
                        mainFragment.bodyLength / 4).start();

                mainFragment.infoButton.setText(R.string.info_button3);//设置按钮为交通选择
                mainFragment.schemeInfoFlag = 2;//如果方案布局为单个方案

                //设置方案信息
                mainFragment.schemeInfo.setText(schemeItem.getAllStationInfo()
                        + "\n" + schemeItem.getDetailInfo() + "\n\n\n");

                //清空临时保存的公交站点信息
                mainFragment.busStationLocations.clear();

                //创建MassTransitRouteOverlay实例
                MassTransitRouteOverlay overlay = new MassTransitRouteOverlay(mainFragment.mBaiduMap);

                //清空地图上的所有标记点和绘制的路线
                mainFragment.mBaiduMap.clear();
                //构建Marker图标
                BitmapDescriptor bitmap = BitmapDescriptorFactory
                        .fromResource(R.drawable.ic_to_location);

                /*
                 * getRouteLines(): 所有规划好的路线
                 * get(0): 第1条规划好的路线
                 *
                 * getNewSteps():
                 * 起终点为同城时，该list表示一个step中的多个方案scheme（方案1、方案2、方案3...）
                 * 起终点为跨城时，该list表示一个step中多个子步骤sub_step（如：步行->公交->火车->步行）
                 *
                 * get(0): 方案1或第1步
                 * get(0): 步行到第1站点
                 * getEndLocation(): 终点站，即步行导航的终点站
                 *
                 */
                //获取所有站点信息
                for (List<MassTransitRouteLine.TransitStep> transitSteps :
                        schemeItem.getRouteLine().getNewSteps()) {
                    for (MassTransitRouteLine.TransitStep transitStep : transitSteps) {
                        //将获取到的站点信息临时保存
                        mainFragment.busStationLocations.add(transitStep.getEndLocation());

                        //构建MarkerOption，用于在地图上添加Marker
                        OverlayOptions option = new MarkerOptions()
                                .position(transitStep.getEndLocation())
                                .icon(bitmap);

                        //在地图上添加Marker，并显示
                        mainFragment.mBaiduMap.addOverlay(option);
                    }
                }

                try {
                    //获取路线规划数据
                    //为MassTransitRouteOverlay设置数据
                    overlay.setData(schemeItem.getRouteLine());
                    //在地图上绘制Overlay
                    overlay.addToMap();
                    //将路线放在最佳视野位置
                    overlay.zoomToSpan();
                } catch (Exception ignored) {
                    Toast.makeText(getContext(), R.string.draw_route_fail, Toast.LENGTH_SHORT).show();
                }
            }
        });

        //伸展按钮的点击事件
        holder.schemeExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                SchemeItem schemeItem = mainFragment.schemeList.get(position);

                if (schemeItem.getExpandFlag()) {
                    expandLayout(holder.infoDrawer, holder.detailInfo, false,
                            mainFragment.schemeResult, position);
                    rotateExpandIcon(holder.schemeExpand, 180, 0);//旋转伸展按钮
                    schemeItem.setExpandFlag(false);//改变伸缩状态
                } else {
                    expandLayout(holder.infoDrawer, holder.detailInfo, true,
                            mainFragment.schemeResult, position);
                    rotateExpandIcon(holder.schemeExpand, 0, 180);//旋转伸展按钮
                    schemeItem.setExpandFlag(true);//改变伸缩状态
                }
            }
        });

        return holder;
    }

    //不允许同时点击多个item
    private boolean unableToClick() {
        if ((System.currentTimeMillis() - clickTime) > 1000) {
            clickTime = System.currentTimeMillis();
            return false;
        } else return true;
    }

    //设置item中的View
    static class ViewHolder extends RecyclerView.ViewHolder {
        View view;
        CardView cardView;
        TextView simpleInfo;
        LinearLayout infoDrawer;
        TextView detailInfo;
        ImageButton schemeExpand;
        View endText;

        ViewHolder(View view) {
            super(view);
            cardView = view.findViewById(R.id.card_view);
            simpleInfo = view.findViewById(R.id.simple_info);
            infoDrawer = view.findViewById(R.id.info_drawer);
            detailInfo = view.findViewById(R.id.detail_info);
            schemeExpand = view.findViewById(R.id.scheme_expand);
            endText = view.findViewById(R.id.end_text);
        }
    }

}