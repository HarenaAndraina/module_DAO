/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package database.util;

import database.access.Postgres;
/**
 *
 * @author Andra
 */
public class GenericDAO {
    private Postgres connection;
    private Pagination pagination;

    public Postgres getConnection() {
        return connection;
    }
    
    
}
