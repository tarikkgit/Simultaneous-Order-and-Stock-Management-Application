package com.inventory.UI;  

import javax.swing.*;  
import java.awt.*;  

public class LogPanel extends JPanel {  
    private DefaultListModel<String> logModel;  
    private JList<String> logList;  

    public LogPanel() {  
        setLayout(new BorderLayout());  
        logModel = new DefaultListModel<>();  
        logList = new JList<>(logModel);  
        add(new JScrollPane(logList), BorderLayout.CENTER);  
        
        // Giriş alanı ve buton ekleyelim  
        JTextField logInputField = new JTextField();  
        JButton addLogButton = new JButton("Log Ekle");  
        
        // Butona tıklama olayını ekle  
        addLogButton.addActionListener(e -> {  
            String logText = logInputField.getText().trim();  
            if (!logText.isEmpty()) {  
                addLog(logText);  
                logInputField.setText(""); // Alanı temizle  
            } else {  
                JOptionPane.showMessageDialog(this, "Lütfen log mesajını girin.", "Hata", JOptionPane.ERROR_MESSAGE);  
            }  
        });  
        
        JPanel inputPanel = new JPanel(new BorderLayout());  
        inputPanel.add(logInputField, BorderLayout.CENTER);  
        inputPanel.add(addLogButton, BorderLayout.EAST);  
        add(inputPanel, BorderLayout.SOUTH);  
    }  

    public void addLog(String log) {  
        logModel.addElement(log);  
    }  
}