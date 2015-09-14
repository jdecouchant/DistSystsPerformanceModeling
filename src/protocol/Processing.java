package protocol;

/**
 * A class to modelize a processing operation made by a given entity. 
 */
public class Processing {

    private final int entityId;
    private final double nbCycles;
    
    public Processing(int entityId, double nbCycles) {
        this.entityId = entityId; 
        this.nbCycles = nbCycles;
    }
    
    public int getEntityId() {
        return entityId;
    }
    
    public double getNbCycles() {
        return nbCycles;
    }
  
}
