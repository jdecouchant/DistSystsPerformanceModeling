package architecture;

public class NetworkNode {
    
    private final int id;
    private final NetworkNodeType type;
    
    NetworkNode(int id, NetworkNodeType type) {
        this.id = id;
        this.type = type;
    }
    
    public NetworkNode(int id) { // Used to create internal network nodes
        this.id = id;
        this.type = NetworkNodeType.SWITCH;
    }
    
    public int getId() {
        return id;
    }
    
    public NetworkNodeType getType() {
        return type; 
    }

    @Override
    public String toString() {
        return Integer.toString(id);
    }
    
    

}
