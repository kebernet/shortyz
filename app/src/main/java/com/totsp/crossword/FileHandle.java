package com.totsp.crossword;

import java.io.File;
import java.util.Date;

import com.totsp.crossword.puz.PuzzleMeta;


public class FileHandle implements Comparable<FileHandle> {
    File file;
    PuzzleMeta meta;

    FileHandle(File f, PuzzleMeta meta) {
        this.file = f;
        this.meta = meta;
    }

    public int compareTo(FileHandle another) {
        FileHandle h = (FileHandle) another;

        try {
            return h.getDate()
                    .compareTo(this.getDate());
        } catch (Exception e) {
            return 0;
        }
    }

    String getCaption() {
        return (meta == null) ? "" : meta.title;
    }

    Date getDate() {
        return (meta == null) ? new Date(file.lastModified()) : meta.date;
    }

    int getProgress() {
        return (meta == null) ? 0 : (meta.updateable ? (-1) : meta.percentComplete);
    }

    String getSource() {
        return ((meta == null) || (meta.source == null)) ? "Unknown" : meta.source;
    }

    String getTitle() {
        return ((meta == null) || (meta.source == null) || (meta.source.length() == 0))
        ? file.getName()
              .substring(0, file.getName().lastIndexOf(".")) : meta.source;
    }

    @Override
    public String toString(){
        return file.getAbsolutePath();
    }
}
