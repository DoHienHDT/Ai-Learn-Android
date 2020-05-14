package jp.co.miosys.aiworldview.adapter;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import jp.co.miosys.aiworldview.R;
import jp.co.miosys.aiworldview.data_post_response.Category;

public class AdapterMemos extends RecyclerView.Adapter<AdapterMemos.RecyclerViewHolder> {

    private IOperationCategory mCallback;
    private List<Category> listCategory;
    public AdapterMemos(IOperationCategory context, List<Category> data) {
        mCallback = context;
        listCategory = data;
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_category, parent, false);
        return new RecyclerViewHolder(view);
}

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {
        int k = 0;
        holder.txtUserName.setText(listCategory.get(position).getName());
        if (position != 0) {
            if (listCategory.get(position).isSelect()) {
                holder.itemView.setBackgroundResource(R.drawable.bg_select);
            } else {
                holder.itemView.setBackgroundResource(R.drawable.bg_description_category);
            }
        }else
            holder.itemView.setBackgroundResource(R.drawable.bg_description_category);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.sendIdCategory(listCategory.get(position));
                if(position == 0) {
                    if (listCategory.get(0).isSelect()) {
                        listCategory.get(0).setName("Select All");
                        for (int i = 1; i < listCategory.size(); i++) {
                            listCategory.get(i).setSelect(false);
                        }
                    }else {
                        listCategory.get(0).setName("UnSelect All");
                        for (int i = 1; i < listCategory.size(); i++) {
                            listCategory.get(i).setSelect(true);
                        }
                    }
                    listCategory.get(0).setSelect(!listCategory.get(0).isSelect());
                    notifyDataSetChanged();
                }else {
                    listCategory.get(position).setSelect(!listCategory.get(position).isSelect());
                    int selectAll = 0;
                    int unSelectAll = 0;
                    for(int i = 1; i<listCategory.size(); i++) {
                        if(listCategory.get(i).isSelect())
                            selectAll += 1;
                        else
                            unSelectAll += 1;
                    }
                    if (selectAll == listCategory.size()-1) {
                        listCategory.get(0).setSelect(true);
                        listCategory.get(0).setName("UnSelect All");
                    }
                    if (unSelectAll == listCategory.size()-1) {
                        listCategory.get(0).setSelect(false);
                        listCategory.get(0).setName("Select All");
                    }
                    notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return listCategory.size();
    }


    public class RecyclerViewHolder extends RecyclerView.ViewHolder {
        TextView txtUserName;
        public RecyclerViewHolder(View itemView) {
            super(itemView);
            txtUserName = itemView.findViewById(R.id.text_category);
        }
    }

    public interface IOperationCategory{
        void sendIdCategory(Category category);
    }
}
