package ru.ifmo.rain.pakulev.statistic;

import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

public class Test {
    public static void main(String[] args) {
//        Arrays.stream(Locale.getAvailableLocales()).map(Locale::getDisplayName).sorted().forEachOrdered(System.out::println);
//        Locale loc = new Locale("th","TH","TH_#u-nu-thai");
//        loc.set
//        System.err.println(loc.getDisplayName());

//        String s = "Norwegian Spanish (United States)";
//        System.out.println(s.split("\\)").length);
//        ResourceBundle bundle = ResourceBundle.getBundle(
//                "ru.ifmo.rain.pakulev.statistic.UsageResourceBundle_en",
//                Locale.FRANCE
//        );
//
//        System.out.println(String.format(
//                "%s Test [%s] %s\n" +
//                        "%s\n" +
//                        "     -o %s\n"+
//                        "...",
//                bundle.getStringArray("analise")[1],      // Usage:
//                bundle.getString("stat"),         // <options>
//                bundle.getString("commands"),   // <commands>
//                bundle.getString("Options"),        // Options:
//                bundle.getString("-o")          // Write output
//        ));
        Double d = 4.97542;
        int i = (int) d.doubleValue();

// or directly:
        int i2 = d.intValue();
        System.out.println(i2);
    }
}
