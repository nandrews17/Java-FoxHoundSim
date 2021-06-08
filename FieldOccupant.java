import java.awt.Color;

/**
 * Abstract parent class for objects that can occupy a cell in the Field
 */
public abstract class FieldOccupant {
   /**
    * @return true if this Fox is alive
    */
   abstract public boolean isAlive();


   // This method sets this creature's alive status to false,
   // brutally killing the animal object
   abstract public void kill();


   /**
    * @return the occupant cell location on the field
    */
   abstract public int[] getCell();


   /**
    * @return the color to use for a cell containing a particular kind
    *         of occupant
    */
   abstract public Color getDisplayColor();

} // FieldOccupant