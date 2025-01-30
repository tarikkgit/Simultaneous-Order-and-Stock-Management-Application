package com.inventory.UI;

import com.inventory.DAO.CustomerDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerPanel extends JPanel {
    private JTable customerTable;
    private DefaultTableModel tableModel;
    private int customerId; // Giriş yapan müşteri ID'si

    public CustomerPanel(int customerId) {
        this.customerId = customerId; // Giriş yapan müşteri ID'sini alın
        setLayout(new BorderLayout());
        tableModel = new DefaultTableModel();
        tableModel.setColumnIdentifiers(new String[] {
                "CustomerID", "Ad", "Customer Type", "Location", "Contact", "Username", "Password","Balance" });
        customerTable = new JTable(tableModel);
        add(new JScrollPane(customerTable), BorderLayout.CENTER);
        loadCustomerData();
    }

    private void loadCustomerData() {
        CustomerDAO customerDAO = new CustomerDAO();
        ResultSet rs = customerDAO.getCustomerData(customerId);
        try {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("cid"),
                        rs.getString("fullname"),
                        rs.getString("customerType"), // Customer Type gösteriliyor
                        rs.getString("location"),
                        rs.getString("phone"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getDouble("balance")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
