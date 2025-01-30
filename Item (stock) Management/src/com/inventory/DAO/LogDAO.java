package com.inventory.DAO;

import com.inventory.DTO.LogDTO;
import com.inventory.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LogDAO {
    public void addLog(LogDTO logDTO) {
        String insertLogSQL = "INSERT INTO logs (customer_id, log_type, customer_type, product, quantity, transaction_time, result) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(insertLogSQL)) {
            pstmt.setInt(1, logDTO.getCustomerID());
            pstmt.setString(2, logDTO.getLogType());
            pstmt.setString(3, logDTO.getCustomerType());
            pstmt.setString(4, logDTO.getProduct());
            pstmt.setInt(5, logDTO.getQuantity());
            pstmt.setString(6, logDTO.getTransactionTime());
            pstmt.setString(7, logDTO.getResult());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<LogDTO> getAllLogs() {
        List<LogDTO> logs = new ArrayList<>();
        String selectAllLogsSQL = "SELECT * FROM logs ORDER BY transaction_time";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(selectAllLogsSQL)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                LogDTO log = new LogDTO();
                log.setLogID(rs.getInt("log_id"));
                log.setCustomerID(rs.getInt("customer_id"));
                log.setLogType(rs.getString("log_type"));
                log.setCustomerType(rs.getString("customer_type"));
                log.setProduct(rs.getString("product"));
                log.setQuantity(rs.getInt("quantity"));
                log.setTransactionTime(rs.getString("transaction_time"));
                log.setResult(rs.getString("result"));
                logs.add(log);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }
}
