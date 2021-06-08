import java.awt.*;
import java.util.*;

/**
 *  The Simulation class is a program that runs and animates a simulation of
 *  Foxes and Hounds.
 */
public class Simulation {
   // The constant CELL_SIZE determines the size of each cell on the 
   // screen during animation.  (You may change this if you wish.)
   private static final int CELL_SIZE = 20;
   private static final String USAGE_MESSAGE = "Usage: java Simulation "
           + "[--graphics] [--width int] [--height int] [--starvetime int] "
           + "[--fox float] [--hound float]";


   /**
    * Draws the current state of the field
    *
    * @param graphicsContext is an optional GUI window to draw to
    * @param theField is the object to display
    */
   private static void drawField(Graphics graphicsContext) {
       Field theField = Field.getInstance();
      // If we have a graphics context then update the GUI, otherwise
      // output text-based display
      if (graphicsContext != null) {
         // Iterate over the cells and draw the thing in that cell
         for (int i = 0; i < theField.getHeight(); i++) {
            for (int j = 0; j < theField.getWidth(); j++) {
               // Get the color of the object in that cell and set its color
               if (theField.isOccupied(j,i)) {
                  try {
                     graphicsContext.setColor
                        (theField.getOccupantAt(j,i).getDisplayColor());
                  }
                  catch (NullPointerException n) {
                      theField.hasChanged().set(true);
                  }
               }
               else {
                  graphicsContext.setColor(Color.white);
               }
               graphicsContext.fillRect(j * CELL_SIZE,
                                        i * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            } // for
         } // for
      }
      else { // No graphics, just text
         // Draw a line above the field
         for (int i = 0; i < theField.getWidth() * 2 + 1; i++) {
            System.out.print("-");
         }
         System.out.println();
         // For each cell, display the thing in that cell
         for (int i = 0; i < theField.getHeight(); i++) {
            System.out.print("|"); // separate cells with '|' 
            for (int j = 0; j < theField.getWidth(); j++) {
               if (theField.isOccupied(j,i)) {
                  System.out.print(theField.getOccupantAt(j,i)+"|");
               }
               else {
                  System.out.print(" |");
               }
            }
            System.out.println();
         } // for

         // Draw a line below the field
         for (int i = 0; i < theField.getWidth() * 2 + 1; i++) {
            System.out.print("-");
         }
         System.out.println();

      } // else
   } // drawField


   /**
    *  Main reads the parameters and performs the simulation and animation.
    */
   public static void main(String[] args) throws InterruptedException {
      /**
       *  Default parameters.  (You may change these if you wish.)
       */
      int width = 50;                              // Default width
      int height  = 25;                            // Default height
      int starveTime = Hound.DEFAULT_STARVE_TIME;  // Default starvation time
      double probabilityFox = 0.5;                 // Default fox probability 
      double probabilityHound = 0.15;              // Default hound probability
      boolean graphicsMode = false;
      Random randomGenerator = new Random();
      Field theField = null;
      Fox fox = null;
      Hound hound = null;

      // If we attach a GUI to this program, these objects will hold
      // references to the GUI elements
      Frame windowFrame = null;
      Graphics graphicsContext = null;
      Canvas drawingCanvas = null;

      /*
       *  Process the input parameters. Switches we understand include:
       *  --graphics for "graphics" mode
       *  --width 999 to set the "width" 
       *  --height 999 to set the height
       *  --starvetime 999 to set the "starve time"
       *  --fox 0.999 to set the "fox probability"
       *  --hound 0.999 to set the "hound probability"
       */
      for (int argNum=0; argNum < args.length; argNum++) {
         try {
            switch(args[argNum]) {
               case "--graphics":  // Graphics mode
                  graphicsMode = true;
                  break;

               case "--width": // Set width
                  width = Integer.parseInt(args[++argNum]);
                  break;

               case "--height": // set height
                  height = Integer.parseInt(args[++argNum]);
                  break;

               case "--starvetime": // set 'starve time'
                  starveTime = Integer.parseInt(args[++argNum]);
                  break;

               case "--fox": // set the probability for adding a fox
                  probabilityFox = Double.parseDouble(args[++argNum]);
                  break;

               case "--hound": // set the probability for adding a hound
                  probabilityHound = Double.parseDouble(args[++argNum]);
                  break;

               default: // Anything else is an error and we'll quit
                  System.err.println("Unrecognized switch.");
                  System.err.println(USAGE_MESSAGE);
                  System.exit(1);
            } // switch
         }
         catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.err.println("Illegal or missing argument.");
            System.err.println(USAGE_MESSAGE);
            System.exit(1);
         }
      } // for

      // Create the initial Field.
      theField = Field.getInstance(width, height);

      // Set the starve time for hounds
      Hound.setStarveTime(starveTime);

      // Visit each cell; randomly placing a Fox, Hound, or nothing in each.
      for (int i=0; i < theField.getWidth(); i++) {
         for (int j=0; j < theField.getHeight(); j++) {
            // If a random number is less than or equal to the probability
            // of adding a fox, then place a fox
            if (randomGenerator.nextFloat() <= probabilityFox) {
               fox = new Fox(i, j);
               theField.setOccupantAt(i, j, fox);
               new Thread(fox).start();
            } 
            // Otherwise if a random number is less than or equal to the 
            // probability of adding a hound, then place a hound. 
            else if (randomGenerator.nextFloat() <= probabilityHound) {
               hound = new Hound(i, j);
               theField.setOccupantAt(i, j, hound);
               new Thread(hound).start();
            }
         } // for
      } // for

      // If we're in graphics mode, then create the frame, canvas, 
      // and window. If not in graphics mode, these will remain null
      if (graphicsMode) {
         windowFrame = new Frame("Foxes and Hounds");
         windowFrame.setSize(theField.getWidth() * CELL_SIZE + 10, 
                             theField.getHeight() * CELL_SIZE + 30);
         windowFrame.setVisible(true);

         // Create a "Canvas" we can draw upon; attach it to the window.
         drawingCanvas = new Canvas();
         drawingCanvas.setBackground(Color.white);
         drawingCanvas.setSize(theField.getWidth() * CELL_SIZE, 
                               theField.getHeight() * CELL_SIZE);
         windowFrame.add(drawingCanvas);
         graphicsContext = drawingCanvas.getGraphics();
      } // if 

      // Draws the Field with its Occupants so they can be seen for just a
      // second before activating the simulation. (Personal preference)
      drawField(graphicsContext);
      Thread.sleep(1000);
      // Start by setting field to active and loop infinitely. We could
      // optionally stop when the Field becomes empty or full, though
      // there is no guarantee either of those will ever arise...
      theField.isActive().set(true);
      while (theField.isActive().get()) {
         // Draw the current state whenever it has been changed
         if (theField.hasChanged().getAndSet(false))
            drawField(graphicsContext);
      }
   } // main

} // Simulation.java