import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *  The Field class defines an object that models a field full of foxes and
 *  hounds.
 */
public class Field {
   /**
    *  Creates an empty field of given width and height
    *  @param width of the field.
    *  @param height of the field.
    */
   private Field(int width, int height) {
      _occupants = new FieldOccupant[width][height];
      _hasChanged = new AtomicBoolean();
      _isActive = new AtomicBoolean();

      // Initializes an array of semaphores for access of occupants
      _occupantLocks = new Semaphore[width][height];
      for (int i = 0; i < width; i++) {
         for (int j = 0; j < height; j++) {
            _occupantLocks[i][j] = new Semaphore(1);
         }
      }
   } // Field


   /**
    * Creates an instance with the given width and height and/or
    * returns the instance of Field
    * @param width  the width to create the instance if one doesn't exist
    * @param height  the height to create the instance if one doesn't exist
    * @return the instance of the Field class
    */
   public static Field getInstance(int width, int height) {
      if (_instance == null) {
          _instance = new Field(width, height);
      }

      return _instance;
   } // getInstance


   /**
    * Creates an instance with default width of 50 and height of 25 and/or
    * returns the instance of Field
    * @return the instance of the Field class
    */
   public static Field getInstance() {
      if (_instance == null) {
          _instance = new Field(50, 25);
      }

      return _instance;
   } // getInstance


   /**
    *  @return the width of the field.
    */
   public int getWidth() {
       return _occupants.length;
   } // getWidth


   /**
    *  @return the height of the field.
    */
   public int getHeight() {
       return _occupants[0].length;
   } // getHeight


   /**
    *  @return the true if field and occupants are active and
    *          false if currently being set up
    */
   public AtomicBoolean isActive() {
       return _isActive;
   } // _isActive


   /**
    *  @return the true if field and occupants have been changed/updated
    */
   public AtomicBoolean hasChanged() {
       return _hasChanged;
   } // hasChanged


   /**
    *  Place an occupant in cell (x, y) and records that a change has been made.
    *
    *  @param x is the x-coordinate of the cell to place a mammal in.
    *  @param y is the y-coordinate of the cell to place a mammal in.
    *  @param toAdd is the occupant to place.
    */
   public void setOccupantAt(int x, int y, FieldOccupant toAdd) {
      _occupants[normalizeIndex(x, WIDTH_INDEX)]
                [normalizeIndex(y, !WIDTH_INDEX)] = toAdd;

      // The field and its occupants have changed
      _hasChanged.set(true);
   } // setOccupantAt


   /**
    *  @param x is the x-coordinate of the cell whose contents are queried.
    *  @param y is the y-coordinate of the cell whose contents are queried.
    *
    *  @return occupant of the cell (or null if unoccupied)
    */
   public FieldOccupant getOccupantAt(int x, int y) {
      return _occupants[normalizeIndex(x, WIDTH_INDEX)]
                       [normalizeIndex(y, !WIDTH_INDEX)];
   } // getOccupantAt


   /**
    *  @param x is the x-coordinate of the cell whose contents are queried.
    *  @param y is the y-coordinate of the cell whose contents are queried.
    *
    *  @return occupant of the cell (or null if unoccupied)
    */
   public Semaphore lockAt(int x, int y) {
      return _occupantLocks[normalizeIndex(x, WIDTH_INDEX)]
                       [normalizeIndex(y, !WIDTH_INDEX)];
   } // lockAt


   /**
    *  @param x is the x-coordinate of the cell whose contents are queried.
    *  @param y is the y-coordinate of the cell whose contents are queried.
    *
    *  @return true if the cell is occupied
    */
   public boolean isOccupied(int x, int y) {
      return getOccupantAt(x,y) != null;
   } // isOccupied


   /**
    * @return a collection of the occupants of cells adjacent to the
    * given cell; collection does not include null objects
    */
   public Set<FieldOccupant> getNeighborsOf(int x, int y) {
      // For any cell there are 8 neighbors - left, right, above, below,
      // and the four diagonals. Define a collection of offset pairs that 
      // we'll step through to access each of the 8 neighbors
      final int[][] indexOffsets = { {0,1}, {1,0}, {0,-1}, {-1, 0}, {1,1},
                                     {1, -1}, {-1, 1}, {-1, -1}
                                   };
      Set<FieldOccupant> neighbors = new HashSet<FieldOccupant>();

      // Iterate over the set of offsets, adding them to the x and y
      // indexes to check the neighboring cells
      for (int[] offset : indexOffsets) {
         // If there's something at that location, add it to our
         // neighbor set
         if (getOccupantAt(x + offset[0], y + offset[1]) != null) {
            neighbors.add(getOccupantAt(x + offset[0], y + offset[1]));
         }
      }

      return neighbors;
   } // getNeighborsOf


   /**
    * @return a collection of the empty cells adjacent to the given cell
    */
   public Set<int[]> getEmptyNeighborsOf(int x, int y) {
      // For any cell there are 8 neighbors - left, right, above, below,
      // and the four diagonals. Define a collection of offset pairs that 
      // we'll step through to access each of the 8 neighbors
      final int[][] indexOffsets = { {0,1}, {1,0}, {0,-1}, {-1, 0}, {1,1},
                                     {1, -1}, {-1, 1}, {-1, -1}
                                   };
      Set<int[]> emptyCells = new HashSet<int[]>();

      // Iterate over the set of offsets, adding them to the x and y
      // indexes to check the neighboring cells
      for (int[] offset : indexOffsets) {
         // Add to our emptyCell set
         if (!isOccupied(x + offset[0], y + offset[1])) {
            emptyCells.add(new int[]{x + offset[0], y + offset[1]});
         }
      }

      return emptyCells;
   } // getEmptyNeighborsOf


   /**
    * Normalize an index (positive or negative) by translating it to a legal
    * reference within the bounds of the field
    *
    * @param index to normalize
    * @param isWidth is true when normalizing a width reference, false if
    *                a height reference
    *
    * @return the normalized index value 
    */
   private int normalizeIndex(int index, boolean isWidthIndex) {
      int normalizedIndex;
      // Set the bounds depending on whether we're working with the
      // width or height (i.e., !width)
      int bounds = isWidthIndex ? getWidth() : getHeight();

      // If x is non-negative use modulo arithmetic to wrap around
      if (index >= 0) {
         normalizedIndex = index % bounds;
      }
      // For negative values we convert to positive, mod the bounds and
      // then subtract from the width (i.e., we count from bounds down to
      // 0. If we get say, -12 on a field 10 wide, we convert -12 to
      // 12, mod with 10 to get 2 and then subract that from 10 to get 8)
      else {
         normalizedIndex = bounds - (-index % bounds);
      }

      return normalizedIndex;
   } // normalizeIndex


   // Field object's array of Field Occupants
   private FieldOccupant[][] _occupants;

   // Field object's array of Occupant Locks for concurrency
   private Semaphore[][] _occupantLocks;

   // Boolean for knowing if the field has been changed
   private AtomicBoolean _hasChanged;

   // Boolean for knowing if the field and occupants are active (in simulation)
   private AtomicBoolean _isActive;

   // Singleton Instance of the field
   private static Field _instance;

   // Used in index normalizing method to distinguish between x and y indices
   private final static boolean WIDTH_INDEX = true;

} // Field.java