package agpar.multifacet.pairwise;

public class PairwiseResult {
    public String user1Id;
    public String user2Id;
    public Double pcc;
    public Double socialJaccard;
    public boolean areFriends;
    public boolean friendsOfFriends;
    public Double itemJaccard;
    public Double categoryJaccard;

    public PairwiseResult(String user1Id,
                          String user2Id,
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
        return String.format("%s,%s,%f,%f,%d,%d,%f,%f",
                this.user1Id, this.user2Id, this.pcc, this.socialJaccard,
                areFriends, areFriendsOfFriends, this.itemJaccard, this.categoryJaccard);
    }

    public static PairwiseResult fromString(String commaSeparated) {
        String[] splitString = commaSeparated.split(",");
        return new PairwiseResult(
            splitString[0],
            splitString[1],
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
