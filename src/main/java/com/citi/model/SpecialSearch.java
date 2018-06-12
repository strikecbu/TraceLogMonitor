package com.citi.model;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author AndyChen
 * @version <ul>
 * <li>2018/6/11 AndyChen,new
 * </ul>
 * @since 2018/6/11
 */
public class SpecialSearch {

    private int allowPendingCount;

    private Pattern pattern;

    private List<String> resultList = new ArrayList<>();

    public int getAllowPendingCount() {
        return allowPendingCount;
    }

    public void setAllowPendingCount(int allowPendingCount) {
        this.allowPendingCount = allowPendingCount;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }
}
