package com.example.foxizz.navigation.scheme_data;

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

import com.example.foxizz.navigation.R;
import com.example.foxizz.navigation.activity.MainActivity;

import java.util.List;

import static com.example.foxizz.navigation.demo.Tools.expandLayout;
import static com.example.foxizz.navigation.demo.Tools.rotateExpandIcon;

/**
 * 路线规划信息列表的适配器
 */
public class SchemeAdapter extends RecyclerView.Adapter<SchemeAdapter.ViewHolder> {

    private MainActivity mainActivity;

    //构造器
    public SchemeAdapter(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
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

    //获取item数量
    @Override
    public int getItemCount() {
        return mainActivity.schemeList.size();
    }

    //获取SearchItem的数据
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SchemeItem schemeItem = mainActivity.schemeList.get(position);
        holder.simpleInfo.setText(schemeItem.getSimpleInfo());
        holder.detailInfo.setText(schemeItem.getDetailInfo());

        //根据保存的展开状态设置信息抽屉的高度和旋转角度
        if(schemeItem.getExpandFlag()) {
            holder.infoDrawer.getLayoutParams().height
                    = holder.detailInfo.getLineHeight() * (holder.detailInfo.getLineCount() + 1);
            holder.schemeExpand.setRotation(180);
        }
        else {
            holder.infoDrawer.getLayoutParams().height = 0;
            holder.schemeExpand.setRotation(0);
        }


        //底部显示提示信息
        if(position == mainActivity.schemeList.size() - 1)
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
            @Override
            public void onClick(View view) {
                mainActivity.expandSelectLayout(true);//展开选择布局
                mainActivity.expandSchemeDrawer(false);//收起方案抽屉
                mainActivity.expandStartLayout(true);//展开开始导航布局

                mainActivity.schemeList.clear();
                notifyDataSetChanged();
            }
        });

        //伸展按钮的点击事件
        holder.schemeExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                SchemeItem schemeItem = mainActivity.schemeList.get(position);
                if(schemeItem.getExpandFlag()) {
                    expandLayout(mainActivity, holder.infoDrawer, holder.detailInfo,false, mainActivity.schemeResult, position);
                    rotateExpandIcon(holder.schemeExpand, 180, 0);//旋转伸展按钮
                    schemeItem.setExpandFlag(false);//改变伸缩状态
                } else {
                    expandLayout(mainActivity, holder.infoDrawer, holder.detailInfo,true, mainActivity.schemeResult, position);
                    rotateExpandIcon(holder.schemeExpand, 0, 180);//旋转伸展按钮
                    schemeItem.setExpandFlag(true);//改变伸缩状态
                }
            }
        });

        return holder;
    }

}