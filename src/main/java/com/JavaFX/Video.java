package com.JavaFX;


import com.google.api.client.util.DateTime;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.ImageView;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Video implements Comparable<Video>{
    private String id;
    private String name;
    private Hyperlink channelName;
    private String channelId;
    private DateTime date;
    private ImageView thumbnail;


    public String getName() {
        return name;
    }

    public Hyperlink getChannelName() {
        return channelName;
    }

    public String getChannelId() {
        return channelId;
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


    public Video(String id, String name, Hyperlink channelName, String channelId, DateTime date, ImageView thumbnail) {
        this.id = id;
        this.name = name;
        this.channelName = channelName;
        this.channelId = channelId;
        this.date = date;
        this.thumbnail = thumbnail;
        }



    @Override
    public int compareTo(Video o) {
        return  ZonedDateTime.parse(this.getDate().toStringRfc3339(), DateTimeFormatter.ISO_DATE_TIME).compareTo(ZonedDateTime.parse(o.getDate().toStringRfc3339(), DateTimeFormatter.ISO_DATE_TIME));

    }
}
