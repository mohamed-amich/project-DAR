package client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import rmi.BookService;
import common.Book;
import corba.SimpleORB;
import corba.UserData;

import java.io.BufferedReader;
import java.io.FileReader;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Optional;

public class LibraryClientGUI extends Application {
    private BookService bookService;
    private SimpleORB.Stub userServiceStub;
    private String currentUser = null;
    private Label statusLabel;
    private Label userLabel;
    private TabPane mainTabPane;
    private TableView<BookDisplay> bookTable;
    private TableView<UserDisplay> userTable;

    // Classe pour afficher les livres dans le tableau
    public static class BookDisplay {
        private String isbn;
        private String title;
        private String author;
        private int year;
        private String available;

        public BookDisplay(Book book) {
            this.isbn = book.getIsbn();
            this.title = book.getTitle();
            this.author = book.getAuthor();
            this.year = book.getYear();
            this.available = book.isAvailable() ? "Oui" : "Non";
        }

        public String getIsbn() { return isbn; }
        public String getTitle() { return title; }
        public String getAuthor() { return author; }
        public int getYear() { return year; }
        public String getAvailable() { return available; }
    }

    // Classe pour afficher les utilisateurs dans le tableau
    public static class UserDisplay {
        private String id;
        private String name;
        private String email;
        private String role;
        private String active;

        public UserDisplay(UserData user) {
            this.id = user.id;
            this.name = user.name;
            this.email = user.email;
            this.role = user.role;
            this.active = user.active ? "Oui" : "Non";
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
        public String getActive() { return active; }
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Système de Gestion de Bibliothèque Distribuée");

        // Connexion aux services
        connectToServices();

        // Layout principal
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Barre supérieure
        HBox topBar = createTopBar();
        root.setTop(topBar);

        // Onglets principaux
        mainTabPane = createMainTabs();
        root.setCenter(mainTabPane);

        // Barre de statut
        statusLabel = new Label("Connecté aux services RMI et CORBA");
        statusLabel.setStyle("-fx-background-color: #e8f5e9; -fx-padding: 5px;");
        root.setBottom(statusLabel);

        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Charger les données initiales
        refreshBooks();
        refreshUsers();
    }

    private void connectToServices() {
        try {
            // Connexion RMI
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            bookService = (BookService) registry.lookup("BookService");
            System.out.println("Connecté au service RMI");

            // Connexion CORBA
            java.io.File refFile = new java.io.File("UserService.ref");
            if (refFile.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(refFile));
                String ref = reader.readLine();
                reader.close();
                
                String[] parts = ref.split(":");
                String host = parts[0];
                int port = Integer.parseInt(parts[1]);
                String serviceName = parts[2];
                
                userServiceStub = new SimpleORB.Stub(host, port, serviceName);
                System.out.println("Connecté au service CORBA");
            }
        } catch (Exception e) {
            showError("Erreur de connexion", "Impossible de se connecter aux services: " + e.getMessage());
        }
    }

    private HBox createTopBar() {
        HBox topBar = new HBox(15);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");

        Label titleLabel = new Label("📚 Bibliothèque Distribuée");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        userLabel = new Label("Non connecté");
        userLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        Button loginBtn = new Button("Connexion");
        loginBtn.setOnAction(e -> showLoginDialog());
        
        Button registerBtn = new Button("Inscription");
        registerBtn.setOnAction(e -> showRegisterDialog());
        
        Button logoutBtn = new Button("Déconnexion");
        logoutBtn.setOnAction(e -> logout());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar.getChildren().addAll(titleLabel, spacer, userLabel, loginBtn, registerBtn, logoutBtn);
        return topBar;
    }

    private TabPane createMainTabs() {
        TabPane tabPane = new TabPane();

        // Onglet Livres
        Tab bookTab = new Tab("📖 Gestion des Livres");
        bookTab.setClosable(false);
        bookTab.setContent(createBookPanel());

        // Onglet Utilisateurs
        Tab userTab = new Tab("👥 Gestion des Utilisateurs");
        userTab.setClosable(false);
        userTab.setContent(createUserPanel());

        tabPane.getTabs().addAll(bookTab, userTab);
        return tabPane;
    }

    @SuppressWarnings("unchecked")
    private VBox createBookPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));

        // Barre d'outils
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Rechercher par titre...");
        searchField.setPrefWidth(300);

        Button searchBtn = new Button("🔍 Rechercher");
        searchBtn.setOnAction(e -> searchBooks(searchField.getText()));

        Button refreshBtn = new Button("🔄 Actualiser");
        refreshBtn.setOnAction(e -> refreshBooks());

        Button addBtn = new Button("➕ Ajouter");
        addBtn.setOnAction(e -> showAddBookDialog());

        Button borrowBtn = new Button("📤 Emprunter");
        borrowBtn.setOnAction(e -> borrowSelectedBook());

        Button returnBtn = new Button("📥 Retourner");
        returnBtn.setOnAction(e -> returnSelectedBook());

        toolbar.getChildren().addAll(searchField, searchBtn, refreshBtn, addBtn, borrowBtn, returnBtn);

        // Tableau des livres
        bookTable = new TableView<>();
        
        TableColumn<BookDisplay, String> isbnCol = new TableColumn<>("ISBN");
        isbnCol.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        isbnCol.setPrefWidth(150);

        TableColumn<BookDisplay, String> titleCol = new TableColumn<>("Titre");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(300);

        TableColumn<BookDisplay, String> authorCol = new TableColumn<>("Auteur");
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        authorCol.setPrefWidth(200);

        TableColumn<BookDisplay, Integer> yearCol = new TableColumn<>("Année");
        yearCol.setCellValueFactory(new PropertyValueFactory<>("year"));
        yearCol.setPrefWidth(80);

        TableColumn<BookDisplay, String> availCol = new TableColumn<>("Disponible");
        availCol.setCellValueFactory(new PropertyValueFactory<>("available"));
        availCol.setPrefWidth(100);

        bookTable.getColumns().addAll(isbnCol, titleCol, authorCol, yearCol, availCol);
        VBox.setVgrow(bookTable, Priority.ALWAYS);

        panel.getChildren().addAll(toolbar, bookTable);
        return panel;
    }

    @SuppressWarnings("unchecked")
    private VBox createUserPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));

        // Barre d'outils
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        Button refreshBtn = new Button("🔄 Actualiser");
        refreshBtn.setOnAction(e -> refreshUsers());

        Button viewBtn = new Button("👁 Voir Détails");
        viewBtn.setOnAction(e -> viewSelectedUser());

        toolbar.getChildren().addAll(refreshBtn, viewBtn);

        // Tableau des utilisateurs
        userTable = new TableView<>();
        
        TableColumn<UserDisplay, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(120);

        TableColumn<UserDisplay, String> nameCol = new TableColumn<>("Nom");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);

        TableColumn<UserDisplay, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(250);

        TableColumn<UserDisplay, String> roleCol = new TableColumn<>("Rôle");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleCol.setPrefWidth(100);

        TableColumn<UserDisplay, String> activeCol = new TableColumn<>("Actif");
        activeCol.setCellValueFactory(new PropertyValueFactory<>("active"));
        activeCol.setPrefWidth(80);

        userTable.getColumns().addAll(idCol, nameCol, emailCol, roleCol, activeCol);
        VBox.setVgrow(userTable, Priority.ALWAYS);

        panel.getChildren().addAll(toolbar, userTable);
        return panel;
    }

    private void showLoginDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Connexion");
        dialog.setHeaderText("Connectez-vous au système");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField userIdField = new TextField();
        userIdField.setPromptText("ID utilisateur");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mot de passe");

        grid.add(new Label("ID:"), 0, 0);
        grid.add(userIdField, 1, 0);
        grid.add(new Label("Mot de passe:"), 0, 1);
        grid.add(passwordField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Boolean success = userServiceStub.invoke("authenticate", 
                    userIdField.getText(), passwordField.getText());
                if (success != null && success) {
                    currentUser = userIdField.getText();
                    userLabel.setText("👤 " + currentUser);
                    showInfo("Succès", "Connexion réussie!");
                } else {
                    showError("Erreur", "Identifiants incorrects");
                }
            } catch (Exception e) {
                showError("Erreur", "Erreur de connexion: " + e.getMessage());
            }
        }
    }

    private void showRegisterDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Inscription");
        dialog.setHeaderText("Créer un nouveau compte");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField userIdField = new TextField();
        TextField nameField = new TextField();
        TextField emailField = new TextField();
        PasswordField passwordField = new PasswordField();

        grid.add(new Label("ID:"), 0, 0);
        grid.add(userIdField, 1, 0);
        grid.add(new Label("Nom:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Mot de passe:"), 0, 3);
        grid.add(passwordField, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Boolean success = userServiceStub.invoke("registerUser",
                    userIdField.getText(), nameField.getText(), 
                    emailField.getText(), passwordField.getText());
                if (success != null && success) {
                    showInfo("Succès", "Inscription réussie! Vous pouvez maintenant vous connecter.");
                } else {
                    showError("Erreur", "L'ID utilisateur existe déjà");
                }
            } catch (Exception e) {
                showError("Erreur", "Erreur d'inscription: " + e.getMessage());
            }
        }
    }

    private void showAddBookDialog() {
        if (currentUser == null) {
            showError("Erreur", "Veuillez vous connecter d'abord");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Ajouter un livre");
        dialog.setHeaderText("Ajouter un nouveau livre");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField isbnField = new TextField();
        TextField titleField = new TextField();
        TextField authorField = new TextField();
        TextField yearField = new TextField();

        grid.add(new Label("ISBN:"), 0, 0);
        grid.add(isbnField, 1, 0);
        grid.add(new Label("Titre:"), 0, 1);
        grid.add(titleField, 1, 1);
        grid.add(new Label("Auteur:"), 0, 2);
        grid.add(authorField, 1, 2);
        grid.add(new Label("Année:"), 0, 3);
        grid.add(yearField, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Book book = new Book(
                    isbnField.getText(),
                    titleField.getText(),
                    authorField.getText(),
                    Integer.parseInt(yearField.getText())
                );
                if (bookService.addBook(book)) {
                    showInfo("Succès", "Livre ajouté avec succès!");
                    refreshBooks();
                } else {
                    showError("Erreur", "Le livre existe déjà");
                }
            } catch (Exception e) {
                showError("Erreur", "Erreur lors de l'ajout: " + e.getMessage());
            }
        }
    }

    private void borrowSelectedBook() {
        if (currentUser == null) {
            showError("Erreur", "Veuillez vous connecter d'abord");
            return;
        }

        BookDisplay selected = bookTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Erreur", "Veuillez sélectionner un livre");
            return;
        }

        try {
            if (bookService.borrowBook(selected.getIsbn(), currentUser)) {
                showInfo("Succès", "Livre emprunté avec succès!");
                refreshBooks();
            } else {
                showError("Erreur", "Le livre n'est pas disponible");
            }
        } catch (Exception e) {
            showError("Erreur", "Erreur: " + e.getMessage());
        }
    }

    private void returnSelectedBook() {
        BookDisplay selected = bookTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Erreur", "Veuillez sélectionner un livre");
            return;
        }

        try {
            if (bookService.returnBook(selected.getIsbn())) {
                showInfo("Succès", "Livre retourné avec succès!");
                refreshBooks();
            } else {
                showError("Erreur", "Le livre n'était pas emprunté");
            }
        } catch (Exception e) {
            showError("Erreur", "Erreur: " + e.getMessage());
        }
    }

    private void searchBooks(String query) {
        try {
            List<Book> books = bookService.searchByTitle(query);
            bookTable.getItems().clear();
            for (Book book : books) {
                bookTable.getItems().add(new BookDisplay(book));
            }
            statusLabel.setText(books.size() + " livre(s) trouvé(s)");
        } catch (Exception e) {
            showError("Erreur", "Erreur de recherche: " + e.getMessage());
        }
    }

    private void refreshBooks() {
        try {
            List<Book> books = bookService.getAllBooks();
            bookTable.getItems().clear();
            for (Book book : books) {
                bookTable.getItems().add(new BookDisplay(book));
            }
            statusLabel.setText(books.size() + " livre(s) chargé(s)");
        } catch (Exception e) {
            showError("Erreur", "Erreur de chargement: " + e.getMessage());
        }
    }

    private void refreshUsers() {
        try {
            UserData[] users = userServiceStub.invoke("getAllUsers");
            userTable.getItems().clear();
            for (UserData user : users) {
                userTable.getItems().add(new UserDisplay(user));
            }
            statusLabel.setText(users.length + " utilisateur(s) chargé(s)");
        } catch (Exception e) {
            showError("Erreur", "Erreur de chargement: " + e.getMessage());
        }
    }

    private void viewSelectedUser() {
        UserDisplay selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Erreur", "Veuillez sélectionner un utilisateur");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails de l'utilisateur");
        alert.setHeaderText("Informations complètes");
        alert.setContentText(
            "ID: " + selected.getId() + "\n" +
            "Nom: " + selected.getName() + "\n" +
            "Email: " + selected.getEmail() + "\n" +
            "Rôle: " + selected.getRole() + "\n" +
            "Actif: " + selected.getActive()
        );
        alert.showAndWait();
    }

    private void logout() {
        currentUser = null;
        userLabel.setText("Non connecté");
        showInfo("Déconnexion", "Vous êtes déconnecté");
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}