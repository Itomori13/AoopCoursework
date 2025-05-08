package weaver;

import javax.swing.SwingUtilities;

/**
 * Main entry point for the Weaver game GUI application.
 * Creates and connects the Model, View, and Controller components.
 */
public class WeaverGUI {
    /**
     * Main method - entry point of the GUI application.
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        // Use Event Dispatch Thread for Swing applications
        SwingUtilities.invokeLater(() -> {
            // Create the MVC components
            Model model = new Model();
            View view = new View(model);
            Controller controller = new Controller(model, view);

            // GUI is already visible since the View constructor calls setVisible(true)
            System.out.println("Weaver Game GUI started successful");
        });
    }
}