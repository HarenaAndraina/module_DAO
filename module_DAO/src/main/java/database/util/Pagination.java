package database.util;

public class Pagination {
    private String moteurSQL;
    private String nombre_pagination;

    public String getMoteurSQL() {
        return moteurSQL;
    }

    public String getNombre_pagination() {
        return nombre_pagination;
    }

    public void setMoteurSQL(String moteurSQL) {
        this.moteurSQL = moteurSQL;
    }

    public void setNombre_pagination(String nombre_pagination) {
        this.nombre_pagination = nombre_pagination;
    }
    
    public boolean checkProperties() {
        
        if (this.getMoteurSQL() == null || this.getNombre_pagination() == null) {
            return false;
        } else {
            return true;
        }
    }

    public String ScriptSql(String offset) throws Exception {
        String script = null;
        
        if (checkProperties()) {
            if (getMoteurSQL().equalsIgnoreCase("postgres")) {
                script = " limit "+getNombre_pagination()+" offset "+offset;
            }
        }

        return script;
    }
}
