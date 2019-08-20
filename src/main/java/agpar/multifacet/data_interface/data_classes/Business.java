package agpar.multifacet.data_interface.data_classes;

import agpar.multifacet.data_interface.io.IdStringToIntMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashSet;

public class Business {
    public Integer itemId;
    public HashSet<Integer> categories;

    public Business(Integer itemId, HashSet<Integer> categories) {
        this.itemId = itemId;
        this.categories = categories;
    }

    public static Business fromJson(JsonObject obj, IdStringToIntMap itemMap, IdStringToIntMap categoryIdMap) {
        Integer itemId = itemMap.getInt(obj.get("business_id").getAsString());
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
}
