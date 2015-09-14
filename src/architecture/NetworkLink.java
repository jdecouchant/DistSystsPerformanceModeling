package architecture;

public class NetworkLink {
    
    private final double bandwidth; // In Bytes per second
    private final double latency; // In seconds
    
    private double sizeInBytes; 

    NetworkLink(double bandwidth, double latency) {
        this.bandwidth = bandwidth;
        this.latency = latency;
    }
    
    public double getBandwidth() {
        return bandwidth;
    }

    public double getLatency() {
        return latency;
    }
    
    public void resetCommunicationLoad() {
        sizeInBytes = 0;
    }
    
    public void addCommunicationLoad(double msgSizeInBytes) {
        assert(msgSizeInBytes > 0);
        sizeInBytes += msgSizeInBytes;
    }
    
    public void removeCommunicationLoad(double msgSizeInBytes) {
        assert(sizeInBytes + 1 >= msgSizeInBytes); // 1 to avoid floating error to cause errors
        sizeInBytes -= msgSizeInBytes;
        if (sizeInBytes < 0.0)
            sizeInBytes = 0.0;
    }
    
    public double getThroughput() { // Nb requests per second
        if (sizeInBytes == 0.0)
            return Double.MAX_VALUE;
        else
            return bandwidth / sizeInBytes;
    }

    @Override
    public String toString() {
        return "NetworkLink [bandwidth=" + bandwidth + ", latency=" + latency + "]";
    }
    
    
}
