package com.company.data_interface;

import java.util.*;

public class ReviewList  {
    private Review[] reviews;
    private HashSet<String> itemsReviewed;

    public ReviewList(List<Review> reviews) {
        this.reviews = this.dedupeReviews(reviews);
    }

    private void initItemsReviewed(List<Review> reviews) {
        this.itemsReviewed = new HashSet<>();
        for (Review review : reviews) {
            this.itemsReviewed.add(review.getItemId());
        }
    }

    private Review[] dedupeReviews(List<Review> reviews) {
        if (this.itemsReviewed == null) {
            this.initItemsReviewed(reviews);
        }
        reviews.sort((review1, review2) -> review1.getItemId().compareTo(review2.getItemId()));
        if (this.itemsReviewed.size() == reviews.size()) {
            reviews.toArray();
        }

        int i = 0;
        int j = 0;
        Review[] dedupedReviews = new Review[this.itemsReviewed.size()];
        String currentItem = reviews.get(0).getItemId();
        Review latestReviewForItem = reviews.get(0);
        while (i < reviews.size() - 1) {
            Review nextReview = reviews.get(i + 1);
            if (nextReview.getItemId().equals(currentItem)) {
                if(nextReview.getDate().compareTo(latestReviewForItem.getDate()) > 0) {
                    latestReviewForItem = nextReview;
                }
            } else {
                dedupedReviews[j] = (latestReviewForItem);
                j++;
                currentItem = nextReview.getItemId();
                latestReviewForItem = nextReview;
            }
            i++;
        }
        dedupedReviews[j] = latestReviewForItem;
        return dedupedReviews;
    }

    public Set<String> getItemsReviewed() {
        return Collections.unmodifiableSet(this.itemsReviewed);
    }
}
