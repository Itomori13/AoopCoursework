package weaver;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.*;

/**
 * Creates and manages the GUI interface and implements Observer to receive updates from the Model class.
 */
public class View extends JFrame implements Observer {
    // Model reference
    private final Model model;

    private JButton helpButton;

    private JPanel startWordPanel;
    private JPanel targetWordPanel;

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

    private JToggleButton pathToggle;
    private JToggleButton errorToggle;
    private JToggleButton randomToggle;
    private JPanel errorPanel;
    private JLabel errorMessageLabel;
    private final boolean inputBlocked = false;

    // Colors
    private final Color PRIMARY_COLOR = new Color(120, 120, 120);
    private final Color GRAY = new Color(120, 120, 120);
    private final Color KEYBOARD_BG = new Color(200, 200, 200);
    private final Color ERROR_BG = new Color(220, 50, 50);
    private final Color CORRECT_COLOR = new Color(106, 170, 100);
    private final Color INCORRECT_COLOR = new Color(120, 120, 120);

    // Fonts
    private final Font TITLE_FONT = new Font("Calibri", Font.BOLD, 30);
    private final Font SUBTITLE_FONT = new Font("Calibri", Font.BOLD, 18);
    private final Font NORMAL_FONT = new Font("Calibri", Font.PLAIN, 16);
    private final Font BUTTON_FONT = new Font("Calibri", Font.BOLD, 14);
    private final Font INPUT_FONT = new Font("Calibri", Font.BOLD, 24);
    private final Font ERROR_FONT = new Font("Calibri", Font.BOLD, 16);

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
        setTitle("Weaver Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 700);
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(Color.WHITE);

        // Set up main content panel
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        setContentPane(mainPanel);

        // Create panels
        createHeaderPanel();
        createGameStatePanel();

        // Center panel to hold input and error message
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(Color.WHITE);

        createInputPanel();
        centerPanel.add(inputPanel, BorderLayout.CENTER);

        createErrorPanel();
        centerPanel.add(errorPanel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);

        createAttemptsPanel();

        // under panel to hold keyboard
        JPanel footerPanel = new JPanel(new BorderLayout(10, 10));
        footerPanel.setBackground(Color.WHITE);

        createKeyboardPanel();
        footerPanel.add(keyboardPanel, BorderLayout.CENTER);

        createControlPanel();
        footerPanel.add(createControlPanel(), BorderLayout.SOUTH);

        add(footerPanel, BorderLayout.SOUTH);

        // Add keyboard support
        addKeyboardSupport();

        // Display the frame
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Create the header panel with title and help button.
     */
    private void createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(15, 0));
        headerPanel.setBorder(new EmptyBorder(0, 0, 15, 0));
        headerPanel.setBackground(Color.WHITE);

        // Title
        JLabel titleLabel = new JLabel("Weaver Game", SwingConstants.CENTER);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(PRIMARY_COLOR);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // Help button
        helpButton = createRoundButton("?", 40);
        helpButton.setFont(new Font("Calibri", Font.BOLD, 18));
        helpButton.setToolTipText("Game Rules");
        helpButton.setForeground(Color.WHITE);
        helpButton.setBackground(PRIMARY_COLOR);

        JPanel helpButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        helpButtonPanel.setBackground(Color.WHITE);
        helpButtonPanel.add(helpButton);
        headerPanel.add(helpButtonPanel, BorderLayout.WEST);

        add(headerPanel, BorderLayout.NORTH);
    }

    /**
     * Create a round button
     */
    private JButton createRoundButton(String text, int size) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                if (getModel().isArmed()) {
                    g.setColor(getBackground().darker());
                } else {
                    g.setColor(getBackground());
                }
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.fillOval(0, 0, getSize().width - 1, getSize().height - 1);
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getForeground().darker());
                g2.drawOval(0, 0, getSize().width - 1, getSize().height - 1);
            }

            Shape shape;
            @Override
            public boolean contains(int x, int y) {
                if (shape == null || !shape.getBounds().equals(getBounds())) {
                    shape = new java.awt.geom.Ellipse2D.Float(0, 0, getWidth(), getHeight());
                }
                return shape.contains(x, y);
            }
        };

        button.setPreferredSize(new Dimension(size, size));
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(false);

        return button;
    }

    /**
     * Create the error message panel
     */
    private void createErrorPanel() {
        errorPanel = new JPanel(new BorderLayout());
        errorPanel.setBackground(Color.WHITE);
        errorPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        errorMessageLabel = new JLabel(" ", SwingConstants.CENTER);
        errorMessageLabel.setFont(ERROR_FONT);
        errorMessageLabel.setForeground(Color.WHITE);
        errorMessageLabel.setOpaque(true);
        errorMessageLabel.setBackground(ERROR_BG);

        // Make error message rounded
        errorMessageLabel.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(ERROR_BG, 15),
                new EmptyBorder(8, 15, 8, 15)
        ));

        // Initially invisible
        errorMessageLabel.setVisible(false);

        errorPanel.add(errorMessageLabel, BorderLayout.CENTER);
    }

    /**
     * Creates a component with common styling
     */
    private <T extends JComponent> T styleComponent(T component, Font font, Color bgColor, Color fgColor) {
        if (font != null) component.setFont(font);
        if (bgColor != null) component.setBackground(bgColor);
        if (fgColor != null) component.setForeground(fgColor);
        return component;
    }

    /**
     * Create the game state panel with start and target words.
     */
    private void createGameStatePanel() {
        JPanel gameStatePanel = new JPanel();
        gameStatePanel.setLayout(new BoxLayout(gameStatePanel, BoxLayout.Y_AXIS));
        gameStatePanel.setBackground(Color.WHITE);
        gameStatePanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        // Create sections for start and target words using helper method
        JPanel startWordSection = createWordSection("Start Word", model.getStartWord(), GRAY);
        JPanel targetWordSection = createWordSection("Target Word", model.getTargetWord(), CORRECT_COLOR);

        // Add to main panel with proper space
        gameStatePanel.add(startWordSection);
        gameStatePanel.add(Box.createVerticalStrut(20));
        gameStatePanel.add(targetWordSection);

        add(gameStatePanel, BorderLayout.WEST);
    }

    /**
     * Helper method to create word sections
     */
    private JPanel createWordSection(String title, String word, Color boxColor) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(Color.WHITE);
        section.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel label = styleComponent(new JLabel(title, SwingConstants.CENTER), SUBTITLE_FONT, null, PRIMARY_COLOR);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel wordPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        wordPanel.setBackground(Color.WHITE);
        wordPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Store reference to panel based on title
        if (title.equals("Start Word")) {
            startWordPanel = wordPanel;
        } else {
            targetWordPanel = wordPanel;
        }

        // Create letter boxes for word
        for (int i = 0; i < word.length(); i++) {
            JLabel letterBox = createLetterBox(String.valueOf(word.charAt(i)), boxColor);
            wordPanel.add(letterBox);
        }

        section.add(label);
        section.add(Box.createVerticalStrut(5));
        section.add(wordPanel);

        return section;
    }

    /**
     * Create a letter box for displaying word letters
     */
    private JLabel createLetterBox(String letter, Color bgColor) {
        JLabel label = new JLabel(letter.toUpperCase(), SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 10, 10));

                super.paintComponent(g);
                g2.dispose();
            }
        };

        label.setFont(INPUT_FONT);
        label.setForeground(Color.WHITE);
        label.setBackground(bgColor);
        label.setOpaque(false);
        label.setPreferredSize(new Dimension(50, 50));

        return label;
    }

    /**
     * Create the input panel with text fields.
     */
    private void createInputPanel() {
        inputPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        inputPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        inputPanel.setBackground(Color.WHITE);

        inputFields = new JTextField[4];
        for (int i = 0; i < 4; i++) {
            inputFields[i] = createStyledTextField();
            inputPanel.add(inputFields[i]);

            // Set focus to next field when a character is entered
            final int index = i;
            inputFields[i].addCaretListener(e -> {
                if (inputFields[index].getText().length() == 1 && index < 3 && !inputBlocked) {
                    inputFields[index + 1].requestFocus();
                    currentInputField = index + 1;
                }
            });
        }
    }

    /**
     * Create a styled text field with rounded corners
     */
    private JTextField createStyledTextField() {
        JTextField textField = new JTextField(1) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 15, 15));

                super.paintComponent(g);
                g2.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(PRIMARY_COLOR);
                g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 15, 15));
                g2.dispose();
            }
        };

        textField.setFont(INPUT_FONT);
        textField.setHorizontalAlignment(JTextField.CENTER);
        textField.setPreferredSize(new Dimension(50, 50));
        textField.setOpaque(false);

        // Limit each field to one letter
        ((AbstractDocument)textField.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                // Block input during error state
                if (inputBlocked) return;

                // Allow only single letter inputs (it;s an empty string when deleted)
                if (text.isEmpty() || (text.length() == 1 && Character.isLetter(text.charAt(0)))) {
                    String newText = text.isEmpty() ? text : text.toLowerCase();
                    // Ensure all feilds can only input 1 letter
                    if (fb.getDocument().getLength() - length + newText.length() <= 1) {
                        super.replace(fb, offset, length, newText, attrs);
                    }
                }
            }

            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                // Block input during error state
                if (inputBlocked) return;

                // Allow only single letter inserts
                if (string.length() == 1 && Character.isLetter(string.charAt(0))) {
                    if (fb.getDocument().getLength() + string.length() <= 1) {
                        super.insertString(fb, offset, string.toLowerCase(), attr);
                    }
                }
            }
        });

        return textField;
    }

    /**
     * Add support for physical keyboard input.
     */
    private void addKeyboardSupport() {
        // Create a key adapter for handling keyboard input
        KeyAdapter keyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // Block input during error display
                if (inputBlocked) return;

                int keyCode = e.getKeyCode();

                // Handle backspace key
                if (keyCode == KeyEvent.VK_BACK_SPACE) {
                    handleBackspace();
                    e.consume();
                }
                // Handle letter keys
                else if (keyCode >= KeyEvent.VK_A && keyCode <= KeyEvent.VK_Z) {
                    // Check if all fields are already filled
                    boolean allFilled = true;
                    for (JTextField field : inputFields) {
                        if (field.getText().isEmpty()) {
                            allFilled = false;
                            break;
                        }
                    }

                    // Only process letter key if there's an empty field
                    if (!allFilled) {
                        char letter = (char)('a' + (keyCode - KeyEvent.VK_A));
                        appendToInput(letter);
                    } else {
                        showErrorMessage("Only 4 letters allowed. Please submit or clear.");
                    }
                    e.consume();
                }
                // Handle Enter key for submission
                else if (keyCode == KeyEvent.VK_ENTER) {
                    // Trigger same action as clicking Enter button
                    for (ActionListener listener : enterButton.getActionListeners()) {
                        listener.actionPerformed(new ActionEvent(enterButton, ActionEvent.ACTION_PERFORMED, "Enter"));
                    }
                    e.consume();
                }
                // Handle arrow keys for navigation
                else if (keyCode == KeyEvent.VK_LEFT && currentInputField > 0) {
                    currentInputField--;
                    inputFields[currentInputField].requestFocus();
                    e.consume();
                }
                else if (keyCode == KeyEvent.VK_RIGHT && currentInputField < 3) {
                    currentInputField++;
                    inputFields[currentInputField].requestFocus();
                    e.consume();
                }
            }
        };

        // Add the key adapter to the frame
        this.addKeyListener(keyAdapter);

        // Add it to all input fields
        for (JTextField field : inputFields) {
            field.addKeyListener(keyAdapter);
        }

        // Make sure the frame can receive key events
        this.setFocusable(true);
    }

    /**
     * Create the attempts panel to show previous attempts.
     */
    private void createAttemptsPanel() {
        JPanel attemptsContainer = new JPanel(new BorderLayout());
        attemptsContainer.setBorder(new EmptyBorder(0, 20, 0, 0));
        attemptsContainer.setBackground(Color.WHITE);

        // Title for attempts section
        JLabel attemptsTitle = styleComponent(new JLabel("Attempts", SwingConstants.CENTER), SUBTITLE_FONT, null, PRIMARY_COLOR);
        attemptsTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        attemptsContainer.add(attemptsTitle, BorderLayout.NORTH);

        // Attempts panel
        attemptsPanel = new JPanel();
        attemptsPanel.setLayout(new BoxLayout(attemptsPanel, BoxLayout.Y_AXIS));
        attemptsPanel.setBackground(Color.WHITE);
        attemptsPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        JScrollPane scrollPane = new JScrollPane(attemptsPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(GRAY, 1, true));
        scrollPane.setPreferredSize(new Dimension(180, 400));
        attemptsContainer.add(scrollPane, BorderLayout.CENTER);

        add(attemptsContainer, BorderLayout.EAST);

        // Update with current attempts
        updateAttempts();
    }

    /**
     * Update the attempts panel with current attempts from model
     */
    private void updateAttempts() {
        attemptsPanel.removeAll();

        // Add each attempt with color coding
        List<String> attempts = model.getAttempts();
        for (String attempt : attempts) {
            addWordToPanel(attemptsPanel, attempt, model.getLetterStatus(attempt));
        }

        // Add path if enabled
        if (model.isShowPath()) {
            attemptsPanel.add(Box.createVerticalStrut(15));

            JLabel pathLabel = styleComponent(new JLabel("Probably Solution Path", SwingConstants.CENTER),
                    SUBTITLE_FONT, null, PRIMARY_COLOR);
            pathLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            attemptsPanel.add(pathLabel);
            attemptsPanel.add(Box.createVerticalStrut(10));

            List<String> path = model.calculatePath();
            if (!path.isEmpty()) {
                // Add each word in the path with color coding
                for (String word : path) {
                    addWordToPanel(attemptsPanel, word, model.getLetterStatus(word));
                }
            } else {
                JLabel noPathLabel = styleComponent(new JLabel("No path found", SwingConstants.CENTER),
                        NORMAL_FONT, null, null);
                noPathLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                attemptsPanel.add(noPathLabel);
            }
        }

        attemptsPanel.revalidate();
        attemptsPanel.repaint();
    }

    /**
     * Helper method to add a word with colored letters to a panel
     */
    private void addWordToPanel(JPanel panel, String word, int[] letterStatus) {
        JPanel wordPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
        wordPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        wordPanel.setBackground(Color.WHITE);

        for (int i = 0; i < word.length(); i++) {
            JPanel letterPanel = createLetterPanel(String.valueOf(word.charAt(i)), letterStatus[i]);
            wordPanel.add(letterPanel);
        }

        panel.add(wordPanel);
        panel.add(Box.createVerticalStrut(5));
    }

    /**
     * Create a letter panel with color based on status.
     */
    private JPanel createLetterPanel(String letter, int status) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 8, 8));

                super.paintComponent(g);
                g2.dispose();
            }
        };
        panel.setPreferredSize(new Dimension(35, 35));
        panel.setLayout(new BorderLayout());
        panel.setOpaque(false);

        JLabel label = new JLabel(letter.toUpperCase(), SwingConstants.CENTER);
        label.setFont(new Font("Calibri", Font.BOLD, 16));

        // Set color based on status
        Color bgColor = (status == 1) ? CORRECT_COLOR : INCORRECT_COLOR;
        panel.setBackground(bgColor);
        label.setForeground(Color.WHITE);

        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Create the keyboard panel.
     */
    private void createKeyboardPanel() {
        keyboardPanel = new JPanel();
        keyboardPanel.setLayout(new BoxLayout(keyboardPanel, BoxLayout.Y_AXIS));
        keyboardPanel.setBackground(KEYBOARD_BG);
        keyboardPanel.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(KEYBOARD_BG, 15),
                new EmptyBorder(15, 15, 15, 15)
        ));
        String[] rows = {
                "QWERTYUIOP",
                "ASDFGHJKL",
                "ZXCVBNM"
        };

        letterButtons = new HashMap<>();

        // Create each row
        for (int i = 0; i < rows.length; i++) {
            JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));
            rowPanel.setBackground(KEYBOARD_BG);
            rowPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

            // Add special buttons to last row
            if (i == 2) {
                enterButton = createKeyButton("Enter", 80, 45);
                enterButton.setFont(BUTTON_FONT);
                rowPanel.add(enterButton);
            }

            // Add letter buttons
            String row = rows[i];
            for (int j = 0; j < row.length(); j++) {
                char letter = row.charAt(j);
                int buttonWidth = (letter == 'W' || letter == 'M') ? 50 : 45;
                JButton button = createKeyButton(String.valueOf(letter), buttonWidth, 45);
                rowPanel.add(button);
                letterButtons.put(Character.toLowerCase(letter), button);
            }

            // Add backspace to last row
            if (i == 2) {
                backspaceButton = createKeyButton("backspace", 120, 45);
                backspaceButton.setFont(new Font("Calibri", Font.BOLD, 16));
                rowPanel.add(backspaceButton);
            }

            keyboardPanel.add(rowPanel);
        }
    }

    /**
     * Create a button for the keyboard with rounded corners.
     */
    private JButton createKeyButton(String text, int width, int height) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2.setColor(getBackground().darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(getBackground().brighter());
                } else {
                    g2.setColor(getBackground());
                }

                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 10, 10));

                super.paintComponent(g);
                g2.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.GRAY);
                g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 10, 10));
                g2.dispose();
            }
        };

        button.setFont(BUTTON_FONT);
        button.setForeground(Color.BLACK);
        button.setBackground(Color.WHITE);
        button.setPreferredSize(new Dimension(width, height));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setOpaque(false);

        return button;
    }

    /**
     * Create the control panel with game controls and toggles.
     */
    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        controlPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        controlPanel.setBackground(Color.WHITE);

        // Game control buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        buttonPanel.setBackground(Color.WHITE);

        resetButton = createStyledButton("Reset Game");
        resetButton.setEnabled(false);

        newGameButton = createStyledButton("New Game");

        buttonPanel.add(resetButton);
        buttonPanel.add(newGameButton);
        JPanel togglePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        togglePanel.setBackground(Color.WHITE);

        // Create toggle buttons with proper states
        errorToggle = createStyledToggleButton("Show Errors", true);
        randomToggle = createStyledToggleButton("Random", model.isUseRandomWords());
        pathToggle = createStyledToggleButton("Show Path", model.isShowPath());

        // Add toggle buttons to panel
        togglePanel.add(errorToggle);
        togglePanel.add(randomToggle);
        togglePanel.add(pathToggle);

        controlPanel.add(buttonPanel);
        controlPanel.add(togglePanel);

        // Set initial model states to match UI
        model.setShowErrorMessage(errorToggle.isSelected());
        model.setUseRandomWords(randomToggle.isSelected());

        return controlPanel;
    }

    /**
     * Create a styled button with rounded corners
     */
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2.setColor(getBackground().darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(getBackground().brighter());
                } else {
                    g2.setColor(getBackground());
                }

                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 10, 10));
                        g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 10, 10));

                super.paintComponent(g);
                g2.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? PRIMARY_COLOR : Color.GRAY);
                g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 10, 10));
                g2.dispose();
            }
        };

        button.setFont(BUTTON_FONT);
        button.setForeground(PRIMARY_COLOR);
        button.setBackground(Color.WHITE);
        button.setPreferredSize(new Dimension(120, 40));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setOpaque(false);

        return button;
    }

    /**
     * Create a styled toggle button with rounded corners
     */
    private JToggleButton createStyledToggleButton(String text, boolean selected) {
        JToggleButton button = new JToggleButton(text, selected) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (isSelected()) {
                    g2.setColor(PRIMARY_COLOR);
                } else if (getModel().isPressed()) {
                    g2.setColor(Color.WHITE.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(Color.WHITE.brighter());
                } else {
                    g2.setColor(Color.WHITE);
                }

                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 10, 10));

                super.paintComponent(g);
                g2.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(PRIMARY_COLOR);
                g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 10, 10));
                g2.dispose();
            }
        };

        button.setFont(BUTTON_FONT);
        button.setForeground(selected ? Color.WHITE : PRIMARY_COLOR);
        button.setBackground(Color.WHITE);
        button.setPreferredSize(new Dimension(120, 40));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setOpaque(false);

        // Update colors when selection changes
        button.addItemListener(e -> {
            button.setForeground(button.isSelected() ? Color.WHITE : PRIMARY_COLOR);
        });

        return button;
    }

    /**
     * Update method called when the observed model changes.
     */
    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof Model) {
            // Update word displays
            updateWordDisplays();

            // Update attempts display
            updateAttempts();

            // Update reset button
            resetButton.setEnabled(!model.getAttempts().isEmpty());

            // Update toggle button
            pathToggle.setSelected(model.isShowPath());
        }
    }

    /**
     * Update the word displays when model changes
     */
    private void updateWordDisplays() {
        // Clear existing panels
        startWordPanel.removeAll();
        targetWordPanel.removeAll();

        // Update with current words from model
        updateWordPanel(startWordPanel, model.getStartWord(), GRAY);
        updateWordPanel(targetWordPanel, model.getTargetWord(), CORRECT_COLOR);
    }

    /**
     * Helper method to update a word panel with letter boxes
     */
    private void updateWordPanel(JPanel panel, String word, Color color) {
        for (int i = 0; i < word.length(); i++) {
            JLabel letterBox = createLetterBox(String.valueOf(word.charAt(i)), color);
            panel.add(letterBox);
        }

        panel.revalidate();
        panel.repaint();
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
        if (inputBlocked) return;

        // Check if all fields are already filled
        boolean allFilled = true;
        for (JTextField field : inputFields) {
            if (field.getText().isEmpty()) {
                allFilled = false;
                break;
            }
        }

        // If all fields are filled, show error message
        if (allFilled) {
            showErrorMessage("Only 4 letters allowed. Please submit current word or use backspace to edit.");
            return;
        }

        // Find the first empty field
        for (int i = 0; i < inputFields.length; i++) {
            if (inputFields[i].getText().isEmpty()) {
                inputFields[i].setText(String.valueOf(c));
                currentInputField = i;

                // Move focus to next field if available
                if (i < inputFields.length - 1) {
                    inputFields[i + 1].requestFocus();
                    currentInputField = i + 1;
                } else {
                    // Keep focus on this field if it's the last one
                    inputFields[i].requestFocus();
                }
                return;
            }
        }
    }

    /**
     * Handle backspace key.
     */
    public void handleBackspace() {
        if (inputBlocked) return;

        // Find current field to modify
        int fieldToModify = currentInputField;

        // If current field is empty and not the first field, move to previous field
        if (inputFields[fieldToModify].getText().isEmpty() && fieldToModify > 0) {
            fieldToModify--;
            currentInputField = fieldToModify;
        }

        // Clear the field
        inputFields[fieldToModify].setText("");
        inputFields[fieldToModify].requestFocus();
    }

    /**
     * Show an error message dialog if error toggle is enabled.
     */
    public void showErrorMessage(String message) {
        // Only show error message when in true condiction
        if (model.isShowErrorMessage()) {
            JOptionPane.showMessageDialog(
                    this,
                    message,
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Listener for the error toggle button.
     */
    public void addErrorToggleListener(ActionListener listener) {
        errorToggle.addActionListener(listener);
    }

    /**
     * Listener for the random toggle button.
     */
    public void addRandomToggleListener(ActionListener listener) {
        randomToggle.addActionListener(listener);
    }

    /**
     * Show win message.
     */
    public void showWinMessage() {
        JPanel winPanel = new JPanel(new BorderLayout(0, 10));
        winPanel.setBackground(Color.WHITE);

        JLabel winLabel = new JLabel("Congratulations!", SwingConstants.CENTER);
        winLabel.setFont(new Font("Calibri", Font.BOLD, 20));
        winLabel.setForeground(new Color(0, 150, 0));

        JLabel messageLabel = new JLabel("You successfully transformed " +
                model.getStartWord() + " into " + model.getTargetWord() + " ",
                SwingConstants.CENTER);
        messageLabel.setFont(NORMAL_FONT);

        winPanel.add(winLabel, BorderLayout.NORTH);
        winPanel.add(messageLabel, BorderLayout.CENTER);

        JOptionPane.showMessageDialog(this, winPanel, "You Win!", JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Show the help rule.
     */
    public void showHelpRule() {
        JPanel helpPanel = new JPanel(new BorderLayout(0, 15));
        helpPanel.setBackground(Color.WHITE);
        helpPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel titleLabel = new JLabel("Weaver Game Rules", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Calibri", Font.BOLD, 18));
        titleLabel.setForeground(PRIMARY_COLOR);

        JPanel rulesPanel = new JPanel();
        rulesPanel.setLayout(new BoxLayout(rulesPanel, BoxLayout.Y_AXIS));
        rulesPanel.setBackground(Color.WHITE);

        String[] rules = {
                "1. Start with the given word and transform it to reach the target word.",
                "2. You can change only one letter at one time.",
                "3. Each new word must be a valid word in the dictionary.",
                "4. Use the keyboard below or type with your keyboard to enter words.",
                "5. Press Enter or click the Enter button to submit.",
                "6. Green letters are in the correct position relative to the target word.",
                "7. Grey letters are in incorrect positions or irrelevant word with the target words."
        };

        for (String rule : rules) {
            JLabel ruleLabel = new JLabel(rule);
            ruleLabel.setFont(NORMAL_FONT);
            ruleLabel.setBorder(new EmptyBorder(3, 0, 3, 0));
            rulesPanel.add(ruleLabel);
        }

        helpPanel.add(titleLabel, BorderLayout.NORTH);
        helpPanel.add(rulesPanel, BorderLayout.CENTER);

        JOptionPane.showMessageDialog(this, helpPanel, "Game Rules", JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Add a listener for word submission.
     */
    public void addWordListener(ActionListener listener) {
        enterButton.addActionListener(listener);

        // Allow pressing Enter in any field
        for (JTextField field : inputFields) {
            field.addActionListener(listener);
        }

        // Add Enter key binding
        KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        getRootPane().registerKeyboardAction(listener, "submit", enter, JComponent.WHEN_IN_FOCUSED_WINDOW);
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
     * Add listener for the path toggle button.
     */
    public void addToggleListeners(ActionListener pathListener) {
        pathToggle.addActionListener(pathListener);
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

    /**
     * Custom rounded border class
     */
    private static class RoundedBorder implements Border {
        private int radius;
        private Color color;

        RoundedBorder(Color color, int radius) {
            this.radius = radius;
            this.color = color;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(this.radius, this.radius, this.radius, this.radius);
        }

        @Override
        public boolean isBorderOpaque() {
            return true;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
        }
    }
}
