package com.inventory.UI;

import com.inventory.DAO.ProductDAO;
import com.inventory.database.DatabaseConnection; // Doğru import

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;



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


/**
 * Current Stock Page
 */
public class CurrentStockPage extends javax.swing.JPanel {

    String username;

    /**
     * Creates new form CurrentStockPage
     */
    public CurrentStockPage(String username) {
        initComponents();
        this.username = username;
        loadDataSet();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jScrollPane1 = new javax.swing.JScrollPane();
        stockTable = new javax.swing.JTable();
        refreshButton = new javax.swing.JButton();
        updateStockButton = new javax.swing.JButton(); // Yeni stok güncelleme butonu

        jLabel1.setFont(new java.awt.Font("Impact", 0, 24)); // NOI18N
        jLabel1.setText("CURRENT STOCK");
        jLabel1.setToolTipText("");

        stockTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Product Code", "Product Name", "Brand", "Quantity"
            }
        ));
        jScrollPane1.setViewportView(stockTable);

        refreshButton.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        refreshButton.setText("REFRESH");
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });

        // Stok güncelleme butonu
        updateStockButton.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        updateStockButton.setText("Stok Güncelle");
        updateStockButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                updateStockButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jSeparator1)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 701, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(refreshButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(updateStockButton))) // Stok güncelleme butonu yerleştirildi
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(updateStockButton, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE) // Stok güncelleme butonu yerleştirildi
                    .addComponent(refreshButton, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 330, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(88, Short.MAX_VALUE))
        );
    }// </editor-fold>

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {
        loadDataSet();
    }

    // Stok güncelleme işlemi
    private void updateStockButtonActionPerformed(ActionEvent evt) {
        int selectedRow = stockTable.getSelectedRow();
        if (selectedRow >= 0) {
            String productCode = (String) stockTable.getValueAt(selectedRow, 0);
            int newQuantity = Integer.parseInt(JOptionPane.showInputDialog(this, "Yeni Miktarı Girin:", "Stok Güncelle", JOptionPane.QUESTION_MESSAGE));

            if (newQuantity >= 0) {
                try (Connection connection = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = connection.prepareStatement("UPDATE currentstock SET quantity = ? WHERE productcode = ?")) {
                    pstmt.setInt(1, newQuantity);
                    pstmt.setString(2, productCode);
                    pstmt.executeUpdate();

                    stockTable.setValueAt(newQuantity, selectedRow, 3); // Yeni miktarı tabloya güncelle
                    JOptionPane.showMessageDialog(this, "Stok miktarı güncellendi.");
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Stok güncellenirken bir hata oluştu: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Geçersiz miktar. Lütfen geçerli bir değer girin.", "Uyarı", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Lütfen güncellemek için bir ürün seçin.", "Uyarı", JOptionPane.WARNING_MESSAGE);
        }
    }

    // Method to load data into the table
    public void loadDataSet() {
        try {
            ProductDAO productDAO = new ProductDAO();
            stockTable.setModel(productDAO.buildTableModel(productDAO.getCurrentStockInfo()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Variables declaration - do not modify
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton refreshButton;
    private javax.swing.JButton updateStockButton; // Yeni stok güncelleme butonu
    private javax.swing.JTable stockTable;
    // End of variables declaration
}
