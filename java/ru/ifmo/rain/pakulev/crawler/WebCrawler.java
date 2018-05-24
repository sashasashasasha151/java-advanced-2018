package ru.ifmo.rain.pakulev.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class WebCrawler implements Crawler {
    private Downloader downloader;
    private ExecutorService downloaders;
    private ExecutorService extractors;

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.downloaders = Executors.newFixedThreadPool(downloaders);
        this.extractors = Executors.newFixedThreadPool(extractors);
    }

    public Result download(String url, int depth) {
        Queue<String> pages = new ConcurrentLinkedQueue<>();
        Map<String, IOException> badPages = new ConcurrentHashMap<>();
        Set<String> hashedURLs = ConcurrentHashMap.newKeySet();
        Queue<String> urlQueue = new ConcurrentLinkedQueue<>();
        Queue<Future<List<String>>> docQueue = new ConcurrentLinkedQueue<>();

        urlQueue.add(url);
        hashedURLs.add(url);

        for (int i = 0; i < depth; ++i) {
            while (!urlQueue.isEmpty()) {
                final String link = urlQueue.poll();
                Future<Document> document = downloaders.submit(() -> {
                    Document doc = null;
                    try {
                        doc = downloader.download(link);
                        pages.add(link);
                    } catch (IOException e) {
                        badPages.put(link, e);
                    }
                    return doc;
                });

                docQueue.add(extractors.submit(() -> {
                    try {
                        return document.get().extractLinks();
                    } catch (IOException | NullPointerException e) {
                        return new ArrayList<>();
                    }
                }));
            }

            for (Future<List<String>> docs : docQueue) {
                try {
                    for (String doc : docs.get()) {
                        if (!hashedURLs.contains(doc)) {
                            hashedURLs.add(doc);
                            urlQueue.add(doc);
                        }
                    }
                } catch (InterruptedException | ExecutionException ignored) {
                }
            }
            docQueue.clear();
        }


        return new Result(new ArrayList<>(pages), badPages);
    }

    public void close() {
        downloaders.shutdown();
        extractors.shutdown();
    }

    public static void main(String[] args) {
        if (args == null) {
            System.err.println("No arguments");
            return;
        }
        if (args.length != 5) {
            System.err.println("Five arguments excepted");
            return;
        }
        for (String s : args) {
            if (s == null) {
                System.err.println("Null argument");
                return;
            }
        }

        try {
            String url = args[0];
            int depth = Integer.parseInt(args[1]);
            int downloads = Integer.parseInt(args[2]);
            int extractors = Integer.parseInt(args[3]);
            int perHost = Integer.parseInt(args[4]);
            WebCrawler crawler = new WebCrawler(new CachingDownloader(), downloads, extractors, perHost);
            crawler.download(url, depth);
        } catch (NumberFormatException | IOException e) {
            System.err.println(e.toString());
        }
    }
}