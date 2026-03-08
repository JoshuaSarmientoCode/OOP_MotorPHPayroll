package dao;

import service.DataService;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.*;

public abstract class BaseDAO<T> implements DataService {
    
    protected static final Logger LOGGER = Logger.getLogger(BaseDAO.class.getName());
    protected String filePath;
    protected List<T> cache;
    
    public BaseDAO(String filePath) {
        this.filePath = filePath;
        this.cache = new ArrayList<>();
        ensureFileExists();
        loadFromFile();
    }
    
    // Abstract methods that child classes must implement
    public abstract T fromCSV(String csvLine);
    public abstract String toCSV(T item);
    protected abstract String[] getHeaders();
    protected abstract String getId(T item);
    
    private void ensureFileExists() {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path.getParent());
                Files.createFile(path);
                // Write headers
                try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                    writer.write(String.join(",", getHeaders()));
                    writer.newLine();
                }
                LOGGER.info("Created new file: " + filePath);
            } catch (IOException e) {
                LOGGER.severe("Cannot create file: " + filePath);
            }
        }
    }
    
    /**
     * Load data from file into cache
     */
    protected void loadFromFile() {
        cache.clear();
        Path path = Paths.get(filePath);
        
        if (!Files.exists(path)) {
            return;
        }
        
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            boolean isFirstLine = true;
            int lineNumber = 0;
            int errorCount = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Skip header
                }
                
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                try {
                    T item = fromCSV(line);
                    if (item != null) {
                        cache.add(item);
                    }
                } catch (Exception e) {
                    errorCount++;
                    LOGGER.fine("Error parsing line " + lineNumber + ": " + e.getMessage());
                }
            }
            
            LOGGER.info("Loaded " + cache.size() + " records from " + filePath + 
                       (errorCount > 0 ? " (skipped " + errorCount + " errors)" : ""));
            
        } catch (IOException e) {
            LOGGER.severe("Error reading file: " + filePath);
        }
    }
    
    /**
     * Read all items from file
     */
    public List<T> readAll() {
        return new ArrayList<>(cache);
    }
    
    /**
     * Force reload from file
     */
    public void refresh() {
        loadFromFile();
    }
    
    /**
     * Find item by ID
     */
    public T findById(String id) {
        return cache.stream()
                .filter(item -> getId(item).equals(id))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Check if exists by ID
     */
    public boolean exists(String id) {
        return cache.stream()
                .anyMatch(item -> getId(item).equals(id));
    }
    
    /**
     * Add new item
     */
    public boolean add(T item) {
        if (item == null) return false;
        
        String id = getId(item);
        if (exists(id)) {
            LOGGER.warning("Item with ID " + id + " already exists");
            return false;
        }
        
        cache.add(item);
        return true;
    }
    
    /**
     * Update existing item
     */
    public boolean update(T item) {
        if (item == null) return false;
        
        String id = getId(item);
        for (int i = 0; i < cache.size(); i++) {
            if (getId(cache.get(i)).equals(id)) {
                cache.set(i, item);
                return true;
            }
        }
        
        LOGGER.warning("Item with ID " + id + " not found for update");
        return false;
    }
    
    /**
     * Delete item by ID
     */
    public boolean delete(String id) {
        return cache.removeIf(item -> getId(item).equals(id));
    }
    
    /**
     * Get count of items
     */
    public int count() {
        return cache.size();
    }
    
    // ========== DataService INTERFACE IMPLEMENTATION ==========
    
    @Override
    public List<String[]> readData(String key) {
        List<String[]> stringData = new ArrayList<>();
        for (T item : cache) {
            stringData.add(toCSV(item).split(",", -1));
        }
        return stringData;
    }
    
    @Override
    public List<String[]> readDataWithHeader(String key) {
        List<String[]> stringData = new ArrayList<>();
        stringData.add(getHeaders());
        for (T item : cache) {
            stringData.add(toCSV(item).split(",", -1));
        }
        return stringData;
    }
    
    @Override
    public void writeData(String key, List<String[]> data) {
        // Not implemented for now
    }
    
    @Override
    public void appendData(String key, String[] newData) {
        String csvLine = String.join(",", newData);
        try {
            T item = fromCSV(csvLine);
            add(item);
        } catch (Exception e) {
            LOGGER.severe("Error appending data: " + e.getMessage());
        }
    }
    
    @Override
    public void deleteData(String key, String id) {
        delete(id);
    }
    
    @Override
    public boolean updateData(String key, String id, String[] updatedData) {
        String csvLine = String.join(",", updatedData);
        try {
            T item = fromCSV(csvLine);
            return update(item);
        } catch (Exception e) {
            LOGGER.severe("Error updating data: " + e.getMessage());
            return false;
        }
    }
}