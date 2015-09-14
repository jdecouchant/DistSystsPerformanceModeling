package protocol;

/**
 * Represents a communication between two protocol modules. 
 */
public class Communication {

    private final int srcEntityId;
    private final int dstEntityId; 
    
    private final double msgSizeBytes; 
    
    public Communication(int srcEntityId, int dstEntityId, double msgSizeBytes) {
        this.srcEntityId = srcEntityId;
        this.dstEntityId = dstEntityId;
        this.msgSizeBytes = msgSizeBytes;
    }
    
    public double getMsgSizeBytes() {
        return msgSizeBytes;
    }
    
    public int getSrcEntityId() {
        return srcEntityId;
    }
    
    public int getDstEntityId() {
        return dstEntityId;
    }
    
}
