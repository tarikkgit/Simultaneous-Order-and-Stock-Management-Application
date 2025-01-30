package com.inventory.UI;

import com.inventory.DAO.UserDAO;
import com.inventory.DTO.UserDTO;
import com.inventory.database.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HomePage extends javax.swing.JPanel {

    private JTable orderTable;
    private DefaultTableModel orderTableModel;
    private JTable customerTable;
    private DefaultTableModel customerTableModel;
    private JButton updateStatusButton;
    private JButton approveAllButton;
    private JButton refreshButton;
    private JButton deleteOrderButton;
    private JButton updateCustomerTypeButton;

    public HomePage(String username) {
        initComponents();
        UserDTO userDTO = new UserDTO();
        new UserDAO().getFullName(userDTO, username);
        welcomeLabel.setText("Welcome,  " + userDTO.getFullName() + ".");

        // Initialize order management components
        orderTableModel = new DefaultTableModel();
        orderTableModel.setColumnIdentifiers(new String[]{"Order ID", "Customer ID", "Product Code", "Brand", "Quantity", "Status", "Customer Type", "Priority Score"});
        orderTable = new JTable(orderTableModel);

        // Initialize customer management components
        customerTableModel = new DefaultTableModel();
        customerTableModel.setColumnIdentifiers(new String[]{"Customer ID", "Customer Name", "Customer Type"});
        customerTable = new JTable(customerTableModel);

        updateStatusButton = new JButton("Seçilenleri Onayla");
        approveAllButton = new JButton("Hepsini Onayla");
        refreshButton = new JButton("Yenile");
        deleteOrderButton = new JButton("Seçilenleri Sil");
        updateCustomerTypeButton = new JButton("Customer Type Güncelle");

        loadOrders(); // Siparişleri yükle
        loadCustomers(); // Müşterileri yükle

        updateStatusButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateOrderStatus(false);
            }
        });

        approveAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateOrderStatus(true);
            }
        });

        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadOrders();
                loadCustomers();
            }
        });

        deleteOrderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelectedOrders();
            }
        });

        updateCustomerTypeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateSelectedCustomerType();
            }
        });

        // Add order and customer management components to the layout
        setLayout(new BorderLayout());
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(new JScrollPane(orderTable), BorderLayout.CENTER);
        topPanel.add(new JScrollPane(customerTable), BorderLayout.EAST);
        add(topPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(updateStatusButton);
        buttonPanel.add(approveAllButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(deleteOrderButton);
        buttonPanel.add(updateCustomerTypeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadOrders() {
    List<Order> orders = new ArrayList<>();
    try (Connection connection = DatabaseConnection.getConnection();
         PreparedStatement pstmt = connection.prepareStatement("SELECT o.order_id, o.customer_id, o.productcode, o.brand, o.quantity, o.status, o.customer_type, o.timestamp " +
                "FROM orders o INNER JOIN customers c ON o.customer_id = c.cid")) {
        ResultSet resultSet = pstmt.executeQuery();
        while (resultSet.next()) {
            int orderId = resultSet.getInt("order_id");
            int customerId = resultSet.getInt("customer_id");
            String productCode = resultSet.getString("productcode");
            String brand = resultSet.getString("brand");
            int quantity = resultSet.getInt("quantity");
            String status = resultSet.getString("status");
            String customerType = resultSet.getString("customer_type");
            long timestamp = resultSet.getTimestamp("timestamp").getTime();
            int priorityScore = calculatePriorityScore(customerType, timestamp);

            orders.add(new Order(orderId, customerId, productCode, brand, quantity, status, customerType, priorityScore));
        }
    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Sipariş verileri yüklenirken bir hata oluştu: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
    }

    orders.sort(Comparator.comparingInt(Order::getPriorityScore).reversed());
    orderTableModel.setRowCount(0);
    for (Order order : orders) {
        orderTableModel.addRow(new Object[]{order.getOrderId(), order.getCustomerId(), order.getProductCode(), order.getBrand(), order.getQuantity(), order.getStatus(), order.getCustomerType(), order.getPriorityScore()});
    }
}

    private void loadCustomers() {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement("SELECT cid, fullname, customerType FROM customers")) {
            ResultSet resultSet = pstmt.executeQuery();
            customerTableModel.setRowCount(0);
            while (resultSet.next()) {
                int customerId = resultSet.getInt("cid");
                String customerName = resultSet.getString("fullname");
                String customerType = resultSet.getString("customerType");
                customerTableModel.addRow(new Object[]{customerId, customerName, customerType});
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Müşteri verileri yüklenirken bir hata oluştu: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

private int calculatePriorityScore(String customerType, long timestamp) {
    if (customerType == null) {
        customerType = "Standard";  // Varsayılan müşteri türü
    }
    int baseScore = customerType.equals("Premium") ? 20 : 10;
    long currentTime = System.currentTimeMillis();
    int waitingTimeInSeconds = (int) ((currentTime - timestamp) / 1000);
    return baseScore + (int) (waitingTimeInSeconds * 0.5);
}

    private void updateOrderStatus(boolean approveAll) {
        int[] selectedRows;
        if (approveAll) {
            int rowCount = orderTableModel.getRowCount();
            selectedRows = new int[rowCount];
            for (int i = 0; i < rowCount; i++) {
                selectedRows[i] = i;
            }
        } else {
            selectedRows = orderTable.getSelectedRows();
        }

        if (selectedRows.length > 0) {
            String newStatus = "İşleniyor";

            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement pstmtUpdateOrder = connection.prepareStatement("UPDATE orders SET status = ? WHERE order_id = ?");
                 PreparedStatement pstmtUpdateStock = connection.prepareStatement("UPDATE currentstock SET quantity = quantity - ? WHERE productcode = ?")) {

                for (int row : selectedRows) {
                    int orderId = (int) orderTableModel.getValueAt(row, 0);
                    String productCode = (String) orderTableModel.getValueAt(row, 2);
                    int quantity = (int) orderTableModel.getValueAt(row, 4);

                    // Sipariş durumunu güncelle
                    pstmtUpdateOrder.setString(1, newStatus);
                    pstmtUpdateOrder.setInt(2, orderId);
                    pstmtUpdateOrder.executeUpdate();

                    // Stok miktarını güncelle
                    pstmtUpdateStock.setInt(1, quantity);
                    pstmtUpdateStock.setString(2, productCode);
                    pstmtUpdateStock.executeUpdate();

                    orderTableModel.setValueAt(newStatus, row, 5); // Durumu tabloya güncelle
                }
                               JOptionPane.showMessageDialog(this, "Seçilen siparişlerin durumu güncellendi ve stok miktarları düzenlendi.");
                              markOrdersAsCompleted(selectedRows);
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Durum güncellenirken bir hata oluştu: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Lütfen güncellemek için bir sipariş seçin.", "Uyarı", JOptionPane.WARNING_MESSAGE);
        }
    }
    private void markOrdersAsCompleted(int[] selectedRows) {
    String newStatus = "Tamamlandı";

    try (Connection connection = DatabaseConnection.getConnection();
         PreparedStatement pstmtUpdateOrder = connection.prepareStatement("UPDATE orders SET status = ? WHERE order_id = ?")) {

        for (int row : selectedRows) {
            int orderId = (int) orderTableModel.getValueAt(row, 0);

            // Sipariş durumunu güncelle
            pstmtUpdateOrder.setString(1, newStatus);
            pstmtUpdateOrder.setInt(2, orderId);
            pstmtUpdateOrder.executeUpdate();

            orderTableModel.setValueAt(newStatus, row, 5); // Durumu tabloya güncelle
        }
        JOptionPane.showMessageDialog(this, "Seçilen siparişlerin durumu 'Tamamlandı' olarak güncellendi.");
    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Tamamlandı durumuna geçiş yapılırken bir hata oluştu: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
    }
}


    private void deleteSelectedOrders() {
        int[] selectedRows = orderTable.getSelectedRows();

        if (selectedRows.length > 0) {
            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement pstmtDeleteOrder = connection.prepareStatement("DELETE FROM orders WHERE order_id = ?")) {

                for (int row : selectedRows) {
                    int orderId = (int) orderTableModel.getValueAt(row, 0);

                    // Siparişi sil
                    pstmtDeleteOrder.setInt(1, orderId);
                    pstmtDeleteOrder.executeUpdate();

                    orderTableModel.removeRow(row); // Siparişi tablodan kaldır
                }
                JOptionPane.showMessageDialog(this, "Seçilen siparişler silindi.");
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Siparişler silinirken bir hata oluştu: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Lütfen silmek için bir sipariş seçin.", "Uyarı", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void updateSelectedCustomerType() {
        int[] selectedRows = customerTable.getSelectedRows();
        if (selectedRows.length == 1) {
            int selectedRow = selectedRows[0];
            int customerId = (int) customerTableModel.getValueAt(selectedRow, 0);
            String currentCustomerType = (String) customerTableModel.getValueAt(selectedRow, 2);

            String newCustomerType = currentCustomerType.equals("Standard") ? "Premium" : "Standard";

            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement pstmtUpdateCustomer = connection.prepareStatement("UPDATE customers SET customerType = ? WHERE cid = ?")) {
                pstmtUpdateCustomer.setString(1, newCustomerType);
                pstmtUpdateCustomer.setInt(2, customerId);
                pstmtUpdateCustomer.executeUpdate();

                customerTableModel.setValueAt(newCustomerType, selectedRow, 2); // Müşteri türünü tabloya güncelle
                JOptionPane.showMessageDialog(this, "Müşteri türü güncellendi.");
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Müşteri türü güncellenirken bir hata oluştu: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Lütfen bir müşteriyi seçin.", "Uyarı", JOptionPane.WARNING_MESSAGE);
        }
    }

    private class Order {
        private int orderId;
        private int customerId;
        private String productCode;
        private String brand;
        private int quantity;
        private String status;
        private String customerType;
        private int priorityScore;

        public Order(int orderId, int customerId, String productCode, String brand, int quantity, String status, String customerType, int priorityScore) {
            this.orderId = orderId;
            this.customerId = customerId;
            this.productCode = productCode;
            this.brand = brand;
            this.quantity = quantity;
            this.status = status;
            this.customerType = customerType;
            this.priorityScore = priorityScore;
        }

        public int getOrderId() { return orderId; }
        public int getCustomerId() { return customerId; }
        public String getProductCode() { return productCode; }
        public String getBrand() { return brand; }
        public int getQuantity() { return quantity; }
        public String getStatus() { return status; }
        public String getCustomerType() { return customerType; }
        public int getPriorityScore() { return priorityScore; }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {
        welcomeLabel = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();

        welcomeLabel.setFont(new java.awt.Font("Impact", 0, 36)); // NOI18N
        welcomeLabel.setText("Welcome My App");

        jLabel1.setFont(new java.awt.Font("Impact", 0, 18)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("<html>Everything is easy if you are wish<html>");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addContainerGap()
                                                .addComponent(welcomeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 355, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(54, 54, 54)
                                                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 355, Short.MAX_VALUE)))
                                .addContainerGap(84, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addComponent(welcomeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(174, Short.MAX_VALUE))
        );
    }// </editor-fold>

    // Variables declaration - do not modify
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel welcomeLabel;
    // End of variables declaration
}
