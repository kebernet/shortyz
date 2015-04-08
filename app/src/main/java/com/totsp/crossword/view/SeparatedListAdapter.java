package com.totsp.crossword.view;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;

import com.totsp.crossword.shortyz.R;


public class SeparatedListAdapter extends BaseAdapter {
    private static final int TYPE_SECTION_HEADER = 0;
    private final ArrayAdapter<String> headers;
    public ArrayList<ArrayAdapter> sections = new ArrayList<ArrayAdapter>();

    public SeparatedListAdapter(Context context) {
        headers = new ArrayAdapter<String>(context, R.layout.puzzle_list_header);
    }

    public int getCount() {
        // total together all sections, plus one for each section header
        int total = 0;

        for (Adapter adapter : this.sections)
            total += (adapter.getCount() + 1);

        return total;
    }

    public void remove(int position){
        int section = 0;


        for (ArrayAdapter adapter : this.sections) {
            int size = adapter.getCount() + 1;

            // check if position inside this section
            if (position == 0) {
                String o = headers.getItem(section);
                headers.remove(o);
                return;
            }

            if (position < size) {
                Object o =  adapter.getItem(position -1);
                System.out.println("Removing "+o );
                adapter.remove(o);
                if(adapter.getCount() == 0){
                    String header = headers.getItem(section);
                    headers.remove(header);
                    this.sections = new ArrayList<>(this.sections);
                    this.sections.remove(section);
                }
                return;
            }

            // otherwise jump into next section
            position -= size;
            section++;
        }
    }

    public boolean isEnabled(int position) {
        return (getItemViewType(position) != TYPE_SECTION_HEADER);
    }

    public Object getItem(int position) {
        int section = 0;

        for (Adapter adapter : this.sections) {
            int size = adapter.getCount() + 1;

            // check if position inside this section
            if (position == 0) {
                return headers.getItem(section);
            }

            if (position < size) {
                return adapter.getItem(position - 1);
            }

            // otherwise jump into next section
            position -= size;
            section++;
        }

        return null;
    }

    public long getItemId(int position) {
        return position;
    }

    public int getItemViewType(int position) {
        for (Adapter adapter : this.sections) {
            int size = adapter.getCount() + 1;

            // check if position inside this section
            if (position == 0) {
                return TYPE_SECTION_HEADER;
            }

            if (position < size) {
                return 1;
            }

            // otherwise jump into next section
            position -= size;

            //type += adapter.getViewTypeCount();
        }

        return -1;
    }

    public View getView(int i, View view, ViewGroup group) {
        int sectionnum = 0;

        for (Adapter adapter : this.sections) {
            int size = adapter.getCount() + 1;

            // check if position inside this section
            if (i == 0) {
                return headers.getView(sectionnum, view, group);
            }

            if (i < size) {
                return adapter.getView(i - 1, view, group);
            }

            // otherwise jump into next section
            i -= size;
            sectionnum++;
        }

        return null;
    }

    public int getViewTypeCount() {
        //		// assume that headers count as one, then total all sections
        //		int total = 1;
        //		for(Adapter adapter : this.sections)
        //			total += adapter.getViewTypeCount();
        //		return total;
        return 2;
    }

    public void addSection(String section, ArrayAdapter adapter) {
        this.headers.add(section);
        this.sections.add(adapter);
    }

    public boolean areAllItemsSelectable() {
        return false;
    }
}
