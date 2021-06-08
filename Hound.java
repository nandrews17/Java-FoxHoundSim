import java.awt.Color;
import java.util.Arrays;
import java.util.Random;

/**
 * Hounds can display themselves, eat, breed, and die (from hunger).
 */
public class Hound extends FieldOccupant implements Runnable {
   /**
    * Create a hound 
    */
   public Hound(int x, int y) {
      _cell = new int[]{x, y};
      _alive = true;
      // Start out well-fed
      eats();
   } // Hound


   /**
    * @return true if this Hound is alive and false otherwise
    */
   @Override
   public boolean isAlive() {
      return _alive;
   } // isAlive


   /**
    * @return true if this Hound has starved to death
    */
   public boolean hasStarved() {
      return _fedStatus < 1;
   } // hasStarved


   /**
    * Make this Hound hungrier
    *
     * @param restTime time the hound has rested without food
    * @return true if the Hound has starved to death
    */
   public boolean getHungrier(int restTime) {
      // Decrease the fed status of this Hound
      _fedStatus -= restTime;
      return hasStarved();
   } // getHungrier


   // Reset the fed status of this Hound
   public void eats() {
      _fedStatus = _houndStarveTime*1000;
   } // eats


   // This method sets this creature's alive status to false
   // (brutally killing the animal object)
   @Override
   public void kill() {
      _alive = false;
      Field.getInstance().setOccupantAt(_cell[0], _cell[1], null);
   } // kill


   /**
    * @return the color to use for a cell occupied by a Hound
    */
   @Override
   public Color getDisplayColor() {
      return Color.red;
   } // getDisplayColor


   /**
    * @return the text representing a Hound
    */
   @Override
   public String toString() {
      return "H";
   } // toString


   /**
    * Sets the starve time for this class
    *
    * @param starveTime 
    */
   public static void setStarveTime(int starveTime) {
      _houndStarveTime = starveTime;
   } // setStarveTime


   /**
    * @return the starve time for Hounds
    */
   public static int getStarveTime() {
      return _houndStarveTime;
   } // getStarveTime


   /**
    * @return the occupant cell location on the field
    */
   @Override
   public int[] getCell() {
      return _cell;
   } // getCell


   /**
    * Runnable method of a Hound Occupant
    */
   @Override
   public void run() {
      // While the Hound is alive...
      while (_alive) {
         try {
            // If the Simulation is active...
            if (Field.getInstance().isActive().get()) {
               int restTime = (new Random().nextInt(50)*10)+750;

               // Rest befor our Hound starts the day
               Thread.sleep(restTime);
               // If it ate recently, the hounds lives on, otherwise it starves
               if (hasStarved()) {
                  kill();
               }

               else {
                  Field theField = Field.getInstance();
                  FieldOccupant neighboringHound = null;
                  FieldOccupant neighboringFox = null;
                  int[][] locks = null;
                  int[] foxCell = null;
                  int[] emptyCell = null;
                  int foxCount = 0;

                  // Iterate over the neighbors and find foxes to eat
                  for (FieldOccupant neighbor : theField.
                           getNeighborsOf(_cell[0], _cell[1])) {
                     if (neighbor instanceof Fox) {
                        neighboringFox = neighbor;
                     }
                  } // for

                  // If any of its neighbors is a Fox, then the Hound sees if
                  // it can eat the little sucker and if other hounds are
                  // nearby for the possibility of a hound baby too
                  if (neighboringFox != null && neighboringFox.isAlive()) {
                      foxCell = neighboringFox.getCell();
                      // Iterate over the neighbors of the fox to find a hound
                      for (FieldOccupant neighbor : theField.getNeighborsOf
                                                     (foxCell[0], foxCell[1])) {
                         if (neighbor instanceof Hound && neighbor != this) {
                            neighboringHound = neighbor;
                         }
                      } // for

                     // Lock cells of fox to see if the hound has a meal!
                     theField.lockAt(foxCell[0], foxCell[1]).acquire();
                     if (neighboringFox.isAlive()) {
                        neighboringFox.kill();
                        eats();
                        // See if fox had another hound nearby so we can
                        // make a new baby hound in the fox's place
                        if (neighboringHound != null &&
                                 neighboringHound.isAlive()) {
                           Hound hound = new Hound(foxCell[0], foxCell[1]);
                           theField.setOccupantAt(foxCell[0],
                                                  foxCell[1], hound);
                           new Thread(hound).start();
                        }
                     }
                     // If another hound got to the Fox first, it gets hungrier
                     else {
                        getHungrier(restTime);
                     }
                     // Releases the lock
                     theField.lockAt(foxCell[0], foxCell[1]).release();
                  } // if there's a fox

                  // If none of its neighbors is a Fox, it looks farther for
                  // food (neighbors of neighboring empty cells) and a
                  // second chance to make a new hound baby
                  else {
                      // Iterate over the neighbors and find empty cells
                      for (int[] emptyNeighbor : theField.
                              getEmptyNeighborsOf(_cell[0], _cell[1])) {
                         emptyCell = emptyNeighbor;
                      } // for

                      if (emptyCell != null) {
                         // Iterate over the neighbors and find foxes to eat and
                         // hounds to potentially make baby hounds with
                         for (FieldOccupant neighbor : theField.
                                  getNeighborsOf(emptyCell[0], emptyCell[1])) {
                            if (neighbor instanceof Fox) {
                               neighboringFox = neighbor;
                               foxCount++;
                            }
                            else if (neighbor instanceof Hound &&
                                                 neighbor != this) {
                               neighboringHound = neighbor;
                            }
                         } // for

                         // See if we have another hound and enough foxes to
                         // make a new baby hound in the empty cell
                         if (neighboringHound != null && foxCount > 1 &&
                                  neighboringFox.isAlive() &&
                                  !theField.isOccupied(emptyCell[0],
                                                       emptyCell[1])) {
                            foxCell = neighboringFox.getCell();
                            // Locks cells to sort and lock in total order with
                            // an anonymous inner class and the array of locks
                            locks = new int[][] {foxCell, emptyCell};
                            Arrays.sort(locks, (int[] lock1, int[] lock2) -> {
                               int result;
                               if (lock1[0] == lock2[0]) {
                                  result = lock2[1] - lock1[1];
                               } else {
                                  result = lock2[0] - lock1[0];
                               }
                               return result;
                            });

                            for (int[] lock : locks) {
                               theField.lockAt(lock[0], lock[1]).acquire();
                            }

                            if (neighboringFox.isAlive() &&
                                     neighboringHound.isAlive() && !theField
                                     .isOccupied(emptyCell[0], emptyCell[1])) {
                               neighboringFox.kill();
                               eats();
                               Hound hound = new Hound(emptyCell[0],
                                                       emptyCell[1]);
                               theField.setOccupantAt(emptyCell[0],
                                                      emptyCell[1], hound);
                               new Thread(hound).start();
                            }

                            else {
                               getHungrier(restTime);
                            }
                            // Releases the lock
                            for (int[] lock : locks) {
                               theField.lockAt(lock[0], lock[1]).release();
                            }
                         } // if enough hounds and foxes
                         else {
                            getHungrier(restTime);
                         }
                      } // if emptyCell
                      else {
                         getHungrier(restTime);
                      }
                  } // no fox

               } // not starved
            } // if simulation is active
            else {
                Thread.sleep(3);
            }
         } // try
         catch (InterruptedException ex) { }

      } // while alive

   } // run


   // Default starve time for Hounds
   public static final int DEFAULT_STARVE_TIME = 3;

   // Class variable for all hounds
   private static int _houndStarveTime = DEFAULT_STARVE_TIME;

   // Will store the x and y values associated with the creature's location 
   private final int[] _cell;

   // Instance attributes to keep track of how hungry we are
   private int _fedStatus;

   // Boolean telling if this Field Occupant is alive or dead
   private Boolean _alive;

} // Hound.java