package com.totsp.crossword.view.recycler;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by rcooper on 7/24/15.
 */
public abstract class RemovableRecyclerViewAdapter<VT extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VT> {

    public abstract void remove(int position);
}
