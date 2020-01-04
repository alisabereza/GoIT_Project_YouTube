package com.JavaFX;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebHistory.Entry;
import javafx.scene.web.WebView;
import javafx.util.Callback;

class BrowserHistory extends HBox {
    BrowserHistory(WebView webView) {
        // Set Spacing
        this.setSpacing(20);

        // Set the Style-properties of the Navigation Bar
        this.setStyle("-fx-padding: 10;" +
                "-fx-border-style: solid inside;" +
                "-fx-border-width: 2;" +
                "-fx-border-insets: 5;" +
                "-fx-border-radius: 5;" +
                "-fx-border-color: blue;");

        // Create the WebHistory
        WebHistory history = webView.getEngine().getHistory();

        // Create the Label
        Label label = new Label("History:");
        label.alignmentProperty().set(Pos.CENTER_LEFT);

        // Create the Buttons
        Button backButton = new Button("Back");
        backButton.setDisable(true);
        Button forwardButton = new Button("Forward");
        forwardButton.setDisable(true);

        // Add an ActionListener to the Back and Forward Buttons
        backButton.setOnAction(event -> history.go(-1));

        forwardButton.setOnAction(event -> {
            try {
                history.go(1);
            } catch (IndexOutOfBoundsException e) {
                System.out.println("Current video is the latest one.");
            }
        });

        // Add an ChangeListener to the currentIndex property
        history.currentIndexProperty().addListener((ov, oldValue, newValue) -> {
            int currentIndex = newValue.intValue();

            if (currentIndex <= 0) {
                backButton.setDisable(true);
            } else {
                backButton.setDisable(false);
            }

            if (currentIndex >= history.getEntries().size()) {
                forwardButton.setDisable(true);
            } else {
                forwardButton.setDisable(false);
            }
        });


        // Create the ComboBox for the History List
        ComboBox<Entry> historyList = new ComboBox<>();
        historyList.setPrefWidth(500);
        historyList.setItems(history.getEntries());

        // Set a cell factory to to show only the page title in the history list
        historyList.setCellFactory(new Callback<ListView<WebHistory.Entry>, ListCell<WebHistory.Entry>>() {
            @Override
            public ListCell<WebHistory.Entry> call(ListView<WebHistory.Entry> list) {

                return new ListCell<Entry>() {
                    @Override
                    public void updateItem(Entry item, boolean empty) {
                        super.updateItem(item, empty);

                        if (empty) {
                            this.setText(null);
                            this.setGraphic(null);
                        } else {
                            String pageTitle = item.getTitle();
                            this.setText(pageTitle);
                        }
                    }
                };
            }
        });


        // Let the user navigate to a page using the history list
        historyList.setOnAction(event -> {
            int currentIndex = history.getCurrentIndex();
            Entry selectedEntry = historyList.getValue();
            int selectedIndex = historyList.getItems().indexOf(selectedEntry);
            int offset = selectedIndex - currentIndex;
            history.go(offset);
        });

        // Add the Children to the BrowserHistory
        this.getChildren().addAll(backButton, forwardButton, label, historyList);
    }
}
