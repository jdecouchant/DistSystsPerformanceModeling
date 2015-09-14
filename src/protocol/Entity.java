package protocol;

public class Entity {

    private final int id; 
    private final String name; 
    
    private boolean cpuIdIsSet;
    private int cpuId; // Id of the cpu node on which the entity is hosted
    
    public Entity(int id, String name) {
        this.id = id;
        this.name = name;
        this.cpuIdIsSet = false;
    }
    
    public int getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public int getcpuId() {
        assert(cpuIdIsSet);
        return cpuId;
    }
    
    public void setcpuId(int cpuId) {
        cpuIdIsSet = true;
        this.cpuId = cpuId;
    }
    
    public void resetcpuId() {
        cpuIdIsSet = false;
        cpuId = -1;
    }

}
