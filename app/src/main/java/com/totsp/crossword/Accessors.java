package com.totsp.crossword;

import java.text.SimpleDateFormat;

final public class Accessors {
    private Accessors() {}

    static Accessor DATE_ASC = new Accessor() {
        public String getLabel(FileHandle o) {
            return getDateLabel(o);
        }

        public int compare(FileHandle object1, FileHandle object2) {
            int dateComparison = compareDate(object1, object2);

            if (dateComparison == 0) {
                return compareSource(object1, object2);
            }

            return dateComparison;
        }
    };

    static Accessor DATE_DESC = new Accessor() {
        public String getLabel(FileHandle o) {
            return getDateLabel(o);
        }

        public int compare(FileHandle object1, FileHandle object2) {
            int dateComparison = compareDate(object2, object1);

            if (dateComparison == 0) {
                return compareSource(object1, object2);
            }

            return dateComparison;
        }
    };

    static Accessor SOURCE = new Accessor() {
        public String getLabel(FileHandle o) {
            return o.getSource();
        }

        public int compare(FileHandle object1, FileHandle object2) {
            int sourceComparison = compareSource(object1, object2);

            if (sourceComparison == 0) {
                return compareDate(object2, object1);
            }

            return sourceComparison;
        }
    };

    @SuppressWarnings("SimpleDateFormat")
    private static String getDateLabel(FileHandle o) {
        SimpleDateFormat df = new SimpleDateFormat("EEEEEEEEE MMM dd, yyyy");

        return df.format(o.getDate());
    }

    private static int compareDate(FileHandle object1, FileHandle object2) {
        return object1.getDate()
                .compareTo(object2.getDate());
    }

    private static int compareSource(FileHandle object1, FileHandle object2) {
        return object1.getSource()
                .compareTo(object2.getSource());
    }
}
