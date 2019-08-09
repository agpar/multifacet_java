package com.company.data_interface;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class ReviewsById {
    private HashMap<String, ArrayList<Review>> reviewsByItem = new HashMap<>();

    public void put(String key, Review review) {
        if (!this.reviewsByItem.containsKey(key)) {
            ArrayList<Review> newList = new ArrayList<>();
            this.reviewsByItem.put(key, newList);
        }
        this.reviewsByItem.get(key).add(review);
    }

    public ArrayList<Review> get(String itemId) {
        if (!this.reviewsByItem.containsKey(itemId)) {
            return new ArrayList<Review>();
        } else {
            return this.reviewsByItem.get(itemId);
        }
    }

    public Collection<ArrayList<Review>> values() {
        return this.reviewsByItem.values();
    }
}
