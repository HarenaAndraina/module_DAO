/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import database.annotation.Column;
import database.annotation.Table;

/**
 *
 * @author Andra
 */

@Table(name = "client")
public class Client {
    @Column(name = "id_client", isPrimaryKey = true,isAutoIncrement = true)
    private int id_client;

    @Column(name = "nom")
    private String nom;

    @Column(name = "prenom")
    private String prenom;

    @Column(name = "telephone")
    private String phone;
    
    public int getId_client() {
        return id_client;
    }
    public void setId_client(int id_client) {
        this.id_client = id_client;
    }
    public String getNom() {
        return nom;
    }
    public void setNom(String nom) {
        this.nom = nom;
    }
    public String getPrenom() {
        return prenom;
    }
    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Client(String nom, String prenom, String phone) {
        this.nom = nom;
        this.prenom = prenom;
        this.phone = phone;
    }
    public Client() {
    }
    public Client(int id_client, String nom, String prenom, String phone) {
        this.id_client = id_client;
        this.nom = nom;
        this.prenom = prenom;
        this.phone = phone;
    }  
}
