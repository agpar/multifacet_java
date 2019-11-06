"""
Filter files, only retaining users who have reviewed at least 20 items.
Also dedupes reviews (retaining only the latest review a user left for an item), which
may or may not be necessary for epinions.

Maps each ID to a contiguous ascending integer, which can later be used (without mapping)
to refer to that user in a matrix of user x item reviews.
"""
