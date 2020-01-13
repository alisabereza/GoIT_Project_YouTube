package com.data;

import com.JavaFX.Video;
import com.JavaFX.VideoChannel;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
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
    private static final String properties_FILENAME = "/youtube.properties";
    private static final JsonFactory JSON_FACTORY = new JacksonFactory();
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static final Properties PROPERTIES = new Properties();
    private static final YouTube YOUTUBE = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, request -> {
    }).setApplicationName("youtube-search").build();

    // This method returns Video List
    public static List<Video> getVideoList(String queryTerm, int maxNumberToShow, int numberOfDaysToShow) {

        YouTube.Search.List search = getYoutubeSearchList();
        LocalDateTime fromDate = LocalDateTime.now().minusDays(numberOfDaysToShow);

        if (search != null) {
            search.setKey(PROPERTIES.getProperty("youtube.apikey"));
            search.setQ(queryTerm);
            search.setType("video").setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url,snippet/channelTitle,snippet/channelId,snippet/publishedAt)");
            search.setMaxResults((long) maxNumberToShow);
            search.setPublishedAfter(DateTime.parseRfc3339(fromDate.toString()));
        }
        return searchResult(Objects.requireNonNull(search));
    }

    // This method is to get channel information
    public static VideoChannel getChannelInfo(String channelID) throws IOException, ExecutionException, InterruptedException {
        List<Video> channelVideos;
        VideoChannel channel;
        ChannelListResponse channelListResponse = YOUTUBE.channels().list("id,snippet,contentDetails,brandingSettings")
                .setId(channelID)
                .setFields("items(id,snippet/title,snippet/description,snippet/thumbnails(default))")
                .setKey(PROPERTIES.getProperty("youtube.apikey"))
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

        YouTube.Search.List search = getYoutubeSearchList();
        if (search != null) {
            search.setKey(PROPERTIES.getProperty("youtube.apikey"));
            search.setChannelId(channelID);
            search.setType("video");
            search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url,snippet/channelTitle,snippet/channelId,snippet/publishedAt)");
            search.setMaxResults((long) maxNumberToShow);
        }
        return searchResult(Objects.requireNonNull(search));
    }

    private static YouTube.Search.List getYoutubeSearchList() {
        try {
            InputStream in = Search.class.getResourceAsStream(properties_FILENAME);
            PROPERTIES.load(in);

        } catch (IOException e) {
            System.err.println("There was an error reading " + properties_FILENAME + ": " + e.getCause()
                    + " : " + e.getMessage());
            System.exit(1);
        }
        YouTube.Search.List search = null;
        try {
            search = YOUTUBE.search().list("id,snippet");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return search;
    }

    private static List<Video> searchResult(YouTube.Search.List search) {
        List<Video> videos = new ArrayList<>();
        SearchListResponse searchResponse = null;
        List<SearchResult> searchResultList;
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
                videos.sort(Comparator.reverseOrder());
            }
        }
        return videos;
    }
}

