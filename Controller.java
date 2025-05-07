package weaver;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Controller class for the Weaver game.
 * Handles user input and coordinates interactions between Model and View.
 */
public class Controller {
    private Model model;
    private View view;

    /**
     * Constructor initializes the controller with the given model and view.
     */
    public Controller(Model model, View view) {
        this.model = model;
        this.view = view;

        // Register action listeners
        registerActionListeners();
    }

    /**
     * Register all action listeners for the view components.
     */
    private void registerActionListeners() {
        // Word submission listener
        view.addWordListener(new WordListener());

        // Game control listeners
        view.addResetListener(new ResetListener());
        view.addNewGameListener(new NewGameListener());
        view.addHelpListener(new HelpListener());

        // Toggle listeners
        view.addToggleListeners(
                new ErrorToggleListener(),
                new PathToggleListener(),
                new RandomToggleListener()
        );

        // Keyboard listeners
        view.addKeyboardListeners(
                new LetterKeyListener(),
                new BackspaceListener()
        );
    }

    /**
     * Listener for word submission.
     */
    private class WordListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String input = view.getInputText().toLowerCase();

            // Check if input is complete
            if (input.length() != 4) {
                if (model.isShowErrorMessage()) {
                    view.showErrorMessage("Please enter a 4-letter word.");
                }
                return;
            }

            // Check if input is a valid word
            if (!model.isValidWord(input)) {
                if (model.isShowErrorMessage()) {
                    view.showErrorMessage("'" + input + "' is not in the dictionary.");
                }
                return;
            }

            // Try the word
            boolean success = model.tryWord(input);

            if (!success) {
                // Word didn't differ by exactly one letter
                if (model.isShowErrorMessage()) {
                    String previousWord = model.getAttempts().isEmpty() ?
                            model.getStartWord() : model.getAttempts().get(model.getAttempts().size() - 1);
                    view.showErrorMessage("'" + input + "' differs by more than one letter from '" + previousWord + "'.");
                }
                return;
            }

            // Clear input fields
            view.clearInput();

            // Check for win
            if (model.hasWon()) {
                view.showWinMessage();
            }
        }
    }

    /**
     * Listener for the reset button.
     */
    private class ResetListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            model.resetGame();
            view.clearInput();
        }
    }

    /**
     * Listener for the new game button.
     */
    private class NewGameListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            model.newGame();
            view.clearInput();
        }
    }

    /**
     * Listener for the help button.
     */
    private class HelpListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            view.showHelpDialog();
        }
    }

    /**
     * Listener for the error toggle button.
     */
    private class ErrorToggleListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            model.setShowErrorMessage(!model.isShowErrorMessage());
        }
    }

    /**
     * Listener for the path toggle button.
     */
    private class PathToggleListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            model.setShowPath(!model.isShowPath());
        }
    }

    /**
     * Listener for the random toggle button.
     */
    private class RandomToggleListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            model.setUseRandomWords(!model.isUseRandomWords());
        }
    }

    /**
     * Listener for letter keys on the virtual keyboard.
     */
    private class LetterKeyListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String letter = e.getActionCommand();
            if (letter.length() == 1) {
                view.appendToInput(letter.charAt(0));
            }
        }
    }

    /**
     * Listener for the backspace button.
     */
    private class BackspaceListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            view.handleBackspace();
        }
    }
}
