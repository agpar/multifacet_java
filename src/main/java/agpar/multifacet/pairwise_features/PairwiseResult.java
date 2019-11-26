package agpar.multifacet.pairwise_features;

public class PairwiseResult {
    public int user1Id;
    public int user2Id;
    public Double pcc;
    public Double socialJaccard;
    public boolean areFriends;
    public boolean friendsOfFriends;
    public Double itemJaccard;
    public Double categoryJaccard;

    public PairwiseResult(int user1Id,
                          int user2Id,
                          Double pcc,
                          double socialJaccard,
                          boolean areFriends,
                          boolean friendOfFriends,
                          double itemJaccard,
                          double categoryJaccard) {
        this.user1Id = user1Id;
        this.user2Id = user2Id;
        this.pcc = pcc;
        this.socialJaccard = socialJaccard;
        this.areFriends = areFriends;
        this.friendsOfFriends = friendOfFriends;
        this.itemJaccard = itemJaccard;
        this.categoryJaccard = categoryJaccard;
    }

    public String toString() {
        int areFriends = this.areFriends ? 1 : 0;
        int areFriendsOfFriends = this.friendsOfFriends ? 1 : 0;
        return String.format("%d,%d,%s,%s,%d,%d,%s,%s",
                this.user1Id, this.user2Id, doubleFmt(this.pcc), doubleFmt(this.socialJaccard),
                areFriends, areFriendsOfFriends, doubleFmt(this.itemJaccard), doubleFmt(this.categoryJaccard));
    }

    private static String doubleFmt(Double d) {
        final double threshold = 0.00000000000001;
        if (d == null) {
           return "null";
        }
        else if (Math.abs(d) < threshold) {
            return "0";
        } else {
            return String.format("%f", d);
        }
    }

    public static PairwiseResult fromString(String commaSeparated) {
        String[] splitString = commaSeparated.split(",");
        return new PairwiseResult(
            Integer.parseInt(splitString[0]),
            Integer.parseInt(splitString[1]),
            Double.parseDouble(splitString[2]),
            Double.parseDouble(splitString[3]),
            Integer.parseInt(splitString[4]) == 1,
            Integer.parseInt(splitString[5]) ==1,
            Double.parseDouble(splitString[6]),
            Double.parseDouble(splitString[7]));
    }

    public static String header() {
        return String.format("%s,%s,%s,%s,%s,%s,%s,%s", "user1Id", "user2Id", "PCC", "socialJacc", "areFriends", "areFriendsOfFriends", "itemJacc", "categoryJacc");
    }

    public boolean isEmpty() {
        return (pcc == null) && (!areFriends);
    }
}
