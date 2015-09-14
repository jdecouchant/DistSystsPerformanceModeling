package architecture;

public class NetworkProcessingNode extends NetworkNode {

    private final String nodeName;
    private final int nbCPUs;
    private final double cpuFreq;
    
    private double cpuLoad;
    
    public NetworkProcessingNode(int id, String nodeName, int nbCPUs, double cpuFreq) {
        super(id, NetworkNodeType.PROCESSING_NODE);
        this.nodeName = nodeName;
        this.nbCPUs = nbCPUs;
        this.cpuFreq = cpuFreq;
        this.cpuLoad = 0;
    }
    
    public double getCPULoad() {
        return cpuLoad;
    }
    
    public void addCPULoad(double load) {
        cpuLoad += load;
    }
    
    public void removeCPULoad(double load) {
        assert(cpuLoad >= load);
        cpuLoad -= load;
    }
    
    public void resetProcessingLoad() {
        cpuLoad = 0;
    }
    
    public double getThroughput() { // Nb requests per second
        if (cpuLoad == 0)
            return Double.MAX_VALUE;
        else
            return (nbCPUs * cpuFreq) / cpuLoad; 
    }
    
    public String getName() {
        return nodeName;
    }

    @Override
    public String toString() {
        return String.format("[id %d, %s]", getId(), nodeName);
    }
    
    
}
