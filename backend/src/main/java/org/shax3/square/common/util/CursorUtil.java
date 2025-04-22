package org.shax3.square.common.util;

import java.util.List;
import java.util.function.Function;

public class CursorUtil {

    public static <T> boolean hasNext(List<T> list, int limit) {
        return list.size() > limit;
    }

    public static <T, V> V getNextCursor(List<T> list, boolean hasNext, Function<T, V> extractor) {
        if (!hasNext || list.isEmpty()) return null;
        return extractor.apply(list.get(list.size() - 1));
    }
}
