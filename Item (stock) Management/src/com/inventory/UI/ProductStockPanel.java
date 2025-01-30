package com.inventory.UI;

import com.inventory.database.DatabaseConnection;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ProductStockPanel extends JPanel {
    private JTable productTable;
    private DefaultTableModel tableModel;

    public ProductStockPanel() {
        setLayout(new BorderLayout());
        tableModel = new DefaultTableModel();
        tableModel.setColumnIdentifiers(new String[]{"Ürün Kodu", "Ürün Adı", "Marka", "Stok Miktarı", "Satış Fiyatı"});

        productTable = new JTable(tableModel);
        add(new JScrollPane(productTable), BorderLayout.CENTER);

        loadProductData();
        // Grafik bölümü, güncellendi
        add(createStockChart(), BorderLayout.EAST);
    }

    private void loadProductData() {
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {

            String query = "SELECT cs.productcode, p.productname, p.brand, cs.quantity, p.sellprice " +
                           "FROM currentstock cs " +
                           "JOIN products p ON cs.productcode = p.productcode"; // Tabloları birleştirerek verileri çekiyoruz
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                String productCode = resultSet.getString("productcode");
                String productName = resultSet.getString("productname");
                String brand = resultSet.getString("brand");
                int quantity = resultSet.getInt("quantity");
                double salePrice = resultSet.getDouble("sellprice");
                addProductData(productCode, productName, brand, quantity, salePrice);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Veri yüklenirken bir hata oluştu: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addProductData(String productCode, String productName, String brand, int quantity, double salePrice) {
        tableModel.addRow(new Object[]{productCode, productName, brand, quantity, salePrice});
    }

    private JPanel createStockChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String productCode = (String) tableModel.getValueAt(i, 0); // Ürün Koduna göre listeleme
            int quantity = (int) tableModel.getValueAt(i, 3);
            dataset.addValue(quantity, "Stok Miktarı", productCode);
        }

        JFreeChart barChart = ChartFactory.createBarChart(
                "Stok Durumu",
                "Ürün Kodu",
                "Miktar",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        return new ChartPanel(barChart);
    }
}
