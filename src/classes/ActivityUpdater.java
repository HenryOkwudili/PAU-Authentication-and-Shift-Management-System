/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package classes;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.concurrent.*;

/**
 *
 * @author Nkon1
 */
public class ActivityUpdater {
    
    public void loadUserActivities(JTable activityTable) {
    String query = "SELECT id, username, activitytype, details, activitytime FROM useractivities ORDER BY activitytime DESC";

    try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/asms_schema", "root", "root");
         PreparedStatement ps = con.prepareStatement(query);
         ResultSet rs = ps.executeQuery()) {

        DefaultTableModel model = (DefaultTableModel) activityTable.getModel();
        model.setRowCount(0);
        String[] columnNames = {"Username", "Activity Type", "Details", "Activity Time"};
        model.setColumnIdentifiers(columnNames);

        while (rs.next()) {
            model.addRow(new Object[]{
//                rs.setInt("id"),
                rs.getString("username"),
                rs.getString("activitytype"),
                rs.getString("details"),
                rs.getTimestamp("activitytime")
            });
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}
    
    
    public void manageUsers(JTable populerItemsTable) {
    DefaultTableModel model = (DefaultTableModel) populerItemsTable.getModel();
    model.setRowCount(0);

    String[] columnNames = {"User ID", "Name", "Phone", "Email", "Unit", "Role"};
    model.setColumnIdentifiers(columnNames);

    String query = """
        SELECT 
            user_id, name, phone, useremail, unit, role
        FROM userdetails
        ORDER BY user_id ASC
        """;

    try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/asms_schema", "root", "root");
         PreparedStatement pst = conn.prepareStatement(query);
         ResultSet rs = pst.executeQuery()) {

        while (rs.next()) {
            model.addRow(new Object[]{
                rs.getInt("user_id"),
                rs.getString("name"),
                rs.getString("phone"),
                rs.getString("useremail"),
                rs.getString("unit"),
                rs.getString("role"),
//                rs.getString("logintime")
            });
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}
    
    public void viewShifts(JTable shiftsTable, String username) {
        String unit = null;

        try (
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/asms_schema", "root", "root");
            PreparedStatement ps = conn.prepareStatement("SELECT unit FROM userdetails WHERE username = ?");
        ) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                unit = rs.getString("unit");
            } else {
                JOptionPane.showMessageDialog(null, unit+ " unit not found.");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error fetching unit: " + e.getMessage());
            return;
        }

        DefaultTableModel model = (DefaultTableModel) shiftsTable.getModel();
        model.setRowCount(0);

        String[] columnNames = {"Shift ID", "Staff Name", "Shift Date", "Shift Time", "Staff Unit"};
        model.setColumnIdentifiers(columnNames);

        String query = """
            SELECT id, staff_name, shift_date, shift_start_time, shift_end_time, unit
            FROM shifts
            WHERE unit = ?
            ORDER BY id ASC
            """;

        try (
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/asms_schema", "root", "root");
            PreparedStatement pst = conn.prepareStatement(query);
        ) {
            pst.setString(1, unit);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                String startTime = rs.getTime("shift_start_time").toString().substring(0, 5);
                String endTime = rs.getTime("shift_end_time").toString().substring(0, 5);
                String shiftTime = startTime + "-" + endTime;

                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("staff_name"),
                    rs.getDate("shift_date"),
                    shiftTime,
                    rs.getString("unit")
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error loading shifts: " + e.getMessage());
        }
    }
    
    public void viewOtherStaffShifts(JTable shiftsTable, String username) {
    String unit = null;
    String staffName = null;

    try (
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/asms_schema", "root", "root");
        PreparedStatement ps = conn.prepareStatement("SELECT unit, name FROM userdetails WHERE username = ?");
    ) {
        ps.setString(1, username);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            unit = rs.getString("unit");
            staffName = rs.getString("name");
        } else {
            JOptionPane.showMessageDialog(null, "Unit or name not found for: " + username);
            return;
        }
    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Error fetching unit/name: " + e.getMessage());
        return;
    }

    DefaultTableModel model = (DefaultTableModel) shiftsTable.getModel();
    model.setRowCount(0);

    String[] columnNames = {"Shift ID", "Staff Name", "Shift Date", "Shift Time", "Staff Unit"};
    model.setColumnIdentifiers(columnNames);

    String query = """
        SELECT id, staff_name, shift_date, shift_start_time, shift_end_time, unit
        FROM shifts
        WHERE unit = ? AND staff_name != ?
        ORDER BY id ASC
    """;

    try (
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/asms_schema", "root", "root");
        PreparedStatement pst = conn.prepareStatement(query);
    ) {
        pst.setString(1, unit);
        pst.setString(2, staffName);
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            String startTime = rs.getTime("shift_start_time").toString().substring(0, 5);
            String endTime = rs.getTime("shift_end_time").toString().substring(0, 5);
            String shiftTime = startTime + "-" + endTime;

            model.addRow(new Object[]{
                rs.getInt("id"),
                rs.getString("staff_name"),
                rs.getDate("shift_date"),
                shiftTime,
                rs.getString("unit")
            });
        }

    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Error loading shifts: " + e.getMessage());
    }
}
    
    
    public void viewSwapRequests(JTable table, String supervisorUsername) {
    String unit = null;

    try (
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/asms_schema", "root", "root");
        PreparedStatement ps = conn.prepareStatement("SELECT unit FROM userdetails WHERE username = ?");
    ) {
        ps.setString(1, supervisorUsername);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            unit = rs.getString("unit");
        } else {
            JOptionPane.showMessageDialog(null, "Unit not found.");
            return;
        }
    } catch (Exception e) {
        e.printStackTrace();
        return;
    }

    DefaultTableModel model = (DefaultTableModel) table.getModel();
    model.setRowCount(0);
    model.setColumnIdentifiers(new String[]{"Swap ID", "Requester", "Target", "Requested At", "Status"});

    String query = """
        SELECT s.id, s.requester_name, s.target_name, s.requested_at, s.status
        FROM shift_swaps s
        JOIN userdetails u ON s.requester_name = u.name
        WHERE u.unit = ?
            AND LOWER(s.status) = 'pending'
        ORDER BY s.requested_at DESC
    """;

    try (
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/asms_schema", "root", "root");
        PreparedStatement pst = conn.prepareStatement(query);
    ) {
        pst.setString(1, unit);
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            model.addRow(new Object[]{
                rs.getInt("id"),
                rs.getString("requester_name"),
                rs.getString("target_name"),
                rs.getTimestamp("requested_at"),
                rs.getString("status")
            });
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}




    public static void exportRosterToCSV(JFileChooser fileChooser, JFrame parentFrame) {
        fileChooser.setDialogTitle("Save Roster CSV");

        int userSelection = fileChooser.showSaveDialog(parentFrame);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".csv")) {
                filePath += ".csv";
            }
            String selectSQL = "SELECT staff_name, shift_date, shift_start_time, shift_end_time, unit FROM shifts ORDER BY shift_date ASC";

            try (
                Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/asms_schema", "root", "root"
                );
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(selectSQL);
                BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
            ) {
                writer.write("staff_name,date,shift_time");
                writer.newLine();

                while (rs.next()) {
                    String name = rs.getString("staff_name");
                    Date date = rs.getDate("shift_date");
                    Time startTime = rs.getTime("shift_start_time");
                    Time endTime = rs.getTime("shift_end_time");
                    String unit = rs.getString("unit");

                    // Format shift_time as a single string like "08:00-17:00"
                    String shiftTime = startTime.toString().substring(0,5) + "-" + endTime.toString().substring(0,5);

                    writer.write(name + "," + date.toString() + "," + shiftTime + "," + unit);
                    writer.newLine();
                }

                writer.flush();
                JOptionPane.showMessageDialog(parentFrame, "Roster exported successfully!");

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(parentFrame, "Error exporting roster: " + e.getMessage());
            }
        }

    }
    
    public static void exportSignInLogToCSV(JFileChooser fileChooser, JFrame parentFrame) {
    fileChooser.setDialogTitle("Save Sign-In Log CSV");
    
    int userSelection = fileChooser.showSaveDialog(parentFrame);
    
    if (userSelection == JFileChooser.APPROVE_OPTION) {
        File fileToSave = fileChooser.getSelectedFile();
        String filePath = fileToSave.getAbsolutePath();
        
        // Ensure .csv extension
        if (!filePath.toLowerCase().endsWith(".csv")) {
            filePath += ".csv";
        }
        
        String query = """
            SELECT sl.id, sl.name, ud.user_id, 
                   sl.sign_in_time, sl.sign_out_time,
                   sl.sign_in_status, sl.sign_out_status, ud.unit
            FROM signin_log sl
            JOIN userdetails ud ON sl.name = ud.name
            ORDER BY sl.sign_in_time DESC
            """;
        
        try (
            Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/asms_schema", 
                "root", 
                "Iamherlix147456369..");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
        ) {
            // Write CSV header
            writer.write("ID,Staff Name,Staff ID,Sign In Time,Sign Out Time,Sign In Status,Sign Out Status,Unit");
            writer.newLine();
            
            // Format for timestamps
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            
            // Write all records
            while (rs.next()) {
                writer.write(String.format(
                    "%d,\"%s\",%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"",
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getInt("user_id"),
                    rs.getTimestamp("sign_in_time") != null ? 
                        dateFormat.format(rs.getTimestamp("sign_in_time")) : "N/A",
                    rs.getTimestamp("sign_out_time") != null ? 
                        dateFormat.format(rs.getTimestamp("sign_out_time")) : "N/A",
                    rs.getString("sign_in_status"),
                    rs.getString("sign_out_status"),
                    rs.getString("unit")
                ));
                writer.newLine();
            }
            
            writer.flush();
            JOptionPane.showMessageDialog(parentFrame,
                "Sign-in log exported successfully to:\n" + filePath,
                "Export Complete",
                JOptionPane.INFORMATION_MESSAGE);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parentFrame,
                "Error exporting sign-in log: " + e.getMessage(),
                "Export Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
    


    
//    public void checkTime(){
//        String query = """
//            SELECT 
//                       
//            """;
//        
//    }

}
