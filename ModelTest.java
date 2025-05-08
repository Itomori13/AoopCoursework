package weaver;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.Assert.*;

/**
 * JUnit tests for the Weaver Model class.
 * These tests validate different aspects of the game model's functionality.
 */
public class ModelTest {

    private Model model;

    @Before
    public void setUp() {
        // Initialize a new model before each test
        model = new Model();
    }

    /**
     * Test Scenario 1: Basic Game Flow and Win Condition
     *
     * This test verifies:
     * 1. The model initializes correctly with valid start and target words
     * 2. The model accepts valid word attempts (differing by one letter)
     * 3. The model correctly detects when a player wins by reaching the target word
     * 4. The letter status reporting works as expected
     */
    @Test
    public void testBasicGameFlowAndWinCondition() {
        // Setup a controlled game state with known words
        setupTestGame(model, "game", "fame");

        // Verify initial state
        assertEquals("Start word should be 'game'", "game", model.getStartWord());
        assertEquals("Target word should be 'fame'", "fame", model.getTargetWord());
        assertTrue("Dictionary should contain the start word", model.isValidWord("game"));
        assertTrue("Dictionary should contain the target word", model.isValidWord("fame"));
        assertEquals("Initial attempts list should be empty", 0, model.getAttempts().size());
        assertFalse("Game should not be won initially", model.hasWon());

        // Try a valid word (in this case, directly to the target)
        boolean attemptResult = model.tryWord("fame");

        // Verify the attempt was successful
        assertTrue("Model should accept valid word attempt", attemptResult);
        assertEquals("Attempts list should have one entry", 1, model.getAttempts().size());
        assertEquals("The attempt should be 'fame'", "fame", model.getAttempts().get(0));

        // Verify win condition
        assertTrue("Game should be won after reaching target word", model.hasWon());

        // Check letter status reporting
        int[] letterStatus = model.getLetterStatus("fame");
        assertEquals("Status array should have length 4", 4, letterStatus.length);
        assertEquals("First letter 'f' should be correct position (1)", 1, letterStatus[0]);
        assertEquals("Second letter 'a' should be correct position (1)", 1, letterStatus[1]);
        assertEquals("Third letter 'm' should be correct position (1)", 1, letterStatus[2]);
        assertEquals("Fourth letter 'e' should be correct position (1)", 1, letterStatus[3]);
    }

    /**
     * Helper method to set up the model with controlled start and target words for testing.
     * This method ensures the words are valid and in the dictionary.
     *
     * @param model The model to set up
     * @param startWord The desired start word
     * @param targetWord The desired target word
     */
    private void setupTestGame(Model model, String startWord, String targetWord) {
        try {
            java.lang.reflect.Field startWordField = Model.class.getDeclaredField("startWord");
            java.lang.reflect.Field targetWordField = Model.class.getDeclaredField("targetWord");
            java.lang.reflect.Field dictionaryField = Model.class.getDeclaredField("dictionary");

            startWordField.setAccessible(true);
            targetWordField.setAccessible(true);
            dictionaryField.setAccessible(true);

            // Get the dictionary and ensure our test words are in it
            @SuppressWarnings("unchecked")
            java.util.Set<String> dictionary = (java.util.Set<String>)dictionaryField.get(model);
            dictionary.add(startWord);
            dictionary.add(targetWord);

            // Set the start and target words
            startWordField.set(model, startWord);
            targetWordField.set(model, targetWord);

            // Clear any existing attempts
            model.resetGame();
        } catch (Exception e) {
            fail("Failed to set up test game: " + e.getMessage());
        }
    }
    /**
     * Test Scenario 2: Invalid Word Attempts and Error Validation
     */
    @Test
    public void testInvalidWordAttemptsAndErrorValidation() {
        // Setup a controlled game state
        setupTestGame(model, "care", "dare");

        // Verify initial state
        assertEquals("Start word should be 'care'", "care", model.getStartWord());
        assertEquals("Target word should be 'dare'", "dare", model.getTargetWord());

        // Test Case 1: Word not in dictionary
        boolean result1 = model.tryWord("xare"); // Made-up word
        assertFalse("Model should reject word not in dictionary", result1);
        assertEquals("Attempts list should remain empty", 0, model.getAttempts().size());

        // Test Case 2: Word of incorrect length
        boolean result2 = model.tryWord("cares"); // Too long (5 letters)
        assertFalse("Model should reject word of incorrect length", result2);
        assertEquals("Attempts list should remain empty", 0, model.getAttempts().size());

        // Test Case 3: Word differs by more than one letter
        // We now know that "fate" differs by multiple letters from "dare", but we're still at "care"
        // Let's find a word that differs by 2+ letters from "care"

        // First, make a valid move to get to "dare"
        boolean validResult = model.tryWord("dare"); // Valid: differs by one letter
        assertTrue("Model should accept valid word", validResult);
        assertEquals("Attempts list should have one entry", 1, model.getAttempts().size());

        // Now try "fate" which we confirmed differs by multiple letters from "dare"
        boolean result4 = model.tryWord("fate");
        assertFalse("Model should reject word differing by more than one letter from previous", result4);
        assertEquals("Attempts list should still have only one entry", 1, model.getAttempts().size());

        // Test null word handling - using try/catch since your model uses assertions
        try {
            model.tryWord(null);
            fail("Model should throw exception for null word");
        } catch (AssertionError e) {
            // This is what we expect - an assertion error
            assertEquals("Word must not be null", e.getMessage());
        } catch (Exception e) {
            // Any exception is acceptable
            assertTrue("Exception for null word is acceptable", true);
        }
    }


    /**
     * Test Scenario 3: Game State Management and Path Calculation
     *
     * This test verifies:
     * 1. The model correctly manages game state through multiple valid moves
     * 2. The reset functionality correctly clears attempts while preserving words
     * 3. The new game functionality properly sets up a new game state
     * 4. The path calculation algorithm correctly finds a valid path between words
     */
    @Test
    public void testGameStateManagementAndPathCalculation() {
        // Choose words from your fixed dictionary that have a reasonable path
        // Example: "game" to "fame" to "fate" to "date"
        setupTestGame(model, "game", "date");

        // Print to confirm words are in dictionary (optional)
        System.out.println("Is 'game' in dictionary: " + model.isValidWord("game"));
        System.out.println("Is 'fame' in dictionary: " + model.isValidWord("fame"));
        System.out.println("Is 'fate' in dictionary: " + model.isValidWord("fate"));
        System.out.println("Is 'date' in dictionary: " + model.isValidWord("date"));

        // Verify initial state
        assertEquals("Start word should be 'game'", "game", model.getStartWord());
        assertEquals("Target word should be 'date'", "date", model.getTargetWord());
        assertEquals("Initial attempts list should be empty", 0, model.getAttempts().size());

        // Make a series of valid moves but don't reach the target yet
        // Ensure these words exist in your dictionary and differ by exactly one letter
        model.tryWord("fame");
        model.tryWord("fate");

        // Verify mid-game state
        assertEquals("Attempts list should have two entries", 2, model.getAttempts().size());
        assertEquals("First attempt should be 'fame'", "fame", model.getAttempts().get(0));
        assertEquals("Second attempt should be 'fate'", "fate", model.getAttempts().get(1));
        assertFalse("Game should not be won yet", model.hasWon());

        // Test the reset game functionality
        model.resetGame();

        // Verify state after reset
        assertEquals("Attempts should be empty after reset", 0, model.getAttempts().size());
        assertEquals("Start word should remain unchanged after reset", "game", model.getStartWord());
        assertEquals("Target word should remain unchanged after reset", "date", model.getTargetWord());

        // Test new game functionality
        // First ensure predictable behavior by turning off random words
        // If your model doesn't have this method, you can skip this part
        if (hasSetUseRandomWordsMethod(model)) {
            setUseRandomWords(model, false);
        }

        // Execute new game
        model.newGame();

        // Verify state after new game
        // Note: exact words might vary based on your implementation
        assertNotNull("Start word should be set after new game", model.getStartWord());
        assertNotNull("Target word should be set after new game", model.getTargetWord());
        assertNotEquals("Start and target words should be different",
                model.getStartWord(), model.getTargetWord());
        assertEquals("Attempts should be empty after new game", 0, model.getAttempts().size());

        // Reset to our controlled state for path calculation test
        setupTestGame(model, "game", "date");

        // Test path calculation
        List<String> path = model.calculatePath();

        // Verify path properties
        assertNotNull("Path should not be null", path);
        assertFalse("Path should not be empty", path.isEmpty());
        assertTrue("Path should end with target word",
                path.get(path.size()-1).equals("date"));

        // Check that each step in the path differs by exactly one letter
        String previous = "game"; // Start word
        for (String step : path) {
            assertTrue("Each step should differ by exactly one letter from previous",
                    differsByOneLetter(previous, step));
            assertTrue("Each word in path should be in dictionary",
                    model.isValidWord(step));
            previous = step;
        }
    }

    /**
     * Helper method to check if the model has setUseRandomWords method
     */
    private boolean hasSetUseRandomWordsMethod(Model model) {
        try {
            Model.class.getMethod("setUseRandomWords", boolean.class);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    /**
     * Set whether the model should use random words for new games
     * Only called if the method exists
     */
    private void setUseRandomWords(Model model, boolean useRandom) {
        try {
            Method method = Model.class.getMethod("setUseRandomWords", boolean.class);
            method.invoke(model, useRandom);
        } catch (Exception e) {
            // Silently ignore if method doesn't exist
        }
    }

    /**
     * Helper method to check if two words differ by exactly one letter
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

