package agpar.multifacet.pairwise;

public class PairwiseResults {
    public String user1Id;
    public String user2Id;
    public Double pcc;
    public double socialJaccard;
    public boolean areFriends;

    public PairwiseResults(String user1Id, String user2Id, Double pcc, double socialJaccard, boolean areFriends) {
        this.user1Id = user1Id;
        this.user2Id = user2Id;
        this.pcc = pcc;
        this.socialJaccard = socialJaccard;
        this.areFriends = areFriends;
    }

    public String toString() {
        int areFriends = this.areFriends ? 1 : 0;
        return String.format("%s,%s,%f,%f,%d", this.user1Id, this.user2Id, this.pcc, this.socialJaccard, areFriends);
    }
}
