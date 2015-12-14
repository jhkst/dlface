package cz.activecode.dl;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

    private static final Pattern URL_PATTERN = Pattern.compile(
            "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
                    + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
                    + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);


    public static List<String> splitURLS(String downloadList) {
        Matcher matcher = URL_PATTERN.matcher(downloadList);
        List<String> result = new LinkedList<>();
        while (matcher.find()) {
            int start = matcher.start(1);
            int end = matcher.end();
            result.add(downloadList.substring(start, end));
        }

        return result;
    }
}
