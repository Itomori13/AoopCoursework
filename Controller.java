package weaver;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Controller class for the Weaver game.
 * Handles user interactions and connects the View with the Model.
 */
public class Controller {
    private Model model;
    private View view;

    /**
     * Constructor initializes the controller with model and view references.
     */
    public Controller(Model model, View view) {
        this.model = model;
        this.view = view;

        // Add action listeners to view components
        view.addWordListener(new WordListener());
        view.addResetListener(new ResetListener());
        view.addNewGameListener(new NewGameListener());
        view.addHelpListener(new HelpListener());
        view.addToggleListeners(new PathToggleListener());
        view.addKeyboardListeners(new LetterListener(), new BackspaceListener());
    }

    /**
     * Listener for word submission.
     */
    private class WordListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String input = view.getInputText().toLowerCase();

            // Check if input is complete
            if (input.length() < 4) {
                view.showErrorMessage("Please enter a 4-letter word");
                return;
            }

            // Check if word is already used
            if (model.getAttempts().contains(input)) {
                view.showErrorMessage("You've already used this word");
                return;
            }

            // Check if word exists in dictionary
            if (!model.isValidWord(input)) {
                view.showErrorMessage("Not a valid word in the dictionary");
                return;
            }

            // Check if only one letter has been changed from the previous word
            String previousWord = model.getAttempts().isEmpty()
                    ? model.getStartWord()
                    : model.getAttempts().get(model.getAttempts().size() - 1);

            if (!differsByOneLetter(previousWord, input)) {
                view.showErrorMessage("You can only change one letter at a time");
                return;
            }

            // Valid move - add to attempts using tryWord method
            boolean success = model.tryWord(input);
            if (success) {
                view.clearInput();

                // Check if the player has won
                if (model.hasWon()) {
                    SwingUtilities.invokeLater(() -> {
                        // Slight delay to ensure UI updates first
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        }
                        view.showWinMessage();
                    });
                }
            } else {
                // This shouldn't happen with our validation, but just in case
                view.showErrorMessage("Invalid word");
            }
        }

        /**
         * Helper method to check if two words differ by exactly one letter.
         * (We're implementing it here since we don't have direct access to Model's private method)
         */
        private boolean differsByOneLetter(String word1, String word2) {
            if (word1.length() != word2.length()) {
                return false;
            }

            int differences = 0;
            for (int i = 0; i < word1.length(); i++) {
                if (word1.charAt(i) != word2.charAt(i)) {
                    differences++;
                }
            }

            return differences == 1;
        }
    }

    /**
     * Listener for letter button clicks.
     */
    private class LetterListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JButton button = (JButton) e.getSource();
            String letter = button.getText().toLowerCase();
            if (letter.length() == 1) {
                view.appendToInput(letter.charAt(0));
            }
        }
    }

    /**
     * Listener for backspace button.
     */
    private class BackspaceListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            view.handleBackspace();
        }
    }

    /**
     * Listener for reset button.
     */
    private class ResetListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int response = JOptionPane.showConfirmDialog(
                    view,
                    "Are you sure you want to reset the game?",
                    "Reset Game",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (response == JOptionPane.YES_OPTION) {
                model.resetGame();
                view.clearInput();
            }
        }
    }

    /**
     * Listener for new game button.
     */
    private class NewGameListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int response = JOptionPane.showConfirmDialog(
                    view,
                    "Are you sure you want to start a new game?",
                    "New Game",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (response == JOptionPane.YES_OPTION) {
                model.newGame();
                view.clearInput();
            }
        }
    }

    /**
     * Listener for help button.
     */
    private class HelpListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            view.showHelpDialog();
        }
    }

    /**
     * Listener for path toggle button.
     */
    private class PathToggleListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JToggleButton button = (JToggleButton) e.getSource();
            model.setShowPath(button.isSelected());
        }
    }
}
