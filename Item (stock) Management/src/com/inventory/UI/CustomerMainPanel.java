package com.inventory.UI;

import javax.swing.*;
import java.awt.*;

public class CustomerMainPanel extends JPanel {
    private int customerId;

    public CustomerMainPanel(int customerId) {
        this.customerId = customerId; // Müşteri ID'sini alın
        setLayout(new BorderLayout());
        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.add("Müşteri Listesi", new CustomerPanel(customerId));
        tabbedPane.add("Sipariş Oluşturma", new OrderFormPanel(customerId));
        tabbedPane.add("Bekleme Durumu", new CustomerStatusPanel(customerId));
        tabbedPane.add("Ürün Stok Durumu", new ProductStockPanel());
        tabbedPane.add("Log", new LogPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }
}
