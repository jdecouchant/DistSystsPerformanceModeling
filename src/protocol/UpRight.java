package protocol;

import java.util.ArrayList;

public class UpRight extends Protocol {

    private final int nbFilters;
    private final int nbOrders;
    private final int nbExecs;

    private int firstFilterId, firstOrderId, firstExecId, firstClientId; // The primary has the id equal to firstOrderId
    private int nbClients;
    private int nbEntities;
    private ArrayList<Integer> filtersIdToCPU, ordersIdToCPU, execsIdToCPU; // Used to, for example, avoid placing two filter nodes on the same machine

    // Size of messages in Bytes
    private static final double sizeReqBytes = 273;
    private static final double sizeRepBytes = 80;
    private static final double nbRequestsInBatch = 30;
    private static final double sizePrePrepareBytes = 88+(nbRequestsInBatch*sizeReqBytes);
    private static final double sizePrepareBytes = 88;
    private static final double sizeCommitBytes = 72;
    private static final double sizeOrderedBatchBytes = 88+(sizeReqBytes * nbRequestsInBatch);

    // Processing counts in cycles 
    private static final double computeMAC = 644; 
    //    private static final double MAC_prepare = 827;
    //    private static final double MAC_commit = 795;
    //    private static final double MAC_reply = 795;

    private static final double send_req = 19 * sizeReqBytes + 643;
    private static final double send_pp = 19 * sizePrePrepareBytes + 643;
    private static final double send_prepare = 19 * sizePrepareBytes + 643;
    private static final double send_commit = 19 * sizeCommitBytes + 643;
    private static final double send_reply = 19 * sizeRepBytes + 643;
    private static final double send_batch = 19 * sizeOrderedBatchBytes + 643;

    private static final double receive_req = 20 * sizeReqBytes + 679;
    private static final double receive_pp = 20 * sizePrePrepareBytes + 679;
    private static final double receive_prepare = 20 * sizePrepareBytes + 679;
    private static final double receive_commit = 20 * sizeCommitBytes + 679;
    private static final double receive_batch = 20 * sizeOrderedBatchBytes + 679;

    private static final double execute_batch = 3000;

    public UpRight(int u, int r, int nbClients) {
        super();
        nbFilters = 2*u+r+1;
        nbOrders = 2*u+r+1;
        nbExecs = u+Math.max(u,  r) + 1;
        System.out.printf("%d filters\n", nbFilters);
        System.out.printf("%d orders\n", nbOrders);
        System.out.printf("%d execs\n", nbExecs);
        System.out.printf("%d clients\n", nbClients);

        int entityId = 0;
        firstFilterId = entityId;
        for (int i = 0; i < nbFilters; ++i, ++entityId) {
            entities.put(entityId, new Entity(entityId, "Filter "+i));
        }
        firstOrderId = entityId;
        for (int i = 0; i < nbOrders; ++i, ++entityId) {
            entities.put(entityId, new Entity(entityId, "Order "+i));
        }
        firstExecId = entityId;
        for (int i = 0; i < nbExecs; ++i, ++entityId) {
            entities.put(entityId, new Entity(entityId, "Exec "+i));
        }

        this.nbClients = nbClients;
        firstClientId = entityId;
        for (int i = 0; i < nbClients; ++i) { 
            entities.put(entityId, new Entity(entityId, "Client "+i));
            ++entityId;
        }

        nbEntities = entityId;

        addCommunicationsUpright();
        addProcessingsUpright();

        filtersIdToCPU = new ArrayList<Integer>();
        ordersIdToCPU = new ArrayList<Integer>();
        execsIdToCPU = new ArrayList<Integer>();
    }

    public boolean isFilter(int entityId) {
        assert(0 <= entityId && entityId < nbEntities);
        return 0 <= entityId && entityId < firstOrderId;
    }

    public boolean isOrder(int entityId) {
        assert(0 <= entityId && entityId < nbEntities);
        return firstOrderId <= entityId && entityId < firstExecId;
    }

    public boolean isExec(int entityId) {
        assert(0 <= entityId && entityId < nbEntities);
        return firstExecId <= entityId && entityId < firstClientId;
    }

    public boolean isClient(int entityId) {
        assert(0 <= entityId && entityId < nbEntities);
        return firstClientId <= entityId && entityId < nbEntities;
    }

    @Override
    public void resetEntityProcessingNodes() {
        for (Entity entity : entities.values()) {
            entity.resetcpuId();
        }
        filtersIdToCPU = new ArrayList<Integer>();
        ordersIdToCPU = new ArrayList<Integer>();
        execsIdToCPU = new ArrayList<Integer>();
    }

    @Override
    public void affectEntityToProcessingNode(int entityId, int processingNodeId) {
        entities.get(entityId).setcpuId(processingNodeId);
        if (isFilter(entityId)) {
            filtersIdToCPU.add(processingNodeId);
        } else if (isOrder(entityId)) {
            ordersIdToCPU.add(processingNodeId);
        } else if (isExec(entityId)) {
            execsIdToCPU.add(processingNodeId);
        }
    }

    @Override
    public void removeEntityFromProcessingNode(int entityId, int processingNodeId) {
        entities.get(entityId).resetcpuId();
        if (isFilter(entityId)) {
            filtersIdToCPU.remove(new Integer(processingNodeId));
        } else if (isOrder(entityId)) {
            ordersIdToCPU.remove(new Integer(processingNodeId));
        } else if (isExec(entityId)) {
            execsIdToCPU.remove(new Integer(processingNodeId));
        }
    }

    @Override
    public boolean repartitionWillBeValid(int entityId, int processingNodeId) {
        assert(0 <= entityId && entityId < nbEntities);
        if (isFilter(entityId)) {
            int size = filtersIdToCPU.size();
            if (size > 0) {
                return filtersIdToCPU.get(size-1) < processingNodeId && !filtersIdToCPU.contains(processingNodeId);
            }
            return true;
        } else if (isOrder(entityId)) {
            if (entityId == 0) // The primary has a special role
                return !ordersIdToCPU.contains(processingNodeId);
            else {
                int size = ordersIdToCPU.size();
                if (size > 1) { // it is necessary not to compare to the primary
                    return ordersIdToCPU.get(size-1) < processingNodeId && !ordersIdToCPU.contains(processingNodeId);
                } else {
                    return !ordersIdToCPU.contains(processingNodeId);
                }

            }
        } else if (isExec(entityId)) {
            int size = execsIdToCPU.size();
            if (size > 0) {
                return execsIdToCPU.get(size-1) < processingNodeId && !execsIdToCPU.contains(processingNodeId);
            }
            return true;
        } else if (isClient(entityId)) { // The client can be placed on any other machine
            return true;
        }
        return false;
    }

    public void broadcastMessageToFilters(int srcEntityId, double msgSizeInBytes) {
        for (int entityId = firstFilterId; entityId <= firstFilterId + nbFilters; ++entityId) {
            addCommunication(new Communication(srcEntityId, entityId, msgSizeInBytes));
        }
    }

    public void broadcastMessageToOrders(int srcEntityId, double msgSizeInBytes) {
        for (int entityId = firstOrderId; entityId < firstOrderId + nbOrders; ++entityId) {
            addCommunication(new Communication(srcEntityId, entityId, msgSizeInBytes));
        }
    }

    public void broadcastMessageToExecs(int srcEntityId, double msgSizeInBytes) {
        for (int entityId = firstExecId; entityId < firstExecId + nbExecs; ++entityId) {
            addCommunication(new Communication(srcEntityId, entityId, msgSizeInBytes));
        }
    }

    public void filtersExecuteProcessing(double processingCost) {
        for (int entityId = firstFilterId; entityId < firstFilterId + nbFilters; ++entityId) {
            addProcessing(new Processing(entityId, processingCost));
        }
    }

    public void ordersExecuteProcessing(double processingCost) {
        for (int entityId = firstOrderId; entityId < firstOrderId + nbOrders; ++entityId) {
            addProcessing(new Processing(entityId, processingCost));
        }
    }

    public void execsExecuteProcessing(double processingCost) {
        for (int entityId = firstExecId; entityId < firstExecId + nbExecs; ++entityId) {
            addProcessing(new Processing(entityId, processingCost));
        }
    }

    public void addCommunicationsUpright() {
        for (int clientId = firstClientId; clientId < nbEntities; ++clientId) {
            // The request is sent from the client to the filter nodes
            broadcastMessageToFilters(clientId, sizeReqBytes);

            // The filters send the request to all order nodes
            for (int entityId = firstFilterId; entityId < firstFilterId + nbFilters; ++entityId) {
                broadcastMessageToOrders(entityId, sizeReqBytes);
            }
            // The primary sends a pre-prepare to all order nodes
            broadcastMessageToOrders(firstOrderId, sizePrePrepareBytes / nbRequestsInBatch);
            // The order nodes send a prepare to all order nodes
            for (int entityId = firstOrderId; entityId < firstOrderId + nbOrders; ++entityId) {
                broadcastMessageToOrders(entityId, sizePrepareBytes / nbRequestsInBatch);
            }
            // The order nodes send a commit to all order nodes
            for (int entityId = firstOrderId; entityId < firstOrderId + nbOrders; ++entityId) {
                broadcastMessageToOrders(entityId, sizeCommitBytes / nbRequestsInBatch);
            }
            // The order nodes send the ordered batch to all exec nodes 
            for (int entityId = firstExecId; entityId < firstExecId + nbExecs; ++entityId) {
                broadcastMessageToExecs(entityId, sizeOrderedBatchBytes / nbRequestsInBatch);
            }

            // The answer is sent to the client from the exec nodes.
            for (int entityId = firstExecId; entityId < firstExecId + nbExecs; ++entityId) {
                addCommunication(new Communication(entityId, clientId, sizeRepBytes));
            }
        }
    }

    public void addProcessingsUpright() {
        for (int clientId = firstClientId; clientId < nbEntities; ++clientId) {
            // Filters receive the request from the client
            filtersExecuteProcessing(receive_req + computeMAC + send_req);
            // Filters send the request to the primary
            ordersExecuteProcessing(receive_req + (nbOrders+1) * computeMAC + send_req * nbOrders);
            // The primary receives the requests and sent it to all other nodes (message is discarded from the other order nodes)
            ordersExecuteProcessing(nbFilters * (receive_req + computeMAC));
            addProcessing(new Processing(firstOrderId, nbOrders * (computeMAC + send_pp)));
            // Orders receive the preprepare message and send a prepare message
            ordersExecuteProcessing(nbOrders * (receive_pp + 2*computeMAC + send_prepare));
            // Orders receive the prepare message and send a commit message
            ordersExecuteProcessing(nbOrders * (receive_prepare + 2*computeMAC + send_commit));
            // Orders receive the commit message and send an ordered batch of messages to the exec nodes
            ordersExecuteProcessing(nbOrders * (receive_commit + computeMAC) + nbExecs * (computeMAC + send_batch));
            // Execs receive the batch and treat it
            execsExecuteProcessing(receive_batch + nbOrders * computeMAC + execute_batch + send_reply);
            // TODO: The client's processing are not taken into account here. 
        }
    }

}
