package agpar.multifacet.data_interface.data_classes;

import agpar.multifacet.data_interface.yelp.IdStringToIntMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashSet;

public class Business {
    public Integer itemId;
    public HashSet<Integer> categories;
    private Location location;

    public Business(Integer itemId, HashSet<Integer> categories) {
        this.itemId = itemId;
        this.categories = categories;
    }

    public static Business fromJson(JsonObject obj, IdStringToIntMap categoryIdMap) {
        Integer itemId = obj.get("business_id").getAsInt();
        HashSet<Integer> categories = new HashSet<>();
        JsonElement jsonCategories = obj.get("categories");
        if (!jsonCategories.isJsonNull()) {
            String[] categoriesSplit = jsonCategories.getAsString().split(", ");
            for(String category : categoriesSplit) {
                categories.add(categoryIdMap.getInt(category));
            }
        }

        return new Business(itemId, categories);
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Region getRegion() {
        if (location == null) {
            return null;
        }
        return location.getRegion();
    }

    public boolean sameRegion(Business other) {
        if (this.location == null || other.location == null)
            return false;
        return this.location.getRegion() == other.location.getRegion();
    }

    public double distanceToInKm(Business other) {
        if (this.location == null || other.location == null)
            return -1;
        return this.location.distanceToInKm(other.location);
    }
}
