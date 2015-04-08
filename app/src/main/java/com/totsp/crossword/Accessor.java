package com.totsp.crossword;

import java.text.SimpleDateFormat;
import java.util.Comparator;


public interface Accessor extends Comparator<FileHandle> {
    public static Accessor DATE_ASC = new Accessor() {
            public String getLabel(FileHandle o) {
                SimpleDateFormat df = new SimpleDateFormat("EEEEEEEEE MMM dd, yyyy");

                return df.format(o.getDate());
            }

            public int compare(FileHandle object1, FileHandle object2) {
                return object1.getDate()
                              .compareTo(object2.getDate());
            }
        };

    public static Accessor DATE_DESC = new Accessor() {
            public String getLabel(FileHandle o) {
                SimpleDateFormat df = new SimpleDateFormat("EEEEEEEEE MMM dd, yyyy");

                return df.format(o.getDate());
            }

            public int compare(FileHandle object1, FileHandle object2) {
                return object2.getDate()
                              .compareTo(object1.getDate());
            }
        };

    public static Accessor SOURCE = new Accessor() {
            public String getLabel(FileHandle o) {
                return o.getSource();
            }

            public int compare(FileHandle object1, FileHandle object2) {
                return object1.getSource()
                              .compareTo(object2.getSource());
            }
        };

    public String getLabel(FileHandle o);
}
