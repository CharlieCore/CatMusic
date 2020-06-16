package com.xicheng.catmusic.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.xicheng.catmusic.beans.ItemBean;
import com.xicheng.catmusic.R;

import java.util.List;

/**
 * Created by Square
 * Date :2020/6/1
 * Description :
 * Version :
 */
public class ListViewAdapter extends RecyclerView.Adapter<ListViewAdapter.InnerHolder> {

    private OnItemClickListener mOnItemClickListener;
    private List<ItemBean> mData;

    //构造方法
    public ListViewAdapter(List<ItemBean> data) {
        this.mData = data;
    }


    /*
     * 创建条目view
     * */
    @NonNull
    @Override
    public ListViewAdapter.InnerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_view, null, false);
        return new InnerHolder(view);
    }

    //绑定holder
    @Override
    public void onBindViewHolder(@NonNull ListViewAdapter.InnerHolder holder, int position) {
        //设置数据
        holder.setData(position);
    }

    @Override
    public int getItemCount() {
        if (mData != null) {
            return mData.size();
        }
        return 0;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }


    //该内部类用来将数据放入布局文件中
    public class InnerHolder extends RecyclerView.ViewHolder {

        private TextView mTitle;
        private TextView mAuthor;
        private TextView mAlbum;
        private View itemView;


        public InnerHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            itemView.setSelected(true);
            mTitle = itemView.findViewById(R.id.list_View_title);
            mAuthor = itemView.findViewById(R.id.bar_song_author);
            mAlbum = itemView.findViewById(R.id.list_View_album);

        }

        //设置数据
        public void setData(final int position) {
            ItemBean itemBean = mData.get(position);
            mTitle.setText(itemBean.titles);
            mAuthor.setText(itemBean.author);
            mAlbum.setText(itemBean.album);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(position);
                    }
                }
            });

        }
    }
}
