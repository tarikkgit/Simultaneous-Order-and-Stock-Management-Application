package com.inventory.UI;

import javax.swing.*;

public class CustomerDashboard extends JFrame {
    private int customerId;

    public CustomerDashboard(int customerId) {
        this.customerId = customerId;
        setTitle("Customer Dashboard");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.add(new JLabel("Welcome, Customer ID: " + customerId));
        // Diğer müşteri bilgilerini ve işlemlerini ekleyebilirsiniz

        add(panel);
    }

    public static void main(String[] args) {
        // Test amaçlı olarak kullanılabilir
        new CustomerDashboard(1).setVisible(true);
    }
}
