package com.inventory.UI;

import com.inventory.database.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerStatusPanel extends JPanel {
    private JTable statusTable;
    private DefaultTableModel tableModel;
    private JLabel balanceLabel;
   private JLabel customerTypeLabel;
   
    public CustomerStatusPanel(int customerId) { // Müşteri ID'sini parametre olarak al
        setLayout(new BorderLayout());
        tableModel = new DefaultTableModel();
        tableModel.setColumnIdentifiers(new String[] {"Order ID", "Ürün Adı", "Marka", "Adet", "Durum"});
        
        statusTable = new JTable(tableModel);
        statusTable.setDefaultRenderer(Object.class, new StatusTableCellRenderer());
        add(new JScrollPane(statusTable), BorderLayout.CENTER);

        // Bakiye etiketi
        balanceLabel = new JLabel();
        add(balanceLabel, BorderLayout.NORTH);
        
        // Müşteri türü etiketi
        customerTypeLabel = new JLabel();
        add(customerTypeLabel,BorderLayout.NORTH);

        loadCustomerType(customerId);
        loadCustomerStatus(customerId); // Müşteri durumu yükleme
        loadCustomerBalance(customerId); // Müşteri bakiyesini yükleme
    }

    private void loadCustomerStatus(int customerId) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(
                "SELECT o.order_id, p.productname, o.brand, o.quantity, o.status " +
                "FROM orders o JOIN products p ON o.productcode = p.productcode " +
                "WHERE o.customer_id = ?")) { // 'customer_id' doğru isimlendirildiğinden emin olun
            pstmt.setInt(1, customerId);
            ResultSet resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                int orderId = resultSet.getInt("order_id");
                String productName = resultSet.getString("productname");
                String brand = resultSet.getString("brand");
                int quantity = resultSet.getInt("quantity");
                String status = resultSet.getString("status");
                addCustomerStatus(orderId, productName, brand, quantity, status);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Veri yüklenirken bir hata oluştu: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadCustomerBalance(int customerId) {
        String selectSQL = "SELECT balance FROM customers WHERE cid = ?"; // Müşteri ID'si doğru isimlendirildiğinden emin olun
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(selectSQL)) {
            pstmt.setInt(1, customerId);
            ResultSet resultSet = pstmt.executeQuery();
            if (resultSet.next()) {
                double balance = resultSet.getDouble("balance");
                balanceLabel.setText("Bakiye: " + balance + " TL");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Bakiye yüklenirken bir hata oluştu: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }
    
   

    
    private void loadCustomerType(int customerId) {
        String selectSQL = "SELECT customerType FROM customers WHERE cid = ?"; // Müşteri ID'si doğru isimlendirildiğinden emin olun
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(selectSQL)) {
            pstmt.setInt(1, customerId);
            ResultSet resultSet = pstmt.executeQuery();
            if (resultSet.next()) {
                String customerType = resultSet.getString("customerType");
                customerTypeLabel.setText("Müşteri Türü: " + customerType);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Müşteri türü yüklenirken bir hata oluştu: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

   

    private void addCustomerStatus(int orderId, String productName, String brand, int quantity, String status) {
        tableModel.addRow(new Object[]{orderId, productName, brand, quantity, status});
    }

    private class StatusTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String status = (String) table.getValueAt(row, 4);
            
            if ("Bekliyor".equals(status)) {
                c.setBackground(Color.YELLOW);
            } else if ("İşleniyor".equals(status)) {
                c.setBackground(Color.ORANGE);
            } else if ("Tamamlandı".equals(status)) {
                c.setBackground(Color.GREEN);
            } else {
                c.setBackground(Color.WHITE);
            }
            return c;
        }
    }
}
