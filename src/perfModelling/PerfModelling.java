package perfModelling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import protocol.Communication;
import protocol.Processing;
import protocol.Protocol;
import protocol.UpRight;
import architecture.Architecture;

public class PerfModelling {

    // Architecture on which the protocol has to be deployed
    private Architecture architecture;
    private Protocol protocol;

    private int nbCPUs;
    private Integer[] CPUsId;

    private int curEntityIndex;
    private int nbEntities;
    private Integer[] entitiesId;

    // Store the best and the worst repartitions
    private double minThroughput, maxThroughput;
    private int nbCPUsMinThroughput, nbCPUsMaxThroughput; // Used to choose the configuration that use the lowest number of machines
    private HashMap<Integer, Integer> minThroughputRepartition, maxThroughputRepartition;
    private String minThroughputInfo, maxThroughputInfo;

    private double nbConfigurationsToSee, nbConfigurationsSeen;

    public PerfModelling(Architecture architecture, Protocol protocol) {
        this.architecture = architecture;
        this.protocol = protocol;

        nbCPUs = architecture.getProcessingNodesSize();
        CPUsId = architecture.getProcessingNodesId().toArray(new Integer[1]);

        curEntityIndex = 0;
        nbEntities = protocol.getEntitiesSize();
        entitiesId = protocol.getEntitiesIdSet().toArray(new Integer[1]);

        maxThroughput = Double.MIN_VALUE;
        minThroughput = Double.MAX_VALUE;
        nbCPUsMinThroughput = Integer.MAX_VALUE;
        nbCPUsMaxThroughput = Integer.MAX_VALUE;
        nbConfigurationsSeen = 0;
        nbConfigurationsToSee = Math.pow(nbCPUs, nbEntities);
    }

    public void printRepartition(HashMap<Integer, Integer> repartition) {
        for (int entityId : repartition.keySet()) {
            //            System.out.printf("(%d, %d)  ", entityId, repartition.get(entityId));
            System.out.printf("(%s, %s) ", protocol.getEntity(entityId).getName(), architecture.getProcessingNodeName(repartition.get(entityId)));
        }
        System.out.printf("\n");
    }

    public void updateMinMaxRepartitions(double throughput) {
        HashSet<Integer> cpuIds = new HashSet<Integer>();
        for (int entityId : protocol.getEntitiesIdSet())
            cpuIds.add(protocol.getEntity(entityId).getcpuId());

        // Update the worst config if its throughput is less 
        if (throughput < minThroughput || (throughput == minThroughput && cpuIds.size() < nbCPUsMinThroughput)) {
            minThroughput = throughput;

            nbCPUsMinThroughput = cpuIds.size();
            minThroughputRepartition = new HashMap<Integer, Integer>();
            for (int entityId : protocol.getEntitiesIdSet()) {
                minThroughputRepartition.put(entityId, protocol.getEntity(entityId).getcpuId());
            }
            minThroughputInfo = architecture.getThroughputInfo();
        } 

        if (throughput > maxThroughput || (throughput == maxThroughput && cpuIds.size() < nbCPUsMinThroughput)) {
            maxThroughput = throughput;

            nbCPUsMaxThroughput = cpuIds.size();
            maxThroughputRepartition = new HashMap<Integer, Integer>();
            for (int entityId : protocol.getEntitiesIdSet()) {
                maxThroughputRepartition.put(entityId, protocol.getEntity(entityId).getcpuId());
            }
            maxThroughputInfo = architecture.getThroughputInfo();
        }
    }

    public void printMinMaxRepartitions() {
        System.out.printf("\nStudied %.0f out of %.0f repartitions\n", nbConfigurationsSeen, nbConfigurationsToSee);
        if (minThroughputRepartition != null) {
            System.out.printf("Min throughput found : %.1f req/s using %d machines\n", minThroughput, nbCPUsMinThroughput);
            printRepartition(minThroughputRepartition);
            System.out.println(minThroughputInfo);
        }
        if (maxThroughputRepartition != null) {
            System.out.printf("Max throughput found : %.1f req/s using %d machines\n", maxThroughput, nbCPUsMaxThroughput);
            printRepartition(maxThroughputRepartition);
            System.out.println(maxThroughputInfo);
        }
    }

    public void addCommunicationLoadsOnArchitecture() {
        for (int entityId : entitiesId) {
            // Add communication loads on architecture
            ArrayList<Communication> communications = protocol.getCommunications(entityId);
            if (communications != null) {
                for (Communication communication : communications) {
                    int srcEntityId = communication.getSrcEntityId();
                    int dstEntityId = communication.getDstEntityId();
                    int srcNodeId = protocol.getEntity(srcEntityId).getcpuId();
                    int dstNodeId = protocol.getEntity(dstEntityId).getcpuId();
                    double communicationLoad = communication.getMsgSizeBytes();
                    architecture.addLoadOnLink(srcNodeId, dstNodeId, communicationLoad);
                }
            }
        }
    }

    public void removeCommunicationLoadsOnArchitecture() {
        for (int entityId : entitiesId) {
            // Add communication loads on architecture
            ArrayList<Communication> communications = protocol.getCommunications(entityId);
            for (Communication communication : communications) {
                int srcEntityId = communication.getSrcEntityId();
                int dstEntityId = communication.getDstEntityId();
                int srcNodeId = protocol.getEntity(srcEntityId).getcpuId();
                int dstNodeId = protocol.getEntity(dstEntityId).getcpuId();
                double communicationLoad = communication.getMsgSizeBytes();
                architecture.removeLoadOnLink(srcNodeId, dstNodeId, communicationLoad);
            }
        }
    }

    public void addCPULoadsOnArchitecture(int entityId) {
        // Add cpu loads on architecture
        ArrayList<Processing> processings = protocol.getProcessings(entityId);
        if (processings != null) {
            for (Processing processing : processings) {
                int srcEntityId = processing.getEntityId();
                int srcNodeId = protocol.getEntity(srcEntityId).getcpuId();
                double cpuLoad = processing.getNbCycles();
                architecture.addLoadOnProcessingNode(srcNodeId, cpuLoad);
            }
        }
    }

    public void removeCPULoadsOnArchitecture(int entityId) {
        // Add cpu loads on architecture
        ArrayList<Processing> processings = protocol.getProcessings(entityId);
        if (processings != null) {
            for (Processing processing : processings) {
                int srcEntityId = processing.getEntityId();
                int srcNodeId = protocol.getEntity(srcEntityId).getcpuId();
                double cpuLoad = processing.getNbCycles();
                architecture.removeLoadOnProcessingNode(srcNodeId, cpuLoad);
            }
        }
    }

    public void resetCommunicationLoadsOnArchitecture() {
        architecture.resetLoadsOnLinks();
    }

    public void enumerateRepartitions() {
        if (curEntityIndex == nbEntities) { // Final configuration

            if (nbConfigurationsSeen % 100000 == 0) {
                printMinMaxRepartitions();
            }

            addCommunicationLoadsOnArchitecture();

            double throughput = architecture.getThroughput() * protocol.getNbClients();
            updateMinMaxRepartitions(throughput);

            //            removeCommunicationLoadsOnArchitecture(); // TODO: it is enough to set the load on each link to 0
            resetCommunicationLoadsOnArchitecture();

            ++nbConfigurationsSeen;
        } else {
            for (Integer cpuId : CPUsId) {
                if (curEntityIndex == 0 || protocol.repartitionWillBeValid(entitiesId[curEntityIndex], cpuId)) {
                    int entityId = entitiesId[curEntityIndex];

                    protocol.affectEntityToProcessingNode(entityId, cpuId);
                    addCPULoadsOnArchitecture(entityId);
                    ++curEntityIndex;

                    enumerateRepartitions();

                    removeCPULoadsOnArchitecture(entityId);
                    protocol.removeEntityFromProcessingNode(entityId, cpuId);
                    --curEntityIndex;
                }
            }
        }
    }

    //    public void enumerateWithoutRecursivity() {
    //
    //        int nbAddedEntities;
    //        int[] entityToCPUIndex = new int[nbEntities];
    //        nbConfigurationsSeen = 0;
    //        
    //        nbAddedEntities = 0;
    //        while (true) {
    //            
    //            // if array is filled, find the next configuration
    //            if (nbAddedEntities == nbEntities) {
    //                
    //                // Remove the values that cannot be incremented
    //                while (nbAddedEntities > 0 && entityToCPUIndex[nbAddedEntities - 1] == nbCPUs - 1) {
    //                    int entityId = entitiesId[nbAddedEntities-1];
    //                    int cpuIndex = entityToCPUIndex[nbAddedEntities-1];
    //                    removeCPULoadsOnArchitecture(entityId);
    //                    protocol.removeEntityFromProcessingNode(entityId, CPUsId[cpuIndex]);
    //                    --nbAddedEntities;
    //                }
    //                
    //                // If all have been removed, all configurations have been studied
    //                if (nbAddedEntities == 0)
    //                    return;
    //                
    //                // Else, increment the cpu on which the last remaining entity is on
    //                entityToCPUIndex[nbAddedEntities-1]++;
    //                
    //                int entityId = entitiesId[nbAddedEntities-1];
    //                protocol.affectEntityToProcessingNode(entityId, CPUsId[entityToCPUIndex[nbAddedEntities-1]]);
    //                addCPULoadsOnArchitecture(entityId);
    //            }
    //            
    //             // Insert zeros to fill the array
    //            while (nbAddedEntities != nbEntities) { 
    //                entityToCPUIndex[nbAddedEntities++] = 0;
    //                int entityId = entitiesId[nbAddedEntities-1];
    //                protocol.affectEntityToProcessingNode(entityId, CPUsId[entityToCPUIndex[nbAddedEntities-1]]);
    //                addCPULoadsOnArchitecture(entityId);
    //            }
    //            
    //            
    //            if (nbConfigurationsSeen % 100000 == 0) {
    //                printMinMaxRepartitions();
    //            }
    //
    //            addCommunicationLoadsOnArchitecture();
    //            double throughput = architecture.getThroughput(false);
    //            updateMinMaxRepartitions(throughput);
    //            resetCommunicationLoadsOnArchitecture();
    //            
    //            ++nbConfigurationsSeen;
    //            
    ////            for (int i = 0; i < nbEntities; ++i) {
    ////                int entityId = entitiesId[i];
    ////                int cpuId = CPUsId[entityToCPUIndex[i]];
    ////                System.out.printf(" (%d, %d) ", entityId, cpuId);
    ////            }
    ////            System.out.printf("\n");
    //        }
    //    }

    public static void main(String[] args) {

        double startTime = (double) System.currentTimeMillis();

        Architecture architecture = Architecture.sci();
        Protocol protocol = new UpRight(1, 1, 1);
        PerfModelling perfModelling = new PerfModelling(architecture, protocol);

        perfModelling.enumerateRepartitions();

        System.out.printf("\nFinal results :");
        perfModelling.printMinMaxRepartitions();

        double endTime = (double) System.currentTimeMillis();
        System.out.printf("\nElapsed time : %.2fs\n", (endTime - startTime) / 1000.0);


    }

}
