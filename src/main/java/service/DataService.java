package service;

import java.util.List;

/**
 * DataService interface for CRUD operations on data sources
 * This follows the Interface Segregation Principle
 */
public interface DataService {
    
    /**
     * Read all data rows, skipping header
     * @param key Identifier for the data source (e.g., file path, table name)
     * @return List of string arrays representing data rows
     */
    List<String[]> readData(String key);

    /**
     * Read all data rows including header
     * @param key Identifier for the data source
     * @return List of string arrays with header as first element
     */
    List<String[]> readDataWithHeader(String key);

    /**
     * Write data, replacing existing content
     * @param key Identifier for the data source
     * @param data List of string arrays to write
     */
    void writeData(String key, List<String[]> data);

    /**
     * Append a single row to data source
     * @param key Identifier for the data source
     * @param newData String array representing new row
     */
    void appendData(String key, String[] newData);

    /**
     * Delete a record by its ID
     * @param key Identifier for the data source
     * @param id Unique identifier of record to delete
     */
    void deleteData(String key, String id);

    /**
     * Update an existing record
     * @param key Identifier for the data source
     * @param id Unique identifier of record to update
     * @param updatedData New data for the record
     * @return true if update successful, false otherwise
     */
    boolean updateData(String key, String id, String[] updatedData);
    
    /**
     * Check if a record exists
     * @param key Identifier for the data source
     * @param id Unique identifier to check
     * @return true if exists, false otherwise
     */
    default boolean exists(String key, String id) {
        return readData(key).stream()
            .anyMatch(row -> row.length > 0 && row[0].equals(id));
    }
    
    /**
     * Get count of records
     * @param key Identifier for the data source
     * @return number of records
     */
    default int count(String key) {
        return readData(key).size();
    }
}