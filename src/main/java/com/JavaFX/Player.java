package com.JavaFX;


import com.data.Search;
import com.google.api.client.util.DateTime;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;


public class Player extends Application {
    private Group root = new Group();
    final private int WIDTH = 1260;
    final private int HEIGHT = 900;
    private VBox strings = new VBox();
    private HBox search = new HBox();
    private HBox action = new HBox();
    private HBox result = new HBox();
    private HBox channelInfo = new HBox();
    private TextField videoName = new TextField();
    private TextField maxResult = new TextField();
    private TextField numberOfDays = new TextField();
    private TableView<Video> table;
    private Button show = new Button("Show");
    private Button advanced = new Button("Advanced");
    private Text partOfName = new Text("Part of video name: ");
    private Text warning = new Text();
    private Hyperlink hyperlink = new Hyperlink();
    private WebView browser = new WebView();

    private ImageView channelAvatar = new ImageView();
    private Text channelName = new Text();
    private Text channelDesc = new Text();
    private Text channelText = new Text("CHANNEL: ");
    private TableView<Video> channelVideos;

    private void setScene(Stage primaryStage) {
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        primaryStage.setTitle("YouTube");
        primaryStage.setScene(scene);
        primaryStage.show();
        root.getChildren().add(strings);
        strings.setPadding(new Insets(30, 30, 10, 30));
        strings.setSpacing(20);
        strings.getChildren().add(search);
        search.setSpacing(10);
        search.getChildren().addAll(partOfName, videoName);
        strings.getChildren().add(action);
        action.setSpacing(30);
        action.getChildren().addAll(show, advanced);
        action.getChildren().add(warning);
        strings.getChildren().add(result);
        result.getChildren().add(table);
    }

    private void playButtonTask(String videoId) {
        Stage stage = new Stage();
        WebView webview = new WebView();
        webview.getEngine().load(
                "http://www.youtube.com/embed/" + videoId + "?autoplay=1"
        );
        webview.setPrefSize(640, 390);

        stage.setScene(new Scene(webview));
        stage.show();
        stage.setOnCloseRequest(event -> webview.getEngine().load(""));

    }

    private void addViewButton(TableColumn<Video, Void> playColumn) {
        Callback<TableColumn<Video, Void>, TableCell<Video, Void>> cellFactory = new Callback<TableColumn<Video, Void>, TableCell<Video, Void>>() {
            @Override
            public TableCell<Video, Void> call(final TableColumn<Video, Void> param) {

                return new TableCell<Video, Void>() {

                    private final Button btn = new Button("Play");

                    {
                        btn.setOnAction((ActionEvent event) -> Platform.runLater(() -> playButtonTask(getTableView().getItems().get(getIndex()).getId())));
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btn);
                        }
                    }
                };
            }
        };
        playColumn.setMinWidth(80);
        playColumn.setCellFactory(cellFactory);
    }


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        TableColumn<Video, String> nameColumn = new TableColumn<>("Video Name");
        nameColumn.setMinWidth(500);
        nameColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Video, Hyperlink> channelColumn = new TableColumn<>("Channel");
        channelColumn.setStyle("-fx-alignment: CENTER;");
        channelColumn.setMinWidth(200);
        channelColumn.setCellValueFactory(new PropertyValueFactory<>("channelName"));
        channelColumn.setCellFactory(column -> {
            return new TableCell<Video, Hyperlink>() {

                @Override
                protected void updateItem(Hyperlink item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                    } else {

                        item.setOnAction(event -> {

                            VideoChannel videoChannel = null;
                            channelVideos = new TableView<>();
                            try {
                                videoChannel = Search.getChannelInfo(table.getItems().get(getIndex()).getChannelId());
                            } catch (IOException e) {
                                System.out.println(e.getMessage());
                            } catch (InterruptedException | ExecutionException e) {
                                System.out.println(e.getMessage());
                            }

                            channelName.setText(Objects.requireNonNull(videoChannel).getName());

                            channelName.setStyle("-fx-alignment: CENTER;");
                            channelDesc.setText(videoChannel.getDescription());
                            channelDesc.setTextAlignment(TextAlignment.LEFT);

                            channelAvatar = videoChannel.getBannerImage();
                            channelAvatar.setFitWidth(150);
                            channelAvatar.setFitHeight(150);
                            channelAvatar.autosize();
                            strings.getChildren().remove(result);
                            strings.getChildren().add(channelInfo);
                            strings.getChildren().add(result);
                            result.getChildren().remove(table);
                            channelInfo.setSpacing(10);
                            channelInfo.getChildren().addAll(channelAvatar, channelText, channelName, channelDesc);

                            TableColumn<Video, String> channelVideoColumn = new TableColumn<>("Video");
                            channelVideoColumn.setMinWidth(700);
                            channelVideoColumn.setStyle("-fx-alignment: CENTER-LEFT;");
                            channelVideoColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

                            TableColumn<Video, String> channelNameColumn = new TableColumn<>("Channel");
                            channelNameColumn.setStyle("-fx-alignment: CENTER;");
                            channelNameColumn.setMinWidth(200);
                            channelNameColumn.setCellValueFactory(new PropertyValueFactory<>("channelName"));

                            TableColumn<Video, DateTime> channelDateColumn = new TableColumn<>("Published_Date");
                            channelDateColumn.setMinWidth(200);
                            channelDateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
                            channelDateColumn.setStyle("-fx-alignment: CENTER;");
                            channelDateColumn.setCellFactory(column -> new TableCell<Video, DateTime>() {

                                @Override
                                protected void updateItem(DateTime item, boolean empty) {
                                    super.updateItem(item, empty);
                                    if (empty) {
                                        setText(null);
                                    } else {
                                        final ZonedDateTime dateTime = ZonedDateTime.parse(table.getItems().get(getIndex()).getDate().toString(), DateTimeFormatter.ISO_DATE_TIME);
                                        setText(dateTime.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")));
                                    }
                                }
                            });

                            TableColumn channelPlayColumn = new TableColumn("Play");
                            channelPlayColumn.setStyle("-fx-alignment: CENTER;");
                            Platform.runLater(() -> {
                                addViewButton(channelPlayColumn);
                            });

                            channelVideos.getColumns().addAll(channelVideoColumn, channelNameColumn, channelDateColumn, channelPlayColumn);
                            ObservableList<Video> channelList = FXCollections.observableList(Objects.requireNonNull(videoChannel.getLatestVideos()));
                            channelVideos.setItems(channelList);

                            result.getChildren().add(channelVideos);

                        });
                        setGraphic(item);

                    }
                }
            };
        });

        TableColumn<Video, DateTime> dateColumn = new TableColumn<>("Published Date");
        dateColumn.setMinWidth(200);
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateColumn.setStyle("-fx-alignment: CENTER;");
        dateColumn.setCellFactory(column -> new TableCell<Video, DateTime>() {

            @Override
            protected void updateItem(DateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    final ZonedDateTime dateTime = ZonedDateTime.parse(table.getItems().get(getIndex()).getDate().toString(), DateTimeFormatter.ISO_DATE_TIME);
                    setText(dateTime.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")));
                }
            }
        });


        TableColumn<Video, ImageView> thumbColumn = new TableColumn<>("Thumbnail");
        thumbColumn.setMinWidth(200);
        Platform.runLater(() -> thumbColumn.setCellValueFactory(new PropertyValueFactory<>("thumbnail")));

        TableColumn playColumn = new TableColumn("Play");
        playColumn.setStyle("-fx-alignment: CENTER;");
        Platform.runLater(() -> {
            addViewButton(playColumn);
        });


        table = new TableView<>();
        table.setPrefSize(1200, 700);
        table.getColumns().addAll(nameColumn, channelColumn, dateColumn, thumbColumn, playColumn);


        show.setOnAction(e -> {
            if (!maxResult.getText().matches("[0-9]*")
                    || !numberOfDays.getText().matches("[0-9]*")) {
                warning.setText("Incorrect input. Number of Days and Max Result should be positive numbers. Try again");

            } else {

                if (!result.getChildren().contains(table)) {
                    result.getChildren().remove(channelVideos);
                    strings.getChildren().remove(channelInfo);
                    channelInfo.getChildren().removeAll(channelAvatar, channelName, channelText, channelDesc);


                    browser.getEngine().load("");
                    result.getChildren().add(table);
                }
                List<Video> videos = null;
                int maxNumberToShow = maxResult.getText().equals("") ? 10 : Integer.parseInt(maxResult.getText());
                int numberOfDaysToShow = numberOfDays.getText().equals("") ? 365 : Integer.parseInt(numberOfDays.getText());
                try {
                    videos = Search.getFutureSearchResults(videoName.getText(), maxNumberToShow, numberOfDaysToShow);

                } catch (ExecutionException | InterruptedException ex) {
                    System.out.println(ex.getMessage());
                }
                ObservableList<Video> observableList = FXCollections.observableList(Objects.requireNonNull(videos));
                table.setItems(observableList);
                videoName.clear();
                maxResult.clear();
                numberOfDays.clear();

            }
        });

        advanced.setOnAction(e -> search.getChildren().addAll(new Text("MAX Results: "), maxResult, new Text("Number Of Days: "), numberOfDays));

        setScene(primaryStage);

    }

}

