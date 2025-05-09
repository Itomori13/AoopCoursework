package weaver;

import javax.swing.SwingUtilities;

/**
 * Main entry for the Weaver game GUI application,it creates and connects the Model, View, and Controller components.
 */
public class WeaverGUI {
    /**
     * Main method - entry point of the GUI application.
     */
    public static void main(String[] args) {
        // Use EDT for Swing applications
        SwingUtilities.invokeLater(() -> {
            Model model = new Model();
            View view = new View(model);
            new Controller(model, view);
        });
    }
}