/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package classes;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;

/**
 *
 * @author Nkon1
 */
public class Messenger {
    
    public void shiftsMail() {
        EmailSender shiftsender = new EmailSender();

        String query = """
            SELECT 
                s.staff_name,
                s.shift_date,
                s.shift_start_time,
                s.shift_end_time,
                d.useremail
            FROM shifts s
            JOIN userdetails d ON s.staff_name = d.name
            """;

        try (
            Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/asms_schema", "root", "root");
            PreparedStatement pst = conn.prepareStatement(query);
            ResultSet rs = pst.executeQuery()
        ) {
            while (rs.next()) {
                String name = rs.getString("staff_name");
                Date shiftDate = rs.getDate("shift_date");

                String startTime = rs.getTime("shift_start_time").toString().substring(0, 5);
                String endTime = rs.getTime("shift_end_time").toString().substring(0, 5);
                String shiftTime = startTime + "-" + endTime;

                String email = rs.getString("d.useremail");

                System.out.println("Sending shift info to: " + email);
                System.out.println("Name: " + name + ", Date: " + shiftDate + ", Time: " + shiftTime);
                String body = "Hello " + name + ". Your new shift details are as follows: " + "Date: " + shiftDate + ", Time: " + shiftTime;

                shiftsender.send(email, "Shift Details", body);


            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    public void sendSwapApprovedMail(int swapId) {
    EmailSender sender = new EmailSender();

    String query = """
        SELECT 
            s.id,
            s.requester_name,
            s.target_name,
            s.requester_shift_id,
            s.target_shift_id,
            r.staff_name,
            r.shift_date,
            r.shift_start_time,
            r.shift_end_time,
            rt.staff_name AS target_name_check,
            rt.shift_date AS target_date,
            rt.shift_start_time AS target_start,
            rt.shift_end_time AS target_end,
            d1.useremail AS requester_email,
            d2.useremail AS target_email
        FROM shift_swaps s
        JOIN shifts r ON s.requester_shift_id = r.id
        JOIN shifts rt ON s.target_shift_id = rt.id
        JOIN userdetails d1 ON s.requester_name = d1.name
        JOIN userdetails d2 ON s.target_name = d2.name
        WHERE s.id = ?
    """;

    try (
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/asms_schema", "root", "root");
        PreparedStatement pst = conn.prepareStatement(query)
    ) {
        pst.setInt(1, swapId);
        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            // Requester
            String requester = rs.getString("requester_name");
            String requesterEmail = rs.getString("requester_email");
            Date newDate1 = rs.getDate("target_date");
            String newStart1 = rs.getTime("target_start").toString().substring(0, 5);
            String newEnd1 = rs.getTime("target_end").toString().substring(0, 5);

            // Target
            String target = rs.getString("target_name");
            String targetEmail = rs.getString("target_email");
            Date newDate2 = rs.getDate("shift_date");
            String newStart2 = rs.getTime("shift_start_time").toString().substring(0, 5);
            String newEnd2 = rs.getTime("shift_end_time").toString().substring(0, 5);

            // Email: Requester
            String body1 = "Hello " + requester + ",\n\nYour shift swap request has been approved.\nYour new shift is on "
                    + newDate1 + " from " + newStart1 + " to " + newEnd1 + ".\n\nBest regards,\nShift Management System";
            sender.send(requesterEmail, "Shift Swap Approved", body1);

            // Email: Target
            String body2 = "Hello " + target + ",\n\nA shift swap you were involved in has been approved.\nYour new shift is on "
                    + newDate2 + " from " + newStart2 + " to " + newEnd2 + ".\n\nBest regards,\nShift Management System";
            sender.send(targetEmail, "Shift Swap Approved", body2);
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}

    public void sendSwapDeniedMail(int swapId) {
    EmailSender sender = new EmailSender();

    String query = """
        SELECT 
            requester_name,
            d.useremail
        FROM shift_swaps s
        JOIN userdetails d ON s.requester_name = d.name
        WHERE s.id = ?
    """;

    try (
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/asms_schema", "root", "root");
        PreparedStatement pst = conn.prepareStatement(query)
    ) {
        pst.setInt(1, swapId);
        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            String requester = rs.getString("requester_name");
            String email = rs.getString("useremail");

            String body = "Hello " + requester + ",\n\nYour shift swap request has been denied by your supervisor.\n\nBest regards,\nShift Management System";
            sender.send(email, "Shift Swap Denied", body);
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}


}
