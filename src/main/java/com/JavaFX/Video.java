package com.JavaFX;


import com.google.api.client.util.DateTime;
import javafx.scene.image.ImageView;

public class Video {
    private String id;
    private String name;
    private String channel;
    private DateTime date;
    private ImageView thumbnail;

    public String getName() {
        return name;
    }

    public String getChannel() {
        return channel;
    }

    public DateTime getDate() {
        return date;
    }

    public ImageView getThumbnail() {
        return thumbnail;
    }

    public String getId() {
        return id;
    }

    public Video(String id, String name, String channel, DateTime date, ImageView thumbnail) {
        this.id = id;
        this.name = name;
        this.channel = channel;
        this.date = date;
        this.thumbnail = thumbnail;
    }

}
