package com.inventory.UI;

import com.inventory.database.DatabaseConnection;
import com.inventory.DTO.LogDTO;
import com.inventory.DAO.LogDAO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class OrderFormPanel extends JPanel {
    private JComboBox<String> productComboBox;
    private JComboBox<String> brandComboBox;
    private JTextField quantityField;
    private JButton orderButton;
    private JLabel statusLabel;
    private int customerId;

    public OrderFormPanel(int customerId) {
        this.customerId = customerId;
        setLayout(new GridLayout(4, 2));

        add(new JLabel("Ürün Seçimi:"));
        productComboBox = new JComboBox<>();
        loadProducts();  // Ürünleri veritabanından yükleme
        add(productComboBox);

        add(new JLabel("Marka Seçimi:"));
        brandComboBox = new JComboBox<>();
        loadBrands();  // Markaları veritabanından yükleme
        add(brandComboBox);

        add(new JLabel("Adet:"));
        quantityField = new JTextField();
        add(quantityField);

        orderButton = new JButton("Sipariş Ver");
        add(orderButton);
        
        statusLabel = new JLabel();
        add(statusLabel);

        orderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleOrder();
            }
        });
    }

    private void loadProducts() {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement("SELECT DISTINCT productname FROM products"); // Ürün tablosu
             ResultSet resultSet = pstmt.executeQuery()) {

            while (resultSet.next()) {
                String productName = resultSet.getString("productname");
                productComboBox.addItem(productName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ürünleri yüklerken bir hata oluştu: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
            logTransaction(customerId, null, 0, "Veritabanı Hatası");
        }
    }

    private void loadBrands() {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement("SELECT DISTINCT brand FROM products"); // Marka tablosu
             ResultSet resultSet = pstmt.executeQuery()) {

            while (resultSet.next()) {
                String brand = resultSet.getString("brand");
                brandComboBox.addItem(brand);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Markaları yüklerken bir hata oluştu: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
            logTransaction(customerId, null, 0, "Veritabanı Hatası");
        }
    }

    private void handleOrder() {
        String selectedProduct = (String) productComboBox.getSelectedItem();
        String selectedBrand = (String) brandComboBox.getSelectedItem();
        try {
            // Kullanıcıdan girilen adet miktarını al
            int quantity = Integer.parseInt(quantityField.getText().trim());

            // Adetleri kontrol et
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Lütfen geçerli bir adet girin (0'dan büyük).", "Geçersiz Girdi", JOptionPane.ERROR_MESSAGE);
                logTransaction(customerId, selectedProduct, quantity, "Geçersiz Girdi");
            } else {
                // Ürünün satış fiyatını al ve toplam maliyeti hesapla
                double salePrice = getSalePrice(selectedProduct, selectedBrand);
                double totalCost = quantity * salePrice;
                
                // Bakiye kontrolü
                if (checkBalance(totalCost)) {
                    // Siparişi veritabanına ekleme ve bakiye düşme
                    boolean success = placeOrder(customerId, selectedProduct, selectedBrand, quantity, totalCost);
                    if (success) {
                        JOptionPane.showMessageDialog(this, selectedProduct + " için " + quantity + " adet sipariş verildi.");
                        statusLabel.setText("Sipariş Başarılı!");
                        logTransaction(customerId, selectedProduct, quantity, "Satın alma başarılı");
                    } else {
                        statusLabel.setText("Sipariş Başarısız!");
                        logTransaction(customerId, selectedProduct, quantity, "Veritabanı Hatası");
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Bakiye yetersiz! Lütfen bakiyenizi kontrol edin.", "Yetersiz Bakiye", JOptionPane.ERROR_MESSAGE);
                    statusLabel.setText("Yetersiz Bakiye");
                    logTransaction(customerId, selectedProduct, quantity, "Müşteri bakiyesi yetersiz");
                }
                // Giriş alanını temizle
                quantityField.setText("");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Lütfen geçerli bir sayı girin.", "Hata", JOptionPane.ERROR_MESSAGE);
            logTransaction(customerId, selectedProduct, 0, "Geçersiz Girdi");
        }
    }

    private double getSalePrice(String productName, String brand) {
        String selectSQL = "SELECT sellprice FROM products WHERE productname = ? AND brand = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(selectSQL)) {
            pstmt.setString(1, productName);
            pstmt.setString(2, brand);
            ResultSet resultSet = pstmt.executeQuery();
            if (resultSet.next()) {
                return resultSet.getDouble("sellprice");
            }
        } catch (SQLException e) {
            e.printStackTrace();
                        logTransaction(customerId, productName, 0, "Veritabanı Hatası");
        }
        return 0.0;
    }

    private boolean checkBalance(double totalCost) {
        String selectSQL = "SELECT balance FROM customers WHERE cid = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(selectSQL)) {
            pstmt.setInt(1, customerId);
            ResultSet resultSet = pstmt.executeQuery();
            if (resultSet.next()) {
                double balance = resultSet.getDouble("balance");
                return balance >= totalCost;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            logTransaction(customerId, null, 0, "Veritabanı Hatası");
        }
        return false;
    }
private boolean placeOrder(int customerId, String productName, String brand, int quantity, double totalCost) {
// TimeoutHandler oluştur
TimeoutHandler timeoutHandler = new TimeoutHandler(15000); // 15 saniye zaman aşımı    
// Her üründen en fazla 5 adet alınabilmesi için kontrol
    if (quantity > 5) {
        JOptionPane.showMessageDialog(this, "En fazla 5 adet satın alabilirsiniz.", "Uyarı", JOptionPane.WARNING_MESSAGE);
        logTransaction(customerId, productName, quantity, "Ürün adeti sınırı aşıldı");
        return false;
    }

    // Müşteri tipini almak için sorgu
    String getCustomerTypeSQL = "SELECT customerType FROM customers WHERE cid = ?";

    // Sipariş veritabanına ekle ve bakiyeyi güncelle
    String insertOrderSQL = "INSERT INTO orders (customer_id, productcode, brand, quantity, totalCost, status, customer_type, timestamp) VALUES (?, ?, ?, ?, ?, 'Bekliyor', ?, ?)";
    String updateBalanceSQL = "UPDATE customers SET balance = balance - ? WHERE cid = ?";

    try (Connection connection = DatabaseConnection.getConnection();
         PreparedStatement pstmtCustomerType = connection.prepareStatement(getCustomerTypeSQL);
         PreparedStatement pstmtOrder = connection.prepareStatement(insertOrderSQL);
         PreparedStatement pstmtBalance = connection.prepareStatement(updateBalanceSQL)) {

        // Müşteri tipini sorgula
        pstmtCustomerType.setInt(1, customerId);
        ResultSet rs = pstmtCustomerType.executeQuery();
        String customerType = rs.next() ? rs.getString("customerType") : "Standard";

        // Ürünün kodunu almak için veritabanından sorgulama
        String productCode = getProductCode(productName, brand);

        if (productCode != null) {
            pstmtOrder.setInt(1, customerId);
            pstmtOrder.setString(2, productCode);
            pstmtOrder.setString(3, brand);
            pstmtOrder.setInt(4, quantity);
            pstmtOrder.setDouble(5, totalCost); // TotalCost değerini burada ekliyoruz
            pstmtOrder.setString(6, customerType); // Müşteri tipini ekliyoruz
            pstmtOrder.setTimestamp(7, new java.sql.Timestamp(System.currentTimeMillis()));
            pstmtOrder.executeUpdate();

            pstmtBalance.setDouble(1, totalCost);
            pstmtBalance.setInt(2, customerId);
            pstmtBalance.executeUpdate();

            updateCustomerStatus(customerId); // Müşteri statüsünü güncelleme
            return true;
        } else {
            JOptionPane.showMessageDialog(this, "Ürün kodu bulunamadı!", "Hata", JOptionPane.ERROR_MESSAGE);
            logTransaction(customerId, productName, quantity, "Ürün kodu bulunamadı");
        }
    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Sipariş kaydederken bir hata oluştu: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        logTransaction(customerId, productName, quantity, "Veritabanı Hatası");
    }
    return false;
}

private void updateCustomerStatus(int customerId) {
    String checkTotalCostSQL = "SELECT SUM(totalCost) as totalSpent FROM orders WHERE customer_id = ?";
    String updateStatusSQL = "UPDATE customers SET customerType = 'Premium' WHERE cid = ? AND customerType = 'Standard'";

    try (Connection connection = DatabaseConnection.getConnection();
         PreparedStatement pstmtCheck = connection.prepareStatement(checkTotalCostSQL);
         PreparedStatement pstmtUpdate = connection.prepareStatement(updateStatusSQL)) {

        pstmtCheck.setInt(1, customerId);
        ResultSet resultSet = pstmtCheck.executeQuery();
        if (resultSet.next()) {
            double totalSpent = resultSet.getDouble("totalSpent");
            if (totalSpent >= 2000) {
                pstmtUpdate.setInt(1, customerId);
                pstmtUpdate.executeUpdate();
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
        logTransaction(customerId, null, 0, "Veritabanı Hatası");
    }
}



    private String getProductCode(String productName, String brand) {
        String selectSQL = "SELECT productcode FROM products WHERE productname = ? AND brand = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(selectSQL)) {
            pstmt.setString(1, productName);
            pstmt.setString(2, brand);
            ResultSet resultSet = pstmt.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("productcode");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            logTransaction(customerId, productName, 0, "Veritabanı Hatası");
        }
        return null; // Ürün kodu bulunamazsa null döner
    }

    // Loglama işlemi
    private void logTransaction(int customerId, String product, int quantity, String result) {
        LogDTO logDTO = new LogDTO();
        logDTO.setLogID(generateLogID()); // Log ID oluşturma
        logDTO.setCustomerID(customerId);
        logDTO.setLogType(result.equals("Satın alma başarılı") ? "Bilgilendirme" : "Hata");
        logDTO.setCustomerType("Standard"); // Örnek müşteri türü
        logDTO.setProduct(product);
        logDTO.setQuantity(quantity);
        logDTO.setTransactionTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        logDTO.setResult(result);

        // Logu veritabanına kaydetme
        LogDAO logDAO = new LogDAO();
        logDAO.addLog(logDTO);
    }

    // Yeni Log ID oluşturma
    private int generateLogID() {
        // Bu, örnek bir ID oluşturma yöntemidir. Gerçek uygulamada, veritabanı otomatik ID oluşturabilir.
        return (int) (Math.random() * 10000);
    }
}
