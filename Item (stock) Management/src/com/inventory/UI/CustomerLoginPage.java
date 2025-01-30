package com.inventory.UI;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.awt.BorderLayout;
import com.inventory.DAO.CustomerDAO;

public class CustomerLoginPage extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private int customerId;

    public CustomerLoginPage() {
        setTitle("Customer Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        panel.add(passwordField);
        loginButton = new JButton("Login");
        panel.add(loginButton);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                CustomerDAO customerDAO = new CustomerDAO();
                ResultSet rs = customerDAO.validateCustomerLogin(username, password);
                try {
                    if (rs != null && rs.next()) {
                        customerId = rs.getInt("cid");
                        JOptionPane.showMessageDialog(null, "Login successful");

                        // Giriş başarılı olduğunda CustomerMainPanel'i açalım
                        openCustomerMainPanel(customerId);
                    } else {
                        JOptionPane.showMessageDialog(null, "Invalid credentials");
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        add(panel);
    }

    private void openCustomerMainPanel(int customerId) {
        JFrame customerFrame = new JFrame("Customer Panel");
        customerFrame.setSize(800, 600);
        customerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        customerFrame.setLocationRelativeTo(null);

        CustomerMainPanel customerMainPanel = new CustomerMainPanel(customerId); // Müşteri ID'si ile paneli oluşturun
        customerFrame.add(customerMainPanel);
        customerFrame.setVisible(true);
    }

    public static void main(String[] args) {
        new CustomerLoginPage().setVisible(true);
    }
}
