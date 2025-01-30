package com.inventory.UI;

import com.inventory.database.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.table.DefaultTableModel;


public class AdminOrderManagementPanel extends JPanel {
    private JTable orderTable;
    private DefaultTableModel tableModel;
    private JButton updateStatusButton;

    public AdminOrderManagementPanel() {
        setLayout(new BorderLayout());
        tableModel = new DefaultTableModel();
        tableModel.setColumnIdentifiers(new String[] { "Order ID", "Product Code", "Brand", "Quantity", "Status" });
        orderTable = new JTable(tableModel);
        add(new JScrollPane(orderTable), BorderLayout.CENTER);

        updateStatusButton = new JButton("Update Status");
        updateStatusButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateOrderStatus();
            }
        });
        add(updateStatusButton, BorderLayout.SOUTH);

        loadOrders();
    }

    private void loadOrders() {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement("SELECT order_id, productcode, brand, quantity, status FROM orders")) {
            ResultSet resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                int orderId = resultSet.getInt("order_id");
                String productCode = resultSet.getString("productcode");
                String brand = resultSet.getString("brand");
                int quantity = resultSet.getInt("quantity");
                String status = resultSet.getString("status");
                tableModel.addRow(new Object[]{orderId, productCode, brand, quantity, status});
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Sipariş verileri yüklenirken bir hata oluştu: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateOrderStatus() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow >= 0) {
            int orderId = (int) tableModel.getValueAt(selectedRow, 0);
            String[] statuses = {"Bekliyor", "İşleniyor", "Tamamlandı"};
            String newStatus = (String) JOptionPane.showInputDialog(this, "Yeni Durum Seçin:", "Durum Güncelle",
                    JOptionPane.QUESTION_MESSAGE, null, statuses, statuses[0]);

            if (newStatus != null) {
                try (Connection connection = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = connection.prepareStatement("UPDATE orders SET status = ? WHERE order_id = ?")) {
                    pstmt.setString(1, newStatus);
                    pstmt.setInt(2, orderId);
                    pstmt.executeUpdate();

                    tableModel.setValueAt(newStatus, selectedRow, 4); // Durumu tabloya güncelle
                    JOptionPane.showMessageDialog(this, "Sipariş durumu güncellendi.");
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Durum güncellenirken bir hata oluştu: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Lütfen güncellemek için bir sipariş seçin.", "Uyarı", JOptionPane.WARNING_MESSAGE);
        }
    }
}
