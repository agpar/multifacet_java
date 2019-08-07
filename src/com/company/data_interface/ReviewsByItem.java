package com.company.data_interface;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class ReviewsByItem {
    private HashMap<String, ArrayList<com.company.data_interface.Review>> reviewsByItem = new HashMap<>();

    public void put(com.company.data_interface.Review review) {
        if (!this.reviewsByItem.containsKey(review.getItemId())) {
            ArrayList<com.company.data_interface.Review> newList = new ArrayList<>();
            this.reviewsByItem.put(review.getItemId(), newList);
        }
        this.reviewsByItem.get(review.getItemId()).add(review);
    }

    public ArrayList<com.company.data_interface.Review> get(String itemId) {
        if (!this.reviewsByItem.containsKey(itemId)) {
            return new ArrayList<com.company.data_interface.Review>();
        } else {
            return this.reviewsByItem.get(itemId);
        }
    }

    public Collection<ArrayList<Review>> values() {
        return this.reviewsByItem.values();
    }
}
