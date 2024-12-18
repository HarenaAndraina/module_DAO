/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import database.service.Service;
import database.util.Pagination;
import java.util.List;

/**
 *
 * @author Andra
 */
public class Main {

    public static void main(String[] args) {
        try {
            ApplicationContext context = new ClassPathXmlApplicationContext("beans.xml");

            Client client1 = new Client(1, null, null, null);
            Client client2 = new Client(3, null, null, null);

            Service obj = (Service) context.getBean("Service");
            List<Client> valiny=obj.getByCriteria(client2, null);
            for (Client client : valiny) {
                System.out.println(client.getNom());
            }
            //Client client=new Client("aaaa", "bbbbbbb", "azerty");
            //List<Client> clients=obj.getAll(Client.class, "2");
            //for (Client client1 : clients) {
            //    System.out.println(client1.getNom());
            //}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
