package com.nexusplay.controller.crud;

import com.nexusplay.dao.ProductDAO;
import com.nexusplay.entity.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;

public class ShopCRUDController extends CRUDController<Product> {

    @FXML protected Button btnAdd;
    @FXML protected Button btnEdit;
    @FXML protected Button btnDelete;
    @FXML protected Button btnRefresh;
    @FXML protected Button btnBack;
    @FXML protected TextField searchField;
    @FXML protected Pagination pagination;
    @FXML protected HBox contentArea;
    @FXML protected Label countLabel;

    private ProductDAO productDAO = new ProductDAO();
    private ObservableList<Product> allItems = FXCollections.observableArrayList();
    
    private TextField currentNameField;
    private TextField currentTypeField;
    private TextField currentDescriptionField;
    private TextField currentImagePathField;
    private TextField currentPriceField;
    private TextField currentQuantityField;

    @FXML
    public void initialize() {
        setupButtons();
        loadCards();
    }

    protected void setupButtons() {
        btnAdd.setOnAction(e -> {
            Product newProduct = createNewItem();
            showProductDialog(newProduct, "Add New Product");
        });
        
        btnEdit.setOnAction(e -> {
            showEditDialog();
        });
        
        btnDelete.setOnAction(e -> {
            showDeleteDialog();
        });
        
        btnRefresh.setOnAction(e -> {
            loadCards();
        });
        
        btnBack.setOnAction(e -> {
            goBackToDashboard();
        });
        
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterCards(newVal);
        });
    }

    protected void goBackToDashboard() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/AdminDashboard.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) btnBack.getScene().getWindow();
            com.nexusplay.util.SceneNavigation.replaceSceneContent(stage, root, com.nexusplay.util.SceneNavigation.DEFAULT_WIDTH, com.nexusplay.util.SceneNavigation.DEFAULT_HEIGHT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showEditDialog() {
        List<Product> products = loadAllItems();
        if (products.isEmpty()) {
            showNotImplemented("No products available to edit");
            return;
        }

        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Select Product to Edit");
        dialog.setHeaderText(null);
        dialog.getDialogPane().setStyle("-fx-background-color: #1a1a2e;");

        ComboBox<Product> productCombo = new ComboBox<>(FXCollections.observableArrayList(products));
        productCombo.setPromptText("Select Product");
        productCombo.setConverter(new javafx.util.StringConverter<Product>() {
            @Override
            public String toString(Product product) {
                if (product == null) return "Select Product";
                return product.getName() != null ? product.getName() : "Product";
            }
            @Override
            public Product fromString(String string) {
                return null;
            }
        });

        VBox dialogContent = new VBox(15);
        dialogContent.setPadding(new javafx.geometry.Insets(25));
        dialogContent.setStyle("-fx-background-color: #1a1a2e;");
        dialogContent.getChildren().addAll(new Label("Product:"), productCombo);
        dialog.getDialogPane().setContent(dialogContent);

        ButtonType selectButtonType = new ButtonType("Select", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(selectButtonType, cancelButtonType);

        dialog.getDialogPane().lookupButton(selectButtonType).setStyle("-fx-background-color: #FF4D2E; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 8;");
        dialog.getDialogPane().lookupButton(cancelButtonType).setStyle("-fx-background-color: #0B0D14; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 8; -fx-border-color: #FF4D2E; -fx-border-width: 2; -fx-border-radius: 8;");

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == selectButtonType) {
                return productCombo.getValue();
            }
            return null;
        });

        Optional<Product> result = dialog.showAndWait();
        result.ifPresent(selectedProduct -> {
            editItem(selectedProduct);
        });
    }

    private void showDeleteDialog() {
        List<Product> products = loadAllItems();
        if (products.isEmpty()) {
            showNotImplemented("No products available to delete");
            return;
        }

        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Select Product to Delete");
        dialog.setHeaderText(null);
        dialog.getDialogPane().setStyle("-fx-background-color: #1a1a2e;");

        ComboBox<Product> productCombo = new ComboBox<>(FXCollections.observableArrayList(products));
        productCombo.setPromptText("Select Product");
        productCombo.setConverter(new javafx.util.StringConverter<Product>() {
            @Override
            public String toString(Product product) {
                if (product == null) return "Select Product";
                return product.getName() != null ? product.getName() : "Product";
            }
            @Override
            public Product fromString(String string) {
                return null;
            }
        });

        VBox dialogContent = new VBox(15);
        dialogContent.setPadding(new javafx.geometry.Insets(25));
        dialogContent.setStyle("-fx-background-color: #1a1a2e;");
        dialogContent.getChildren().addAll(new Label("Product:"), productCombo);
        dialog.getDialogPane().setContent(dialogContent);

        ButtonType deleteButtonType = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(deleteButtonType, cancelButtonType);

        dialog.getDialogPane().lookupButton(deleteButtonType).setStyle("-fx-background-color: #FF4D2E; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 8;");
        dialog.getDialogPane().lookupButton(cancelButtonType).setStyle("-fx-background-color: #0B0D14; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 8; -fx-border-color: #FF4D2E; -fx-border-width: 2; -fx-border-radius: 8;");

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == deleteButtonType) {
                return productCombo.getValue();
            }
            return null;
        });

        Optional<Product> result = dialog.showAndWait();
        result.ifPresent(selectedProduct -> {
            deleteItem(selectedProduct);
        });
    }

    @Override
    protected void setupTableView() {
        // Not using table view - using cards instead
    }

    @Override
    protected List<Product> loadAllItems() {
        return productDAO.findAll();
    }

    @Override
    protected void deleteItem(Product item) {
        productDAO.delete(item);
        loadCards();
    }

    @Override
    protected void addItem() {
        Product newProduct = createNewItem();
        showProductDialog(newProduct, "Add New Product");
    }

    @Override
    protected void editItem(Product item) {
        showProductDialog(item, "Edit Product");
    }

    @Override
    protected Product createNewItem() {
        Product product = new Product();
        return product;
    }

    @Override
    protected void updateItem(Product item) {
    }

    @Override
    protected boolean saveItem(Product item) {
        try {
            if (item.getId() == null) {
                productDAO.save(item);
            } else {
                productDAO.update(item);
            }
            loadCards();
            return true;
        } catch (Exception e) {
            showNotImplemented("Error saving product: " + e.getMessage());
            return false;
        }
    }

    protected void loadCards() {
        try {
            List<Product> products = loadAllItems();
            allItems.clear();
            allItems.addAll(products);
            
            int totalProducts = products != null ? products.size() : 0;
            countLabel.setText(totalProducts + " products");
            
            if (totalProducts == 0) {
                pagination.setPageCount(1);
                pagination.setPageFactory(pageIndex -> {
                    VBox emptyPage = new VBox();
                    Label emptyLabel = new Label("No products available");
                    emptyLabel.setStyle("-fx-text-fill: #8B92B9; -fx-font-size: 16px;");
                    emptyPage.getChildren().add(emptyLabel);
                    emptyPage.setAlignment(javafx.geometry.Pos.CENTER);
                    return emptyPage;
                });
            } else {
                pagination.setPageFactory(this::createPage);
                pagination.setPageCount((int) Math.ceil((double) totalProducts / 6.0));
            }
            pagination.setCurrentPageIndex(0);
        } catch (Exception e) {
            System.out.println("Error loading cards: " + e.getMessage());
            e.printStackTrace();
        }
    }

    protected void filterCards(String searchText) {
        List<Product> allProducts = loadAllItems();
        allItems.clear();
        
        for (Product product : allProducts) {
            if (product.getName() != null && product.getName().toLowerCase().contains(searchText.toLowerCase())) {
                allItems.add(product);
            }
        }
        
        countLabel.setText(allItems.size() + " products");
        
        int pageCount = (int) Math.ceil((double) allItems.size() / 6.0);
        pagination.setPageFactory(this::createPage);
        pagination.setPageCount(pageCount);
        pagination.setCurrentPageIndex(0);
    }

    protected VBox createPage(int pageIndex) {
        FlowPane flowPane = new FlowPane(15, 15);
        flowPane.setStyle("-fx-background-color: transparent;");
        
        int fromIndex = pageIndex * 6;
        int toIndex = Math.min(fromIndex + 6, allItems.size());
        
        if (fromIndex < allItems.size()) {
            List<Product> pageItems = allItems.subList(fromIndex, toIndex);
            for (Product product : pageItems) {
                flowPane.getChildren().add(createProductCard(product));
            }
        }
        
        VBox page = new VBox(10);
        page.getChildren().add(flowPane);
        return page;
    }

    protected VBox createProductCard(Product product) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: #1E2139; -fx-background-radius: 12; -fx-border-color: #2D3142; -fx-border-radius: 12; -fx-border-width: 1; -fx-padding: 16; -fx-pref-width: 280;");
        
        // Product image
        javafx.scene.layout.StackPane imagePane = new javafx.scene.layout.StackPane();
        imagePane.setStyle("-fx-background-color: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); -fx-background-radius: 8;");
        imagePane.setPrefSize(250, 140);
        
        if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
            try {
                javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView(new javafx.scene.image.Image(product.getImagePath(), true)); // background loading
                imageView.setFitWidth(250);
                imageView.setFitHeight(140);
                imageView.setPreserveRatio(false);
                imageView.setSmooth(true);
                imagePane.getChildren().add(imageView);
            } catch (Exception e) {
                System.out.println("Invalid product image path: " + product.getImagePath());
                Label productIcon = new Label(product.getName() != null && product.getName().length() > 0 ? product.getName().substring(0, 1).toUpperCase() : "P");
                productIcon.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: white;");
                imagePane.getChildren().add(productIcon);
            }
        } else {
            Label productIcon = new Label(product.getName() != null && product.getName().length() > 0 ? product.getName().substring(0, 1).toUpperCase() : "P");
            productIcon.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: white;");
            imagePane.getChildren().add(productIcon);
        }
        
        Label nameLabel = new Label(product.getName());
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        
        Label typeLabel = new Label(product.getType());
        typeLabel.setStyle("-fx-text-fill: #8B92B9; -fx-font-size: 13px;");
        
        // Price
        Label priceLabel = new Label(product.getPrice() != null ? "$" + String.format("%.2f", product.getPrice()) : "Price: N/A");
        priceLabel.setStyle("-fx-text-fill: #10B981; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        // Description
        Label descriptionLabel = new Label(product.getDescription() != null ? product.getDescription() : "");
        descriptionLabel.setStyle("-fx-text-fill: #8B92B9; -fx-font-size: 12px;");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(250);
        
        // Quantity
        Label quantityLabel = new Label(product.getQuantity() != null ? "Quantity: " + product.getQuantity() : "Quantity: N/A");
        quantityLabel.setStyle("-fx-text-fill: #8B92B9; -fx-font-size: 12px;");
        
        // Action buttons
        HBox actionsBox = new HBox(8);
        actionsBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        
        Button editBtn = new Button("Edit");
        editBtn.setStyle("-fx-background-color: #2D9CDB; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 6 12; -fx-background-radius: 6;");
        editBtn.setOnAction(e -> editItem(product));
        
        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 6 12; -fx-background-radius: 6;");
        deleteBtn.setOnAction(e -> deleteItem(product));
        
        actionsBox.getChildren().addAll(editBtn, deleteBtn);
        
        card.getChildren().addAll(imagePane, nameLabel, typeLabel, priceLabel, descriptionLabel, quantityLabel, actionsBox);
        
        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #252845; -fx-background-radius: 12; -fx-border-color: #f093fb; -fx-border-radius: 12; -fx-border-width: 1; -fx-padding: 16; -fx-pref-width: 280;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #1E2139; -fx-background-radius: 12; -fx-border-color: #2D3142; -fx-border-radius: 12; -fx-border-width: 1; -fx-padding: 16; -fx-pref-width: 280;"));
        
        return card;
    }

    @Override
    protected VBox createDialogContent(Product item) {
        VBox content = new VBox(10);
        content.setPadding(new javafx.geometry.Insets(20));

        currentNameField = new TextField(item.getName());
        currentNameField.setPromptText("Product Name");

        currentTypeField = new TextField(item.getType());
        currentTypeField.setPromptText("Product Type");

        currentDescriptionField = new TextField(item.getDescription());
        currentDescriptionField.setPromptText("Description");

        currentImagePathField = new TextField(item.getImagePath());
        currentImagePathField.setPromptText("Image Path");

        currentPriceField = new TextField(item.getPrice() != null ? item.getPrice().toString() : "");
        currentPriceField.setPromptText("Price");

        currentQuantityField = new TextField(item.getQuantity() != null ? item.getQuantity().toString() : "");
        currentQuantityField.setPromptText("Quantity");

        content.getChildren().addAll(
            new Label("Name:"), currentNameField,
            new Label("Type:"), currentTypeField,
            new Label("Description:"), currentDescriptionField,
            new Label("Image Path:"), currentImagePathField,
            new Label("Price:"), currentPriceField,
            new Label("Quantity:"), currentQuantityField
        );

        return content;
    }

    @Override
    protected boolean validateInput(Product item) {
        String nameText = currentNameField != null ? currentNameField.getText().trim() : "";
        String typeText = currentTypeField != null ? currentTypeField.getText().trim() : "";
        String descriptionText = currentDescriptionField != null ? currentDescriptionField.getText().trim() : "";
        String imagePathText = currentImagePathField != null ? currentImagePathField.getText().trim() : "";
        String priceText = currentPriceField != null ? currentPriceField.getText().trim() : "";
        String quantityText = currentQuantityField != null ? currentQuantityField.getText().trim() : "";

        if (nameText.isEmpty()) {
            showNotImplemented("Product name is required");
            return false;
        }
        if (nameText.length() < 4) {
            showNotImplemented("Product name must be at least 4 characters");
            return false;
        }
        if (nameText.length() > 10000) {
            showNotImplemented("Product name must be less than 10000 characters");
            return false;
        }

        if (typeText.isEmpty()) {
            showNotImplemented("Product type is required");
            return false;
        }
        if (typeText.length() < 4) {
            showNotImplemented("Product type must be at least 4 characters");
            return false;
        }
        if (typeText.length() > 10000) {
            showNotImplemented("Product type must be less than 10000 characters");
            return false;
        }

        if (descriptionText.length() > 10000) {
            showNotImplemented("Description must be less than 10000 characters");
            return false;
        }

        if (imagePathText.length() > 10000) {
            showNotImplemented("Image path must be less than 10000 characters");
            return false;
        }

        if (priceText.isEmpty()) {
            showNotImplemented("Price is required");
            return false;
        }
        if (priceText.length() < 4) {
            showNotImplemented("Price must be at least 4 characters");
            return false;
        }
        if (priceText.length() > 10000) {
            showNotImplemented("Price must be less than 10000 characters");
            return false;
        }

        if (quantityText.length() > 10000) {
            showNotImplemented("Quantity must be less than 10000 characters");
            return false;
        }

        item.setName(nameText);
        item.setType(typeText);
        item.setDescription(descriptionText);
        item.setImagePath(imagePathText);
        
        try {
            item.setPrice(Double.parseDouble(priceText));
        } catch (NumberFormatException e) {
            showNotImplemented("Price must be a valid number");
            return false;
        }
        
        if (!quantityText.isEmpty()) {
            try {
                item.setQuantity(Integer.parseInt(quantityText));
            } catch (NumberFormatException e) {
                showNotImplemented("Quantity must be a valid number");
                return false;
            }
        }

        return true;
    }

    private void showProductDialog(Product product, String title) {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.getDialogPane().setStyle("-fx-background-color: #1a1a2e;");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);

        dialog.getDialogPane().lookupButton(saveButtonType).setStyle("-fx-background-color: #FF4D2E; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 8;");
        dialog.getDialogPane().lookupButton(cancelButtonType).setStyle("-fx-background-color: #0B0D14; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 8; -fx-border-color: #FF4D2E; -fx-border-width: 2; -fx-border-radius: 8;");

        VBox content = createDialogContent(product);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (validateInput(product)) {
                    return product;
                }
            }
            return null;
        });

        Optional<Product> result = dialog.showAndWait();
        result.ifPresent(savedProduct -> {
            if (saveItem(savedProduct)) {
            }
        });
    }

    private void showNotImplemented(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
