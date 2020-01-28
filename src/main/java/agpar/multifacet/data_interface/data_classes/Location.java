package agpar.multifacet.data_interface.data_classes;

import com.google.gson.JsonObject;

public class Location {
    private int regionHash;

    private double lat;
    private double lon;

    public Location(String city, String state, double lat, double lon) {
        city = city.toLowerCase().strip();
        state = state.toLowerCase().strip();
        this.regionHash = (city.concat(state)).hashCode();

        this.lat = lat;
        this.lon = lon;
    }

    public static Location fromJson(JsonObject businessObj) {
        return new Location(
                businessObj.get("city").getAsString(),
                businessObj.get("state").getAsString(),
                businessObj.get("longitude").getAsDouble(),
                businessObj.get("latitude").getAsDouble()
        );
    }
    public boolean sameRegion(Location other) {
        return this.regionHash == other.regionHash;
    }

    public double distanceToInKm(Location other) {
        int R = 6371; // Radius of the earth in km
        double dLat = deg2rad(other.lat-this.lat);
        double dLon = deg2rad(other.lon-this.lon);

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(deg2rad(this.lat)) * Math.cos(deg2rad(other.lat)) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }

    private double deg2rad(double deg) {
        return deg * (Math.PI/180);
    }
}
