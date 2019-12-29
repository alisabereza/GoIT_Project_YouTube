package com.JavaFX;


import com.data.Search;
import com.google.api.client.util.DateTime;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.data.Search.*;


public class Player extends Application {
    private Group root = new Group();
    final private int WIDTH = 1260;
    final private int HEIGHT = 900;
    private VBox strings = new VBox();
    private HBox search = new HBox();
    private HBox action = new HBox();
    private HBox result = new HBox();
    private TextField videoName = new TextField();
    private TextField maxResult = new TextField();
    private TextField numberOfDays = new TextField();
    private TableView<Video> table;
    private Button show = new Button("Show");
    private Button advanced = new Button("Advanced");
    private Text searchText = new Text("Search video on Youtube");
    private Text partOfName = new Text("Part of video name: ");


    public static void main(String[] args) {
        launch(args);
    }

    private void playButtonTask (String videoId) {
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


    @Override
    public void start(Stage primaryStage) {

        TableColumn<Video, String> idColumn = new TableColumn<>("Video ID");
        idColumn.setMinWidth(200);
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));


        TableColumn<Video, String> nameColumn = new TableColumn<>("Video Name");
        nameColumn.setMinWidth(300);
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Video, String> channelColumn = new TableColumn<>("Channel");
        channelColumn.setMinWidth(200);
        channelColumn.setCellValueFactory(new PropertyValueFactory<>("channel"));

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
        table.getColumns().addAll(idColumn, nameColumn, channelColumn, dateColumn, thumbColumn, playColumn);


        show.setOnAction(e -> {
            List<Video> videos = null;
            try {
                videos = Search.getFutureSearchResults(videoName.getText());
            } catch (ExecutionException|InterruptedException ex) {
                System.out.println(ex.getMessage());}
            ObservableList<Video> observableList = FXCollections.observableList(Objects.requireNonNull(videos));
            table.setItems(observableList);
        });

        advanced.setOnAction(e -> search.getChildren().addAll(new Text("MAX Results: "), maxResult, new Text("Number Of Days: "), numberOfDays));

setScene(primaryStage);

    }

    private void setScene (Stage primaryStage) {
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        primaryStage.setTitle("YouTube");
        primaryStage.setScene(scene);
        primaryStage.show();
        root.getChildren().add(strings);
        strings.setPadding(new Insets(30, 30, 10, 30));
        strings.setSpacing(20);
        strings.getChildren().add(searchText);
        strings.getChildren().add(search);
        search.setSpacing(10);
        search.getChildren().addAll(partOfName,videoName );
        strings.getChildren().add(action);
        action.setSpacing(30);
        action.getChildren().addAll(show, advanced);
        strings.getChildren().add(result);
        result.getChildren().add(table);
    }


}

