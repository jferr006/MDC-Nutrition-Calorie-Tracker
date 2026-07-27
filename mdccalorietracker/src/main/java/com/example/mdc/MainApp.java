package com.example.mdc;

import com.example.mdc.dao.FoodDao;
import com.example.mdc.model.FavoriteFood;
import com.example.mdc.model.FoodLog;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MainApp extends Application {

    private final FoodDao foodDao = new FoodDao();
    private int dailyGoal = 2000;

    // Global layout nodes
    private BorderPane rootLayout;
    private VBox dashboardView;
    private VBox myFoodsView;

    // Dashboard dynamic components
    private Label consumedLabel;
    private Label remainingLabel;
    private Label goalLabel;
    private ProgressBar progressBar;
    private VBox logTableContainer;

    // My Foods dynamic components
    private VBox favoritesListContainer;
    private String selectedCategory = "Breakfast";

    public static void main(String[] args) {
        Database.initializeDatabase();
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("MDC Student Wellness - Calorie Tracker");

        rootLayout = new BorderPane();
        rootLayout.setTop(createHeaderNav());

        // Build main views
        dashboardView = createDashboardView();
        myFoodsView = createMyFoodsView();

        // Default view
        rootLayout.setCenter(dashboardView);

        Scene scene = new Scene(rootLayout, 1100, 750);
        // Load CSS stylesheet
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();

        refreshData();
    }

    // --- NAVIGATION HEADER ---
    private HBox createHeaderNav() {
        HBox header = new HBox(20);
        header.setPadding(new Insets(15, 30, 15, 30));
        header.setStyle("-fx-background-color: #0032A0; -fx-border-color: #FFB81C; -fx-border-width: 0 0 3 0;");
        header.setAlignment(Pos.CENTER_LEFT);

        Label logoTitle = new Label("MDC Student Wellness\nCALORIE & NUTRITION TRACKER");
        logoTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnDashboard = new Button("Dashboard");
        Button btnMyFoods = new Button("My Foods");
        btnDashboard.getStyleClass().add("nav-button");
        btnMyFoods.getStyleClass().add("nav-button");

        btnDashboard.setOnAction(e -> {
            rootLayout.setCenter(dashboardView);
            refreshData();
        });

        btnMyFoods.setOnAction(e -> {
            rootLayout.setCenter(myFoodsView);
            refreshData();
        });

        header.getChildren().addAll(logoTitle, spacer, btnDashboard, btnMyFoods);
        return header;
    }

    // --- DASHBOARD VIEW ---
    private VBox createDashboardView() {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(25));
        layout.setStyle("-fx-background-color: #f0f2f7;");

        // Top Banner
        VBox banner = new VBox(5);
        banner.setPadding(new Insets(20));
        banner.setStyle("-fx-background-color: #0032A0; -fx-background-radius: 10;");
        Label dateLbl = new Label(LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        dateLbl.setStyle("-fx-text-fill: #b0c4de; -fx-font-size: 11px;");
        Label welcomeLbl = new Label("Good day, Student! 🏃");
        welcomeLbl.setStyle("-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;");
        banner.getChildren().addAll(dateLbl, welcomeLbl);

        // Stats Cards Layout
        HBox cardsBox = new HBox(15);
        
        // Card 1: Daily Target
        VBox goalCard = createCard("DAILY TARGET");
        goalLabel = new Label(dailyGoal + " kcal");
        goalLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #0032A0;");
        Button editGoalBtn = new Button("Edit Goal");
        editGoalBtn.setOnAction(e -> showEditGoalDialog());
        goalCard.getChildren().addAll(goalLabel, editGoalBtn);

        // Card 2: Consumed
        VBox consumedCard = createCard("CONSUMED TODAY");
        consumedLabel = new Label("0 kcal");
        consumedLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #0032A0;");
        progressBar = new ProgressBar(0.0);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        consumedCard.getChildren().addAll(consumedLabel, progressBar);

        // Card 3: Remaining
        VBox remainingCard = createCard("REMAINING");
        remainingLabel = new Label(dailyGoal + " kcal");
        remainingLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #FFB81C;");
        remainingCard.getChildren().add(remainingLabel);

        cardsBox.getChildren().addAll(goalCard, consumedCard, remainingCard);
        HBox.setHgrow(goalCard, Priority.ALWAYS);
        HBox.setHgrow(consumedCard, Priority.ALWAYS);
        HBox.setHgrow(remainingCard, Priority.ALWAYS);

        // Add Log Entry Form
        HBox addForm = new HBox(10);
        addForm.setPadding(new Insets(15));
        addForm.setStyle("-fx-background-color: white; -fx-background-radius: 8;");
        addForm.setAlignment(Pos.CENTER_LEFT);

        TextField txtName = new TextField();
        txtName.setPromptText("Food Name (e.g., Turkey Sandwich)");
        TextField txtCalories = new TextField();
        txtCalories.setPromptText("Calories (e.g., 350)");

        Button btnAdd = new Button("+ Add Food");
        btnAdd.setStyle("-fx-background-color: #0032A0; -fx-text-fill: white; -fx-font-weight: bold;");

        btnAdd.setOnAction(e -> {
            if (validateAndAddFood(txtName.getText(), txtCalories.getText())) {
                txtName.clear();
                txtCalories.clear();
                refreshData();
            }
        });

        HBox.setHgrow(txtName, Priority.ALWAYS);
        addForm.getChildren().addAll(new Label("Log Item:"), txtName, txtCalories, btnAdd);

        // Food Log List Container
        logTableContainer = new VBox(8);
        logTableContainer.setPadding(new Insets(15));
        logTableContainer.setStyle("-fx-background-color: white; -fx-background-radius: 8;");

        ScrollPane scrollPane = new ScrollPane(logTableContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        layout.getChildren().addAll(banner, cardsBox, addForm, scrollPane);
        return layout;
    }

    private VBox createCard(String title) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);");
        Label lblTitle = new Label(title);
        lblTitle.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #7a84a0;");
        card.getChildren().add(lblTitle);
        return card;
    }

    // --- MY FOODS VIEW ---
    private VBox createMyFoodsView() {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(25));
        layout.setStyle("-fx-background-color: #f0f2f7;");

        Label title = new Label("Saved Favorite Foods");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #0032A0;");

        HBox mainContent = new HBox(20);

        // Form to create new favorite
        VBox favForm = new VBox(12);
        favForm.setPadding(new Insets(20));
        favForm.setStyle("-fx-background-color: white; -fx-background-radius: 8;");
        favForm.setPrefWidth(320);

        Label formTitle = new Label("Add Favorite Food");
        formTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        TextField txtFavName = new TextField();
        txtFavName.setPromptText("Food Name");
        TextField txtFavCalories = new TextField();
        txtFavCalories.setPromptText("Calories");

        ComboBox<String> catBox = new ComboBox<>();
        catBox.getItems().addAll("Breakfast", "Lunch", "Dinner", "Snack", "Drink", "Other");
        catBox.setValue(selectedCategory);
        catBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedCategory = newVal;
            }
        });

        Button btnSaveFav = new Button("Save to Favorites");
        btnSaveFav.setMaxWidth(Double.MAX_VALUE);
        btnSaveFav.setStyle("-fx-background-color: #0032A0; -fx-text-fill: white; -fx-font-weight: bold;");

        btnSaveFav.setOnAction(e -> {
            if (validateAndSaveFavorite(txtFavName.getText(), txtFavCalories.getText(), catBox.getValue())) {
                txtFavName.clear();
                txtFavCalories.clear();
                refreshData();
            }
        });

        favForm.getChildren().addAll(formTitle, new Label("Name:"), txtFavName, new Label("Calories:"), txtFavCalories, new Label("Category:"), catBox, btnSaveFav);

        // Favorites List Container
        favoritesListContainer = new VBox(10);
        favoritesListContainer.setPadding(new Insets(15));
        favoritesListContainer.setStyle("-fx-background-color: white; -fx-background-radius: 8;");

        ScrollPane scroll = new ScrollPane(favoritesListContainer);
        scroll.setFitToWidth(true);
        HBox.setHgrow(scroll, Priority.ALWAYS);

        mainContent.getChildren().addAll(favForm, scroll);
        layout.getChildren().addAll(title, mainContent);

        return layout;
    }

    // --- REFRESH DATA & RE-RENDER UI ---
    private void refreshData() {
        try {
            // Update Dashboard Logs
            List<FoodLog> logs = foodDao.getAllLogs();
            logTableContainer.getChildren().clear();

            int totalConsumed = 0;
            Label header = new Label("Today's Food Log");
            header.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
            logTableContainer.getChildren().add(header);

            for (FoodLog log : logs) {
                totalConsumed += log.getCalories();

                HBox row = new HBox(15);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(8));
                row.setStyle("-fx-border-color: #eef0f7; -fx-border-width: 0 0 1 0;");

                Label name = new Label(log.getName());
                Label cal = new Label(log.getCalories() + " kcal");
                cal.setStyle("-fx-font-weight: bold; -fx-text-fill: #0032A0;");
                Label time = new Label(log.getTime());
                time.setStyle("-fx-text-fill: #888888;");

                Region r = new Region();
                HBox.setHgrow(r, Priority.ALWAYS);

                Button delBtn = new Button("✕");
                delBtn.setStyle("-fx-background-color: #fff0f0; -fx-text-fill: #e53e3e; -fx-font-weight: bold;");
                delBtn.setOnAction(e -> {
                    try {
                        foodDao.deleteLog(log.getId());
                        refreshData();
                    } catch (SQLException ex) {
                        showError("Delete Error", ex.getMessage());
                    }
                });

                row.getChildren().addAll(name, r, cal, time, delBtn);
                logTableContainer.getChildren().add(row);
            }

            // Update Header Metrics
            consumedLabel.setText(totalConsumed + " kcal");
            int remaining = Math.max(0, dailyGoal - totalConsumed);
            remainingLabel.setText(remaining + " kcal");
            progressBar.setProgress(Math.min(1.0, (double) totalConsumed / dailyGoal));

            // Update Favorites View
            List<FavoriteFood> favorites = foodDao.getAllFavorites();
            favoritesListContainer.getChildren().clear();

            Label favHeader = new Label("Saved Items (" + favorites.size() + ")");
            favHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
            favoritesListContainer.getChildren().add(favHeader);

            for (FavoriteFood fav : favorites) {
                HBox row = new HBox(15);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(10));
                row.setStyle("-fx-border-color: #e6eaf3; -fx-border-radius: 5; -fx-border-width: 1;");

                Label name = new Label(fav.getName());
                Label cat = new Label("[" + fav.getCategory() + "]");
                cat.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");
                Label cal = new Label(fav.getCalories() + " kcal");
                cal.setStyle("-fx-font-weight: bold; -fx-text-fill: #0032A0;");

                Region r = new Region();
                HBox.setHgrow(r, Priority.ALWAYS);

                Button logItBtn = new Button("+ Log It");
                logItBtn.setStyle("-fx-background-color: #0032A0; -fx-text-fill: white;");
                logItBtn.setOnAction(e -> {
                    String timeNow = LocalDateTime.now().format(DateTimeFormatter.ofPattern("h:mm a"));
                    try {
                        foodDao.addLog(new FoodLog(fav.getName(), fav.getCalories(), timeNow));
                        refreshData();
                    } catch (SQLException ex) {
                        showError("Database Error", ex.getMessage());
                    }
                });

                Button delBtn = new Button("✕");
                delBtn.setStyle("-fx-background-color: #fff0f0; -fx-text-fill: #e53e3e;");
                delBtn.setOnAction(e -> {
                    try {
                        foodDao.deleteFavorite(fav.getId());
                        refreshData();
                    } catch (SQLException ex) {
                        showError("Delete Error", ex.getMessage());
                    }
                });

                row.getChildren().addAll(name, cat, r, cal, logItBtn, delBtn);
                favoritesListContainer.getChildren().add(row);
            }

        } catch (SQLException e) {
            showError("Database Query Exception", e.getMessage());
        }
    }

    // --- INPUT VALIDATION METHODS ---
    private boolean validateAndAddFood(String name, String calStr) {
        if (name == null || name.trim().isEmpty()) {
            showError("Validation Error", "Food Name cannot be empty.");
            return false;
        }
        try {
            int cal = Integer.parseInt(calStr.trim());
            if (cal <= 0) {
                showError("Validation Error", "Calories must be a positive number.");
                return false;
            }
            String timeNow = LocalDateTime.now().format(DateTimeFormatter.ofPattern("h:mm a"));
            foodDao.addLog(new FoodLog(name.trim(), cal, timeNow));
            return true;
        } catch (NumberFormatException e) {
            showError("Validation Error", "Calories must be a valid integer.");
            return false;
        } catch (SQLException e) {
            showError("Database Error", e.getMessage());
            return false;
        }
    }

    private boolean validateAndSaveFavorite(String name, String calStr, String category) {
        if (name == null || name.trim().isEmpty()) {
            showError("Validation Error", "Favorite food name cannot be empty.");
            return false;
        }
        try {
            int cal = Integer.parseInt(calStr.trim());
            if (cal <= 0) {
                showError("Validation Error", "Calories must be a positive integer.");
                return false;
            }
            foodDao.addFavorite(new FavoriteFood(name.trim(), cal, category));
            return true;
        } catch (NumberFormatException e) {
            showError("Validation Error", "Calories must be a valid number.");
            return false;
        } catch (SQLException e) {
            showError("Database Error", e.getMessage());
            return false;
        }
    }

    private void showEditGoalDialog() {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(dailyGoal));
        dialog.setTitle("Edit Calorie Goal");
        dialog.setHeaderText("Set your daily target calories:");
        dialog.setContentText("Target (kcal):");

        dialog.showAndWait().ifPresent(input -> {
            try {
                int g = Integer.parseInt(input.trim());
                if (g > 0) {
                    dailyGoal = g;
                    goalLabel.setText(dailyGoal + " kcal");
                    refreshData();
                } else {
                    showError("Invalid Input", "Goal must be positive.");
                }
            } catch (NumberFormatException e) {
                showError("Invalid Input", "Please enter a valid integer.");
            }
        });
    }

    private void showError(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}