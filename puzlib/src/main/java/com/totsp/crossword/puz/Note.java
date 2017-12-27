package com.totsp.crossword.puz;

import java.io.Serializable;

public class Note implements Serializable {
    private String scratch;
    private String text;
    private String anagramSource;
    private String anagramSolution;

    public Note(String scratch,
                String text,
                String anagramSource,
                String anagramSolution) {
        this.text = text;
        this.scratch = scratch;
        this.anagramSource = anagramSource;
        this.anagramSolution = anagramSolution;
    }

    public String getText() {
        return text;
    }

    public String getSratch() {
        return scratch;
    }

    public String getAnagramSource() {
        return anagramSource;
    }

    public String getAnagramSolution() {
        return anagramSolution;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setScratch(String scratch) {
        this.scratch = scratch;
    }

    public void setAnagramSource(String anagramSource) {
        this.anagramSource = anagramSource;
    }

    public void setAnagramSolution(String anagramSolution) {
        this.anagramSolution = anagramSolution;
    }

    public boolean isEmpty() {
        return (text == null || text.length() == 0) &&
               (scratch == null || scratch.trim().length() == 0) &&
               (anagramSource == null || anagramSource.trim().length() == 0) &&
               (anagramSolution == null || anagramSolution.trim().length() == 0);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Note) {
            Note n = (Note) o;
            return safeStringEquals(this.text, n.text) &&
                   safeStringEquals(this.scratch, n.scratch) &&
                   safeStringEquals(this.anagramSource, n.anagramSource) &&
                   safeStringEquals(this.anagramSolution, n.anagramSolution);
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        result = (prime * result) + (text == null ? 0 : text.hashCode());
        result = (prime * result) +
                 (scratch == null ? 0 : scratch.hashCode());
        result = (prime * result) +
                 (anagramSource == null ? 0 : anagramSource.hashCode());
        result = (prime * result) +
                 (anagramSolution == null ? 0 : anagramSolution.hashCode());

        return result;
    }


    private static final boolean safeStringEquals(String s1, String s2) {
        if (s1 == null) {
            return (s2 == null);
        } else {
            return s1.equals(s2);
        }
    }
}
