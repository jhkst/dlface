package cz.activecode.dl.utils;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigDecimal;
import java.net.Proxy;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

/**
 * Util methods
 */
public class Util {

    private static final Logger LOGGER = LoggerFactory.getLogger(Util.class);

    private final static Pattern fileNamePattern = Pattern.compile("/([^/]*?(\\.|-)(zip|rar|avi|wmv|mp\\d?|srt|sub|apk))\\.html?", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    private final static Pattern encodedPattern = Pattern.compile("%[A-Z0-9]{2}%");
    private final static Pattern fileExtensionPattern = Pattern.compile("[\\.\\-_]([a-z\\d]+?)$");
    private static final int DOWNLOAD_BUFFER = 16 * 1024;

    //TODO: improve this
    private static final Pattern URL_PATTERN = Pattern.compile(
            "(?:^|[\\W])((ht|f)tp(s?)://|www\\.)"
                    + "(([\\w\\-]+\\.)+?([\\w\\-.~]+/?)*"
                    + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    private static final Pattern SIZE_PATTERN = Pattern.compile("([\\d.]+)(([KMGT]?)(i?))?B?", Pattern.CASE_INSENSITIVE);

    private static final List<String> SIZE_POWER_MAP = Arrays.asList("", "K", "M", "G", "T");


    /**
     * Parses and splits String into list of URL.
     * @param downloadList String containing urls.
     * @return list of valid urls.
     */
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

    /**
     * Parses size in human readable format. It's case insensitive
     * and accepts values like
     * 10G = 10 * 1000 * 1000 * 1000
     * 10GB = 10 * 1000 * 1000 * 1000
     * 10GiB = 10 * 1024 * 1024 * 1024
     * 10 = 10
     * 10Mb = 10 * 1000 * 1000
     * 10Mib = 10 * 1024 * 1024
     * 1kiB = 1 * 1024
     *
     * @param value human readable size value
     * @param defaultValue default value used if {@code null} or non-parsable
     * @param overflowValue default value if size is too big
     * @return size in {@code long}.
     */
    public static long parseSize(String value, long defaultValue, long overflowValue) {
        if(value == null) {
            return defaultValue;
        }
        Matcher m = SIZE_PATTERN.matcher(value);
        if(m.find()) {
            long num = Long.parseLong(m.group(1));
            int man = SIZE_POWER_MAP.indexOf(m.group(3).toUpperCase());
            if(man < 0) {
                man = 0;
            }
            int thresh = "I".equals(m.group(4).toUpperCase()) ? 1024 : 1000;
            BigDecimal mul = BigDecimal.valueOf(thresh).pow(man);
            try {
                return num * mul.longValueExact();
            } catch (ArithmeticException e) {
                LOGGER.warn("Value of too big ({})", value);
                return overflowValue;
            }
        } else {
            return defaultValue;
        }
    }

    public static String identifyFileName(final String url) {
        final Matcher matcher = fileNamePattern.matcher(url);
        if (matcher.find()) {
            return checkEncodedFileName(matcher.group(1));
        }
        final String[] strings = url.split("/");
        for (int i = strings.length - 1; i >= 0; i--) {
            final String s = strings[i].trim();
            if (!s.isEmpty())
                return s;
        }
        String s = url.replace(":", "_").trim();
        if (s.startsWith("?"))
            s = s.substring(1);
        if (s.isEmpty()) {
            return "?";
        }
        return checkEncodedFileName(s);
    }

    private static String checkEncodedFileName(String name) {
        if (encodedPattern.matcher(name).find()) {
            return urlDecode(name);
        }
        return name;
    }

    public static String identifyFileType(String fileName) {
        if (fileName == null) {
            return "";
        }
        fileName = fileName.toLowerCase(Locale.ENGLISH);
        final Matcher matcher = fileExtensionPattern.matcher(fileName);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return "";
        }
    }

    public static void copyURLToFile(URL source, File destination, Proxy proxy) throws IOException {
        if(proxy == null) {
            FileUtils.copyURLToFile(source, destination);
        } else {
            downloadUsingProxy(source, destination, proxy);
        }
    }

    /**
     * TODO: make this public, global used with callbacks ... (maybe some factory/builder)
     */
    private static void downloadUsingProxy(URL source, File destination, Proxy proxy) throws IOException {
        try(OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(destination));
            InputStream inputStream = source.openConnection(proxy).getInputStream()) {

            byte[] buffer = new byte[DOWNLOAD_BUFFER];
            int len;
            while ((len = inputStream.read(buffer)) >= 0) {
                outputStream.write(buffer, 0, len);
            }
        }
    }

    private static String urlDecode(String s) {
        try {
            String decoded = URLDecoder.decode(s, "UTF-8");
            if (decoded.contains("\uFFFD")) {
                decoded = URLDecoder.decode(s, "Windows-1250");
            }
            if (decoded.contains("\uFFFD")) {
                return s;
            }
            return decoded;
        } catch (UnsupportedEncodingException e) {
            LOGGER.debug("Unsupported encoding", e);
        }
        return s;
    }

    public static byte[] compress(byte[] data) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(data.length);
        try(GZIPOutputStream zos = new GZIPOutputStream(baos)) {
            zos.write(data);
        } catch (IOException e) {
            LOGGER.warn("Cannot compress data", e);
            return data;
        }
        return baos.toByteArray();
    }

    public static String compressToUri(BitSet bitSet) {
        return compressToUri(bitSet.toByteArray());
    }

    public static String compressToUri(byte[] data) {
        return "url(data:application/gzip;base64," +
                Base64.getEncoder().encodeToString(Util.compress(data)) +
                ")";

    }

}
