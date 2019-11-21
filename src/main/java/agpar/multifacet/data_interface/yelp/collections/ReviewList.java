package agpar.multifacet.data_interface.yelp.collections;

import agpar.multifacet.data_interface.yelp.data_classes.Review;

import java.util.*;

/* Holds the list of reviews for a user.

    Guaranteed to be sorted in item order.
    Guaranteed to be duplicate free.
 */
public class ReviewList implements Iterable<Review>{
    private Review[] reviews;
    private HashSet<Integer> itemsReviewed;

    public ReviewList(List<Review> reviews) {
        this.reviews = this.dedupeReviews(reviews);
    }

    private void initItemsReviewed(List<Review> reviews) {
        this.itemsReviewed = new HashSet<>();
        for (Review review : reviews) {
            this.itemsReviewed.add(review.getItemIdInt());
        }
    }

    private Review[] dedupeReviews(List<Review> reviews) {
        if (this.itemsReviewed == null) {
            this.initItemsReviewed(reviews);
        }
        reviews.sort(Comparator.comparingInt(Review::getItemIdInt));
        if (this.itemsReviewed.size() == reviews.size()) {
            return reviews.toArray(new Review[reviews.size()]);
        }

        int i = 0;
        int j = 0;
        Review[] dedupedReviews = new Review[this.itemsReviewed.size()];
        int currentItem = reviews.get(0).getItemIdInt();
        Review latestReviewForItem = reviews.get(0);
        while (i < reviews.size() - 1) {
            Review nextReview = reviews.get(i + 1);
            if (nextReview.getItemIdInt() == (currentItem)) {
                if(nextReview.getDate().compareTo(latestReviewForItem.getDate()) > 0) {
                    latestReviewForItem = nextReview;
                }
            } else {
                dedupedReviews[j] = (latestReviewForItem);
                j++;
                currentItem = nextReview.getItemIdInt();
                latestReviewForItem = nextReview;
            }
            i++;
        }
        dedupedReviews[j] = latestReviewForItem;
        return dedupedReviews;
    }

    public Set<Integer> getItemsReviewed() {
        return Collections.unmodifiableSet(this.itemsReviewed);
    }

    @Override
    public Iterator<Review> iterator() {
        return Arrays.asList(this.reviews).iterator();
    }
}
