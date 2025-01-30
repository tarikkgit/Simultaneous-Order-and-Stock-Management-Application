package com.inventory.UI;

import javax.swing.*;

public class CustomerMainFrame extends JFrame {
    public CustomerMainFrame(int customerId) {
        setTitle("Customer Panel");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        CustomerMainPanel customerMainPanel = new CustomerMainPanel(customerId);
        add(customerMainPanel);
    }

    public static void main(String[] args) {
        int customerId = 1; // Bu değeri giriş yapan müşterinin ID'siyle değiştirin
        new CustomerMainFrame(customerId).setVisible(true);
    }
}
