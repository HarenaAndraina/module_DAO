/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package database.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 *
 * @author Andra
 */
public class AnnotationCheker {
    private String tableName;
    private Map<String,Object> columnNames;

    public String getTableName() {
        return tableName;
    }

    public Map<String, Object> getColumnNames() {
        return columnNames;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setColumnNames(Map<String, Object> columnNames) {
        this.columnNames = columnNames;
    }

    public AnnotationCheker(String tableName, Map<String, Object> columnNames) {
        this.tableName = tableName;
        this.columnNames = columnNames;
    }

    public AnnotationCheker(String tableName) {
        this.tableName = tableName;
    }
    

    public static AnnotationCheker getAnnotationInsert(Object obj) throws Exception {
        Class<?> type = obj.getClass();        

        // Vérifie l'annotation @Table
        Table tableAttribute = type.getAnnotation(Table.class);
        if (tableAttribute == null) {
            throw new Exception("Annotation cheker: annotation @Table non trouvée");
        }

        String table = tableAttribute.name();

        // Récupère les attributs de colonne
        Field[] fields = type.getDeclaredFields();
        Map<String, Object> columnValues = new HashMap<>();

        for (Field field : fields) {
            Column columnAttribute = field.getAnnotation(Column.class);
            if (columnAttribute != null && !columnAttribute.isAutoIncrement()) {
                field.setAccessible(true);
                try {
                    columnValues.put(columnAttribute.name(), field.get(obj));
                } catch (Exception e) {
                    throw e;
                }
            }
        }

        if (columnValues.isEmpty()) {
            throw new Exception("Annotation cheker: annotation @Column non trouvée");
        }

        return new AnnotationCheker( table, columnValues);
    }

    
    public static <T> String getFieldNameByAnnotationName(Class<T> clazz, String annotationName) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            Annotation[] annotations = field.getDeclaredAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation instanceof Column) {
                    String value = ((Column) annotation).name();
                    if (value.equals(annotationName)) {
                        return field.getName();
                    }
                }
            }
        }
        return null; // Return null if no field found with the given annotation name
    }

    public static AnnotationCheker getAnnotationSelect(Class<?> type) throws Exception{
        // Vérifie l'annotation @Table
        Table tableAttribute = type.getAnnotation(Table.class);
        if (tableAttribute == null) {
            throw new Exception("Annotation cheker: annotation @Table non trouvée");
        }

        String table = tableAttribute.name();

        return new AnnotationCheker( table);
    }

    public static AnnotationCheker getAnnotationCriteria( Object obj) throws Exception{
        Class<?> type = obj.getClass();

        // Vérifie l'annotation @Table
        Table tableAttribute = type.getAnnotation(Table.class);

        if (tableAttribute == null) {
            throw new Exception("Annotation cheker: annotation @Table non trouvée");
        }

        String table = tableAttribute.name();

        // Récupère les attributs de colonne
        Field[] fields = type.getDeclaredFields();
        Map<String, Object> columnValues = new HashMap<>();

        for (Field field : fields) {
            Column columnAttribute = field.getAnnotation(Column.class);
            if (columnAttribute != null) {
                field.setAccessible(true);
                try {
                    if (field.get(obj) != null && !field.get(obj).equals(0)) {
                        columnValues.put(columnAttribute.name(),  field.get(obj));
                    }
                } catch (Exception e) {
                    throw e;
                }
            }
        }

        if (columnValues.isEmpty()) {   
            throw new Exception("Annotation cheker: annotation @Column non trouvée");
        }

        return new AnnotationCheker( table,columnValues);
    }
    //pagination 
    

    //listbetweenValue
    public static AnnotationCheker getAnnotationBetweenValue(Object min,Object max) throws Exception {
        Class<?> mintype = min.getClass();
        Class<?> maxtype=max.getClass();

        // Vérifie l'annotation @Table
        Table tableAttributemin = mintype.getAnnotation(Table.class);
        if (tableAttributemin == null) {
            throw new Exception("Annotation cheker: annotation @Table non trouvée");
        }

        Table tableAttributemax=maxtype.getAnnotation(Table.class);
        if (tableAttributemax == null) {
            throw new Exception("Annotation cheker: annotation @Table non trouvée");
        }

        if (!tableAttributemin.equals(tableAttributemax)) {
            throw new Exception("Annotation cheker: annotation @Table non identique");
        }

        String table = tableAttributemin.name();

        // Récupère les attributs de colonne min
        Field[] fieldsmin = mintype.getDeclaredFields();
        Map<String, Object> columnValuesmin = new HashMap<>();

        for (Field field : fieldsmin) {
            Column columnAttribute = field.getAnnotation(Column.class);
            if (columnAttribute != null) {
                field.setAccessible(true);
                try {
                    if (field.get(min) != null && !field.get(min).equals(0)) {
                        columnValuesmin.put(columnAttribute.name(),  field.get(min));
                    }
                } catch (Exception e) {
                    throw e;
                }
            }
        }

        if (columnValuesmin.isEmpty()) {
            throw new Exception("Annotation cheker: annotation @Column non trouvée");
        }

        // Récupère les attributs de colonne
        Field[] fieldsmax = maxtype.getDeclaredFields();
        Map<String, Object> columnValuesmax = new HashMap<>();

        for (Field field : fieldsmax) {
            Column columnAttribute = field.getAnnotation(Column.class);
            if (columnAttribute != null) {
                field.setAccessible(true);
                try {
                    if (field.get(max) != null && !field.get(max).equals(0)) {
                        columnValuesmax.put(columnAttribute.name(),  field.get(max));
                    }
                } catch (Exception e) {
                    throw e;
                }
            }
        }

        if (columnValuesmax.isEmpty()) {
            throw new Exception("Annotation cheker: annotation @Column non trouvée");
        }

        if (columnValuesmax.size() != columnValuesmin.size() ) {
            throw new Exception("Annotation cheker: il faut que nombre de colonne soit identique");
        }

        //rasseblement des valeurs min et max
        Map<String,Object>columnValues=new HashMap<>();
        for (Map.Entry<String,Object> entry : columnValuesmin.entrySet()) {
            String columnName=entry.getKey();
            Object minValue=entry.getValue();

            if (columnValuesmax.containsKey(columnName)) {
                Object maxValue=columnValuesmax.get(columnName);
                
                columnValues.put(columnName, Map.of("min", minValue, "max", maxValue));
            }else{
                throw new Exception("Annotation cheker: mila hita anaty kilasy roa nenaran'ilay colonne");
            }
        }

        return new AnnotationCheker( table,columnValues);
    }
}
