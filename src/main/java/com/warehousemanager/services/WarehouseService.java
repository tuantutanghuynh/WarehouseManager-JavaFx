package com.warehousemanager.services;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.warehousemanager.models.entity.Goods;
import com.warehousemanager.repositories.GoodsRepository;

import javafx.application.Platform;

public class WarehouseService<T extends Goods> {
    // Singleton instance used across the application.
    private static WarehouseService<Goods> instance;

    // Returns the shared service instance, creating it on first access.
    public static WarehouseService<Goods> getInstance() {
        if (instance == null) {
            instance = new WarehouseService<>();
        }
        return instance;
    }

    // Resets the singleton instance, mainly for reinitialization or testing.
    public static void reset() {
        instance = null;
    }

    // In-memory cache for fast lookup and ordered iteration.
    private final Map<String, T> map = new HashMap<>();
    private final List<T> list = new ArrayList<>();
    private final Object lock = new Object();
    private final GoodsRepository repo = new GoodsRepository();

    // Loads all goods from the database into the in-memory cache.
    @SuppressWarnings("unchecked")
    public void loadFromDB() {
        List<Goods> dbList = repo.findAll();

        Map<String, T> tempMap = new HashMap<>();
        List<T> tempList = new ArrayList<>();

        for (Goods g : dbList) {
            T value = (T) g;
            if (!tempMap.containsKey(value.getCode())) {
                tempMap.put(value.getCode(), value);
                tempList.add(value);
            }
        }

        synchronized (lock) {
            map.clear();
            list.clear();
            map.putAll(tempMap);
            list.addAll(tempList);
        }
    }

    // Imports a product into stock or increases quantity if it already exists.
    public boolean importGoods(T g) {
        synchronized (lock) {
            if (map.containsKey(g.getCode())) {
                T existing = map.get(g.getCode());
                existing.setQuantity(existing.getQuantity() + g.getQuantity());
                return repo.updateQuantity(existing.getCode(), existing.getQuantity());
            }
            if (!repo.insert(g)) {
                return false;
            }
            map.put(g.getCode(), g);
            list.add(g);
        }
        return true;
    }

    // Exports goods by reducing the available quantity after validation.
    public void exportGoods(String code, int qty) {
        boolean success = false;
        try {
            if (qty <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than 0");
            }

            synchronized (lock) {
                T g = map.get(code);
                if (g == null) {
                    throw new IllegalArgumentException("Product" + code + " not found");
                }
                if (qty > g.getQuantity()) {
                    throw new IllegalStateException("Product's available quantity is not enough");
                }
                g.setQuantity(g.getQuantity() - qty);
                repo.updateQuantity(code, g.getQuantity());
            }
            success = true;
        } finally {
            System.out.printf("[LOG] Export | code=%s | qty=%d | status=%s%n",
                    code, qty, success ? "OK" : "FAILED");
        }

    }

    // Deletes a product from both the database and the in-memory cache.
    public boolean delete(String code) {
        synchronized (lock) {
            T g = map.get(code);
            if (g == null) {
                return false;
            }
            if (!repo.delete(code)) {
                return false;
            }
            list.remove(g);
            map.remove(code);
        }
        return true;
    }

    // Finds a product by its unique code.
    public T findByCode(String code) {
        synchronized (lock) {
            return map.get(code);
        }

    }

    // Returns a snapshot copy of all cached goods.
    public List<T> getAll() {
        synchronized (lock) {
            return new ArrayList<>(list);
        }
    }

    // Filters goods by type code; returns all items when the filter is empty.
    public List<T> filterByType(String typeCode) {
        if (typeCode == null || typeCode.isBlank()) {
            return getAll();
        }
        List<T> result = new ArrayList<>();
        for (T g : getAll()) {
            if (g.getTypeCode().equalsIgnoreCase(typeCode)) {
                result.add(g);
            }
        }
        return result;
    }

    // Calculates the total stock value of all goods currently in memory.
    public double calcTotalStockValue() {
        double total = 0;
        for (T g : getAll()) {
            total += g.calcStockValue();
        }
        return total;
    }

    // Collects items whose quantity is considered low.
    public List<T> findLowStock() {
        List<T> result = new ArrayList<>();
        for (T g : getAll()) {
            if (g.IsLow()) {
                result.add(g);
            }
        }
        return result;
    }

    // Sorts goods by quantity in descending order using selection sort.
    public List<T> sortByQuantityDesc() {
        List<T> sorted = getAll();
        int n = sorted.size();
        for (int i = 0; i < n - 1; i++) {
            int maxIdx = i;
            for (int j = i + 1; j < n; j++) {
                if (sorted.get(j).getQuantity() > sorted.get(maxIdx).getQuantity()) {
                    maxIdx = j;
                }
            }
            T temp = sorted.get(i);
            sorted.set(i, sorted.get(maxIdx));
            sorted.set(maxIdx, temp);
        }
        return sorted;
    }

    // Sorts goods by total stock value in descending order.
    public List<T> sortedByStockValueDesc() {
        List<T> sorted = getAll();
        Collections.sort(sorted, (a, b) -> Double.compare(b.calcStockValue(), a.calcStockValue()));
        return sorted;
    }

    // Returns the item with the smallest quantity on hand.
    public T findMinQuantity() {
        List<T> snapshot = getAll();
        if (snapshot.isEmpty())
            return null;
        return Collections.min(snapshot, Comparator.comparingInt(T::getQuantity));
    }

    public static final String DEFAULT_BACKUP_FILE = "goods_backup.ser";

    // Writes the full in-memory inventory snapshot to a backup file.
    public void saveToFile(String filePath) {
        List<T> snapshot = getAll();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(snapshot);
            System.out.println("[FILE] Saved " + snapshot.size() + " items to " + filePath);
        } catch (IOException e) {
            System.out.println("[FILE] Write error: " + e.getMessage());
        }
    }

    // Restores the in-memory cache from a backup file.
    // This only reloads memory and does not sync the restored data back to the
    // database.
    @SuppressWarnings("unchecked")
    public void loadFromFile(String filePath) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            List<T> loaded = (List<T>) ois.readObject();
            synchronized (lock) {
                map.clear();
                list.clear();
                for (T g : loaded) {
                    if (!map.containsKey(g.getCode())) {
                        map.put(g.getCode(), g);
                        list.add(g);
                    }
                }
            }
            System.out.println("[FILE] Loaded" + loaded.size() + " items from " + filePath);
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("[FILE] Read error: " + e.getMessage());
        }
    }

    // Computes an urgency label based on the remaining shelf-life in days.
    public String calcUrgency(int expiryDays) {
        String urgency = switch (expiryDays) {
            case 0, 1 -> "Expires today/tomorrow";
            case 2, 3 -> "Expiring soon";
            default -> {
                yield expiryDays < 7 ? "Sell urgently" : "In stock";
            }
        };
        return urgency;
    }

    // Loads data from the database on a background thread without a callback.
    public Thread loadFromDBAsync() {
        Thread worker = new Thread(this::loadFromDB, "load-db-thread");
        worker.setDaemon(true);
        worker.start();
        return worker;
    }

    // Loads data asynchronously and notifies the UI thread when finished.
    public Thread loadFromDBAsync(Runnable onCommplete) {
        Thread worker = new Thread(() -> {
            loadFromDB();
            if (onCommplete != null) {
                Platform.runLater(onCommplete);
            }
        }, "load-db-thread");
        worker.setDaemon(true);
        worker.start();
        return worker;
    }

    // Imports goods on a background thread and returns the result on the JavaFX UI
    // thread.
    public Thread importAsync(T g, Consumer<Boolean> onResult) {
        Thread worker = new Thread(() -> {
            boolean ok = importGoods(g);
            Platform.runLater(() -> onResult.accept(ok));
        }, "import-thread");
        worker.setDaemon(true);
        worker.start();
        return worker;
    }

    // Exports goods asynchronously and reports an error message, if any, to the UI
    // thread.
    public Thread exportAsync(String code, int qty, Consumer<String> onError) {
        Thread worker = new Thread(() -> {
            try {
                exportGoods(code, qty);
                Platform.runLater(() -> onError.accept(null));
            } catch (IllegalArgumentException | IllegalStateException e) {
                Platform.runLater(() -> onError.accept(e.getMessage()));
            }
        }, "export-thread");
        worker.setDaemon(true);
        worker.start();
        return worker;
    }

    // Calculates the total stock value in the background and sends the result to
    // the UI thread.
    public Thread calcTotalStockValueAsync(Consumer<Double> onResult) {
        Thread worker = new Thread(() -> {
            double total = calcTotalStockValue();
            Platform.runLater(() -> onResult.accept(total));
        }, "calc-total-thread");
        worker.setDaemon(true);
        worker.start();
        return worker;
    }

    // Sorts the inventory asynchronously and returns the sorted snapshot to the UI
    // thread.
    public Thread sortAndDisplayAsync(Consumer<List<T>> onResult) {
        Thread worker = new Thread(() -> {
            List<T> sorted = sortByQuantityDesc();
            Platform.runLater(() -> onResult.accept(sorted));
        }, "sort-thread");
        worker.setDaemon(true);
        worker.start();
        return worker;
    }

    // Saves the current inventory to a file in the background and runs a completion
    // callback.
    public Thread saveToFileAsync(String filePath, Runnable onComplete) {
        Thread worker = new Thread(() -> {
            saveToFile(filePath);
            if (onComplete != null) {
                Platform.runLater(onComplete);
            }
        }, "backup-thread");
        worker.setDaemon(true);
        worker.start();
        return worker;
    }

    // Restores the inventory from a file in the background and runs a completion
    // callback.
    public Thread loadFromFileAsync(String filePath, Runnable onComplete) {
        Thread worker = new Thread(() -> {
            loadFromFile(filePath);
            if (onComplete != null) {
                Platform.runLater(onComplete);
            }
        }, "restore-thread");
        worker.setDaemon(true);
        worker.start();
        return worker;
    }
}
