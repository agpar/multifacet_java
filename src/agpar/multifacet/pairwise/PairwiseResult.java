package agpar.multifacet.pairwise;

public class PairwiseResult {
    public String user1Id;
    public String user2Id;
    public Double pcc;
    public Double socialJaccard;
    public boolean areFriends;

    public PairwiseResult(String user1Id, String user2Id, Double pcc, double socialJaccard, boolean areFriends) {
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

    public static PairwiseResult fromString(String commaSeperated) {
        String[] splitString = commaSeperated.split(",");
        return new PairwiseResult(
            splitString[0],
            splitString[1],
            Double.parseDouble(splitString[2]),
            Double.parseDouble(splitString[3]),
            Integer.parseInt(splitString[4]) == 1
        );
    }

    public static String header() {
        return String.format("%s,%s,%s,%s,%s", "user1Id", "user2Id", "PCC", "socialJacc", "areFriends");
    }
}
