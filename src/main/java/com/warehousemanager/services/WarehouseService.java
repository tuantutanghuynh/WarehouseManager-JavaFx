package com.warehousemanager.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.warehousemanager.models.entity.Goods;
import com.warehousemanager.repositories.GoodsRepository;

public class WarehouseService<T extends Goods> {
    // singleton
    private static WarehouseService<Goods> instance;

    public static WarehouseService<Goods> getInstance() {
        if (instance == null) {
            instance = new WarehouseService<>();
        }
        return instance;
    }

    public static void reset() {
        instance = null;
    }

    // state----------------------------
    private final Map<String, T> map = new HashMap<>();
    private final List<T> list = new ArrayList<>();
    private final Object lock = new Object();
    private final GoodsRepository repo = new GoodsRepository();

    // load from DB
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

    // crud----------------------------------------
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

    public T findByCode(String code) {
        synchronized (lock) {
            return map.get(code);
        }

    }

    public List<T> getAll() {
        synchronized (lock) {
            return new ArrayList<>(list);
        }
    }

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

    public double calcTotalStockValue() {
        double total = 0;
        for (T g : getAll()) {
            total += g.calcStockValue();
        }
        return total;
    }

    public List<T> findLowStock() {
        List<T> result = new ArrayList<>();
        for (T g: getAll()){
            if(g.IsLow()){
                result.add(g);
            }
        }
        return result;
    }

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

    public List<T> sortedByStockValueDesc() {
        List<T> sorted = getAll();
        Collections.sort(sorted, (a, b) -> Double.compare(b.calcStockValue(), a.calcStockValue()));
        return sorted;
    }

    // Item with the smallest quantity on hand (uses Collections.min)
    public T findMinQuantity() {
        List<T> snapshot = getAll();
        if (snapshot.isEmpty())
            return null;
        return Collections.min(snapshot, Comparator.comparingInt(T::getQuantity));
    }
}
