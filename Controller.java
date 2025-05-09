package weaver;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
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
        view.addErrorToggleListener(new ErrorToggleListener());
        view.addRandomToggleListener(new RandomToggleListener());
        view.addKeyboardListeners(new LetterListener(), new BackspaceListener());
    }

    /**
     * Listener for word to enter.
     */
    private class WordListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String input = view.getInputText().toLowerCase();

            Model.WordError error = model.validateWord(input);

            if (error != Model.WordError.NONE) {
                // Show error message if errors are enabled
                if (model.isShowErrorMessage()) {
                    view.showErrorMessage(model.getErrorMessage(error, input));
                }
                return;
            }

            // Valid move - add to attempts using tryWord method
            boolean success = model.tryWord(input);
            if (success) {
                view.clearInput();

                // Check if the player has won
                if (model.hasWon()) {
                    SwingUtilities.invokeLater(() -> {
                        // take some time for UI update
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        }
                        view.showWinMessage();
                    });
                }
            } else {
                // Out of expectation output
                view.showErrorMessage("Invalid word");
            }


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
            view.showHelpRule();
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
    /**
     * Listener for error toggle button.
     */
    private class ErrorToggleListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JToggleButton button = (JToggleButton) e.getSource();
            model.setShowErrorMessage(button.isSelected());
        }
    }
    /**
     * Listener for random words toggle button.
     */
    private class RandomToggleListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JToggleButton button = (JToggleButton) e.getSource();
            model.setUseRandomWords(button.isSelected());

            // Ask if user wants to start a new game with the new setting
            int response = JOptionPane.showConfirmDialog(
                    view,
                    "Do you want to start a new game with " +
                            (button.isSelected() ? "random" : "default") + " words?",
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
}
