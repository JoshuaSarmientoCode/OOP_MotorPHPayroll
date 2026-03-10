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

    public abstract T fromCSV(String csvLine);
    public abstract String toCSV(T item);
    protected abstract String[] getHeaders();
    protected abstract String getId(T item);

    // ========== SHARED CSV HELPERS (inherited by all DAOs) ==========

    /**
     * Parse a CSV line respecting quoted fields
     */
    protected String[] parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }
        fields.add(currentField.toString());
        return fields.toArray(new String[0]);
    }

    /**
     * Safely get a value from a parsed CSV array by index
     */
    protected String safeGet(String[] data, int index) {
        if (index < 0 || index >= data.length) return "";
        return data[index] != null ? data[index].trim() : "";
    }

    /**
     * Parse a double from a string, removing commas and currency symbols
     */
    protected double parseDouble(String value) {
        if (value == null || value.trim().isEmpty()) return 0.0;
        try {
            return Double.parseDouble(value.trim().replace(",", "").replace("₱", "").trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * Parse an int from a string safely
     */
    protected int parseInt(String value) {
        if (value == null || value.trim().isEmpty()) return 0;
        try {
            return Integer.parseInt(value.trim().replace(",", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // ========== FILE OPERATIONS ==========

    private void ensureFileExists() {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path.getParent());
                Files.createFile(path);
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

    protected void loadFromFile() {
        cache.clear();
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) return;

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            boolean isFirstLine = true;
            int lineNumber = 0;
            int errorCount = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (isFirstLine) { isFirstLine = false; continue; }
                if (line.trim().isEmpty()) continue;

                try {
                    T item = fromCSV(line);
                    if (item != null) cache.add(item);
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

    public synchronized boolean writeToFile() {
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("temp_", ".tmp");

            try (BufferedWriter writer = Files.newBufferedWriter(tempFile)) {
                writer.write(String.join(",", getHeaders()));
                writer.newLine();
                for (T item : cache) {
                    writer.write(toCSV(item));
                    writer.newLine();
                }
                writer.flush();
            }

            Path targetFile = Paths.get(filePath);
            Files.move(tempFile, targetFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            LOGGER.info("Successfully wrote " + cache.size() + " records to " + filePath);
            return true;

        } catch (IOException e) {
            LOGGER.severe("Error writing to file: " + filePath + " - " + e.getMessage());
            if (tempFile != null) {
                try { Files.deleteIfExists(tempFile); } catch (IOException ex) { /* ignore */ }
            }
            return false;
        }
    }

    public synchronized boolean appendToFile(T item) {
        try {
            Path path = Paths.get(filePath);
            boolean fileHasContent = Files.exists(path) && Files.size(path) > 0;

            try (FileWriter fw = new FileWriter(filePath, true);
                 BufferedWriter bw = new BufferedWriter(fw);
                 PrintWriter out = new PrintWriter(bw)) {

                if (fileHasContent) {
                    boolean endsWithNewline = false;
                    try (RandomAccessFile raf = new RandomAccessFile(filePath, "r")) {
                        if (raf.length() >= 2) {
                            raf.seek(raf.length() - 2);
                            byte[] lastTwo = new byte[2];
                            raf.read(lastTwo);
                            if ((lastTwo[0] == '\r' && lastTwo[1] == '\n') || lastTwo[1] == '\n') {
                                endsWithNewline = true;
                            }
                        } else if (raf.length() == 1) {
                            raf.seek(0);
                            byte lastChar = raf.readByte();
                            if (lastChar == '\n' || lastChar == '\r') endsWithNewline = true;
                        }
                    } catch (Exception e) {
                        endsWithNewline = false;
                    }
                    if (!endsWithNewline) out.println();
                }

                out.print(toCSV(item));
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

    public List<T> readAll() { return new ArrayList<>(cache); }

    public void refresh() { loadFromFile(); }

    public T findById(String id) {
        return cache.stream()
                .filter(item -> getId(item).equals(id))
                .findFirst().orElse(null);
    }

    public boolean exists(String id) {
        return cache.stream().anyMatch(item -> getId(item).equals(id));
    }

    public boolean add(T item) {
        if (item == null) return false;
        String id = getId(item);
        if (exists(id)) {
            LOGGER.warning("Item with ID " + id + " already exists");
            return false;
        }
        cache.add(item);
        boolean written = appendToFile(item);
        if (!written) {
            written = writeToFile();
            if (!written) { cache.remove(item); return false; }
        }
        LOGGER.info("Added item with ID: " + id);
        return true;
    }

    public boolean update(T item) {
        if (item == null) return false;
        String id = getId(item);
        for (int i = 0; i < cache.size(); i++) {
            if (getId(cache.get(i)).equals(id)) {
                T oldItem = cache.get(i);
                cache.set(i, item);
                boolean written = writeToFile();
                if (written) {
                    LOGGER.info("Updated item with ID: " + id);
                    return true;
                } else {
                    cache.set(i, oldItem);
                    return false;
                }
            }
        }
        LOGGER.warning("Item with ID " + id + " not found for update");
        return false;
    }

    public boolean delete(String id) {
        Optional<T> toRemove = cache.stream()
                .filter(item -> getId(item).equals(id))
                .findFirst();

        if (toRemove.isPresent()) {
            cache.remove(toRemove.get());
            boolean written = writeToFile();
            if (written) {
                LOGGER.info("Deleted item with ID: " + id);
                return true;
            } else {
                cache.add(toRemove.get());
                return false;
            }
        }
        LOGGER.warning("Item with ID " + id + " not found for deletion");
        return false;
    }

    public int count() { return cache.size(); }

    // ========== DataService INTERFACE IMPLEMENTATION ==========

    @Override
    public List<String[]> readData(String key) {
        List<String[]> stringData = new ArrayList<>();
        for (T item : cache) stringData.add(toCSV(item).split(",", -1));
        return stringData;
    }

    @Override
    public List<String[]> readDataWithHeader(String key) {
        List<String[]> stringData = new ArrayList<>();
        stringData.add(getHeaders());
        for (T item : cache) stringData.add(toCSV(item).split(",", -1));
        return stringData;
    }

    @Override
    public void writeData(String key, List<String[]> data) {
        List<T> items = new ArrayList<>();
        for (String[] row : data) {
            String csvLine = String.join(",", row);
            try {
                T item = fromCSV(csvLine);
                if (item != null) items.add(item);
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
            if (item != null) add(item);
        } catch (Exception e) {
            LOGGER.severe("Error appending data: " + e.getMessage());
        }
    }

    @Override
    public void deleteData(String key, String id) { delete(id); }

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