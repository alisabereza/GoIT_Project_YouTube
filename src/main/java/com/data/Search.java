package com.data;


import com.Auth;
import com.JavaFX.Video;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Thumbnail;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Search {
    private static final String PROPERTIES_FILENAME = "/youtube.properties";

    private static final long NUMBER_OF_VIDEOS_RETURNED = 25;

    private static Scanner scanner = new Scanner(System.in);

    //Standard methods are commented for now, but not deleted for any case
/*
    public static void main(String[] args) {
        System.out.println("Enter NAME");
        String finder = scanner.nextLine();
        connectClient(finder);
    }
*/

/*

    private static void connectClient(String queryTerm) {
        YouTube youtube;
        Properties properties = new Properties();
        youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, request -> {
        }).setApplicationName("youtube-search").build();
        try {
            InputStream in = Search.class.getResourceAsStream(PROPERTIES_FILENAME);
            properties.load(in);

        } catch (IOException e) {
            System.err.println("There was an error reading " + PROPERTIES_FILENAME + ": " + e.getCause()
                    + " : " + e.getMessage());
            System.exit(1);
        }
        YouTube.Search.List search = null;

        try {
            search = youtube.search().list("id,snippet");
        } catch (IOException e) {
            e.printStackTrace();
        }
        String apiKey = properties.getProperty("youtube.apikey");
        if (search != null) {
            search.setKey(apiKey);
            search.setQ(queryTerm);
            search.setType("video");
            search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url,snippet/channelTitle,snippet/publishedAt)");

            SearchListResponse searchResponse = null;
            try {
                searchResponse = search.execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            List<SearchResult> searchResultList;
            if (searchResponse != null) {
                searchResultList = searchResponse.getItems();
                prettyPrint(searchResultList.iterator(), queryTerm);
            }
        }

    }

    private static void prettyPrint(Iterator<SearchResult> iteratorSearchResults, String query) {

        System.out.println("\n=============================================================");
        System.out.println(
                "   First " + NUMBER_OF_VIDEOS_RETURNED + " videos for search on \"" + query + "\".");
        System.out.println("=============================================================\n");

        if (!iteratorSearchResults.hasNext()) {
            System.out.println(" There aren't any results for your query.");
        }

        while (iteratorSearchResults.hasNext()) {

            SearchResult singleVideo = iteratorSearchResults.next();
            ResourceId rId = singleVideo.getId();


            if (rId.getKind().equals("youtube#video")) {
                Thumbnail thumbnail = singleVideo.getSnippet().getThumbnails().getDefault();


                System.out.println(" Video Id: " + rId.getVideoId());
                System.out.println(" Title: " + singleVideo.getSnippet().getTitle());
                System.out.println(" Channel Title: " + singleVideo.getSnippet().getChannelTitle());
                System.out.println(" Published date : " + singleVideo.getSnippet().getPublishedAt());
                System.out.println(" Thumbnail: " + thumbnail.getUrl());
                System.out.println("\n-------------------------------------------------------------\n");
            }
        }
    }
*/

    // Newly added method to add search results to ArrayList
    private static List<Video> connectClientList(String queryTerm, int maxNumberToShow, int numberOfDaysToShow) {
        YouTube youtube;
        List<Video> videos = new ArrayList<>();
        Properties properties = new Properties();
        youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, request -> {
        }).setApplicationName("youtube-search").build();
        try {
            InputStream in = Search.class.getResourceAsStream(PROPERTIES_FILENAME);
            properties.load(in);

        } catch (IOException e) {
            System.err.println("There was an error reading " + PROPERTIES_FILENAME + ": " + e.getCause()
                    + " : " + e.getMessage());
            System.exit(1);
        }
        YouTube.Search.List search = null;
        LocalDateTime fromDate = LocalDateTime.now().minusDays(numberOfDaysToShow);
        try {
            search = youtube.search().list("id,snippet");
        } catch (IOException e) {
            e.printStackTrace();
        }
        String apiKey = properties.getProperty("youtube.apikey");
        List<SearchResult> searchResultList;
        if (search != null) {
            search.setKey(apiKey);
            search.setQ(queryTerm);
            search.setType("video");
            search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url,snippet/channelTitle,snippet/publishedAt)");
            search.setMaxResults((long) maxNumberToShow);
            search.setPublishedAfter(DateTime.parseRfc3339(fromDate.toString()));
            SearchListResponse searchResponse = null;

            try {

                searchResponse = search.execute();

            } catch (IOException e) {
                e.printStackTrace();
            }
            searchResultList = Objects.requireNonNull(searchResponse).getItems();
            Iterator<SearchResult> iterator = searchResultList.iterator();
            Video video;
            while (iterator.hasNext()) {

                SearchResult singleVideo = iterator.next();
                ResourceId rId = singleVideo.getId();

                if (rId.getKind().equals("youtube#video")) {
                    Thumbnail thumbnail = singleVideo.getSnippet().getThumbnails().getDefault();
                    ImageView imageView = new ImageView(new Image(thumbnail.getUrl()));
                    video = new Video(rId.getVideoId(), singleVideo.getSnippet().getTitle(), singleVideo.getSnippet().getChannelTitle(), singleVideo.getSnippet().getPublishedAt(), imageView);
                    videos.add(video);

                }

            }
        }
        return videos;
    }

    public static List<Video> getFutureSearchResults(String queryTerm, int maxNumberToShow, int numberOfDaysToShow) throws ExecutionException, InterruptedException {
        ExecutorService service = Executors.newSingleThreadExecutor();
        List<Video> list = service.submit(() -> connectClientList(queryTerm, maxNumberToShow, numberOfDaysToShow)).get();
        service.shutdown();
        return list;

    }

}

