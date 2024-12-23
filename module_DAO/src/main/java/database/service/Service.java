/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package database.service;

import database.annotation.AnnotationCheker;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Map;
import java.sql.Timestamp;
import database.access.Postgres;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import database.util.Pagination;

/**
 *
 * @author Andra
 */
public class Service {

    private Postgres conn;
    private Pagination pagination;

    public Postgres getConn() {
        return conn;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setConn(Postgres conn) {
        this.conn = conn;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

    //yes
    public void insert(Object obj, Connection conn) throws Exception {
        try {

            AnnotationCheker annot = AnnotationCheker.getAnnotationInsert(obj);

            String table = annot.getTableName();
            Map<String, Object> columns = annot.getColumnNames();

            String column = String.join(",", columns.keySet());
            String valuePlaceholders = String.join(",", columns.keySet().stream().map(c -> "?").toArray(String[]::new));

            String query = "INSERT INTO " + table + " (" + column + ") VALUES (" + valuePlaceholders + ")";

            PreparedStatement stmt = null;
            try {
                stmt = conn.prepareStatement(query);
                conn.setAutoCommit(false);

                int parameterIndex = 1;
                for (Object paramValue : columns.values()) {
                    if (paramValue instanceof Timestamp) {
                        stmt.setTimestamp(parameterIndex, (Timestamp) paramValue);
                    } else {
                        stmt.setObject(parameterIndex, paramValue);
                    }
                    parameterIndex++;
                }
                int rowsAffected = stmt.executeUpdate();

                // Commit the transaction if successful
                conn.commit();
            } finally {
                if (stmt != null) {
                    stmt.close();
                }
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public void insert(Object obj) throws Exception {
        Connection conn = getConn().getConnection();
        try {
            insert(obj, conn);
        } catch (Exception e) {
            if (conn != null) {
                conn.rollback();
            }

            throw e;
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    //yes
    public <T> List<T> getAll(Class<T> resultType, String offset, Connection connection) throws Exception {
        List<T> resultList = new ArrayList<>();

        AnnotationCheker annot = AnnotationCheker.getAnnotationSelect(resultType);

        String table = annot.getTableName();
        String query = "SELECT * FROM " + table;

        if (offset != null) {
            query += getPagination().ScriptSql(offset);
        }

        try (PreparedStatement statement = connection.prepareStatement(query); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                T classObject = resultType.getDeclaredConstructor().newInstance();

                for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                    String columnName = resultSet.getMetaData().getColumnName(i);
                    Object columnValue = resultSet.getObject(i);

                    // Traduction du nom d'annotation par son nom d'attribut
                    String fieldName = AnnotationCheker.getFieldNameByAnnotationName(resultType, columnName);

                    try {
                        Field field = null;

                        try {
                            field = resultType.getDeclaredField(fieldName);
                            field.setAccessible(true);
                            if (field != null) {
                                field.set(classObject, convertToFieldType(columnValue, field.getType()));
                            }
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            e.printStackTrace(); // Handle or log the exception appropriately
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                resultList.add(classObject);
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                | NoSuchMethodException e) {
            // Handle or log the exception appropriately
            e.printStackTrace();
        }

        return resultList;
    }

    public <T> List<T> getAll(Class<T> resultType, String offset) throws Exception {
        List<T> resultList = null;
        Connection conn = getConn().getConnection();
        try {
            resultList = getAll(resultType, offset, conn);
        } catch (Exception e) {
            throw e;
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
        return resultList;
    }

    private static Object convertToFieldType(Object columnValue, Class<?> fieldType) {
        return fieldType.isPrimitive()
                ? convertPrimitiveType(columnValue, fieldType)
                : fieldType.cast(columnValue);
    }

    private static Object convertPrimitiveType(Object columnValue, Class<?> primitiveType) {
        if (primitiveType == int.class) {
            return ((Number) columnValue).intValue();
        } else if (primitiveType == double.class) {
            return ((Number) columnValue).doubleValue();
        } else if (primitiveType == float.class) {
            return ((Number) columnValue).floatValue();
        } // Add more conversions as needed

        return columnValue;
    }

    //recherche par une seule variable
    //Il ne faut pas que le donnée soit èquivalent à zero 
    //ceci est une requete qui utilise seulement AND
    public <T> List<T> getByCriteria(Object obj, String offset, Connection conn) throws Exception {
        List<T> resultList = new ArrayList<>();

        try {
            AnnotationCheker annot = AnnotationCheker.getAnnotationCriteria(obj);

            String table = annot.getTableName();
            Map<String, Object> columns = annot.getColumnNames();

            StringBuilder whereClause = new StringBuilder();
            for (Map.Entry<String, Object> entry : columns.entrySet()) {
                String columnName = entry.getKey();
                Object columnValue = entry.getValue();

                if (whereClause.length() > 0) {
                    whereClause.append(" AND ");
                }
                whereClause.append(columnName).append(" = ?");
            }

            String query = "SELECT * FROM " + table + " WHERE " + whereClause.toString();

            if (offset != null) {
                query += getPagination().ScriptSql(offset);
            }

            try (PreparedStatement stmt = conn.prepareStatement(query)) {

                int parameterIndex = 1;
                for (Object paramValue : columns.values()) {
                    if (paramValue instanceof Timestamp) {
                        stmt.setTimestamp(parameterIndex, (Timestamp) paramValue);
                    } else {
                        stmt.setObject(parameterIndex, paramValue);
                    }
                    parameterIndex++;
                }

                try (ResultSet resultSet = stmt.executeQuery()) {
                    while (resultSet.next()) {
                        Class<?> resultType = obj.getClass();
                        T classObject = (T) resultType.getDeclaredConstructor().newInstance();

                        for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                            String columnName = resultSet.getMetaData().getColumnName(i);
                            Object columnValue = resultSet.getObject(i);

                            // Translate annotation name to field name
                            String fieldName = AnnotationCheker.getFieldNameByAnnotationName(resultType, columnName);

                            try {
                                Field field = resultType.getDeclaredField(fieldName);
                                field.setAccessible(true);
                                field.set(classObject, convertToFieldType(columnValue, field.getType()));
                            } catch (NoSuchFieldException | IllegalAccessException e) {
                                e.printStackTrace(); // Handle or log the exception appropriately
                            }
                        }
                        resultList.add(classObject);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Handle or log the exception appropriately
        }
        return resultList;
    }

    public <T> List<T> getByCriteria(Object obj, String offset) throws Exception {
        List<T> resultList = null;
        Connection conn = getConn().getConnection();
        try {
            resultList = getByCriteria(obj, offset, conn);
        } catch (Exception e) {
            throw e;
        } finally {
            if (conn != null) {
                conn.close();
            }
        }

        return resultList;
    }

    //recherche entre deux variable
    public <T> List<T> getListBetween(Object min, Object max, String offset, Connection conn) throws Exception {
        List<T> resuList = new ArrayList<>();

        try {
            AnnotationCheker annot = AnnotationCheker.getAnnotationBetweenValue(min, max);

            String table = annot.getTableName();
            Map<String, Object> columns = annot.getColumnNames();

            StringBuilder whereClause = new StringBuilder();
            List<Object> parameters = new ArrayList<>();

            for (Map.Entry<String, Object> entry : columns.entrySet()) {
                String columnName = entry.getKey();
                Object columnValue = entry.getValue();

                if (columnValue instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> valueMap = (Map<String, Object>) columnValue;
                    if (valueMap.containsKey("min") && valueMap.containsKey("max")) {
                        if (whereClause.length() > 0) {
                            whereClause.append(" AND ");
                        }
                        whereClause.append(columnName).append(" BETWEEN ? AND ?");
                        parameters.add(valueMap.get("min"));
                        parameters.add(valueMap.get("max"));
                    } else {
                        throw new IllegalArgumentException("Invalid criteria: " + columnValue);
                    }
                }
            }

            String query = "SELECT * FROM " + table + " WHERE " + whereClause.toString();

            if (offset != null) {
                query += getPagination().ScriptSql(offset);
            }

            try (PreparedStatement stmt = conn.prepareStatement(query)) {

                int parameterIndex = 1;
                for (Object paramValue : parameters) {
                    if (paramValue instanceof Timestamp) {
                        stmt.setTimestamp(parameterIndex, (Timestamp) paramValue);
                    } else {
                        stmt.setObject(parameterIndex, paramValue);
                    }
                    parameterIndex++;
                }

                try (ResultSet resultset = stmt.executeQuery()) {
                    while (resultset.next()) {
                        Class<?> resultType = min.getClass();
                        T classObject = (T) resultType.getDeclaredConstructor().newInstance();

                        for (int i = 1; i <= resultset.getMetaData().getColumnCount(); i++) {
                            String columnName = resultset.getMetaData().getColumnName(i);
                            Object columnValue = resultset.getObject(i);

                            String fieldName = AnnotationCheker.getFieldNameByAnnotationName(resultType, columnName);
                            try {
                                Field field = resultType.getDeclaredField(fieldName);
                                field.setAccessible(true);
                                field.set(classObject, convertToFieldType(columnValue, field.getType()));
                            } catch (NoSuchFieldException | IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        resuList.add(classObject);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resuList;
    }

    public <T> List<T> getListBetween(Object min, Object max, String offset) throws Exception {
        List<T> resuList = null;
        Connection conn = getConn().getConnection();

        try {
            resuList = getListBetween(min, max, offset, conn);
        } catch (Exception e) {
            throw e;
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
        return resuList;
    }

    public void delete(Object obj, Connection conn) throws Exception {
        try {
            // Get annotations for the object
            AnnotationCheker annot = AnnotationCheker.getAnnotationCriteria(obj);

            String table = annot.getTableName();
            Map<String, Object> columns = annot.getColumnNames();

            if (columns.isEmpty()) {
                throw new Exception("Primary key information is required for deletion.");
            }

            String whereClause = String.join(" AND ",
                    columns.keySet().stream().map(key -> key + " = ?").toArray(String[]::new));

            String query = "DELETE FROM " + table + " WHERE " + whereClause;

            PreparedStatement stmt = null;
            try {
                stmt = conn.prepareStatement(query);
                conn.setAutoCommit(false);

                int parameterIndex = 1;
                for (Object paramValue : columns.values()) {
                    if (paramValue instanceof Timestamp) {
                        stmt.setTimestamp(parameterIndex, (Timestamp) paramValue);
                    } else {
                        stmt.setObject(parameterIndex, paramValue);
                    }
                    parameterIndex++;
                }
                int rowsAffected = stmt.executeUpdate();

                // Commit the transaction if successful
                conn.commit();
            } finally {
                if (stmt != null) {
                    stmt.close();
                }
            }
        } catch (Exception e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        }
    }

    public void delete(Object obj) throws Exception {
        Connection conn = getConn().getConnection();
        try {
            delete(obj, conn);
        } catch (Exception e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

}
