import java.awt.Color;
import java.util.Arrays;
import java.util.Random;

/**
 * Foxes can display themselves, breed, and die.
 * What else to life is there?
 */
public class Fox extends FieldOccupant implements Runnable {
   /**
    * Create a fox
    */
   public Fox(int x, int y) {
      _cell = new int[]{x, y};
      _alive = true;
   } // Fox


   /**
    * @return true if this Fox is alive and false otherwise
    */
    @Override
   public boolean isAlive() {
      return _alive;
   } // isAlive


   // This method sets this creature's alive status to false,
   // (brutally killing the animal object)
   @Override
   public void kill() {
      _alive = false;
      Field.getInstance().setOccupantAt(_cell[0], _cell[1], null);
   } // kill


   /**
    * @return the color to use for a cell occupied by a Fox
    */
   @Override
   public Color getDisplayColor() {
      return Color.green;
   } // getDisplayColor


   /**
    * @return the text representing a Fox
    */
   @Override
   public String toString() {
      return "F";
   } // toString


   /**
    * @return the occupant cell location on the field
    */
   @Override
   public int[] getCell() {
      return _cell;
   } // getCell


   /**
    * Runnable method of a Fox Field Occupant
    */
   @Override
   public void run() {
      // While the Fox is alive...
      while (_alive) {
         try {
            // If the Simulation is active...
            if (Field.getInstance().isActive().get()) {
               // Rest before our Fox starts the day
               Thread.sleep((new Random().nextInt(50)*10)+750);

               Field theField = Field.getInstance();
               int[][] locks = null;
               int[] emptyCell = null;
               FieldOccupant neighboringFox = null;
               int houndCount = 0;

               // Iterate over the neighbors and find empty cells
               // to explore and see if we can make a fox baby
               for (int[] emptyNeighbor : theField.
                            getEmptyNeighborsOf(_cell[0], _cell[1])) {
                  emptyCell = emptyNeighbor;
               } // for

               // If we see an empty cell, check if we can place a baby fox
               if (emptyCell != null && _alive) {
                  // Iterate over the neighbors and find foxes to mate with and
                  // the number of hounds to see if its safe
                  for (FieldOccupant neighbor : theField.
                            getNeighborsOf(emptyCell[0], emptyCell[1])) {
                     if (neighbor instanceof Fox && neighbor != this) {
                        neighboringFox = neighbor;
                     }
                     else if (neighbor instanceof Hound) {
                        houndCount++;
                     }
                  } // for

                  // Now we lock if there are less than 2 hounds nearby and mate
                  if (houndCount < 2 && neighboringFox != null && _alive &&
                                      neighboringFox.isAlive() && !theField.
                                      isOccupied(emptyCell[0], emptyCell[1])) {
                     // Lots of locks to sort and lock in total order through
                     // an anonymous inner class and the array of locks
                     locks = new int[][]
                        {_cell, neighboringFox.getCell(), emptyCell};
                     Arrays.sort(locks, (int[] lock1, int[] lock2) -> {
                         int result;
                         if (lock1[0] == lock2[0])
                             result =  lock2[1] - lock1[1];
                         else
                             result = lock2[0] - lock1[0];
                         return result;
                     });

                     for (int[] lock : locks) {
                         theField.lockAt(lock[0], lock[1]).acquire();
                     }

                     // check if fox can still make a baby after getting locks
                     if (neighboringFox.isAlive() && _alive && !theField.
                                      isOccupied(emptyCell[0], emptyCell[1])) {
                        Fox fox = new Fox(emptyCell[0], emptyCell[1]);
                        theField.setOccupantAt(emptyCell[0], emptyCell[1], fox);
                        new Thread(fox).start();
                     }

                     // Release locks!
                      for (int[] lock : locks) {
                          theField.lockAt(lock[0], lock[1]).release();
                      }
                  } // if there is another safe fox
               } // if there is an emptyCell
            } // if simulation is active
            else {
               Thread.sleep(3);
            }
         } // try
         catch (InterruptedException ex) { }

      } // while alive

   } // run


   // Boolean telling if this Field Occupant is alive or dead
   private Boolean _alive;

   // Will store the x and y values associated with the creature's location 
   private final int[] _cell;

} // Fox