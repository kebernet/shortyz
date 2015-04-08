package com.totsp.crossword;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.totsp.crossword.puz.Box;
import com.totsp.crossword.puz.Playboard.Clue;
import com.totsp.crossword.shortyz.R;
import com.totsp.crossword.shortyz.ShortyzApplication;


public class ClueListAdapter extends ArrayAdapter {
    private final int defaultBackground;
    private final int highlight;
    private final int highlightLine;
    private final int normalLineText;
    public int textSize = 14;
    private Clue highlightClue;
    private Context context;
    private HashMap<Integer, Box[]> cache = new HashMap<Integer, Box[]>();
    private Clue[] clues;
    private boolean across;
    private boolean isActive = false;

    public ClueListAdapter(Context context, Clue[] clues, boolean across) {
        super(context, R.layout.clue_detail_item, R.id.clueLine, clues);
        this.clues = clues;
        this.context = context;
        this.across = across;
        this.highlight = context.getResources().getColor(R.color.accent);
        this.highlightLine = context.getResources().getColor(R.color.accentText);
        this.normalLineText = context.getResources().getColor(R.color.textColorPrimary);
        this.defaultBackground = context.getResources().getColor(R.color.background_material_light);

    }



    public void setActiveDirection(boolean isActive) {
        this.isActive = isActive;
    }

    public int getCount() {
        return clues.length;
    }

    public void setHighlightClue(Clue c) {
        this.highlightClue = c;
    }

    public Object getItem(int i) {
        return clues[i];
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getApplicationContext()
                                                              .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.clue_detail_item, null);
        }

        TextView line = (TextView) view.findViewById(R.id.clueLine);
        line.setTextSize(TypedValue.COMPLEX_UNIT_SP, this.textSize);

        TextView word = (TextView) view.findViewById(R.id.clueWord);
        word.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (this.textSize - 1.75));

        Clue c = this.clues[position];
        line.setText(c.number + ". " + c.hint);

        Box[] boxes = this.cache.get(c.number);

        if (boxes == null) {
            boxes = ShortyzApplication.BOARD.getWordBoxes(c.number, across);
            cache.put(c.number, boxes);
        }

        StringBuilder sb = new StringBuilder();

        for (Box b : boxes) {
            if (b == null) {
                continue;
            }

            sb.append((b.getResponse() == ' ') ? '_' : b.getResponse())
              .append(' ');
        }

        sb.append(" [" + boxes.length + "]");
        word.setText(sb);

        if (this.isActive && c.equals(this.highlightClue)) {
            view.setBackgroundColor(highlight);
            line.setTextColor(highlightLine);
        } else {
            view.setBackgroundColor(defaultBackground);
            line.setTextColor(normalLineText);
        }

        return view;
    }

    public int indexOf(Clue clue) {
        return Arrays.binarySearch(clues, clue,
            new Comparator<Clue>() {
                public int compare(Clue arg0, Clue arg1) {
                    return ((Integer) arg0.number).compareTo(arg1.number);
                }
            });
    }
}
