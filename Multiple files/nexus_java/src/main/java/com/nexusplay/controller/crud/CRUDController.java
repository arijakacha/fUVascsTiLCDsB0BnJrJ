package com.nexusplay.controller.crud;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class CRUDController<T> {

    @FXML protected TableView<T> tableView;
    @FXML protected Button btnAdd;
    @FXML protected Button btnEdit;
    @FXML protected Button btnDelete;
    @FXML protected Button btnRefresh;
    @FXML protected TextField searchField;
    @FXML protected Pagination pagination;
    
    protected ObservableList<T> dataList = FXCollections.observableArrayList();
    protected int itemsPerPage = 10;
    protected int currentPage = 0;
    protected List<T> allItems;

    @FXML
    public void initialize() {
        setupTableView();
        setupPagination();
        setupEventHandlers();
        loadData();
    }

    protected abstract void setupTableView();
    protected abstract List<T> loadAllItems();
    protected abstract void addItem();
    protected abstract void editItem(T item);
    protected abstract void deleteItem(T item);
    protected abstract T createNewItem();
    protected abstract void updateItem(T item);
    protected abstract boolean saveItem(T item);

    protected void setupPagination() {
        pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            currentPage = newIndex.intValue();
            updateTableView();
        });
    }

    protected void setupEventHandlers() {
        btnAdd.setOnAction(this::handleAdd);
        btnEdit.setOnAction(this::handleEdit);
        btnDelete.setOnAction(this::handleDelete);
        btnRefresh.setOnAction(this::handleRefresh);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> handleSearch(newVal));
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> updateButtonStates());
    }

    protected void handleAdd(ActionEvent event) {
        addItem();
    }

    protected void handleEdit(ActionEvent event) {
        T selectedItem = tableView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            editItem(selectedItem);
        }
    }

    protected void handleDelete(ActionEvent event) {
        T selectedItem = tableView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Delete");
            alert.setHeaderText("Are you sure you want to delete this item?");
            alert.setContentText("This action cannot be undone.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                deleteItem(selectedItem);
            }
        }
    }

    protected void handleRefresh(ActionEvent event) {
        loadData();
    }

    protected void handleSearch(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            updateTableView();
            return;
        }
        
        String searchLower = searchText.toLowerCase();
        List<T> filteredItems = allItems.stream()
                .filter(item -> matchesSearch(item, searchLower))
                .toList();
        
        updateTableView(filteredItems);
    }

    protected boolean matchesSearch(T item, String searchText) {
        return item.toString().toLowerCase().contains(searchText);
    }

    protected void updateButtonStates() {
        T selectedItem = tableView.getSelectionModel().getSelectedItem();
        btnEdit.setDisable(selectedItem == null);
        btnDelete.setDisable(selectedItem == null);
    }

    protected void loadData() {
        allItems = loadAllItems();
        updateTableView();
        updatePagination();
    }

    protected void updateTableView() {
        updateTableView(allItems);
    }

    protected void updateTableView(List<T> items) {
        int fromIndex = currentPage * itemsPerPage;
        int toIndex = Math.min(fromIndex + itemsPerPage, items.size());
        
        List<T> pageItems = items.subList(fromIndex, toIndex);
        dataList.setAll(pageItems);
        tableView.setItems(dataList);
        
        updateButtonStates();
    }

    protected void updatePagination() {
        int pageCount = (int) Math.ceil((double) allItems.size() / itemsPerPage);
        pagination.setPageCount(pageCount);
        pagination.setCurrentPageIndex(currentPage);
    }

    protected void refreshData() {
        loadData();
    }

    protected void showItemDialog(T item, String title) {
        Dialog<T> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);

        VBox content = createDialogContent(item);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (validateInput(item)) {
                    updateItem(item);
                    return item;
                }
            }
            return null;
        });

        Optional<T> result = dialog.showAndWait();
        result.ifPresent(savedItem -> {
            if (saveItem(savedItem)) {
                refreshData();
            }
        });
    }

    protected abstract VBox createDialogContent(T item);
    protected abstract boolean validateInput(T item);

    protected <S> TableColumn<T, S> createColumn(String title, String property) {
        TableColumn<T, S> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        return column;
    }

    protected <S> TableColumn<T, S> createColumn(String title, Function<T, S> extractor) {
        TableColumn<T, S> column = new TableColumn<>(title);
        column.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createObjectBinding(() -> extractor.apply(cellData.getValue()))
        );
        return column;
    }
}
