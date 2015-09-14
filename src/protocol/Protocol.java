package protocol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class Protocol {
    
    protected HashMap<Integer, Entity> entities; // A mapping entityId to entity
    
    private HashMap<Integer, ArrayList<Communication>> communications; // A mapping from srcEntityId to communication
    private HashMap<Integer, ArrayList<Processing>> processings; // A mapping from entityId to its processings
    private int nbClients; 
    
    
    public Protocol() {
        entities = new HashMap<Integer, Entity>();
        communications = new HashMap<Integer, ArrayList<Communication>>();
        processings = new HashMap<Integer, ArrayList<Processing>>();
        nbClients = 1;
    }
    
    public int getNbClients() {
        return nbClients;
    }
    
    public void addEntity(Entity entity) {
        assert(entity != null && !entities.containsKey(entity.getId()));
        entities.put(entity.getId(), entity);
        communications.put(entity.getId(), new ArrayList<Communication>());
        processings.put(entity.getId(), new ArrayList<Processing>());
    }
    
    public void addCommunication(Communication communication) {
        assert(communication != null);
        int srcEntityId = communication.getSrcEntityId();
        if (communications.containsKey(srcEntityId)) {
            communications.get(srcEntityId).add(communication);
        } else {
            ArrayList<Communication> newEntityCommunications = new ArrayList<Communication>();
            newEntityCommunications.add(communication);
            communications.put(srcEntityId, newEntityCommunications);
        }
    }
    
    public void addProcessing(Processing processing) {
        assert(processing != null);   
        int entityId = processing.getEntityId();
        if (processings.containsKey(entityId)) {
            processings.get(entityId).add(processing);
        } else {
            ArrayList<Processing> newEntityProcessings = new ArrayList<Processing>();
            newEntityProcessings.add(processing);
            processings.put(entityId, newEntityProcessings);
        }
    }
    
    
    public int getEntitiesSize() {
        return entities.size();
    }
    
    public Set<Integer> getEntitiesIdSet() {
        return entities.keySet();
    }
    
    public Entity getEntity(int entityId) {
        return entities.get(entityId);
    }
    
    public ArrayList<Communication> getCommunications(int entityId) {
        return communications.get(entityId);
    }
    
    public ArrayList<Processing> getProcessings(int entityId) {
        return processings.get(entityId);
    }
    
    public void resetEntityProcessingNodes() {
        for (Entity entity : entities.values()) {
            entity.resetcpuId();
        }
    }
    
    public void affectEntityToProcessingNode(int entityId, int processingNodeId) {
        entities.get(entityId).setcpuId(processingNodeId);
    }
    
    public void removeEntityFromProcessingNode(int entityId) {
        entities.get(entityId).resetcpuId();
    }
    
    public void removeEntityFromProcessingNode(int entityId, int processingNodeId) {
        entities.get(entityId).resetcpuId();
    }
    
    public boolean repartitionWillBeValid(int entityId, int processingNodeId) {
        return true;
    }
    
    public static Protocol getExample() {
        Protocol protocol = new Protocol();
        
        protocol.addEntity(new Entity(1, "filter 0"));
        protocol.addEntity(new Entity(2, "filter 1"));
        protocol.addEntity(new Entity(3, "filter 3"));
        protocol.addEntity(new Entity(4, "filter 4"));
        
        protocol.addProcessing(new Processing(1, 2000));
        protocol.addCommunication(new Communication(1, 2, 1000));
        
        return protocol;
    }

    public static void main(String[] args) {
//        Protocol protocol = getExample();
        
    }

}
