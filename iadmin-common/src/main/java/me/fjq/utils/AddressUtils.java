package me.fjq.utils;

import cn.hutool.core.io.resource.ClassPathResource;
import me.fjq.constant.Constants;
import org.lionsoul.ip2region.DataBlock;
import org.lionsoul.ip2region.DbConfig;
import org.lionsoul.ip2region.DbSearcher;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
 * 获取地址类
 */
public class AddressUtils {

    /**
     * 根据ip获取详细地址
     */
    public static String getCityInfo(String ip) {
        DbSearcher searcher = null;
        try {
            String path = "ip2region/ip2region.db";
            String name = "ip2region.db";
            DbConfig config = new DbConfig();
            File file = FileUtil.inputStreamToFile(new ClassPathResource(path).getStream(), name);
            searcher = new DbSearcher(config, file.getPath());
            Method method;
            method = searcher.getClass().getMethod("btreeSearch", String.class);
            DataBlock dataBlock;
            dataBlock = (DataBlock) method.invoke(searcher, ip);
            String address = dataBlock.getRegion().replace("0|", "");
            char symbol = '|';
            if (address.charAt(address.length() - 1) == symbol) {
                address = address.substring(0, address.length() - 1);
            }
            return address.equals(Constants.REGION) ? "内网IP" : address;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (searcher != null) {
                try {
                    searcher.close();
                } catch (IOException ignored) {
                }
            }

        }
        return "";
    }

    private static boolean dealSeqValid(String str) {
//        if (StringUtils.contains(str, "〔") && StringUtils.contains(str, "〕")) {
//            return true;
//        }
//        return false;
//[\u4e00-\u9fa5_a-zA-Z0-9]
        return Pattern.matches("^[\\u4e00-\\u9fa5\\-_a-zA-Z0-9]{1,20}[\\〔|\\[][0-9]{4}\\〕[\\u4e00-\\u9fa5_a-zA-Z0-9]{1,10}", str);
//        return Pattern.matches("^[\\u4e00-\\u9fa5_a-zA-Z0-9]+\\〔[0-9]{4}\\〕+[\\u4e00-\\u9fa5_a-zA-Z0-9]", str);
    }
    public static void main(String[] args) {
        String s = "asdf黑发_-发顿发[1234〕asdfasd顿发a";
        System.out.println(dealSeqValid(s));
    }

}
