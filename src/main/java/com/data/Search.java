package com.data;


import com.Auth;
import com.JavaFX.Video;
import com.JavaFX.VideoChannel;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import javafx.scene.control.Hyperlink;
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

    private static final String CLIENT_SECRETS = "client_secret.json";
    private static final Collection<String> SCOPES =
            Arrays.asList("https://www.googleapis.com/auth/youtube.readonly");

    private static final String APPLICATION_NAME = "API code samples";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static Properties properties;
    private static YouTube youtube;

    //private static Scanner scanner = new Scanner(System.in);

    //Standard methods are commented for now, but not deleted for any case

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        //     System.out.println("Enter NAME");
        //      String finder = scanner.nextLine();
        getChannelInfo("UC5A8ElbxeHIJgLwf1k8wb5Q");
    }

    // This method is for test purposes. To be removed in final project
    private static void connectClient(String queryTerm) {
        properties = new Properties();
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

    // This method is for test purposes. To be removed in final project
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

    // This method returns Video List
    private static List<Video> connectClientList(String queryTerm, int maxNumberToShow, int numberOfDaysToShow) {
        List<Video> videos = new ArrayList<>();
        properties = new Properties();
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
        List<SearchResult> searchResultList;
        if (search != null) {
            search.setKey(properties.getProperty("youtube.apikey"));
            search.setQ(queryTerm);
            search.setType("video");
            search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url,snippet/channelTitle,snippet/channelId,snippet/publishedAt)");
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
            ImageView imageView;
            Hyperlink hpl;
            while (iterator.hasNext()) {

                SearchResult singleVideo = iterator.next();
                ResourceId rId = singleVideo.getId();

                if (rId.getKind().equals("youtube#video")) {
                    Thumbnail thumbnail = singleVideo.getSnippet().getThumbnails().getDefault();
                    imageView = new ImageView(new Image(thumbnail.getUrl()));
                    hpl = new Hyperlink(singleVideo.getSnippet().getChannelTitle());


                    video = new Video(rId.getVideoId(),
                            singleVideo.getSnippet().getTitle(),
                            hpl,
                            singleVideo.getSnippet().getChannelId(),
                            singleVideo.getSnippet().getPublishedAt(),
                            imageView);
                    videos.add(video);

                }

            }
        }
        return videos;
    }

    // This method is to run connectClientList from Player class in separate thread. In Player class, everything is run in same JavaFX Application Thread.
    public static List<Video> getFutureSearchResults(String queryTerm, int maxNumberToShow, int numberOfDaysToShow) throws ExecutionException, InterruptedException {
        ExecutorService service = Executors.newSingleThreadExecutor();
        List<Video> list = service.submit(() -> connectClientList(queryTerm, maxNumberToShow, numberOfDaysToShow)).get();
        service.shutdown();
        return list;

    }

    // This method is to get channel information
    public static VideoChannel getChannelInfo(String channelID) throws IOException, ExecutionException, InterruptedException {
        List<Video> channelVideos;
        VideoChannel channel;
        youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, request -> {
        }).setApplicationName("youtube-search").build();
        ChannelListResponse channelListResponse = youtube.channels().list("id,snippet,contentDetails,brandingSettings")
                .setId(channelID)
                .setFields("items(id,snippet/title,snippet/description,snippet/thumbnails(default))")
                .setKey(properties.getProperty("youtube.apikey"))
                .execute();

        Channel myChannel = channelListResponse.getItems().get(0);
        ExecutorService service = Executors.newSingleThreadExecutor();
        channelVideos = service.submit(() -> channelVideos(channelID, 10)).get();
        channel = new VideoChannel(myChannel.getSnippet().getTitle(), myChannel.getSnippet().getDescription(), new ImageView(new Image(myChannel.getSnippet().getThumbnails().getDefault().getUrl())), channelVideos);
        service.shutdown();
        return channel;
    }

    // This method is to get a list of Videos of certain Channel.
    private static List<Video> channelVideos(String channelID, int maxNumberToShow) {
        List<Video> videos = new ArrayList<>();
        properties = new Properties();
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
        List<SearchResult> searchResultList;
        if (search != null) {
            search.setKey(apiKey);
            search.setChannelId(channelID);
            search.setType("video");
            search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url,snippet/channelTitle,snippet/channelId,snippet/publishedAt)");
            search.setMaxResults((long) maxNumberToShow);
            SearchListResponse searchResponse = null;

            try {

                searchResponse = search.execute();

            } catch (IOException e) {
                e.printStackTrace();
            }
            searchResultList = Objects.requireNonNull(searchResponse).getItems();
            Iterator<SearchResult> iterator = searchResultList.iterator();
            Video video;
            ImageView imageView;
            Hyperlink hpl;
            while (iterator.hasNext()) {

                SearchResult singleVideo = iterator.next();
                ResourceId rId = singleVideo.getId();

                if (rId.getKind().equals("youtube#video")) {
                    Thumbnail thumbnail = singleVideo.getSnippet().getThumbnails().getDefault();
                    imageView = new ImageView(new Image(thumbnail.getUrl()));
                    hpl = new Hyperlink(singleVideo.getSnippet().getChannelTitle());


                    video = new Video(rId.getVideoId(),
                            singleVideo.getSnippet().getTitle(),
                            hpl,
                            singleVideo.getSnippet().getChannelId(),
                            singleVideo.getSnippet().getPublishedAt(),
                            imageView);
                    videos.add(video);

                }

            }
        }
        return videos;
    }

}

