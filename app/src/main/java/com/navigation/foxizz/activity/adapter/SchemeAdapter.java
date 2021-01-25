package com.navigation.foxizz.activity.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.navigation.foxizz.R;
import com.navigation.foxizz.activity.fragment.MainFragment;
import com.navigation.foxizz.data.SchemeItem;
import com.navigation.foxizz.util.LayoutUtil;

/**
 * 路线规划信息列表的适配器
 */
public class SchemeAdapter extends RecyclerView.Adapter<SchemeAdapter.ViewHolder> {

    private final MainFragment mainFragment;
    public SchemeAdapter(MainFragment mainFragment) {
        this.mainFragment = mainFragment;
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

    private long clickTime = 0;

    //设置item中的View
    static class ViewHolder extends RecyclerView.ViewHolder {
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
            endText = view.findViewById(R.id.scheme_end_text).findViewById(R.id.end_text);
        }
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

        //根据保存的展开状态设置信息抽屉的高度、旋转角度和最大行数
        if (schemeItem.getExpandFlag()) {
            LayoutUtil.setViewHeight(holder.infoDrawer, holder.detailInfo.getLineHeight() *
                    (holder.detailInfo.getLineCount() + 1));
            holder.schemeExpand.setRotation(180);
            holder.simpleInfo.setMaxLines(8);
        } else {
            LayoutUtil.setViewHeight(holder.infoDrawer, 0);
            holder.schemeExpand.setRotation(0);
            holder.simpleInfo.setMaxLines(1);
        }

        //底部显示提示信息
        if (position == mainFragment.schemeList.size() - 1)
            holder.endText.setVisibility(View.VISIBLE);
        else holder.endText.setVisibility(View.GONE);
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

                LayoutUtil.expandLayout(mainFragment.schemeDrawer, false);//收起方案抽屉
                LayoutUtil.expandLayout(mainFragment.schemeInfoLayout, true);//展开方案信息抽屉
                mainFragment.middleButton.setText(R.string.middle_button3);//设置按钮为交通选择
                mainFragment.schemeFlag = MainFragment.SCHEME_INFO;//如果方案布局为单个方案

                mainFragment.myRoutePlanSearch.startMassTransitRoutePlan(position);
            }
        });

        //伸展按钮的点击事件
        holder.schemeExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                SchemeItem schemeItem = mainFragment.schemeList.get(position);

                if (schemeItem.getExpandFlag()) {//收起
                    holder.simpleInfo.setMaxLines(1);
                    LayoutUtil.expandLayout(holder.infoDrawer, false, holder.detailInfo,
                            mainFragment.schemeResult, position);
                    LayoutUtil.rotateExpandIcon(holder.schemeExpand, 180, 0);//旋转伸展按钮
                    schemeItem.setExpandFlag(false);
                } else {//展开
                    holder.simpleInfo.setMaxLines(8);
                    LayoutUtil.expandLayout(holder.infoDrawer, true, holder.detailInfo,
                            mainFragment.schemeResult, position);
                    LayoutUtil.rotateExpandIcon(holder.schemeExpand, 0, 180);//旋转伸展按钮
                    schemeItem.setExpandFlag(true);
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

}