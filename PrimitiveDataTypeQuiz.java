import javax.swing.*;
import java.awt.event.*;
import javax.swing.JPanel;
import java.awt.GridLayout;


public class PrimitiveDataTypeQuiz extends JFrame implements ActionListener {

    private JLabel questionLabel;
    private JCheckBox optionA, optionB, optionC, optionD, optionE;
    private JButton submitButton;

    public PrimitiveDataTypeQuiz() {
        super("Primitive Data Type Quiz");

        // Create question label
        questionLabel = new JLabel("Which of the following primitive data types are NOT integer types?");
        questionLabel.setFont(questionLabel.getFont().deriveFont(16f));

        // Create checkboxes for options
        optionA = new JCheckBox("A. boolean");
        optionB = new JCheckBox("B. byte");
        optionC = new JCheckBox("C. float");
        optionD = new JCheckBox("D. short");
        optionE = new JCheckBox("E. double");

        // Create submit button
        submitButton = new JButton("Submit Answer");
        submitButton.addActionListener(this);

        // Arrange components in a panel using GridLayout
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.add(questionLabel);
        panel.add(optionA);
        panel.add(optionB);
        panel.add(optionC);
        panel.add(optionD);
        panel.add(optionE);
        panel.add(submitButton);

        // Add panel to the frame
        add(panel);

        // Set frame properties
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == submitButton) {
            String selectedAnswers = "";
            if (optionA.isSelected()) {
                selectedAnswers += "A ";
            }
            if (optionB.isSelected()) {
                selectedAnswers += "B ";
            }
            if (optionC.isSelected()) {
                selectedAnswers += "C ";
            }
            if (optionD.isSelected()) {
                selectedAnswers += "D ";
            }
            if (optionE.isSelected()) {
                selectedAnswers += "E ";
            }

            // Check answer and display message
            if (selectedAnswers.trim().equals("A C E")) {
                JOptionPane.showMessageDialog(this, "Correct! Those are not integer types.", "Answer", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Incorrect. Please try again.", "Answer", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        new PrimitiveDataTypeQuiz();
    }
}

