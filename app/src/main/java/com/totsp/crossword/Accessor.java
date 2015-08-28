package com.totsp.crossword;

import java.text.SimpleDateFormat;
import java.util.Comparator;


public interface Accessor extends Comparator<FileHandle> {
    Accessor DATE_ASC = new Accessor() {
            @SuppressWarnings("SimpleDateFormat")
            public String getLabel(FileHandle o) {
                SimpleDateFormat df = new SimpleDateFormat("EEEEEEEEE MMM dd, yyyy");

                return df.format(o.getDate());
            }

            public int compare(FileHandle object1, FileHandle object2) {
                return object1.getDate()
                              .compareTo(object2.getDate());
            }
        };

    Accessor DATE_DESC = new Accessor() {
            @SuppressWarnings("SimpleDateFormat")
            public String getLabel(FileHandle o) {
                SimpleDateFormat df = new SimpleDateFormat("EEEEEEEEE MMM dd, yyyy");

                return df.format(o.getDate());
            }

            public int compare(FileHandle object1, FileHandle object2) {
                return object2.getDate()
                              .compareTo(object1.getDate());
            }
        };

    Accessor SOURCE = new Accessor() {
            public String getLabel(FileHandle o) {
                return o.getSource();
            }

            public int compare(FileHandle object1, FileHandle object2) {
                return object1.getSource()
                              .compareTo(object2.getSource());
            }
        };

    String getLabel(FileHandle o);
}
