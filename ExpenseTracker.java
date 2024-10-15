

import java.awt.*;
        import java.awt.event.ActionEvent;
        import java.awt.event.ActionListener;
        import java.sql.*;
import java.util.Date;
        import javax.swing.JButton;
        import javax.swing.JFrame;
        import javax.swing.JLabel;
        import javax.swing.JOptionPane;
        import javax.swing.JPanel;
        import javax.swing.JScrollPane;
        import javax.swing.JTable;
        import javax.swing.JTextField;
        import javax.swing.table.DefaultTableModel;

public class ExpenseTracker extends JFrame implements ActionListener {
    private JLabel lblDescription, lblAmount, lblDate, lblId;
    private JTextField txtDescription, txtAmount, txtDate, txtId;
    private JButton btnAddExpense, btnViewExpenses, btnEditExpense, btnDeleteExpense;

    private JTable tblExpenses;
    private DefaultTableModel tblModel;

    public ExpenseTracker() {
        super("Expense Tracker");

        // Create and configure the components
        lblDescription = new JLabel("Description:");
        txtDescription = new JTextField(20);
        lblAmount = new JLabel("Amount:");
        txtAmount = new JTextField(10);
        lblDate = new JLabel("Date (yyyy-mm-dd):");
        txtDate = new JTextField(10);
        lblId = new JLabel("ID:");
        txtId = new JTextField(5);
        btnAddExpense = new JButton("Add Expense");
        btnViewExpenses = new JButton("View Expenses");
        btnEditExpense = new JButton("Edit Expense");
        btnDeleteExpense = new JButton("Delete Expense");

        // Add the components to the content pane
        JPanel contentPane = (JPanel) getContentPane();
        contentPane.setLayout(new FlowLayout());
        contentPane.add(lblDescription);
        contentPane.add(txtDescription);
        contentPane.add(lblAmount);
        contentPane.add(txtAmount);
        contentPane.add(lblDate);
        contentPane.add(txtDate);
        contentPane.add(btnAddExpense);
        contentPane.add(btnViewExpenses);
        contentPane.add(lblId);
        contentPane.add(txtId);
        contentPane.add(btnEditExpense);
        contentPane.add(btnDeleteExpense);

        // Add action listeners to the buttons
        btnAddExpense.addActionListener(this);
        btnViewExpenses.addActionListener(this);
        btnEditExpense.addActionListener(this);
        btnDeleteExpense.addActionListener(this);

        // Set up the expenses table
        tblModel = new DefaultTableModel(new String[]{"ID", "Description", "Amount", "Date"}, 0);
        tblExpenses = new JTable(tblModel);
        JScrollPane scrollPane = new JScrollPane(tblExpenses);
        scrollPane.setPreferredSize(new Dimension(380, 150));
        contentPane.add(scrollPane);

        // Set the window properties
        setSize(400, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {
        ExpenseTracker et = new ExpenseTracker();
        et.setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnAddExpense) {
            addExpense();
        } else if (e.getSource() == btnViewExpenses) {
            viewExpenses();
        } else if (e.getSource() == btnEditExpense) {
            editExpense();
        } else if (e.getSource() == btnDeleteExpense) {
            deleteExpense();
        }
    }

    private void viewExpenses(){
        // Create a table to display the expenses
        String[] columns = {"ID", "Description", "Amount", "Date"};
        Object[][] data = null;
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/expense?user=root&password=12345")) {
            String sql = "SELECT * FROM expense";
            try (PreparedStatement stmt = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    rs.last();
                    int rowCount = rs.getRow();
                    rs.beforeFirst();
                    data = new Object[rowCount][4];
                    int i = 0;
                    while (rs.next()) {
                        data[i][0] = rs.getInt("id");
                        data[i][1] = rs.getString("description");
                        data[i][2] = rs.getDouble("amount");
                        data[i][3] = rs.getDate("date");
                        i++;
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error viewing expenses: " + ex.getMessage());
            return;
        }
        JTable table = new JTable(data, columns);

        // Show the table in a dialog
        JOptionPane.showMessageDialog(this, new JScrollPane(table));
    }



    private void addExpense() {

        // Get the expense data from the user interface
        String description = txtDescription.getText();
        double amount = Double.parseDouble(txtAmount.getText());
        Date date = java.sql.Date.valueOf(txtDate.getText());

        // Get a connection to the database
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/expense?user=root&password=12345"))
        {
            // Prepare a SQL statement to insert a new expense into the expenses table
            String sql = "INSERT INTO expense (description, amount, date) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                // Set the parameter values for the SQL statement
                stmt.setString(1, description);
                stmt.setDouble(2, amount);
                stmt.setDate(3, new java.sql.Date(date.getTime()));

                // Execute the SQL statement to insert the expense into the database
                int rows = stmt.executeUpdate();

                // Display a message indicating that the expense was added
                JOptionPane.showMessageDialog(this, rows + " expense added.");
            }
        } catch (SQLException ex) {
            // Handle any errors that occur during database access
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding expense: " + ex.getMessage());
        }
    }




    private void editExpense() {
        // Get the user input
        String id = txtId.getText();
        String description = txtDescription.getText();
        double amount = Double.parseDouble(txtAmount.getText());
        String date = txtDate.getText();

        // Update the expense in the database
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/expense?user=root&password=12345")) {
            String sql = "UPDATE expense SET description = ?, amount = ?, date = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, description);
                stmt.setDouble(2, amount);
                stmt.setString(3, date);
                stmt.setString(4, id);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    JOptionPane.showMessageDialog(this, "No expense found with ID " + id);
                } else {
                    JOptionPane.showMessageDialog(this, "Expense updated successfully.");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating expense: " + ex.getMessage());
        }

        // Clear the text fields
        txtId.setText("");
        txtDescription.setText("");
        txtAmount.setText("");
        txtDate.setText("");
    }

    private void deleteExpense() {
        // Get the user input
        String id = txtId.getText();

        // Delete the expense from the database
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/expense?user=root&password=12345")) {
            String sql = "DELETE FROM expense WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, id);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    JOptionPane.showMessageDialog(this, "No expense found with ID " + id);
                } else {
                    JOptionPane.showMessageDialog(this, "Expense deleted successfully.");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting expense: " + ex.getMessage());
        }

        // Clear the text fields
        txtId.setText("");
        txtDescription.setText("");
        txtAmount.setText("");
        txtDate.setText("");
    }}
