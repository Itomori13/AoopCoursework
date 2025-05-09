package weaver;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Manages game state, logic, and dictionary operations,it extends Observable to notify the View of changes.
 * @invariant dictionary != null && !dictionary.isEmpty()
 * @invariant startWord != null && targetWord != null
 * @invariant startWord.length() == targetWord.length() == 4
 * @invariant dictionary.contains(startWord) && dictionary.contains(targetWord)
 * @invariant attempts != null
 */

public class Model extends Observable {
    // Game state
    private String startWord;
    private String targetWord;
    private final List<String> attempts;

    // Dictionary
    private final Set<String> dictionary;
    private final Map<Integer, List<String>> wordsByLength;

    // Game flags
    private boolean showErrorMessage;
    private boolean showPath;
    private boolean useRandomWords;
    // add error type
    /**
     * Represents possible errors when validating a word in the game.
     * @invariant Each enum value presents a specific error condition
     */
    public enum WordError {
        NONE,
        TOO_SHORT,
        NOT_IN_DICTIONARY,
        ALREADY_USED,
        MULTIPLE_LETTERS,
        SAME_AS_PREVIOUS
    }

    /**
     * Validates a word for the game rules.
     *  @param word The word to try
     *  @requires word = null
     *  @ensures adds valid word to attempts to validate
     */
    public WordError validateWord(String word) {
        // Check for null input
        if (word == null) {
            return WordError.TOO_SHORT;
        }

        // Check word length
        if (word.length() != 4) {
            return WordError.TOO_SHORT;
        }

        // Check if word exists in dictionary
        if (!isValidWord(word)) {
            return WordError.NOT_IN_DICTIONARY;
        }

        // Check if word is already used
        if (attempts.contains(word)) {
            return WordError.ALREADY_USED;
        }

        // Get previous word
        String previousWord = attempts.isEmpty() ? startWord : attempts.get(attempts.size() - 1);

        // Check if same as previous
        if (word.equals(previousWord)) {
            return WordError.SAME_AS_PREVIOUS;
        }

        // Check if only one letter has changed
        if (!differsByOneLetter(previousWord, word)) {
            return WordError.MULTIPLE_LETTERS;
        }

        return WordError.NONE;
    }

    /**
     * error message for a given WordError.
     * @param error The error type
     * @param word The word that caused the error
     * @requires error != null
     * @requires word can be null
     * @ensures result != null
     * @ensures result is a message for the given error and word
     */
    public String getErrorMessage(WordError error, String word) {
        switch (error) {
            case TOO_SHORT:
                return "Please enter a 4-letter word";
            case NOT_IN_DICTIONARY:
                return "'" + word + "' is not a valid word in the dictionary";
            case ALREADY_USED:
                return "You've already used the word '" + word + "'";
            case MULTIPLE_LETTERS:
                String previous = attempts.isEmpty() ? startWord : attempts.get(attempts.size() - 1);
                return "'" + word + "' differs by more than one letter from '" + previous + "'";
            case SAME_AS_PREVIOUS:
                return "Please enter a different word";
            default:
                return "";
        }
    }

    /**
     * Constructor initializes the model with default values and loads the dictionary.
     * @ensures dictionary loaded and default words set
     */

    public Model() {
        // Initialize game state
        attempts = new ArrayList<>();

        // Initialize flags
        showErrorMessage = true;
        showPath = false;
        useRandomWords = false;

        // Load dictionary
        dictionary = new HashSet<>();
        wordsByLength = new HashMap<>();
        loadDictionary();

        // Set initial words
        setupNewGame();

        // Check postconditions
        assert attempts.isEmpty() : "Attempts must be initialized and empty";
        assert showErrorMessage : "ShowErrorMessage must be initialized to true";
        assert !showPath : "ShowPath must be initialized to false";
        assert !useRandomWords : "UseRandomWords must be initialized to false";
        assert !dictionary.isEmpty() : "Dictionary must be loaded";
        assert startWord != null && startWord.length() == 4 : "StartWord must be initialized correctly";
        assert targetWord != null && targetWord.length() == 4 : "TargetWord must be initialized correctly";
        assert !startWord.equals(targetWord) : "StartWord and TargetWord must be different";

        checkInvariants();
    }
    /**
     * Checks class invariants are maintained or not
     */
    private void checkInvariants() {
        assert dictionary != null : "Dictionary must not be null";
        assert !dictionary.isEmpty() : "Dictionary must not be empty";
        assert wordsByLength != null : "WordsByLength must not be null";
        assert startWord != null : "StartWord must not be null";
        assert startWord.length() == 4 : "StartWord must be 4 letters";
        assert dictionary.contains(startWord) : "StartWord must be in dictionary";
        assert targetWord != null : "TargetWord must not be null";
        assert targetWord.length() == 4 : "TargetWord must be 4 letters";
        assert dictionary.contains(targetWord) : "TargetWord must be in dictionary";
        assert !startWord.equals(targetWord) : "StartWord and TargetWord must be different";
        assert attempts != null : "Attempts must not be null";

        // Check all words in attempts are valid
        for (int i = 0; i < attempts.size(); i++) {
            String word = attempts.get(i);
            assert word != null : "Attempt word must not be null";
            assert word.length() == 4 : "Attempt word must be 4 letters";
            assert dictionary.contains(word) : "Attempt word must be in dictionary";

            // Check neighbour words differ by one letter
            if (i == 0) {
                assert differsByOneLetter(startWord, word) :
                        "First attempt must differ by one letter from startWord";
            } else {
                assert differsByOneLetter(attempts.get(i-1), word) :
                        "Neighbour attempts must differ by one letter";
            }
        }
    }



    /**
     * Load dictionary from file.
     */
    private void loadDictionary() {
        try (BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\Administrator\\Desktop\\Aoop_Coursework\\Resources\\Dictionary.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String word = line.trim().toLowerCase();

                // Add to main dictionary
                dictionary.add(word);

                // Add to map
                int length = word.length();
                if (!wordsByLength.containsKey(length)) {
                    wordsByLength.put(length, new ArrayList<>());
                }
                wordsByLength.get(length).add(word);
            }
        } catch (IOException e) {
            System.err.println("Error loading dictionary: " + e.getMessage());
            // Initialize with default words if dictionary loading fails
            initializeDefaultWords();
        }
    }

    /**
     * Initialize with default words if dictionary fails to load.
     */
    private void initializeDefaultWords() {
        // Add soul and mate in figure2
        String[] defaultWords = {"soul", "mate"};
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
            startWord = "soul";
            targetWord = "mate";
        }

    }

    /**
     * Try a word as the next attempt.
     * @param word The word to try
     * @requires word != null
     * @requires validateWord(word) == NONE
     * @ensures attempts.size() == old(attempts.size()) + 1
     * @ensures attempts.get(attempts.size() - 1).equals(word)
     * @ensures Observer.update() will be called
     * @ensures result == true
     */
    public boolean tryWord(String word) {
        // Precondition
        assert word != null : "Word must not be null";
        checkInvariants();

        // Save state for postcondition verification
        int oldAttemptsSize = attempts.size();

        // Validate word
        word = word.toLowerCase();

        // Check if word is in dictionary
        if (!dictionary.contains(word)) {
            checkInvariants();
            return false;
        }

        // Check length
        if (word.length() != 4) {
            checkInvariants();
            return false;
        }

        // Check if word differs by exactly one letter from previous word
        String previousWord = attempts.isEmpty() ? startWord : attempts.get(attempts.size() - 1);
        if (!differsByOneLetter(previousWord, word)) {
            checkInvariants();
            return false;
        }

        // Add valid word to attempts
        attempts.add(word);

        // Notify observers
        setChanged();
        notifyObservers();

        // Postconditions
        assert attempts.size() == oldAttemptsSize + 1 : "Attempts size should increase by 1";
        assert attempts.get(attempts.size() - 1).equals(word) : "Word should be added to attempts";

        checkInvariants();
        return true;
    }

    /**
     * Check if two words differ by exactly one letter.
     * @param word1 First word to compare
     * @param word2 Second word to compare
     * @requires word1 != null && word2 != null
     * @ensures result == true and word1 and word2 have the same length and differ in exactly one position
     * @ensures result == false and word1 and word2 have different lengths or differ in zero or more than one position
     */
    public boolean differsByOneLetter(String word1, String word2) {
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
     * @ensures attempts.isEmpty()
     * @ensures startWord == old(startWord)
     * @ensures targetWord == old(targetWord)
     * @ensures Observer.update() will be called
     */
    public void resetGame() {
        checkInvariants();

        // Save state for postcondition verification
        String oldStartWord = startWord;
        String oldTargetWord = targetWord;

        attempts.clear();

        // Notify observers
        setChanged();
        notifyObservers();

        // Postconditions
        assert attempts.isEmpty() : "Attempts must be cleared";
        assert startWord.equals(oldStartWord) : "StartWord must remain unchanged";
        assert targetWord.equals(oldTargetWord) : "TargetWord must remain unchanged";

        checkInvariants();
    }

    /**
     * Start a new game with new words.
     * @ensures attempts.isEmpty()
     * @ensures startWord != null && startWord.length() == 4
     * @ensures targetWord != null && targetWord.length() == 4
     * @ensures !startWord.equals(targetWord)
     * @ensures dictionary.contains(startWord) && dictionary.contains(targetWord)
     * @ensures use useRandomWords method then startWord and targetWord are randomly selected
     */
    public void newGame() {
        checkInvariants();

        setupNewGame();

        // Notify observers
        setChanged();
        notifyObservers();

        // Postconditions
        assert attempts.isEmpty() : "Attempts must be cleared for new game";
        assert startWord != null && startWord.length() == 4 : "New startWord must be valid";
        assert targetWord != null && targetWord.length() == 4 : "New targetWord must be valid";
        assert !startWord.equals(targetWord) : "New startWord and targetWord must be different";

        checkInvariants();
    }

    /**
     * Check if the player has won the game.
     * @ensures result == (!attempts.isEmpty() && attempts.get(attempts.size() - 1).equals(targetWord))
     */
    public boolean hasWon() {
        checkInvariants();

        boolean result = false;
        if (!attempts.isEmpty()) {
            result = attempts.get(attempts.size() - 1).equals(targetWord);
        }

        // No state should change
        checkInvariants();

        // Postcondition verification
        if (result) {
            assert !attempts.isEmpty() : "Cannot win with empty attempts";
            assert attempts.get(attempts.size() - 1).equals(targetWord) :
                    "Last attempt must equal targetWord to win";
        }

        return result;
    }

    /**
     * Get the status of each letter in a word compared to the target word and return an array of status codes
     */
    public int[] getLetterStatus(String word) {
        // Precondition
        assert word != null : "Word must not be null";
        checkInvariants();

        int[] status = new int[word.length()];

        for (int i = 0; i < word.length(); i++) {
            if (i < targetWord.length() && word.charAt(i) == targetWord.charAt(i)) {
                status[i] = 1;  // Correct position
            } else {
                status[i] = 0;  // Incorrect position
            }
        }

        for (int i = 0; i < status.length && i < targetWord.length(); i++) {
            if (word.charAt(i) == targetWord.charAt(i)) {
                assert status[i] == 1 : "Matching letters should have status 1";
            } else {
                assert status[i] == 0 : "Non-matching letters should have status 0";
            }
        }

        checkInvariants();

        return status;
    }

    /**
     * Calculate a possible path from start word to target word by using bf search algorithm
     * if there is no path to the answer,return empty path
     * @ensures returns valid path or empty list if no path
     */
    public List<String> calculatePath() {
        checkInvariants();

        // Save state for postcondition verification
        String oldStartWord = startWord;
        String oldTargetWord = targetWord;
        List<String> oldAttempts = new ArrayList<>(attempts);

        // If dictionary is empty or start/target are the same, return empty path
        if (dictionary.isEmpty() || startWord.equals(targetWord)) {
            return new ArrayList<>();
        }

        // Use breadth-first search to find a path
        Queue<String> queue = new LinkedList<>();
        Map<String, String> parentMap = new HashMap<>();
        Set<String> visited = new HashSet<>();

        queue.offer(startWord);
        visited.add(startWord);

        while (!queue.isEmpty()) {
            String current = queue.poll();

            // If we found the target word, reconstruct the path
            if (current.equals(targetWord)) {
                List<String> path = new ArrayList<>();
                String word = targetWord;
                while (!word.equals(startWord)) {
                    path.add(0, word);
                    word = parentMap.get(word);
                }

                // Postconditions
                if (!path.isEmpty()) {
                    assert path.get(path.size() - 1).equals(targetWord) :
                            "Path must end with targetWord";
                    assert differsByOneLetter(startWord, path.get(0)) :
                            "First word in path must differ by one letter from startWord";

                    // Check that neighbour words in path differ by one letter
                    for (int i = 0; i < path.size() - 1; i++) {
                        assert differsByOneLetter(path.get(i), path.get(i+1)) :
                                "Adjacent words in path must differ by one letter";
                        assert dictionary.contains(path.get(i)) :
                                "All words in path must be in dictionary";
                    }
                }

                // Verify state hasn't changed
                assert startWord.equals(oldStartWord) : "StartWord must not change";
                assert targetWord.equals(oldTargetWord) : "TargetWord must not change";
                assert attempts.equals(oldAttempts) : "Attempts must not change";

                checkInvariants();
                return path;
            }

            // Try changing each position
            char[] wordChars = current.toCharArray();
            for (int i = 0; i < wordChars.length; i++) {
                char originalChar = wordChars[i];

                // Try all possible letters
                for (char c = 'a'; c <= 'z'; c++) {
                    if (c != originalChar) {
                        wordChars[i] = c;
                        String newWord = new String(wordChars);

                        // Check if it's a valid word and not visited
                        if (dictionary.contains(newWord) && !visited.contains(newWord)) {
                            queue.offer(newWord);
                            visited.add(newWord);
                            parentMap.put(newWord, current);
                        }
                    }
                }

                // Restore original character
                wordChars[i] = originalChar;
            }
        }

        // Verify state hasn't changed
        assert startWord.equals(oldStartWord) : "StartWord must not change";
        assert targetWord.equals(oldTargetWord) : "TargetWord must not change";
        assert attempts.equals(oldAttempts) : "Attempts must not change";

        checkInvariants();

        // If no path found, return empty list
        return new ArrayList<>();
    }

    // Getters and setters methods

    /**
     * Get the start word.
     */
    public String getStartWord() {
        checkInvariants();
        return startWord;
    }

    /**
     * Get the target word.
     */
    public String getTargetWord() {
        checkInvariants();
        return targetWord;
    }

    /**
     * Get the list of attempts.
     */
    public List<String> getAttempts() {
        checkInvariants();
        return Collections.unmodifiableList(attempts);
    }

    /**
     * Check if a word is in the dictionary.
     */
    public boolean isValidWord(String word) {

        return dictionary.contains(word.toLowerCase());
    }


    /**
     * Check if error messages should be shown.
     */
    public boolean isShowErrorMessage() {
        checkInvariants();
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
        checkInvariants();
        return showPath;
    }

    /**
     * Set whether path should be shown.
     */
    public void setShowPath(boolean showPath) {
        checkInvariants();

        this.showPath = showPath;

        // Notify observers
        setChanged();
        notifyObservers();

        // Postcondition
        assert this.showPath == showPath : "ShowPath should be updated to new value";

        checkInvariants();
    }

    /**
     * Check if random words should be used.
     */
    public boolean isUseRandomWords() {
        checkInvariants();
        return useRandomWords;
    }

    /**
     * Set whether random words should be used.
     */
    public void setUseRandomWords(boolean useRandomWords) {
        checkInvariants();

        this.useRandomWords = useRandomWords;

        // Notify observers
        setChanged();
        notifyObservers();

        // Postcondition
        assert this.useRandomWords == useRandomWords :
                "UseRandomWords should be updated to new value";

        checkInvariants();
    }
}
