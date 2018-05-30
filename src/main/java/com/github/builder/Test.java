package com.github.builder;

import java.util.Map;
import java.util.TreeMap;

/**

 */
public class Test {
    public static void main(String... args) {

//        should be alias test test, and alias test.test1 test1
        System.out.println(getAliasMap("test.test1.testValue.testValue2"));

    }

    private static Map<String, String> getAliasMap(String arg) {
        Map<String, String> aliasMap = new TreeMap<>((o1, o2) -> o1.split("\\.").length-o2.split("\\.").length);

        String[] tmp = arg.split("\\.");
        for (int i = 0; i < tmp.length-1; i++) {
            if (i != 0) {
                StringBuilder builder = new StringBuilder();
                for (int s = 0; s < i+1; s++) {
                    builder.append(tmp[s]);
                    if (s != i) {
                        builder.append(".");
                    }
                }
                aliasMap.put( builder.toString(),tmp[i]);
            } else {
                aliasMap.put(tmp[i], tmp[i]);
            }
        }
        return aliasMap;

    }
}
