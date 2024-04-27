import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class LoginForm extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JTextField usernameTextField;
    private JPasswordField passwordField;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                LoginForm frame = new LoginForm();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public LoginForm() {
        setTitle("Login Form");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 450, 300);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        JLabel lblUsername = new JLabel("Username");
        lblUsername.setFont(new Font("Tahoma", Font.PLAIN, 14));
        lblUsername.setBounds(100, 50, 80, 17);
        contentPane.add(lblUsername);

        usernameTextField = new JTextField();
        usernameTextField.setBounds(200, 47, 150, 25);
        contentPane.add(usernameTextField);
        usernameTextField.setColumns(10);

        JLabel lblPassword = new JLabel("Password");
        lblPassword.setFont(new Font("Tahoma", Font.PLAIN, 14));
        lblPassword.setBounds(100, 100, 80, 17);
        contentPane.add(lblPassword);

        passwordField = new JPasswordField();
        passwordField.setBounds(200, 97, 150, 25);
        contentPane.add(passwordField);

        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(e -> {
            String username = usernameTextField.getText();
            String password = new String(passwordField.getPassword());
            if (username.equals("admin") && password.equals("welcome")) {
                JOptionPane.showMessageDialog(null, "Login Successful!");
              
            } else {
                JOptionPane.showMessageDialog(null, "Invalid username or password!");
            }
        });
        submitButton.setFont(new Font("Tahoma", Font.PLAIN, 14));
        submitButton.setBounds(150, 150, 100, 25);
        contentPane.add(submitButton);

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> {
            usernameTextField.setText("");
            passwordField.setText("");
        });
        clearButton.setFont(new Font("Tahoma", Font.PLAIN, 14));
        clearButton.setBounds(270, 150, 100, 25);
        contentPane.add(clearButton);

        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> System.exit(0));
        exitButton.setFont(new Font("Tahoma", Font.PLAIN, 14));
        exitButton.setBounds(390, 150, 100, 25); // Adjust the x-coordinate here
        contentPane.add(exitButton);
    }
}
