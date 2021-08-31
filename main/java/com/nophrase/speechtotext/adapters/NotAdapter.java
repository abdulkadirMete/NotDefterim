package com.nophrase.speechtotext.adapters;

import android.content.Context;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.nophrase.speechtotext.R;
import com.nophrase.speechtotext.model.Not;

import java.util.ArrayList;
import java.util.List;


public class NotAdapter extends RecyclerView.Adapter<NotAdapter.NotViewHolder> implements Filterable {
    private static final int[] et_colors = {R.color.colorEt1,R.color.colorEt2,R.color.colorEt3
            ,R.color.colorEt4,R.color.colorEt5,R.color.colorEt6};
    private Context mContext;
    private OnNotListener onNotListener;
    private List<Not> mNotList;
    private List<Not> mNotlistFull;

    private long mLastClickTime = 0;


    public NotAdapter(Context mContext,OnNotListener onNotListener,List<Not> mNotList) {
        this.mContext = mContext;
        this.mNotList=mNotList ;
        this.onNotListener = onNotListener;
        mNotlistFull = new ArrayList<>(mNotList);
    }

    @NonNull
    @Override
    public NotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.item_not,parent,false);
        return new NotViewHolder(v,onNotListener);
    }

    @Override
    public void onBindViewHolder(@NonNull NotViewHolder holder, int position) {
        Not currentNot = mNotList.get(position);
        holder.tv_header.setText(currentNot.getHeader());
        holder.tv_date.setText(currentNot.getDate());
        holder.cv_not.setBackgroundResource(et_colors[currentNot.getColorNum()]);
    }

    @Override
    public int getItemCount() {
        return mNotList.size();
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    private Filter mFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Not> filteredAnime = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredAnime.addAll(mNotlistFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Not not : mNotlistFull) {
                    if (not.getHeader().toLowerCase().contains(filterPattern)) {
                        filteredAnime.add(not);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredAnime;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mNotList.clear();
            mNotList.addAll((List)results.values);
            notifyDataSetChanged();
        }
    };

    public class NotViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tv_header,tv_date;
        OnNotListener onNotListener;
        CardView cv_not;


        public NotViewHolder(@NonNull View itemView,OnNotListener onNotListener) {
            super(itemView);
            tv_header = itemView.findViewById(R.id.tv_header);
            tv_date = itemView.findViewById(R.id.tv_date);
            cv_not = itemView.findViewById(R.id.cv_not);
            this.onNotListener = onNotListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();
            onNotListener.onNotClick(getAdapterPosition());
        }

    }

    public interface OnNotListener{
        void onNotClick(int position);
    }

}
