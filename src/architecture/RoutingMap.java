package architecture;

import java.util.HashMap;
import java.util.Set;

public class RoutingMap {

    private HashMap<CoupleNodesId, Integer> routingMap; // For each node gives the following node on path towards destination

    public RoutingMap() {
        routingMap = new HashMap<CoupleNodesId, Integer>();
    }

    public boolean containsKey(int srcNodeId, int dstNodeId) {
        return routingMap.containsKey(new CoupleNodesId(srcNodeId, dstNodeId));
    }

    public int getSize() {
        return routingMap.size();
    }

    public int get(int srcNodeId, int dstNodeId) {
        try {
            assert(routingMap.containsKey(new CoupleNodesId(srcNodeId, dstNodeId)));
        } catch (AssertionError e) {
            System.out.println("Key ("+srcNodeId+", "+dstNodeId+") does not exist in routingMap");
        }
        return routingMap.get(new CoupleNodesId(srcNodeId, dstNodeId));
    }

    public void put(int srcNodeId, int dstNodeId, int goToNodeId) {
        routingMap.put(new CoupleNodesId(srcNodeId, dstNodeId), goToNodeId);
    }

    public void printRoutingMapKeys() {
        for (CoupleNodesId key : routingMap.keySet())
            System.out.println(key);
    }

    public Set<CoupleNodesId> getKeys() {
        return routingMap.keySet();
    }
}
