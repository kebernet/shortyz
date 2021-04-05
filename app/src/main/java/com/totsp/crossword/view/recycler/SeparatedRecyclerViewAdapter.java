package com.totsp.crossword.view.recycler;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by rcooper on 7/8/15.
 */
public class SeparatedRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Dismissable {
    private static int HEADER = Integer.MIN_VALUE;

    private LinkedHashMap<String, RemovableRecyclerViewAdapter> sections = new LinkedHashMap<>();
    private final int textViewId;

    public SeparatedRecyclerViewAdapter(int textViewId) {
        this.textViewId = textViewId;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if(viewType == HEADER){
            // For headers, we can create a simple text holder.
            TextView view = (TextView) LayoutInflater.from(viewGroup.getContext())
                    .inflate(textViewId, viewGroup, false);
            return new SimpleTextViewHolder(view);
        } else {
            // For puzzle views, any sectionAdapter can be used to create the view holder, so find
            // the first one that gives us a result.
            for(RecyclerView.Adapter sectionAdapter : sections.values()){
                RecyclerView.ViewHolder result = null;
                try {
                    result = sectionAdapter.onCreateViewHolder(viewGroup, viewType);
                } catch(Exception e){
                    e.printStackTrace();
                }
                if (result != null) {
                    return result;
                }
            }
            return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        int sectionPosition = 0;
        for (Map.Entry<String, RemovableRecyclerViewAdapter> entry : this.sections.entrySet()) {
            int size = entry.getValue().getItemCount() + 1;
            if(position < sectionPosition + size){
                int index = position - sectionPosition;
                if(index == 0){
                    TextView view = (TextView) ((SimpleTextViewHolder) viewHolder).itemView;
                    view.setText(entry.getKey());
                } else {
                    entry.getValue().onBindViewHolder(viewHolder, index - 1);
                }
                break;
            }
            sectionPosition += size;
        }
    }

    @Override
    public int getItemCount() {
        int count = 0;
        for(RecyclerView.Adapter section : sections.values()){
            count++;
            count += section.getItemCount();
        }
        return count;
    }

    @Override
    public int getItemViewType(int position) {
        int sectionPosition = 0;
        for (Map.Entry<String, RemovableRecyclerViewAdapter> entry : this.sections.entrySet()) {
            int size = entry.getValue().getItemCount() + 1;
            if(position < sectionPosition + size){
                int index = position - sectionPosition;
                if(index == 0){
                    return HEADER;
                } else {
                    return entry.getValue().getItemViewType(index -1);
                }
            }
            sectionPosition += size;
        };
        throw new RuntimeException("Unable to find anything for position "+position);
    }

    public void addSection(String header, RemovableRecyclerViewAdapter adapter){
        this.sections.put(header, adapter);
    }


    @Override
    public void onItemDismiss(int position) {
       int sectionPosition = 0;
        for (Map.Entry<String, RemovableRecyclerViewAdapter> entry : new LinkedList<>(this.sections.entrySet())) {
            int size = entry.getValue().getItemCount() + 1;
            if (position < sectionPosition + size) {
                int index = position - sectionPosition;
                if (index == 0) {
                    return;
                } else {
                    entry.getValue().remove(index - 1);
                    notifyItemRemoved(position);
                    if(entry.getValue().getItemCount() == 0){
                        this.sections.remove(entry.getKey());
                        notifyItemRemoved(position - 1);
                    }
                    break;
                }
            }
            sectionPosition += size;
        }

    }

    public static class SimpleTextViewHolder extends RecyclerView.ViewHolder {
        public SimpleTextViewHolder(TextView itemView) {
            super(itemView);
        }
    }

}
