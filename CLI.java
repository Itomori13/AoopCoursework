package weaver;

import java.util.List;
import java.util.Scanner;

/**
 * CLI version.
 */
public class CLI {
    private Model model;
    private Scanner scanner;
    private boolean running;

    /**
     * Constructor initializes the CLI with the Model.
     */
    public CLI(){
        model = new Model();
        scanner = new Scanner(System.in);
        running = true;
    }

    /**
     * Start the CLI game
     */
    public void start() {
        printWelcome();

        while (running) {
            printGameState();
            String command = promptCommand();
            processCommand(command);

            // Check for win condition
            if (model.hasWon()) {
                printWin();
                promptNewGame();
            }
        }

        scanner.close();
    }

    /**
     * Print the welcome message and instructions.
     */
    private void printWelcome() {
        System.out.println("            WEAVER             ");
        System.out.println("game of the rules are shown below:");
        System.out.println("1.Start with the given word and transform it to reach the target word.");
        System.out.println("2.changing one letter at a time.");
        System.out.println("3.Each new word must be a valid dictionary word.");
        System.out.println();
        printHelp();
    }

    /**
     * Print the help text.
     */
    private void printHelp() {
        System.out.println("COMMANDS:");
        System.out.println("  word   - Enter a 4-letter word as your next attempt");
        System.out.println("  new    - Start a new game");
        System.out.println("  reset  - Reset current game");
        System.out.println("  random - Toggle random word selection (current: " +
                (model.isUseRandomWords() ? "ON" : "OFF") + ")");
        System.out.println("  path   - Toggle showing solution path (current: " +
                (model.isShowPath() ? "ON" : "OFF") + ")");
        System.out.println("  error  - Toggle error messages (current: " +
                (model.isShowErrorMessage() ? "ON" : "OFF") + ")");
        System.out.println("  help   - Show these instructions");
        System.out.println("  quit   - Exit the game");
    }

    /**
     * Print the current game state.
     */
    private void printGameState() {
        System.out.println();
        System.out.println("START WORD:  " + model.getStartWord());
        System.out.println("TARGET WORD: " + model.getTargetWord());
        System.out.println();

        // Print previous attempts
        List<String> attempts = model.getAttempts();
        if (!attempts.isEmpty()) {
            System.out.println("ATTEMPTS:");
            for (int i = 0; i < attempts.size(); i++) {
                String attempt = attempts.get(i);
                System.out.print((i + 1) + ". " + attempt + " ");

                // Print letter status
                int[] letterStatus = model.getLetterStatus(attempt);
                for (int j = 0; j < letterStatus.length; j++) {
                    if (letterStatus[j] == 1) {
                        System.out.print("*"); // Correct position
                    } else {
                        System.out.print("-"); // Incorrect position
                    }
                }
                System.out.println();
            }
            System.out.println();
        }

        // Print path
        if (model.isShowPath()) {
            List<String> path = model.calculatePath();
            if (!path.isEmpty()) {
                System.out.println("Probably solution path:");
                for (int i = 0; i < path.size(); i++) {
                    System.out.println((i + 1) + ". " + path.get(i));
                }
                System.out.println();
            }
        }
    }

    /**
     * Prompt for and read a command from the user.
     */
    private String promptCommand() {
        System.out.print("Enter a word or command: ");
        return scanner.nextLine().trim().toLowerCase();
    }

    /**
     * Process the user's command.
     */
    private void processCommand(String command) {
        if (command.equals("help")) {
            printHelp();
        } else if (command.equals("new")) {
            model.newGame();
            System.out.println("Started a new game.");
        } else if (command.equals("reset")) {
            model.resetGame();
            System.out.println("Game reset.");
        } else if (command.equals("random")) {
            model.setUseRandomWords(!model.isUseRandomWords());
            System.out.println("Random word selection: " +
                    (model.isUseRandomWords() ? "ON" : "OFF"));
        } else if (command.equals("path")) {
            model.setShowPath(!model.isShowPath());
            System.out.println("Show solution path: " +
                    (model.isShowPath() ? "ON" : "OFF"));
        } else if (command.equals("error")) {
            model.setShowErrorMessage(!model.isShowErrorMessage());
            System.out.println("Show error messages: " +
                    (model.isShowErrorMessage() ? "ON" : "OFF"));
        } else if (command.equals("quit")) {
            running = false;
        } else if (command.length() == 4) {
            tryWord(command);
        } else {
            if (model.isShowErrorMessage()) {
                System.out.println("Unknown command or invalid word length. Type 'help' for instructions.");
            }
        }
    }

    /**
     * Try a word as the next attempt.
     */
    private void tryWord(String word) {
        // Validate the word
        Model.WordError error = model.validateWord(word);

        if (error != Model.WordError.NONE) {
            // Show error message if errors are enabled
            if (model.isShowErrorMessage()) {
                System.out.println(model.getErrorMessage(error, word));
            }
            return;
        }

        // If we get here, word is valid
        model.tryWord(word);
    }

    /**
     * Print the win message.
     */
    private void printWin() {
        System.out.println("Congratulations!You've successfully transformed '" + model.getStartWord() +
                "' into '" + model.getTargetWord() + "'!");
        System.out.println("Number of steps: " + model.getAttempts().size());
    }

    /**
     * Prompt for a new game after winning.
     */
    private void promptNewGame() {
        System.out.print("play again? (y/n): ");
        String response = scanner.nextLine().trim().toLowerCase();

        if (response.equals("y") || response.equals("yes")) {
            model.newGame();
        } else {
            running = false;
        }
    }

    /**
     * Main method
     */
    public static void main(String[] args) {
        CLI cli = new CLI();
        cli.start();
    }
}