/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package pauasms;

import classes.EmailSender;
import com.digitalpersona.onetouch.DPFPDataPurpose;
import com.digitalpersona.onetouch.DPFPFeatureSet;
import com.digitalpersona.onetouch.DPFPGlobal;
import com.digitalpersona.onetouch.DPFPSample;
import com.digitalpersona.onetouch.DPFPTemplate;
import com.digitalpersona.onetouch.capture.DPFPCapture;
import com.digitalpersona.onetouch.capture.event.DPFPDataAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPDataEvent;
import com.digitalpersona.onetouch.capture.event.DPFPErrorAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPErrorEvent;
import com.digitalpersona.onetouch.capture.event.DPFPReaderStatusAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPReaderStatusEvent;
import com.digitalpersona.onetouch.capture.event.DPFPSensorAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPSensorEvent;
import com.digitalpersona.onetouch.processing.DPFPEnrollment;
import com.digitalpersona.onetouch.processing.DPFPFeatureExtraction;
import com.digitalpersona.onetouch.processing.DPFPImageQualityException;
import static com.digitalpersona.onetouch.processing.DPFPTemplateStatus.TEMPLATE_STATUS_FAILED;
import static com.digitalpersona.onetouch.processing.DPFPTemplateStatus.TEMPLATE_STATUS_READY;
import com.digitalpersona.onetouch.verification.DPFPVerification;
import com.digitalpersona.onetouch.verification.DPFPVerificationResult;
import com.formdev.flatlaf.FlatLightLaf;
import static fingerprint.Retrieve.TEMPLATE_PROPERTY;
import java.awt.Image;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.sql.Timestamp;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.sql.SQLException;


/**
 *
 * @author Nkon1
 */
public class AttendanceOUT extends javax.swing.JFrame {
    private DPFPCapture Reader = DPFPGlobal.getCaptureFactory().createCapture();
    private DPFPEnrollment CaptureFingerPrint = DPFPGlobal.getEnrollmentFactory().createEnrollment();
    private DPFPVerification Checker = DPFPGlobal.getVerificationFactory().createVerification();
    private DPFPTemplate template;
    public static String TEMPLATE_PROPERTY = "template";
    

    /**
     * Creates new form Attendance
     */
    public AttendanceOUT() {
        initComponents();
        
        ImageIcon icon = new ImageIcon(getClass().getResource("/images/logo3.png"));
        setIconImage(icon.getImage());
        
        //Setting Frame Title
        this.setTitle("Attendance (Sign Out)");
        
        StartDigitaPersonaRetrieve();
        stop();
        start();
        
        initializeSignOutTable(jTable1);
    }
    
    public void DisplayMsg(String message) {
        jLabel4.setText(message);
    }

    protected void StartDigitaPersonaRetrieve() {
        Reader.addDataListener(new DPFPDataAdapter() {

            public void dataAcquired(final DPFPDataEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        DisplayMsg("Capturing FingerPrint");
                        FingerCaptureProcess(e.getSample());
                        try {
                            IdentifyFingerPrintForSignOut();
                            CaptureFingerPrint.clear();
                        } catch (Exception e) {
                        }
                    }
                });
            }
        });

        Reader.addReaderStatusListener(new DPFPReaderStatusAdapter() {

            public void readerConnected(final DPFPReaderStatusEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {

                        DisplayMsg("The FingerPrint Sensor is Connected");
                    }
                });
            }

            public void readerDisconnected(final DPFPReaderStatusEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        DisplayMsg("The FingerPrint Sensor is disconnected");
                    }
                });
            }
        });

        Reader.addSensorListener(new DPFPSensorAdapter() {

            public void fingerTouched(final DPFPSensorEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        DisplayMsg("Reading FingerPrint");
                    }
                });
            }

            public void fingerRemoved(final DPFPSensorEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {

                        DisplayMsg("Place your Finger on the FingerPrint Scanner");
                    }
                });
            }
        });

        Reader.addErrorListener(new DPFPErrorAdapter() {
            public void errorReader(final DPFPErrorEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        DisplayMsg("Error: " + e.getError());
                    }
                });
            }
        });
    }
    
    public DPFPFeatureSet FingerPrintFeatureEnrollment;
    public DPFPFeatureSet FingerPrintFeatureVerification;

    public DPFPFeatureSet extractFingerPrintCharacteristic(DPFPSample sample, DPFPDataPurpose purpose) {
        DPFPFeatureExtraction extractor = DPFPGlobal.getFeatureExtractionFactory().createFeatureExtraction();
        try {
            return extractor.createFeatureSet(sample, purpose);
        } catch (DPFPImageQualityException e) {
            return null;
        }
    }
    
    public void FingerCaptureProcess(DPFPSample sample) {

        FingerPrintFeatureEnrollment = extractFingerPrintCharacteristic(sample, DPFPDataPurpose.DATA_PURPOSE_ENROLLMENT);
        FingerPrintFeatureVerification = extractFingerPrintCharacteristic(sample, DPFPDataPurpose.DATA_PURPOSE_VERIFICATION);

        if (FingerPrintFeatureEnrollment != null) {
            try {

                CaptureFingerPrint.addFeatures(FingerPrintFeatureEnrollment);
                Image image;
                image = CreateImageFingerprint(sample);
                DrawFingerPrint(image);
                DisplayMsg("Done Capturing");

            } catch (DPFPImageQualityException ex) {

            } finally {

                switch (CaptureFingerPrint.getTemplateStatus()) {
                    case TEMPLATE_STATUS_READY:
                        stop();
                        setTemplate(CaptureFingerPrint.getTemplate());
                        DisplayMsg("FingerPrint Captured");

                        break;

                    case TEMPLATE_STATUS_FAILED:
                        CaptureFingerPrint.clear();
                        stop();

                        setTemplate(null);
                        start();
                        break;
                }
            }
        }

    }
    
    public void DrawFingerPrint(Image image) {
        jLabel1.setIcon(new ImageIcon(
                image.getScaledInstance(jLabel1.getWidth(), jLabel1.getHeight(), Image.SCALE_DEFAULT)));
        repaint();
    }

    public void start() {
        Reader.startCapture();
        DisplayMsg("FingerPrint is Connected");
    }

    public void setTemplate(DPFPTemplate templat) {
        template = templat;
        DPFPTemplate old = templat;
        templat = template;
        firePropertyChange(TEMPLATE_PROPERTY, old, template);
    }

    public Image CreateImageFingerprint(DPFPSample sample) {
        return DPFPGlobal.getSampleConversionFactory().createImage(sample);
    }

    public void stop() {
        Reader.stopCapture();
        DisplayMsg("Done Capturing");
    }

    public DPFPTemplate getTemplate() {
        return template;
    }

    


    public void IdentifyFingerPrintForSignOut() {
    boolean found = false;
    DefaultTableModel model = (DefaultTableModel) jTable1.getModel();

    try (Connection con = DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/asms_schema", "root", "root..");
         PreparedStatement ps = con.prepareStatement("SELECT * FROM userdetails");
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            try {
                byte[] templateBuffer = rs.getBytes("fingerprint");
                if (templateBuffer == null || templateBuffer.length < 100) {
                    System.out.println("Invalid fingerprint template for: " + rs.getString("name"));
                    continue;
                }

                DPFPTemplate referenceTemplate = DPFPGlobal.getTemplateFactory().createTemplate(templateBuffer);
                setTemplate(referenceTemplate);
                DPFPVerificationResult result = Checker.verify(FingerPrintFeatureVerification, getTemplate());

                if (result.isVerified()) {
                    found = true;
                    String name = rs.getString("name");
                    String unit = rs.getString("unit");
                    String email = rs.getString("useremail");

                    if (!hasScheduledShift(con, name)) {
                        SwingUtilities.invokeLater(() -> showShiftWarning(name, email, "sign out"));
                        break;
                    }

                    Timestamp signOutTime = new Timestamp(System.currentTimeMillis());
                    String status = calculateSignOutStatus(con, name, signOutTime);
                    processSignOut(con, model, name, unit, email, signOutTime, status);
                    sendSignNotification(email, name, signOutTime, status, "out");
                    
                    break;
                }
            } catch (Exception e) {
                handleFingerprintError(rs.getString("name"), e);
                continue;
            }
        }

        if (!found) showNoUserFound();

    } catch (Exception e) {
        handleDatabaseError(e);
    }
}
    
private boolean hasScheduledShift(Connection con, String name) throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(
            "SELECT 1 FROM shifts WHERE staff_name = ? AND shift_date = CURDATE()")) {
        ps.setString(1, name);
        try (ResultSet rs = ps.executeQuery()) {
            return rs.next();
        }
    }
}

private boolean hasSignedInToday(Connection con, String name) throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(
            "SELECT 1 FROM signin_log WHERE name = ? AND DATE(sign_in_time) = CURDATE()")) {
        ps.setString(1, name);
        try (ResultSet rs = ps.executeQuery()) {
            return rs.next();
        }
    }
}

private String calculateSignInStatus(Connection con, String name, Timestamp signInTime) throws SQLException {
    String status = "On Time";
    try (PreparedStatement ps = con.prepareStatement(
            "SELECT shift_start_time FROM shifts WHERE staff_name = ? AND shift_date = CURDATE()")) {
        ps.setString(1, name);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                long minutesDiff = ChronoUnit.MINUTES.between(
                    rs.getTime("shift_start_time").toLocalTime(),
                    signInTime.toLocalDateTime().toLocalTime()
                );
                if (minutesDiff < -5) status = "Early";
                else if (minutesDiff > 5) status = "Late";
            }
        }
    }
    return status;
}

private String calculateSignOutStatus(Connection con, String name, Timestamp signOutTime) throws SQLException {
    String status = "Normal";
    try (PreparedStatement ps = con.prepareStatement(
            "SELECT shift_end_time FROM shifts WHERE staff_name = ? AND shift_date = CURDATE()")) {
        ps.setString(1, name);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                long minutesDiff = ChronoUnit.MINUTES.between(
                    rs.getTime("shift_end_time").toLocalTime(),
                    signOutTime.toLocalDateTime().toLocalTime()
                );
                if (minutesDiff < -5) status = "Early Departure";
                else if (minutesDiff > 5) status = "Late Departure";
            }
        }
    }
    return status;
}

private void recordSignIn(Connection con, String name, Timestamp signInTime, String status) throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(
            "INSERT INTO signin_log (name, sign_in_time, sign_in_status) VALUES (?, ?, ?)")) {
        ps.setString(1, name);
        ps.setTimestamp(2, signInTime);
        ps.setString(3, status);
        ps.executeUpdate();
    }
}



//private void updateSignInUI(DefaultTableModel model, String name, String unit, 
//                          String email, Timestamp time, String status) {
//    SwingUtilities.invokeLater(() -> {
//        jLabel2.setText(name);
//        jLabel3.setText(email);
//        model.addRow(new Object[]{name, unit, time.toString(), status});
//        jTable1.scrollRectToVisible(jTable1.getCellRect(jTable1.getRowCount()-1, 0, true));
//    });
//}

private void updateSignOutUI(DefaultTableModel model, String name, String unit, 
                           Timestamp signInTime, Timestamp signOutTime, String status) {
    SwingUtilities.invokeLater(() -> {
        jLabel2.setText(name);
        jLabel3.setText(name + " signed out");
        model.addRow(new Object[]{
            name, 
            unit, 
            signInTime != null ? signInTime.toString() : "N/A", 
            signOutTime.toString(),
            signInTime != null ? 
                String.format("%d min", ChronoUnit.MINUTES.between(
                    signInTime.toLocalDateTime(), 
                    signOutTime.toLocalDateTime())) : "N/A",
            status
        });
        jTable1.scrollRectToVisible(jTable1.getCellRect(jTable1.getRowCount()-1, 0, true));
    });
}

private void sendSignNotification(String email, String name, 
                                 Timestamp time, String status, String inOut) {
    String subject = "Sign-" + (inOut.equals("in") ? "In" : "Out") + " Notification";
    String body = String.format("Hello %s,\n\nYou have successfully signed %s at: %s\nStatus: %s\n\nThank you",
                              name, inOut, time, status);
    EmailSender.send(email, subject, body);
}

private void showShiftWarning(String name, String email, String action) {
    JOptionPane.showMessageDialog(rootPane,
        name + " has no scheduled shift today - cannot " + action,
        "No Shift Scheduled",
        JOptionPane.WARNING_MESSAGE);
    jLabel2.setText(name);
    jLabel3.setText(email);
}

private void showDuplicateWarning(String name, String email, String action) {
    JOptionPane.showMessageDialog(rootPane,
        name + " has already completed " + action + " today",
        "Duplicate " + (action.equals("sign in") ? "Sign-In" : "Sign-Out"),
        JOptionPane.WARNING_MESSAGE);
    jLabel2.setText(name);
    jLabel3.setText(email);
}

private void handleFingerprintError(String name, Exception e) {
    System.err.println("Fingerprint error for " + name + ": " + e.getMessage());
    e.printStackTrace();
}

private void showNoUserFound() {
    SwingUtilities.invokeLater(() -> 
        JOptionPane.showMessageDialog(rootPane, "No matching fingerprint found"));
}

private void handleDatabaseError(Exception e) {
    e.printStackTrace();
    SwingUtilities.invokeLater(() -> 
        JOptionPane.showMessageDialog(rootPane, "Database Error: " + e.getMessage(),
            "Error", JOptionPane.ERROR_MESSAGE));
}
    
private String determineSignOutStatus(Connection con, String name, Timestamp signOutTime) throws SQLException {
    String status = "Normal"; // Default status
    
    try (PreparedStatement shiftStmt = con.prepareStatement(
            "SELECT shift_end_time FROM shifts WHERE staff_name = ? AND shift_date = CURDATE()")) {
        shiftStmt.setString(1, name);
        try (ResultSet shiftRs = shiftStmt.executeQuery()) {
            if (shiftRs.next()) {
                LocalTime shiftEnd = shiftRs.getTime("shift_end_time").toLocalTime();
                long minutesDifference = ChronoUnit.MINUTES.between(
                    shiftEnd,
                    signOutTime.toLocalDateTime().toLocalTime()
                );
                
                if (minutesDifference < -5) status = "Early Departure";
                else if (minutesDifference > 5) status = "Late Departure";
            }
        }
    }
    return status;
}

private void processSignOut(Connection con, DefaultTableModel model, String name, 
                          String unit, String email, Timestamp signOutTime, 
                          String status) throws SQLException {
    
    final String finalStatus = status;
    
    try (PreparedStatement checkStmt = con.prepareStatement(
            "SELECT id, sign_in_time FROM signin_log " +
            "WHERE name = ? AND sign_out_time IS NULL " +
            "ORDER BY sign_in_time DESC LIMIT 1")) {
        
        checkStmt.setString(1, name);
        try (ResultSet checkRs = checkStmt.executeQuery()) {
            if (checkRs.next()) {
                Timestamp signInTime = checkRs.getTimestamp("sign_in_time");
                long minutesDuration = ChronoUnit.MINUTES.between(
                    signInTime.toLocalDateTime(),
                    signOutTime.toLocalDateTime()
                );

                try (PreparedStatement updateStmt = con.prepareStatement(
                        "UPDATE signin_log SET sign_out_time = ?, sign_out_status = ? WHERE id = ?")) {
                    updateStmt.setTimestamp(1, signOutTime);
                    updateStmt.setString(2, status);
                    updateStmt.setInt(3, checkRs.getInt("id"));
                    updateStmt.executeUpdate();
                }

                SwingUtilities.invokeLater(() -> {
                    model.addRow(new Object[]{
                        name, 
                        unit, 
                        signInTime.toString(), 
                        signOutTime.toString(),
                        String.format("%d minutes", minutesDuration),
                        finalStatus
                    });
                    jTable1.scrollRectToVisible(jTable1.getCellRect(
                        jTable1.getRowCount()-1, 0, true));
                });
            } else {
                status = "No Sign-In Record";
                try (PreparedStatement insertStmt = con.prepareStatement(
                        "INSERT INTO signin_log (name, sign_out_time, sign_out_status) VALUES (?, ?, ?)")) {
                    insertStmt.setString(1, name);
                    insertStmt.setTimestamp(2, signOutTime);
                    insertStmt.setString(3, status);
                    insertStmt.executeUpdate();
                }

                SwingUtilities.invokeLater(() -> {
                    model.addRow(new Object[]{
                        name, 
                        unit, 
                        "N/A", 
                        signOutTime.toString(),
                        "N/A",
                        finalStatus
                    });
                });
            }
        }
    }

    String emailSubject = "Sign-Out Notification";
    String emailBody = String.format(
        "Hello %s,\n\nYou have signed out at: %s\nStatus: %s\n\nThank you",
        name, signOutTime, status);
    EmailSender.send(email, emailSubject, emailBody);
}
    
    private void initializeSignOutTable(JTable jTableSignOut) {
    DefaultTableModel model = new DefaultTableModel(
        new Object[][]{}, 
        new String[]{"Name", "Unit", "Sign-In Time", "Sign-Out Time", "Duration", "Status"}
    );
    jTableSignOut.setModel(model);
    
    jTableSignOut.getColumnModel().getColumn(0).setPreferredWidth(150);
    jTableSignOut.getColumnModel().getColumn(1).setPreferredWidth(80);
    jTableSignOut.getColumnModel().getColumn(2).setPreferredWidth(180);
    jTableSignOut.getColumnModel().getColumn(3).setPreferredWidth(180);
    jTableSignOut.getColumnModel().getColumn(4).setPreferredWidth(80);
    jTableSignOut.getColumnModel().getColumn(5).setPreferredWidth(80);
}



    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(46, 56, 106));

        jLabel1.setBackground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("jLabel1");
        jLabel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel1.setOpaque(true);

        jLabel4.setText("jLabel4");

        jLabel2.setText("jLabel2");

        jLabel3.setText("jLabel3");

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(jTable1);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 477, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 37, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(43, 43, 43)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 695, Short.MAX_VALUE)
                .addGap(107, 107, 107))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(306, 306, 306)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(13, 13, 13)
                        .addComponent(jLabel2)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel3)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(AttendanceOUT.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AttendanceOUT.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AttendanceOUT.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AttendanceOUT.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        
    try {
        UIManager.setLookAndFeel(new FlatLightLaf());
    } catch (UnsupportedLookAndFeelException e) {
        e.printStackTrace();
    }
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AttendanceOUT().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
}
