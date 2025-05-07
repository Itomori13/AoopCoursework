package weaver;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Model class for the Weaver word game.
 * Manages game state, logic, and dictionary operations.
 * Extends Observable to notify the View of changes.
 */
public class Model extends Observable {
    // Game state
    private String startWord;
    private String targetWord;
    private List<String> attempts;

    // Dictionary
    private Set<String> dictionary;
    private Map<Integer, List<String>> wordsByLength;

    // Game flags
    private boolean showErrorMessage;
    private boolean showPath;
    private boolean useRandomWords;

    /**
     * Constructor initializes the model with default values and loads the dictionary.
     */
    public Model() {
        // Initialize game state
        attempts = new ArrayList<>();

        // Initialize flags
        showErrorMessage = true;
        showPath = false;
        useRandomWords = true;

        // Load dictionary
        dictionary = new HashSet<>();
        wordsByLength = new HashMap<>();
        loadDictionary("C:\\Users\\Administrator\\Desktop\\Aoop_Coursework\\Resources\\Dictionary.txt");

        // Set initial words
        setupNewGame();
    }

    /**
     * Load dictionary from file.
     */
    private void loadDictionary(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String word = line.trim().toLowerCase();

                // Add to main dictionary
                dictionary.add(word);

                // Add to length-indexed map
                int length = word.length();
                if (!wordsByLength.containsKey(length)) {
                    wordsByLength.put(length, new ArrayList<>());
                }
                wordsByLength.get(length).add(word);
            }
        } catch (IOException e) {
            System.err.println("Error loading dictionary: " + e.getMessage());
            // Initialize with some default words if dictionary loading fails
            initializeDefaultWords();
        }
    }

    /**
     * Initialize with default words if dictionary fails to load.
     */
    private void initializeDefaultWords() {
        // Add some default 4-letter words
        String[] defaultWords = {"game", "play", "word", "move", "hint", "clue"};
        for (String word : defaultWords) {
            dictionary.add(word);
            if (!wordsByLength.containsKey(4)) {
                wordsByLength.put(4, new ArrayList<>());
            }
            wordsByLength.get(4).add(word);
        }
    }

    /**
     * Set up a new game with start and target words.
     */
    private void setupNewGame() {
        // Clear previous attempts
        attempts.clear();

        if (useRandomWords && wordsByLength.containsKey(4) && wordsByLength.get(4).size() >= 2) {
            // Select random start and target words
            List<String> fourLetterWords = wordsByLength.get(4);
            Random random = new Random();

            startWord = fourLetterWords.get(random.nextInt(fourLetterWords.size()));

            // Ensure target word is different from start word
            do {
                targetWord = fourLetterWords.get(random.nextInt(fourLetterWords.size()));
            } while (targetWord.equals(startWord));
        } else {
            // Use default words
            startWord = "baby";
            targetWord = "what";
        }
    }

    /**
     * Try a word as the next attempt.
     * Returns true if the word was valid and added to attempts.
     */
    public boolean tryWord(String word) {
        // Validate word
        word = word.toLowerCase();

        // Check if word is in dictionary
        if (!dictionary.contains(word)) {
            return false;
        }

        // Check length
        if (word.length() != 4) {
            return false;
        }

        // Check if word differs by exactly one letter from previous word
        String previousWord = attempts.isEmpty() ? startWord : attempts.get(attempts.size() - 1);
        if (!differsByOneLetter(previousWord, word)) {
            return false;
        }

        // Add valid word to attempts
        attempts.add(word);

        // Notify observers
        setChanged();
        notifyObservers();

        return true;
    }

    /**
     * Check if two words differ by exactly one letter.
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

    /**
     * Reset the game to its initial state.
     */
    public void resetGame() {
        attempts.clear();

        // Notify observers
        setChanged();
        notifyObservers();
    }

    /**
     * Start a new game with new words.
     */
    public void newGame() {
        setupNewGame();

        // Notify observers
        setChanged();
        notifyObservers();
    }

    /**
     * Check if the player has won the game.
     */
    public boolean hasWon() {
        if (attempts.isEmpty()) {
            return false;
        }

        return attempts.get(attempts.size() - 1).equals(targetWord);
    }

    /**
     * Get the status of each letter in a word compared to the target word.
     * Returns an array of status codes (1 = correct position, 0 = incorrect position).
     */
    public int[] getLetterStatus(String word) {
        int[] status = new int[word.length()];

        for (int i = 0; i < word.length(); i++) {
            if (i < targetWord.length() && word.charAt(i) == targetWord.charAt(i)) {
                status[i] = 1;  // Correct position
            } else {
                status[i] = 0;  // Incorrect position
            }
        }

        return status;
    }

    // Getters and setters

    /**
     * Get the start word.
     */
    public String getStartWord() {
        return startWord;
    }

    /**
     * Get the target word.
     */
    public String getTargetWord() {
        return targetWord;
    }

    /**
     * Get the list of attempts.
     */
    public List<String> getAttempts() {
        return Collections.unmodifiableList(attempts);
    }

    /**
     * Check if a word is in the dictionary.
     */
    public boolean isValidWord(String word) {
        return dictionary.contains(word.toLowerCase());
    }

    /**
     * Get the path from start word to target word (if showPath is enabled).
     */
    public List<String> getPath() {
        if (!showPath) {
            return Collections.emptyList();
        }

        List<String> path = new ArrayList<>();
        path.add(startWord);
        path.addAll(attempts);

        return path;
    }

    /**
     * Check if error messages should be shown.
     */
    public boolean isShowErrorMessage() {
        return showErrorMessage;
    }

    /**
     * Set whether error messages should be shown.
     */
    public void setShowErrorMessage(boolean showErrorMessage) {
        this.showErrorMessage = showErrorMessage;

        // Notify observers
        setChanged();
        notifyObservers();
    }

    /**
     * Check if path should be shown.
     */
    public boolean isShowPath() {
        return showPath;
    }

    /**
     * Set whether path should be shown.
     */
    public void setShowPath(boolean showPath) {
        this.showPath = showPath;

        // Notify observers
        setChanged();
        notifyObservers();
    }

    /**
     * Check if random words should be used.
     */
    public boolean isUseRandomWords() {
        return useRandomWords;
    }

    /**
     * Set whether random words should be used.
     */
    public void setUseRandomWords(boolean useRandomWords) {
        this.useRandomWords = useRandomWords;

        // Notify observers
        setChanged();
        notifyObservers();
    }
}
