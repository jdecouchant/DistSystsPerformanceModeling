package architecture;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;

public class Architecture {

    private HashMap<Integer, NetworkNode> nodes; // nodeId to network node
    private HashMap<Integer, NetworkProcessingNode> processingNodes; // nodeId to processing node

    private HashMap<CoupleNodesId, NetworkLink> links; // Couple (src, dst) of nodes to next node on path from src to dst

    private RoutingMap routingMap;

    public Architecture() {
        nodes = new HashMap<Integer, NetworkNode>();
        processingNodes = new HashMap<Integer, NetworkProcessingNode>();
        links = new HashMap<CoupleNodesId, NetworkLink>();
        routingMap = new RoutingMap();
    }
    
    public String getProcessingNodeName(int nodeId) {
        return processingNodes.get(nodeId).getName();
    }

    public void addNewNode(NetworkNode node) {
        assert(!nodes.containsKey(node.getId()));
        nodes.put(node.getId(), node);
    }

    public void addNewNode(NetworkProcessingNode node) {
        assert(!processingNodes.containsKey(node.getId()) && !nodes.containsKey(node.getId()));
        nodes.put(node.getId(), node);
        if (node.getType() == NetworkNodeType.PROCESSING_NODE)
            processingNodes.put(node.getId(), node);
    }
    
    public void addLink(int srcNodeId, int dstNodeId, double bandwidth, double latency) { // Create a direct link from srcNodeId to dstNodeId, and update the routingMap accordingly
        assert(nodes.containsKey(srcNodeId) && nodes.containsKey(dstNodeId));
        CoupleNodesId coupleNodesId = new CoupleNodesId(srcNodeId, dstNodeId);
        NetworkLink link = new NetworkLink(bandwidth, latency);
        links.put(coupleNodesId, link);
//        assert(!routingMap.containsKey(srcNodeId, dstNodeId));
//        addRoute(srcNodeId, dstNodeId, dstNodeId);
//        assert(routingMap.containsKey(srcNodeId, dstNodeId));
    }

    public void addBidirectionalLink(int srcNodeId, int dstNodeId, double bandwidth, double latency) {
        addLink(srcNodeId, dstNodeId, bandwidth, latency);
        addLink(dstNodeId, srcNodeId, bandwidth, latency);
    }

    public boolean routingMapIsFilled() {
        for (int srcNodeId : nodes.keySet()) {
            for (int dstNodeId : nodes.keySet()) {
                if (!routingMap.containsKey(srcNodeId, dstNodeId)) {
                    System.err.println("Routing map does not contain the key ("+srcNodeId+", "+dstNodeId+")");
                    return false;
                }
            }
        }
        return true;
    }

    public int getRoutingMapValue(int srcNodeId, int dstNodeId) {
        return routingMap.get(srcNodeId, dstNodeId);
    }
    
    public int getRoutingMapSize() {
        return routingMap.getSize();
    }
    
    public void printRoutingMapWithNames() {
        for (CoupleNodesId coupleNodesId : routingMap.getKeys()) {
            int srcNodeId = coupleNodesId.getSrcNodeId();
            int dstNodeId = coupleNodesId.getDstNodeId();
            int withNodeId = routingMap.get(srcNodeId, dstNodeId);
            System.out.println("From " + nodes.get(srcNodeId).toString() + " to " + nodes.get(dstNodeId).toString() 
                    + " go to " + nodes.get(withNodeId).toString());
        }
    }

    public void addRoute(int srcNodeId, int dstNodeId, int goToNodeId) {
        assert(nodes.containsKey(srcNodeId) && nodes.containsKey(dstNodeId) && nodes.containsKey(goToNodeId)); // Nodes Ids exist   
        assert(!routingMap.containsKey(srcNodeId, dstNodeId)); // A route to this node does not yet exist
        routingMap.put(srcNodeId, dstNodeId, goToNodeId);
        assert(routingMap.containsKey(srcNodeId, dstNodeId)); // A route to this node exists
    }

    public NetworkLink getLinkOnPath(int srcNodeId, int dstNodeId) { // return next link on path from srcNodeId to dstNodeId
        assert(nodes.containsKey(srcNodeId) && nodes.containsKey(dstNodeId));
        int nextNodeIdOnPath =  routingMap.get(srcNodeId, dstNodeId);
        return links.get(new CoupleNodesId(srcNodeId, nextNodeIdOnPath));
    }
    
    public Set<Integer> getProcessingNodesId() {
        return processingNodes.keySet();
    }
    
    public int getProcessingNodesSize() {
        return processingNodes.size();
    }

    public void addLoadOnProcessingNode(int nodeId, double processingLoad) {
        assert(processingNodes.containsKey(nodeId));
        NetworkProcessingNode processingNode = processingNodes.get(nodeId);
        processingNode.addCPULoad(processingLoad);
    }
    
    public void removeLoadOnProcessingNode(int nodeId, double processingLoad) {
        assert(processingNodes.containsKey(nodeId));
        NetworkProcessingNode processingNode = processingNodes.get(nodeId);
        processingNode.removeCPULoad(processingLoad);
    }
    
    public void addLoadOnLink(int srcNodeId, int dstNodeId, double communicationLoad) {
        assert(nodes.containsKey(srcNodeId) && nodes.containsKey(dstNodeId));
        
        int curNodeId = srcNodeId;
        while (curNodeId != dstNodeId) { // No load is affected on a message sent from a node to itself
            int nextNodeId = routingMap.get(curNodeId, dstNodeId);
            CoupleNodesId coupleNodes = new CoupleNodesId(curNodeId, nextNodeId);
            assert(links.containsKey(coupleNodes));
            NetworkLink curLink = links.get(coupleNodes);
            curLink.addCommunicationLoad(communicationLoad);
            curNodeId = nextNodeId;
        }
    }
    
    public void removeLoadOnLink(int srcNodeId, int dstNodeId, double communicationLoad) {
        assert(nodes.containsKey(srcNodeId) && nodes.containsKey(dstNodeId));
        
        int curNodeId = srcNodeId;
        while (curNodeId != dstNodeId) { // No load is affected on a message sent from a node to itself
            int nextNodeId = routingMap.get(curNodeId, dstNodeId);
            CoupleNodesId coupleNodes = new CoupleNodesId(curNodeId, nextNodeId);
            assert(links.containsKey(coupleNodes));
            NetworkLink curLink = links.get(coupleNodes);
            curLink.removeCommunicationLoad(communicationLoad);
            curNodeId = nextNodeId;
        }
    }
    
    public void resetLoadsOnLinks() {
        for (NetworkLink link : links.values())
            link.resetCommunicationLoad();
    }
    
    public double getThroughput() {
        double throughput = Double.MAX_VALUE;
        for (NetworkProcessingNode processingNode : processingNodes.values()) {
            double nodeThroughput = processingNode.getThroughput();
            if (nodeThroughput != Double.MAX_VALUE && nodeThroughput <= throughput) {
                throughput = nodeThroughput;
            }
        }
        for (CoupleNodesId coupleNodesId : links.keySet()) {
            double linkThroughput = links.get(coupleNodesId).getThroughput();
            if (linkThroughput != Double.MAX_VALUE && linkThroughput <= throughput) {
                throughput = linkThroughput;
            }
        }
        return throughput;
    }
    
    public String getThroughputInfo() {
        double throughput = Double.MAX_VALUE;
        String s = "";        
        for (NetworkProcessingNode processingNode : processingNodes.values()) {
            double nodeThroughput = processingNode.getThroughput();
            if (nodeThroughput != Double.MAX_VALUE && nodeThroughput <= throughput) {
                if (processingNode.getThroughput() == throughput)
                    s += String.format("\tLimiting resource: node %s with %.2f req/s\n", processingNode.toString(), nodeThroughput); 
                else 
                    s = String.format("\tLimiting resource: node %s with %.2f req/s\n", processingNode.toString(), nodeThroughput); 
                throughput = nodeThroughput;
            }
        }
        for (CoupleNodesId coupleNodesId : links.keySet()) {
            double linkThroughput = links.get(coupleNodesId).getThroughput();
            if (linkThroughput != Double.MAX_VALUE && linkThroughput <= throughput) {
                if (linkThroughput == throughput)
                    s += String.format("\tLimiting resource: link from %d to %d with %.2f req/s\n", coupleNodesId.getSrcNodeId(), coupleNodesId.getDstNodeId(), 
                            linkThroughput);
                else 
                    s = String.format("\tLimiting resource: link from %d to %d with %.2f req/s\n", coupleNodesId.getSrcNodeId(), coupleNodesId.getDstNodeId(), 
                            linkThroughput);
                throughput = linkThroughput;
            }
        }
        return s;
    }
    
    public static Architecture getExample() {
        Architecture archi = new Architecture();

        archi.addNewNode(new NetworkProcessingNode(0, "Machine 0", 1, 3000001000.0));
        archi.addNewNode(new NetworkProcessingNode(4, "Machine 1", 1, 2000002000.0));
        archi.addNewNode(new NetworkProcessingNode(5, "Machine 2", 1, 3000003000.0));

        archi.addNewNode(new NetworkNode(1));
        archi.addNewNode(new NetworkNode(2));
        archi.addNewNode(new NetworkNode(3));

        archi.addBidirectionalLink(0, 1, 10100.0, 0.0);
        archi.addBidirectionalLink(1, 2, 100000020.0, 0.0);
        archi.addBidirectionalLink(1, 3, 700000300.0, 0.0);
        archi.addBidirectionalLink(2, 3, 300000400.0, 0.0);
        archi.addBidirectionalLink(2, 4, 600000500.0, 0.0);
        archi.addBidirectionalLink(3, 5, 500000060.0, 0.0);

        for (int i = 0; i <= 5; ++i)
            archi.addRoute(i, i, i);

        for (int i = 0; i <= 5; ++i) {
            if (i != 0 && i != 1)
                archi.addRoute(0, i, 1);
            if (i != 2 && i != 4)
                archi.addRoute(4, i, 2);
            if (i != 3 && i != 5)     
                archi.addRoute(5, i, 3);
        }

        archi.addRoute(1, 4, 2);
        archi.addRoute(1, 5, 3);
        archi.addRoute(2, 0, 1);
        archi.addRoute(2, 5, 3);
        archi.addRoute(3, 0, 1);
        archi.addRoute(3, 4, 2);
        
        return archi;
    }
    
    public static Architecture sevenCPUOneSwitch() {
        Architecture archi = new Architecture();

        archi.addNewNode(new NetworkProcessingNode(0, "Machine 0", 1, 3000000000.0));
        archi.addNewNode(new NetworkProcessingNode(1, "Machine 1", 4, 1000000000.0));
        archi.addNewNode(new NetworkProcessingNode(2, "Machine 2", 1, 3000000000.0));
        archi.addNewNode(new NetworkProcessingNode(3, "Machine 3", 1, 2800000000.0));
        archi.addNewNode(new NetworkProcessingNode(4, "Machine 4", 2, 1200000000.0));
        archi.addNewNode(new NetworkProcessingNode(5, "Machine 5", 1, 2500000000.0));
        archi.addNewNode(new NetworkProcessingNode(6, "Machine 6", 1, 3000000000.0));

        archi.addNewNode(new NetworkNode(7));

        for (int i = 0; i <= 6; ++i)
            archi.addBidirectionalLink(i, 7, 1000000000.0, 0.0);

        for (int i = 0; i <= 7; ++i)
            archi.addRoute(i, i, i);
        
        for (int src = 0; src <= 6; ++src) {
            for (int dst = 0; dst <= 7; ++dst) {
                if (src != dst)
                    archi.addRoute(src, dst, 7);
            }
        }
        
        for (int i = 0; i <= 6; ++i)
            archi.addRoute(7, i, i);

        assert(archi.routingMapIsFilled());
        
        return archi;
    }
    
    public static Architecture sci() {
        Architecture archi = new Architecture();
        
        int nodeId = 0;
        archi.addNewNode(new NetworkProcessingNode(nodeId++, "sci71", 8, 2394000000.0));
        archi.addNewNode(new NetworkProcessingNode(nodeId++, "sci72", 8, 2394000000.0));
        archi.addNewNode(new NetworkProcessingNode(nodeId++, "sci73", 8, 2394000000.0));
        archi.addNewNode(new NetworkProcessingNode(nodeId++, "sci74", 8, 2394000000.0));
        archi.addNewNode(new NetworkProcessingNode(nodeId++, "sci75", 8, 1500000000.0));
        archi.addNewNode(new NetworkProcessingNode(nodeId++, "sci76", 8, 1500000000.0));
        archi.addNewNode(new NetworkProcessingNode(nodeId++, "sci77", 8, 1500000000.0));
        //archi.addNewNode(new NetworkProcessingNode(nodeId++, "sci25", 4, 1863000000.0));
        
        // The switch
        int switchId = nodeId;
        archi.addNewNode(new NetworkNode(switchId));

        // All processing nodes are connected to the switch
        for (int i = 0; i < switchId; ++i)
            archi.addBidirectionalLink(i, switchId, 1000000000.0, 0.0);
        
        for (int src = 0; src < switchId; ++src) {
            for (int dst = 0; dst <= switchId; ++dst) {
                if (src != dst) // Go through the switch
                    archi.addRoute(src, dst, switchId);
                else // Local routes
                    archi.addRoute(src, dst, src);
            }
        }
        
        // Routes from the switch to nodes (including itself)
        for (int i = 0; i <= switchId; ++i)
            archi.addRoute(switchId, i, i);

        assert(archi.routingMapIsFilled());
        
        return archi;  
    }
    
    public static Architecture rennes() {
        Architecture archi = new Architecture();

        archi.addNewNode(new NetworkProcessingNode(0, "Paradent 0", 8, 2500000000.0));
        archi.addNewNode(new NetworkProcessingNode(1, "Paradent 1", 8, 2500000000.0));
        archi.addNewNode(new NetworkProcessingNode(2, "Paradent 2", 8, 2500000000.0));
        
        archi.addNewNode(new NetworkProcessingNode(3, "Paranoia 0", 20, 2200000000.0));
        archi.addNewNode(new NetworkProcessingNode(4, "Paranoia 1", 20, 2200000000.0));
        archi.addNewNode(new NetworkProcessingNode(5, "Paranoia 2", 20, 2200000000.0));
        
        archi.addNewNode(new NetworkProcessingNode(6, "Parapide 0", 8, 2930000000.0));
        archi.addNewNode(new NetworkProcessingNode(7, "Parapide 1", 8, 2930000000.0));
        archi.addNewNode(new NetworkProcessingNode(8, "Parapide 2", 8, 2930000000.0));
        
        archi.addNewNode(new NetworkProcessingNode(9, "Parapluie 0", 24, 1700000000.0));
        archi.addNewNode(new NetworkProcessingNode(10, "Parapluie 1", 24, 1700000000.0));
        archi.addNewNode(new NetworkProcessingNode(11, "Parapluie 2", 24, 1700000000.0));

        // The switch
        archi.addNewNode(new NetworkNode(12));

        // All processing nodes are connected to the switch
        for (int i = 0; i <= 11; ++i)
            archi.addBidirectionalLink(i, 12, 1000000000.0, 0.0);
        
        for (int src = 0; src <= 11; ++src) {
            for (int dst = 0; dst <= 12; ++dst) {
                if (src != dst) // Go through the switch
                    archi.addRoute(src, dst, 12);
                else // Local routes
                    archi.addRoute(src, dst, src);
            }
        }
        
        // Routes from the switch to nodes (including itself)
        for (int i = 0; i <= 12; ++i)
            archi.addRoute(12, i, i);

        assert(archi.routingMapIsFilled());
        
        return archi;
    }
    
    // TODO: not functional !
    public static Architecture readArchitecture(String configFileName) {
        Architecture architecture = new Architecture();

        File configFile = new File(configFileName);

        try {
            Scanner scanner = new Scanner(configFile);

            // Read processing nodes
            int nbProcessingNodes = Integer.decode(scanner.nextLine());
            for (int i = 0; i < nbProcessingNodes; ++i) {
                String[] values = scanner.nextLine().split(",");
                int id = Integer.decode(values[0]);
                String name = values[1];
                int nbCPUs = Integer.decode(values[2]);
                double cpuFreq = Double.parseDouble(values[3]);
                architecture.addNewNode(new NetworkProcessingNode(id, name, nbCPUs, cpuFreq));
            }

            // Read switch nodes
            int nbSwitchNodes = Integer.decode(scanner.nextLine());
            for (int i = 0; i < nbSwitchNodes; ++i) {
                int id = Integer.decode(scanner.nextLine());
                architecture.addNewNode(new NetworkNode(id));
            }

            // Read bidirectional links
            int nbBidirectionalNodes = Integer.decode(scanner.nextLine());
            for (int i = 0; i < nbBidirectionalNodes; ++i) {
                String[] values = scanner.nextLine().split(",");
                int srcNodeId = Integer.decode(values[0]);
                int dstNodeId = Integer.decode(values[1]);
                double bandwidth = Double.parseDouble(values[2]);
                double latency = Double.parseDouble(values[3]);
                architecture.addBidirectionalLink(srcNodeId, dstNodeId, bandwidth, latency);
            }

            // Read unidirectional links
            int nbUnidirectionalNodes = Integer.decode(scanner.nextLine());
            for (int i = 0; i < nbUnidirectionalNodes; ++i) {
                String[] values = scanner.nextLine().split(",");
                int srcNodeId = Integer.decode(values[0]);
                int dstNodeId = Integer.decode(values[1]);
                double bandwidth = Double.parseDouble(values[2]);
                double latency = Double.parseDouble(values[3]);
                architecture.addLink(srcNodeId, dstNodeId, bandwidth, latency); 
            }

            // TODO: not finished
            //            // Read routing map
            //            int nbRoutes = Integer.decode(scanner.nextLine());
            //            for (int i = 0; i < nbRoutes; ++i) {
            //                String[] values = scanner.nextLine().split(",");
            //            }

            scanner.close();
        } catch (FileNotFoundException e) {e.printStackTrace();}

        return architecture;
    }
    
    public static void main(String[] args) {
        Architecture archi = getExample();
        
        archi.addLoadOnLink(0, 4, 1000);
        archi.addLoadOnProcessingNode(0, 1000);

//        System.out.println("Number of keys : "+archi.getRoutingMapSize());
//
//        System.out.println("Map is filled : " + archi.routingMapIsFilled());
//        
//        archi.printRoutingMapWithNames();
//        
//        System.out.println("Throughput : " + archi.getThroughput() + " req/s");
        archi.getThroughput();
        System.out.println(archi.getThroughputInfo());
    }

}
