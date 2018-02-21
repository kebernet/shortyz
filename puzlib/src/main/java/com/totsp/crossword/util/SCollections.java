package com.totsp.crossword.util;

import java.util.Collection;
import java.util.Collections;

/**
 * Created by rcooper on 8/5/15.
 */
public abstract class SCollections {

    public static <T, C extends Collection<T>> Collection<T> neverNull(C collection){
        if(collection == null){
            return Collections.EMPTY_LIST;
        } else {
            return collection;
        }
    }
}
