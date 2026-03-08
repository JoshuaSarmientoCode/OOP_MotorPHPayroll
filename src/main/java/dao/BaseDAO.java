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
     * Write all items to file (overwrites entire file)
     * Each item becomes a new row with fields in correct columns
     */
    public synchronized boolean writeToFile() {
        Path tempFile = null;
        try {
            // Create a temporary file first
            tempFile = Files.createTempFile("temp_", ".tmp");

            // Write to temporary file
            try (BufferedWriter writer = Files.newBufferedWriter(tempFile)) {
                // Write headers first
                writer.write(String.join(",", getHeaders()));
                writer.newLine(); // This adds a line separator

                // Write each item as a new row
                for (T item : cache) {
                    writer.write(toCSV(item)); // toCSV should not include newline
                    writer.newLine(); // Add newline after each record
                }
                writer.flush();
            }

            // Atomically move temp file to target location
            Path targetFile = Paths.get(filePath);
            Files.move(tempFile, targetFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);

            LOGGER.info("Successfully wrote " + cache.size() + " records to " + filePath);
            return true;

        } catch (IOException e) {
            LOGGER.severe("Error writing to file: " + filePath + " - " + e.getMessage());
            // Clean up temp file if it exists
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ex) {
                    // Ignore
                }
            }
            return false;
        }
    }

    /**
     * Append a single item to the file (as a new row at the end)
     * FIXED: Ensures the new record starts on a new line in the first column
     */
    public synchronized boolean appendToFile(T item) {
        try {
            Path path = Paths.get(filePath);

            // Check if file exists and has content
            boolean fileExists = Files.exists(path);
            boolean fileHasContent = fileExists && Files.size(path) > 0;

            try (FileWriter fw = new FileWriter(filePath, true);
                 BufferedWriter bw = new BufferedWriter(fw);
                 PrintWriter out = new PrintWriter(bw)) {

                // If file exists and has content, ensure we're starting on a new line
                if (fileHasContent) {
                    // Check if the file ends with a newline
                    boolean endsWithNewline = false;

                    try (RandomAccessFile raf = new RandomAccessFile(filePath, "r")) {
                        if (raf.length() >= 2) {
                            raf.seek(raf.length() - 2);
                            byte[] lastTwo = new byte[2];
                            raf.read(lastTwo);

                            // Check for Windows (\r\n) or Unix (\n) line endings
                            if ((lastTwo[0] == '\r' && lastTwo[1] == '\n') || lastTwo[1] == '\n') {
                                endsWithNewline = true;
                            }
                        } else if (raf.length() == 1) {
                            raf.seek(0);
                            byte lastChar = raf.readByte();
                            if (lastChar == '\n' || lastChar == '\r') {
                                endsWithNewline = true;
                            }
                        }
                    } catch (Exception e) {
                        // If we can't read, assume it doesn't end with newline
                        endsWithNewline = false;
                    }

                    // Add a newline if the file doesn't end with one
                    if (!endsWithNewline) {
                        out.println();
                    }
                }

                // Write the item data (without any extra newline at the end)
                out.print(toCSV(item));

                // Always add a newline after the record
                out.println();
                out.flush();

                LOGGER.info("Successfully appended record to " + filePath);
                return true;

            }
        } catch (IOException e) {
            LOGGER.severe("Error appending to file: " + filePath + " - " + e.getMessage());
            return false;
        }
    }

    /**
     * Read all items from cache
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
     * Add new item - FIXED to ensure proper CSV formatting
     */
    public boolean add(T item) {
        if (item == null) return false;

        String id = getId(item);
        if (exists(id)) {
            LOGGER.warning("Item with ID " + id + " already exists");
            return false;
        }

        cache.add(item);

        // Use appendToFile for better performance, but fall back to writeToFile if needed
        boolean written = appendToFile(item);
        if (!written) {
            // Try rewriting entire file as fallback
            written = writeToFile();
            if (!written) {
                // Rollback cache if file write fails
                cache.remove(item);
                return false;
            }
        }

        LOGGER.info("Added item with ID: " + id);
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
                T oldItem = cache.get(i);
                cache.set(i, item);

                // Must rewrite entire file for updates (to maintain order)
                boolean written = writeToFile();
                if (written) {
                    LOGGER.info("Updated item with ID: " + id);
                    return true;
                } else {
                    // Rollback cache if file write fails
                    cache.set(i, oldItem);
                    return false;
                }
            }
        }

        LOGGER.warning("Item with ID " + id + " not found for update");
        return false;
    }

    /**
     * Delete item by ID
     */
    public boolean delete(String id) {
        Optional<T> toRemove = cache.stream()
                .filter(item -> getId(item).equals(id))
                .findFirst();

        if (toRemove.isPresent()) {
            cache.remove(toRemove.get());

            // Must rewrite entire file for deletions
            boolean written = writeToFile();
            if (written) {
                LOGGER.info("Deleted item with ID: " + id);
                return true;
            } else {
                // If file write fails, restore the item
                cache.add(toRemove.get());
                return false;
            }
        }

        LOGGER.warning("Item with ID " + id + " not found for deletion");
        return false;
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
        // Convert string arrays to items and save
        List<T> items = new ArrayList<>();
        for (String[] row : data) {
            String csvLine = String.join(",", row);
            try {
                T item = fromCSV(csvLine);
                if (item != null) {
                    items.add(item);
                }
            } catch (Exception e) {
                LOGGER.warning("Error parsing row: " + Arrays.toString(row));
            }
        }
        cache = items;
        writeToFile();
    }

    @Override
    public void appendData(String key, String[] newData) {
        String csvLine = String.join(",", newData);
        try {
            T item = fromCSV(csvLine);
            if (item != null) {
                add(item);
            }
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