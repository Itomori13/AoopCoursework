package weaver;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

/**
 * View class for the Weaver game.
 * Creates and manages the GUI interface.
 * Implements Observer to receive updates from the Model.
 */
public class View extends JFrame implements Observer {
    // Model reference
    private Model model;

    // UI components
    private JLabel titleLabel;
    private JButton helpButton;

    private JLabel startWordLabel;
    private JLabel targetWordLabel;

    private JPanel inputPanel;
    private JTextField[] inputFields;
    private int currentInputField;

    private JPanel attemptsPanel;

    private JPanel keyboardPanel;
    private Map<Character, JButton> letterButtons;
    private JButton enterButton;
    private JButton backspaceButton;

    private JButton resetButton;
    private JButton newGameButton;

    private JToggleButton errorToggle;
    private JToggleButton pathToggle;
    private JToggleButton randomToggle;

    /**
     * Constructor initializes the view with the given model.
     */
    public View(Model model) {
        this.model = model;
        model.addObserver(this);
        currentInputField = 0;

        initializeUI();
    }

    /**
     * Initialize the user interface.
     */
    private void initializeUI() {
        // Set up the frame
        setTitle("Weaver Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 600);
        setLayout(new BorderLayout(10, 10));

        // Create panels
        createHeaderPanel();
        createGameStatePanel();
        createInputPanel();
        createAttemptsPanel();
        createKeyboardPanel();
        createControlPanel();

        // Display the frame
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Create the header panel with title and help button.
     */
    private void createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        // Title
        titleLabel = new JLabel("Weaver", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // Help button
        helpButton = new JButton("?");
        helpButton.setFont(new Font("Arial", Font.BOLD, 16));
        helpButton.setPreferredSize(new Dimension(40, 40));
        helpButton.setToolTipText("Game Rules");
        headerPanel.add(helpButton, BorderLayout.WEST);

        add(headerPanel, BorderLayout.NORTH);
    }

    /**
     * Create the game state panel with start and target words.
     */
    private void createGameStatePanel() {
        JPanel statePanel = new JPanel(new GridLayout(2, 1, 10, 10));
        statePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Start word panel
        JPanel startPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel startLabel = new JLabel("Start Word: ");
        startLabel.setFont(new Font("Arial", Font.BOLD, 16));
        startWordLabel = new JLabel(model.getStartWord());
        startWordLabel.setFont(new Font("Arial", Font.BOLD, 18));
        startPanel.add(startLabel);
        startPanel.add(startWordLabel);

        // Target word panel
        JPanel targetPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel targetLabel = new JLabel("Target Word: ");
        targetLabel.setFont(new Font("Arial", Font.BOLD, 16));
        targetWordLabel = new JLabel(model.getTargetWord());
        targetWordLabel.setFont(new Font("Arial", Font.BOLD, 18));
        targetPanel.add(targetLabel);
        targetPanel.add(targetWordLabel);

        statePanel.add(startPanel);
        statePanel.add(targetPanel);

        add(statePanel, BorderLayout.WEST);
    }

    /**
     * Create the input panel with text fields.
     */
    private void createInputPanel() {
        inputPanel = new JPanel(new GridLayout(1, 4, 5, 5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 50, 10, 50));

        inputFields = new JTextField[4];
        for (int i = 0; i < 4; i++) {
            inputFields[i] = new JTextField();
            inputFields[i].setFont(new Font("Arial", Font.BOLD, 24));
            inputFields[i].setHorizontalAlignment(JTextField.CENTER);
            inputPanel.add(inputFields[i]);

            // Set focus to next field when a character is entered
            final int index = i;
            inputFields[i].addCaretListener(e -> {
                if (inputFields[index].getText().length() == 1 && index < 3) {
                    inputFields[index + 1].requestFocus();
                    currentInputField = index + 1;
                }
            });
        }

        add(inputPanel, BorderLayout.CENTER);
    }

    /**
     * Create the attempts panel to show previous attempts.
     */
    private void createAttemptsPanel() {
        attemptsPanel = new JPanel();
        attemptsPanel.setLayout(new BoxLayout(attemptsPanel, BoxLayout.Y_AXIS));
        attemptsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(attemptsPanel);
        scrollPane.setPreferredSize(new Dimension(150, 300));

        add(scrollPane, BorderLayout.EAST);

        // Update with current attempts
        updateAttempts();
    }

    /**
     * Update the attempts panel with current attempts.
     */
    private void updateAttempts() {
        attemptsPanel.removeAll();

        // Add header
        JLabel attemptsLabel = new JLabel("Attempts", SwingConstants.CENTER);
        attemptsLabel.setFont(new Font("Arial", Font.BOLD, 16));
        attemptsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        attemptsPanel.add(attemptsLabel);
        attemptsPanel.add(Box.createVerticalStrut(10));

        // Add each attempt with color coding
        List<String> attempts = model.getAttempts();
        for (String attempt : attempts) {
            JPanel wordPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
            wordPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

            int[] letterStatus = model.getLetterStatus(attempt);

            for (int i = 0; i < attempt.length(); i++) {
                JPanel letterPanel = createLetterPanel(String.valueOf(attempt.charAt(i)), letterStatus[i]);
                wordPanel.add(letterPanel);
            }

            attemptsPanel.add(wordPanel);
            attemptsPanel.add(Box.createVerticalStrut(5));
        }

        // Add path if enabled
        if (model.isShowPath()) {
            attemptsPanel.add(Box.createVerticalStrut(10));

            JLabel pathLabel = new JLabel("Path", SwingConstants.CENTER);
            pathLabel.setFont(new Font("Arial", Font.BOLD, 16));
            pathLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            attemptsPanel.add(pathLabel);
            attemptsPanel.add(Box.createVerticalStrut(5));

            List<String> path = model.getPath();
            for (String word : path) {
                JLabel wordLabel = new JLabel(word, SwingConstants.CENTER);
                wordLabel.setFont(new Font("Arial", Font.PLAIN, 14));
                wordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                attemptsPanel.add(wordLabel);
            }
        }

        attemptsPanel.revalidate();
        attemptsPanel.repaint();
    }

    /**
     * Create a letter panel with color based on status.
     */
    private JPanel createLetterPanel(String letter, int status) {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(30, 30));
        panel.setLayout(new BorderLayout());

        JLabel label = new JLabel(letter.toUpperCase(), SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 16));

        // Set color based on status
        if (status == 1) {
            panel.setBackground(new Color(106, 170, 100));  // Green for correct position
            label.setForeground(Color.WHITE);
        } else {
            panel.setBackground(new Color(120, 124, 126));  // Grey for incorrect position
            label.setForeground(Color.WHITE);
        }

        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Create the keyboard panel.
     */
    private void createKeyboardPanel() {
        keyboardPanel = new JPanel();
        keyboardPanel.setLayout(new BoxLayout(keyboardPanel, BoxLayout.Y_AXIS));
        keyboardPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create keyboard layout
        String[] rows = {
                "QWERTYUIOP",
                "ASDFGHJKL",
                "ZXCVBNM"
        };

        letterButtons = new HashMap<>();

        // Create each row
        for (int i = 0; i < rows.length; i++) {
            JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));

            // Add special buttons to last row
            if (i == 2) {
                enterButton = new JButton("Enter");
                enterButton.setFont(new Font("Arial", Font.BOLD, 12));
                enterButton.setPreferredSize(new Dimension(80, 40));
                rowPanel.add(enterButton);
            }

            // Add letter buttons
            String row = rows[i];
            for (int j = 0; j < row.length(); j++) {
                char letter = row.charAt(j);
                JButton button = createKeyButton(letter);
                rowPanel.add(button);
                letterButtons.put(letter, button);
            }

            // Add backspace to last row
            if (i == 2) {
                backspaceButton = new JButton("⌫");
                backspaceButton.setFont(new Font("Arial", Font.BOLD, 16));
                backspaceButton.setPreferredSize(new Dimension(60, 40));
                rowPanel.add(backspaceButton);
            }

            rowPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
            keyboardPanel.add(rowPanel);
        }

        add(keyboardPanel, BorderLayout.SOUTH);
    }

    /**
     * Create a button for the keyboard.
     */
    private JButton createKeyButton(char letter) {
        JButton button = new JButton(String.valueOf(letter));
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(40, 40));
        return button;
    }

    /**
     * Create the control panel with game controls and toggles.
     */
    private void createControlPanel() {
        JPanel controlPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Game control buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));

        resetButton = new JButton("Reset Game");
        resetButton.setFont(new Font("Arial", Font.PLAIN, 14));
        resetButton.setEnabled(false);  // Initially disabled

        newGameButton = new JButton("New Game");
        newGameButton.setFont(new Font("Arial", Font.PLAIN, 14));

        buttonPanel.add(resetButton);
        buttonPanel.add(newGameButton);

        // Toggle buttons
        JPanel togglePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));

        errorToggle = new JToggleButton("Show Errors", model.isShowErrorMessage());
        pathToggle = new JToggleButton("Show Path", model.isShowPath());
        randomToggle = new JToggleButton("Random Words", model.isUseRandomWords());

        togglePanel.add(errorToggle);
        togglePanel.add(pathToggle);
        togglePanel.add(randomToggle);

        controlPanel.add(buttonPanel);
        controlPanel.add(togglePanel);

        // Add control panel to the bottom of the header area
        JPanel topContainer = (JPanel) getContentPane().getComponent(0);
        topContainer.add(controlPanel, BorderLayout.SOUTH);
    }

    /**
     * Update method called when the observed model changes.
     */
    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof Model) {
            // Update word labels
            startWordLabel.setText(model.getStartWord());
            targetWordLabel.setText(model.getTargetWord());

            // Update attempts display
            updateAttempts();

            // Update reset button (enabled after first attempt)
            resetButton.setEnabled(!model.getAttempts().isEmpty());

            // Update toggle buttons
            errorToggle.setSelected(model.isShowErrorMessage());
            pathToggle.setSelected(model.isShowPath());
            randomToggle.setSelected(model.isUseRandomWords());
        }
    }

    /**
     * Get the current input as a string.
     */
    public String getInputText() {
        StringBuilder input = new StringBuilder();
        for (JTextField field : inputFields) {
            input.append(field.getText());
        }
        return input.toString();
    }

    /**
     * Clear the input fields.
     */
    public void clearInput() {
        for (JTextField field : inputFields) {
            field.setText("");
        }
        currentInputField = 0;
        inputFields[0].requestFocus();
    }

    /**
     * Append a character to the current input field.
     */
    public void appendToInput(char c) {
        // Find the first empty or incomplete field
        for (int i = 0; i < inputFields.length; i++) {
            if (inputFields[i].getText().isEmpty()) {
                inputFields[i].setText(String.valueOf(c));
                currentInputField = i;

                // Move to next field if available
                if (i < inputFields.length - 1) {
                    inputFields[i + 1].requestFocus();
                    currentInputField = i + 1;
                }

                return;
            }
        }
    }

    /**
     * Handle backspace key.
     */
    public void handleBackspace() {
        // Find the current field or last non-empty field
        int fieldToModify = currentInputField;

        if (inputFields[fieldToModify].getText().isEmpty() && fieldToModify > 0) {
            fieldToModify--;
            currentInputField = fieldToModify;
        }

        inputFields[fieldToModify].setText("");
        inputFields[fieldToModify].requestFocus();
    }

    /**
     * Show an error message.
     */
    public void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Show a win message.
     */
    public void showWinMessage() {
        JOptionPane.showMessageDialog(this,
                "Congratulations! You successfully transformed " +
                        model.getStartWord() + " into " + model.getTargetWord() + "!",
                "You Win!", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Show the help dialog.
     */
    public void showHelpDialog() {
        String helpText =
                "Weaver Game Rules:\n\n" +
                        "1. Start with the given word and transform it to reach the target word.\n" +
                        "2. You can change only one letter at a time.\n" +
                        "3. Each new word must be a valid word in the dictionary.\n" +
                        "4. Use the keyboard or type to enter words.\n" +
                        "5. Press Enter or click the Enter button to submit.\n" +
                        "6. Green letters are in the correct position relative to the target word.\n" +
                        "7. Grey letters are in incorrect positions.\n";

        JOptionPane.showMessageDialog(this, helpText, "Game Rules", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Add a listener for word submission.
     */
    public void addWordListener(ActionListener listener) {
        enterButton.addActionListener(listener);

        // Also allow pressing Enter in the last field
        inputFields[3].addActionListener(listener);
    }

    /**
     * Add a listener for the reset button.
     */
    public void addResetListener(ActionListener listener) {
        resetButton.addActionListener(listener);
    }

    /**
     * Add a listener for the new game button.
     */
    public void addNewGameListener(ActionListener listener) {
        newGameButton.addActionListener(listener);
    }

    /**
     * Add a listener for the help button.
     */
    public void addHelpListener(ActionListener listener) {
        helpButton.addActionListener(listener);
    }

    /**
     * Add listeners for the toggle buttons.
     */
    public void addToggleListeners(
            ActionListener errorListener,
            ActionListener pathListener,
            ActionListener randomListener) {
        errorToggle.addActionListener(errorListener);
        pathToggle.addActionListener(pathListener);
        randomToggle.addActionListener(randomListener);
    }

    /**
     * Add listeners for keyboard buttons.
     */
    public void addKeyboardListeners(
            ActionListener letterListener,
            ActionListener backspaceListener) {
        // Add listener to each letter button
        for (JButton button : letterButtons.values()) {
            button.addActionListener(letterListener);
        }

        // Add listener to backspace button
        backspaceButton.addActionListener(backspaceListener);
    }
}
