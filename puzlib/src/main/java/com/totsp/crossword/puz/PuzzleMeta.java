package com.totsp.crossword.puz;

import com.totsp.crossword.puz.Playboard.Position;

import java.io.Serializable;
import java.util.Date;


public class PuzzleMeta implements Serializable {

    public String author;
    public String title;
    public String source;
    public Date date;
    public int percentComplete;
    public boolean updatable;
    public String sourceUrl;
    public Position position;
    public boolean across;


    public String toString() {
        return new StringBuilder("author: ")
                .append(author)
                .append("title: ")
                .append(title)
                .append(" source: ")
                .append(source)
                .append(" sourceUrl: ")
                .append(sourceUrl)
                .append(" date: ")
                .append(date)
                .append(" percentComplete: ")
                .append(percentComplete)
                .append(" updatable: ")
                .append(updatable)
                .append(" position: ")
                .append(position)
                .append(" across: ")
                .append(across)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PuzzleMeta that = (PuzzleMeta) o;

        if (percentComplete != that.percentComplete) return false;
        if (updatable != that.updatable) return false;
        if (across != that.across) return false;
        if (author != null ? !author.equals(that.author) : that.author != null) return false;
        if (title != null ? !title.equals(that.title) : that.title != null) return false;
        if (source != null ? !source.equals(that.source) : that.source != null) return false;
        if (date != null ? !date.equals(that.date) : that.date != null) return false;
        if (sourceUrl != null ? !sourceUrl.equals(that.sourceUrl) : that.sourceUrl != null)
            return false;
        return !(position != null ? !position.equals(that.position) : that.position != null);

    }

    @Override
    public int hashCode() {
        int result = author != null ? author.hashCode() : 0;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (source != null ? source.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + percentComplete;
        result = 31 * result + (updatable ? 1 : 0);
        result = 31 * result + (sourceUrl != null ? sourceUrl.hashCode() : 0);
        result = 31 * result + (position != null ? position.hashCode() : 0);
        result = 31 * result + (across ? 1 : 0);
        return result;
    }
}