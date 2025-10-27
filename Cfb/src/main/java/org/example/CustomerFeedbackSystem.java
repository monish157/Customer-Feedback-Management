import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class CustomerFeedbackSystem extends JFrame {

    JTextField nameField, emailField, ratingField;
    JTextArea messageArea;
    JButton submitBtn, adminBtn;

    final String DB_URL = "jdbc:mysql://localhost:3306/feedback_db";
    final String DB_USER = "root"; // change if needed
    final String DB_PASS = "root";     // change if needed

    // Admin credentials (Module 3 - Auth)
    final String ADMIN_USER = "admin";
    final String ADMIN_PASS = "admin123";

    public CustomerFeedbackSystem() {
        setTitle("Customer Feedback System");
        setSize(400, 500);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel formPanel = new JPanel(new GridLayout(8, 1, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        nameField = new JTextField();
        emailField = new JTextField();
        messageArea = new JTextArea();
        ratingField = new JTextField();

        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Email:"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("Message:"));
        formPanel.add(new JScrollPane(messageArea));
        formPanel.add(new JLabel("Rating (1-5):"));
        formPanel.add(ratingField);

        submitBtn = new JButton("Submit Feedback");
        adminBtn = new JButton("Admin Login");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(submitBtn);
        buttonPanel.add(adminBtn);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        submitBtn.addActionListener(e -> submitFeedback());
        adminBtn.addActionListener(e -> adminLogin());

        setVisible(true);
    }

    private void submitFeedback() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String message = messageArea.getText().trim();
        int rating;

        try {
            rating = Integer.parseInt(ratingField.getText().trim());
            if (rating < 1 || rating > 5) throw new NumberFormatException();

            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            String sql = "INSERT INTO feedbacks (name, email, message, rating) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, message);
            stmt.setInt(4, rating);

            int result = stmt.executeUpdate();
            if (result > 0) {
                JOptionPane.showMessageDialog(this, "Feedback Submitted!");
                nameField.setText("");
                emailField.setText("");
                messageArea.setText("");
                ratingField.setText("");
            }

            stmt.close();
            conn.close();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Rating must be a number between 1 and 5.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "DB Error: " + ex.getMessage());
        }
    }
    private void adminLogin() {
        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();
        Object[] fields = {
                "Username:", userField,
                "Password:", passField
        };

        int option = JOptionPane.showConfirmDialog(this, fields, "Admin Login",
                JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String user = userField.getText();
            String pass = new String(passField.getPassword());

            if (user.equals(ADMIN_USER) && pass.equals(ADMIN_PASS)) {
                showAdminPanel();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials.");
            }
        }
    }

    private void showAdminPanel() {
        JFrame adminFrame = new JFrame("Admin - Feedback Viewer");
        adminFrame.setSize(700, 400);
        adminFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        String[] columns = {"ID", "Name", "Email", "Message", "Rating", "Date"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);

        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM feedbacks");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("message"),
                        rs.getInt("rating"),
                        rs.getTimestamp("submitted_at")
                });
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "DB Error: " + e.getMessage());
        }

        adminFrame.add(new JScrollPane(table));
        adminFrame.setVisible(true);
    }

    public static void main(String[] args) {
        // Load JDBC driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "MySQL JDBC driver not found.");
            return;
        }

        SwingUtilities.invokeLater(() -> new CustomerFeedbackSystem());
    }
}
