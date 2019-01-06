package com.totsp.crossword;

import java.util.Comparator;


public interface Accessor extends Comparator<FileHandle> {
    String getLabel(FileHandle o);
}
