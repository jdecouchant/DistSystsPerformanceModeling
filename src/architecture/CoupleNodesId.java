package architecture;

public class CoupleNodesId {

    private final int srcNodeId;
    private final int dstNodeId;

    public CoupleNodesId(int srcNodeId, int dstNodeId) {
        this.srcNodeId = srcNodeId;
        this.dstNodeId = dstNodeId;
    }

    public int getSrcNodeId() {
        return srcNodeId;
    }

    public int getDstNodeId() {
        return dstNodeId;
    }
    
    @Override
    public String toString() {
        return "CoupleNodesId [srcNodeId=" + srcNodeId + ", dstNodeId="
                + dstNodeId + "]";
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + dstNodeId;
        result = prime * result + srcNodeId;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CoupleNodesId other = (CoupleNodesId) obj;
        if (dstNodeId != other.dstNodeId)
            return false;
        if (srcNodeId != other.srcNodeId)
            return false;
        return true;
    }

}
