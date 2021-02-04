package com.navigation.foxizz.activity.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.navigation.foxizz.R;
import com.navigation.foxizz.activity.fragment.MainFragment;
import com.navigation.foxizz.data.SchemeItem;
import com.navigation.foxizz.util.LayoutUtil;

import org.jetbrains.annotations.NotNull;

/**
 * 路线规划信息列表的适配器
 */
public class SchemeAdapter extends RecyclerView.Adapter<SchemeAdapter.ViewHolder> {

    private final MainFragment mainFragment;
    public SchemeAdapter(MainFragment mainFragment) {
        this.mainFragment = mainFragment;
    }

    private long clickTime = 0;

    //设置item中的View
    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardSchemeInfo;
        TextView tvSimpleInfo;
        LinearLayout llInfoDrawer;
        TextView tvDetailInfo;
        ImageButton ibSchemeExpand;
        View tvEnd;

        ViewHolder(View view) {
            super(view);
            cardSchemeInfo = view.findViewById(R.id.card_scheme_info);
            tvSimpleInfo = view.findViewById(R.id.tv_simple_info);
            llInfoDrawer = view.findViewById(R.id.ll_info_drawer);
            tvDetailInfo = view.findViewById(R.id.tv_detail_info);
            ibSchemeExpand = view.findViewById(R.id.ib_scheme_expand);
            tvEnd = view.findViewById(R.id.include_end_text_scheme).findViewById(R.id.tv_end);
        }
    }

    /**
     * 获取item数量
     *
     * @return int
     */
    @Override
    public int getItemCount() {
        return mainFragment.schemeList.size();
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
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SchemeItem schemeItem = mainFragment.schemeList.get(position);
        holder.tvSimpleInfo.setText(schemeItem.getSimpleInfo());
        holder.tvDetailInfo.setText(schemeItem.getDetailInfo());

        //根据保存的展开状态设置信息抽屉的高度、旋转角度和最大行数
        if (schemeItem.getExpandFlag()) {
            LayoutUtil.setViewHeight(holder.llInfoDrawer, holder.tvDetailInfo.getLineHeight() *
                    (holder.tvDetailInfo.getLineCount() + 1));
            holder.ibSchemeExpand.setRotation(180);
            holder.tvSimpleInfo.setMaxLines(8);
        } else {
            LayoutUtil.setViewHeight(holder.llInfoDrawer, 0);
            holder.ibSchemeExpand.setRotation(0);
            holder.tvSimpleInfo.setMaxLines(1);
        }

        //底部显示提示信息
        if (position == mainFragment.schemeList.size() - 1)
            holder.tvEnd.setVisibility(View.VISIBLE);
        else holder.tvEnd.setVisibility(View.GONE);
    }

    //为recyclerView的每一个item设置点击事件
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_scheme_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);

        //cardView的点击事件
        holder.cardSchemeInfo.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                if (unableToClick()) return;
                int position = holder.getAdapterPosition();

                LayoutUtil.expandLayout(mainFragment.llSchemeDrawer, false);//收起方案抽屉
                LayoutUtil.expandLayout(mainFragment.llSchemeInfoLayout, true);//展开方案信息抽屉
                mainFragment.btMiddle.setText(R.string.middle_button3);//设置按钮为交通选择
                mainFragment.schemeFlag = MainFragment.SCHEME_INFO;//如果方案布局为单个方案

                mainFragment.myRoutePlanSearch.startMassTransitRoutePlan(position);
            }
        });

        //伸展按钮的点击事件
        holder.ibSchemeExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                SchemeItem schemeItem = mainFragment.schemeList.get(position);

                if (schemeItem.getExpandFlag()) {//收起
                    holder.tvSimpleInfo.setMaxLines(1);
                    LayoutUtil.expandLayout(holder.llInfoDrawer, false, holder.tvDetailInfo,
                            mainFragment.recyclerSchemeResult, position);
                    LayoutUtil.rotateExpandIcon(holder.ibSchemeExpand, 180, 0);//旋转伸展按钮
                    schemeItem.setExpandFlag(false);
                } else {//展开
                    holder.tvSimpleInfo.setMaxLines(8);
                    LayoutUtil.expandLayout(holder.llInfoDrawer, true, holder.tvDetailInfo,
                            mainFragment.recyclerSchemeResult, position);
                    LayoutUtil.rotateExpandIcon(holder.ibSchemeExpand, 0, 180);//旋转伸展按钮
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