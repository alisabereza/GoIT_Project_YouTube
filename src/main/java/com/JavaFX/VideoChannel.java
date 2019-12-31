package com.JavaFX;


import javafx.scene.image.ImageView;

import java.util.List;

public class VideoChannel {
    private String name;
    private String description;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ImageView getBannerImage() {
        return bannerImage;
    }

    public List<Video> getLatestVideos() {
        return latestVideos;
    }

    public VideoChannel(String name, String description, ImageView bannerImage, List<Video> latestVideos) {
        this.name = name;
        this.description = description;
        this.bannerImage = bannerImage;
        this.latestVideos = latestVideos;
    }

    private ImageView bannerImage;
    private List<Video> latestVideos;
}
