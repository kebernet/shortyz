package com.totsp.crossword;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.totsp.crossword.shortyz.R;


public class SourceListAdapter extends BaseAdapter {
    static final String ALL_SOURCES = "All Sources";
    public String current = ALL_SOURCES;
    private final Context context;
    private List<String> sources;

    public SourceListAdapter(Context context, List<String> sources) {
        this.sources = sources;
        this.context = context;
    }

    public int getCount() {
        return sources.size() + 1;
    }

    public Object getItem(int i) {
        if (i == 0) {
            return ALL_SOURCES;
        } else {
            return sources.get(i - 1);
        }
    }

    public long getItemId(int i) {
        return i;
    }

    public View getView(int index, View view, ViewGroup group) {
        String value = (index == 0) ? ALL_SOURCES : sources.get(index - 1);
        LayoutInflater inflater = (LayoutInflater) context.getApplicationContext()
                                                          .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(value.equals(this.current) ? R.layout.source_item_highlight : R.layout.source_item, null);

        TextView text = (TextView) view;

        text.setText(value);
        text.setTag(value);

        return text;
    }
}
