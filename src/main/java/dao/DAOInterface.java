package dao;

import java.util.List;


public interface DAOInterface<T> {

    // ========== CSV CONVERSION ==========

    T fromCSV(String csvLine);

    String toCSV(T item);

    // ========== CRUD OPERATIONS ==========

    boolean add(T item);

    boolean update(T item);

    boolean delete(String id);

    List<T> readAll();

    T findById(String id);

    // ========== UTILITY ==========

    boolean exists(String id);

    int count();

    void refresh();

    boolean writeToFile();

    boolean appendToFile(T item);
}