package ru.ifmo.rain.pakulev.statistic;

import net.java.quickcheck.collection.Pair;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.text.*;
import java.util.*;
import java.util.stream.Collectors;


public class TextStatistics {
    private String txt;
    private Locale locale;

    private TextStatistics(Locale inputLocale, String txt) {
        this.txt = txt;
        this.locale = inputLocale;
    }

    private StatInfo getStat(BreakIterator iterator, boolean type) {
        StatInfo stat = new StatInfo();

        Set<String> stringSet = new HashSet<>();
        int lengthSum = 0;
        Collator comp = Collator.getInstance(locale);

        iterator.setText(txt);
        int start = iterator.first();
        int end = iterator.next();

        while (end != BreakIterator.DONE) {
            String elem = (type) ? txt.substring(start, end) : txt.substring(start, end).replaceAll("\\s", "");
            if (elem.isEmpty()) {
                start = end;
                end = iterator.next();
                continue;
            }

            stat.numberOfElements++;

            if (!stringSet.contains(elem)) {
                stringSet.add(elem);
                stat.uniqueElements++;
            }

            if (stat.minLengthElement == null || elem.length() < stat.minLength) {
                stat.minLengthElement = elem;
                stat.minLength = elem.length();
            }

            if (stat.maxLengthElement == null || elem.length() > stat.maxLength) {
                stat.maxLengthElement = elem;
                stat.maxLength = elem.length();
            }

            if (stat.minElement == null || comp.compare(elem, stat.minElement) < 0) {
                stat.minElement = elem;
            }

            if (stat.maxElement == null || comp.compare(stat.maxElement, elem) < 0) {
                stat.maxElement = elem;
            }

            lengthSum += elem.length();

            start = end;
            end = iterator.next();
        }

        stat.averageLength = lengthSum / stat.numberOfElements;

        return stat;
    }

    private List<StatInfo> getNumbersStat() {
        StatInfo statCur = new StatInfo();
        StatInfo statDate = new StatInfo();
        StatInfo statNum = new StatInfo();

        BreakIterator iterator = BreakIterator.getWordInstance(locale);

        NumberFormat isNumber = NumberFormat.getNumberInstance(locale);
        NumberFormat isCurrency = NumberFormat.getCurrencyInstance(locale);
        DateFormat isDate = DateFormat.getDateInstance(DateFormat.SHORT, locale);

        iterator.setText(txt);
        int start = iterator.first();
        int end = iterator.next();

        List<Pair<Number, String>> lstCur = new ArrayList<>();
        List<Pair<Date, String>> lstDate = new ArrayList<>();
        List<Double> lstNum = new ArrayList<>();

        while (end != BreakIterator.DONE) {
            String elem = txt.substring(start, end);
            start = end;
            end = iterator.next();
            try {
                Date n = isDate.parse(elem);
                lstDate.add(new Pair<>(n, elem));
                lstNum.add((double) n.getDate());
                lstNum.add((double) (n.getMonth() + 1));
                lstNum.add((double) n.getYear());
                continue;
            } catch (ParseException ignored) {
            }
            try {
                Number n = isCurrency.parse(elem);
                lstCur.add(new Pair<>(n, elem));
                lstNum.add(n.doubleValue());
                continue;
            } catch (ParseException ignored) {
            }
            try {
                Number n = isNumber.parse(elem);
                lstNum.add(n.doubleValue());
            } catch (ParseException ignored) {
            }
        }

        if (!lstCur.isEmpty()) {
            statCur.numberOfElements = lstCur.size();
            statCur.minElement = lstCur.stream().reduce((e1, e2) -> (e1.getFirst().doubleValue() < e2.getFirst().doubleValue()) ? e1 : e2).get().getSecond();
            statCur.maxElement = lstCur.stream().reduce((e1, e2) -> (e1.getFirst().doubleValue() > e2.getFirst().doubleValue()) ? e1 : e2).get().getSecond();
            statCur.minLengthElement = lstCur.stream().map(Pair::getSecond).min(Comparator.comparing(String::length)).get();
            statCur.minLength = statCur.minLengthElement.length();
            statCur.maxLengthElement = lstCur.stream().map(Pair::getSecond).max(Comparator.comparing(String::length)).get();
            statCur.maxLength = statCur.maxLengthElement.length();
            statCur.averageLength = lstCur.stream().map(e -> e.getFirst().doubleValue()).reduce((d1, d2) -> d1 + d2).get() / statCur.numberOfElements;
            statCur.uniqueElements = lstCur.stream().distinct().collect(Collectors.toList()).size();
        }

        if (!lstDate.isEmpty()) {
            statDate.numberOfElements = lstDate.size();
            statDate.minElement = lstDate.stream().reduce((e1, e2) -> (e1.getFirst().before(e2.getFirst())) ? e1 : e2).get().getSecond();
            statDate.maxElement = lstDate.stream().reduce((e1, e2) -> (e1.getFirst().after(e2.getFirst())) ? e1 : e2).get().getSecond();
            statDate.minLengthElement = lstDate.stream().map(Pair::getSecond).min(Comparator.comparing(String::length)).get();
            statDate.minLength = statDate.minLengthElement.length();
            statDate.maxLengthElement = lstDate.stream().map(Pair::getSecond).max(Comparator.comparing(String::length)).get();
            statDate.maxLength = statDate.maxLengthElement.length();
            statDate.uniqueElements = lstDate.stream().distinct().collect(Collectors.toList()).size();
        }

        if (!lstNum.isEmpty()) {
            statNum.numberOfElements = lstNum.size();
            statNum.minElement = lstNum.stream().min(Double::compare).get().toString();
            statNum.maxElement = lstNum.stream().max(Double::compare).get().toString();
            statNum.minLengthElement = lstNum.stream().map(d -> ((d == Math.floor(d)) && !Double.isInfinite(d)) ? Integer.toString(d.intValue()) : d.toString()).min(Comparator.comparing(String::length)).get();
            statNum.minLength = statNum.minLengthElement.length();
            statNum.maxLengthElement = lstNum.stream().map(d -> ((d == Math.floor(d)) && !Double.isInfinite(d)) ? Integer.toString(d.intValue()) : d.toString()).max(Comparator.comparing(String::length)).get();
            statNum.maxLength = statNum.maxLengthElement.length();
            statNum.uniqueElements = lstNum.stream().distinct().collect(Collectors.toList()).size();
            statNum.averageLength = lstNum.stream().reduce((d1, d2) -> d1 + d2).get() / statNum.numberOfElements;
        }


        List<StatInfo> lst = new ArrayList<>();
        lst.add(statCur);
        lst.add(statDate);
        lst.add(statNum);
        return lst;
    }

    public static void main(String[] args) {
        if (args == null) {
            System.err.println("Arguments are null");
            return;
        }
        for (String arg : args) {
            if (arg == null) {
                System.err.println("One of arguments is null");
                return;
            }
        }
        if (args.length < 4) {
            System.err.println("Not enough arguments");
            return;
        }

        String outputFile = args[args.length - 1];
        String inputFile = args[args.length - 2];
        String firstLocale, secondLocale;
        String locales = Arrays.stream(args).limit(args.length - 2).reduce((s1, s2) -> s1 + " " + s2).orElse("");
        String[] locs = locales.split("\\) ");
        if (locs.length == 1) {
            firstLocale = args[0];
            secondLocale = Arrays.stream(args).skip(1).limit(args.length - 3).reduce((s1, s2) -> s1 + " " + s2).orElse("");
        } else {
            firstLocale = locs[0] + ")";
            secondLocale = locs[1];
        }

        Locale firstLoc;
        Locale secondLoc;
        try {
            firstLoc = Arrays.stream(Locale.getAvailableLocales()).filter(l -> l.getDisplayName().equals(firstLocale)).findFirst().get();
            secondLoc = Arrays.stream(Locale.getAvailableLocales()).filter(l -> l.getDisplayName().equals(secondLocale)).findFirst().get();
        } catch (NoSuchElementException e) {
            System.err.println("Unavailable locale\n" + "Please use one of this locales:");
            Arrays.stream(Locale.getAvailableLocales()).map(Locale::getDisplayName).sorted().forEachOrdered(System.err::println);
            return;
        }

        String text;

        try {
            text = new String(Files.readAllBytes(Paths.get(inputFile)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Something wrong with your input");
            return;
        } catch (InvalidPathException e) {
            System.err.println("Invalid input file");
            return;
        }

        TextStatistics stat = new TextStatistics(firstLoc, text);
        StatInfo linesStat = stat.getStat(BreakIterator.getLineInstance(stat.locale), false);
        StatInfo sentenceStat = stat.getStat(BreakIterator.getSentenceInstance(stat.locale), true);
        StatInfo wordsStat = stat.getStat(BreakIterator.getWordInstance(stat.locale), false);
        List<StatInfo> lst = stat.getNumbersStat();
        StatInfo currencyStat = lst.get(0);
        StatInfo dateStat = lst.get(1);
        StatInfo numbersStat = lst.get(2);

        ResourceBundle bundle;
        switch (secondLoc.getLanguage()) {
            case "en":
                bundle = ResourceBundle.getBundle("ru.ifmo.rain.pakulev.statistic.UsageResourceBundle_en");
                break;
            case "ru":
                bundle = ResourceBundle.getBundle("ru.ifmo.rain.pakulev.statistic.UsageResourceBundle_ru");
                break;
            default:
                System.err.println("Unsupported uotput locale");
                return;
        }

        String out = String.format("<html>\n" +
                        " <head>\n" +
                        "  <meta charset=\"utf-8\">\n" +
                        "  <title>Text statistic</title>\n" +
                        " </head>\n" +
                        " <body>\n" +
                        "\n" +
                        "  <h1>%s: %s</h1>\n" +

                        "<p><b>%s:</b><br>\n" +
                        "%s %s: %d<br>\n" +
                        "%s %s: %d<br>\n" +
                        "%s %s: %d<br>\n" +
                        "%s %s: %d<br>\n" +
                        "%s %s: %d<br>\n" +
                        "%s %s: %d<br>\n</p>" +

                        "<p><b>%s:</b><br>\n" +
                        "%s %s: %d (%d %s)<br>\n" +
                        "%s %s: %s<br>\n" +
                        "%s %s: %s<br>\n" +
                        "%s %s %s: %d (%s)<br>\n" +
                        "%s %s %s: %d (%s)<br>\n" +
                        "%s %s %s: %d<br>\n</p>" +
                        "\n" +

                        "<p><b>%s:</b><br>\n" +
                        "%s %s: %d (%d %s)<br>\n" +
                        "%s %s: %s<br>\n" +
                        "%s %s: %s<br>\n" +
                        "%s %s %s: %d (%s)<br>\n" +
                        "%s %s %s: %d (%s)<br>\n" +
                        "%s %s %s: %d<br>\n</p>" +
                        "\n" +

                        "<p><b>%s:</b><br>\n" +
                        "%s %s: %d (%d %s)<br>\n" +
                        "%s %s: %s<br>\n" +
                        "%s %s: %s<br>\n" +
                        "%s %s %s: %d (%s)<br>\n" +
                        "%s %s %s: %d (%s)<br>\n" +
                        "%s %s %s: %d<br>\n</p>" +
                        "\n" +

                        "<p><b>%s:</b><br>\n" +
                        "%s %s: %d (%d %s)<br>\n" +
                        "%s %s: %s<br>\n" +
                        "%s %s: %s<br>\n" +
                        "%s %s %s: %d (%s)<br>\n" +
                        "%s %s %s: %d (%s)<br>\n" +
                        "%s %s: %f<br>\n</p>" +
                        "\n" +

                        "<p><b>%s:</b><br>\n" +
                        "%s %s: %d (%d %s)<br>\n" +
                        "%s %s: %s<br>\n" +
                        "%s %s: %s<br>\n" +
                        "%s %s %s: %d (%s)<br>\n" +
                        "%s %s %s: %d (%s)<br>\n" +
                        "%s %s: %f<br>\n</p>" +
                        "\n" +

                        "<p><b>%s:</b><br>\n" +
                        "%s %s: %d (%d %s)<br>\n" +
                        "%s %s: %s<br>\n" +
                        "%s %s: %s<br>\n" +
                        "%s %s %s: %d (%s)<br>\n" +
                        "%s %s %s: %d (%s)<br>\n" +
                        "\n" +
                        " </body>\n" +
                        "</html>",
                bundle.getString("analise"),
                inputFile,

                bundle.getString("common"),
                bundle.getString("Number"),
                bundle.getString("words"),
                wordsStat.numberOfElements,
                bundle.getString("Number"),
                bundle.getString("sentenses"),
                sentenceStat.numberOfElements,
                bundle.getString("Number"),
                bundle.getString("lines"),
                linesStat.numberOfElements,
                bundle.getString("Number"),
                bundle.getString("dates"),
                dateStat.numberOfElements,
                bundle.getString("Number"),
                bundle.getString("numberel"),
                numbersStat.numberOfElements,
                bundle.getString("Number"),
                bundle.getString("currency"),
                currencyStat.numberOfElements,

                bundle.getString("sentensesStat"),
                bundle.getString("Number"),
                bundle.getString("sentenses"),
                sentenceStat.numberOfElements,
                sentenceStat.uniqueElements,
                bundle.getString("unique"),
                bundle.getString("min"),
                bundle.getString("sentense"),
                sentenceStat.minElement,
                bundle.getString("max"),
                bundle.getString("sentense"),
                sentenceStat.maxElement,
                bundle.getString("maxya"),
                bundle.getString("length"),
                bundle.getString("sentensesya"),
                sentenceStat.maxLength,
                sentenceStat.minLengthElement,
                bundle.getString("minya"),
                bundle.getString("length"),
                bundle.getString("sentensesya"),
                sentenceStat.maxLength,
                sentenceStat.maxLengthElement,
                bundle.getString("averageya"),
                bundle.getString("length"),
                bundle.getString("sentensesya"),
                (int) sentenceStat.averageLength,

                bundle.getString("wordsStat"),
                bundle.getString("Number"),
                bundle.getString("words"),
                wordsStat.numberOfElements,
                wordsStat.uniqueElements,
                bundle.getString("unique"),
                bundle.getString("min"),
                bundle.getString("wordo"),
                wordsStat.minElement,
                bundle.getString("max"),
                bundle.getString("wordo"),
                wordsStat.maxElement,
                bundle.getString("maxya"),
                bundle.getString("length"),
                bundle.getString("word"),
                wordsStat.maxLength,
                wordsStat.minLengthElement,
                bundle.getString("minya"),
                bundle.getString("length"),
                bundle.getString("word"),
                wordsStat.maxLength,
                wordsStat.maxLengthElement,
                bundle.getString("averageya"),
                bundle.getString("length"),
                bundle.getString("word"),
                (int) wordsStat.averageLength,

                bundle.getString("linesStat"),
                bundle.getString("Number"),
                bundle.getString("line"),
                linesStat.numberOfElements,
                linesStat.uniqueElements,
                bundle.getString("unique"),
                bundle.getString("minya"),
                bundle.getString("linea"),
                linesStat.minElement,
                bundle.getString("maxya"),
                bundle.getString("linea"),
                linesStat.maxElement,
                bundle.getString("maxya"),
                bundle.getString("length"),
                bundle.getString("line"),
                linesStat.maxLength,
                linesStat.minLengthElement,
                bundle.getString("minya"),
                bundle.getString("length"),
                bundle.getString("line"),
                linesStat.maxLength,
                linesStat.maxLengthElement,
                bundle.getString("averageya"),
                bundle.getString("length"),
                bundle.getString("line"),
                (int) linesStat.averageLength,

                bundle.getString("currencyStat"),
                bundle.getString("Number"),
                bundle.getString("currency"),
                currencyStat.numberOfElements,
                currencyStat.uniqueElements,
                bundle.getString("unique"),
                bundle.getString("minya"),
                bundle.getString("sum"),
                currencyStat.minElement,
                bundle.getString("maxya"),
                bundle.getString("sum"),
                currencyStat.maxElement,
                bundle.getString("maxya"),
                bundle.getString("length"),
                bundle.getString("currency"),
                currencyStat.maxLength,
                currencyStat.minLengthElement,
                bundle.getString("minya"),
                bundle.getString("length"),
                bundle.getString("currency"),
                currencyStat.maxLength,
                currencyStat.maxLengthElement,
                bundle.getString("averageya"),
                bundle.getString("sum"),
                currencyStat.averageLength,

                bundle.getString("numbersStat"),
                bundle.getString("Number"),
                bundle.getString("numberel"),
                numbersStat.numberOfElements,
                numbersStat.uniqueElements,
                bundle.getString("unique"),
                bundle.getString("min"),
                bundle.getString("number"),
                numbersStat.minElement,
                bundle.getString("max"),
                bundle.getString("number"),
                numbersStat.maxElement,
                bundle.getString("maxya"),
                bundle.getString("length"),
                bundle.getString("numbers"),
                numbersStat.maxLength,
                numbersStat.minLengthElement,
                bundle.getString("minya"),
                bundle.getString("length"),
                bundle.getString("numbers"),
                numbersStat.maxLength,
                numbersStat.maxLengthElement,
                bundle.getString("average"),
                bundle.getString("number"),
                numbersStat.averageLength,

                bundle.getString("datesStat"),
                bundle.getString("Number"),
                bundle.getString("dates"),
                dateStat.numberOfElements,
                dateStat.uniqueElements,
                bundle.getString("unique"),
                bundle.getString("minya"),
                bundle.getString("datea"),
                dateStat.minElement,
                bundle.getString("maxya"),
                bundle.getString("datea"),
                dateStat.maxElement,
                bundle.getString("maxya"),
                bundle.getString("length"),
                bundle.getString("date"),
                dateStat.maxLength,
                dateStat.minLengthElement,
                bundle.getString("minya"),
                bundle.getString("length"),
                bundle.getString("date"),
                dateStat.maxLength,
                dateStat.maxLengthElement);

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
            writer.write(out);
            writer.close();
        } catch (IOException ignored) {
        }
    }
}
