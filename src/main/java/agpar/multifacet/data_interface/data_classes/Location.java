package agpar.multifacet.data_interface.data_classes;

import com.google.gson.JsonObject;

import java.util.Collection;

public class Location {
    private Region region;
    private double lat;
    private double lon;
    private static int earthRadius = 6371;

    public Location(String city, String state, double lat, double lon) {
        this.region = new Region(city, state);
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

    public Region getRegion() {
        return this.region;
    }

    public double distanceToInKm(Location other) {
        double dLat = Math.toRadians(other.lat-this.lat);
        double dLon = Math.toRadians(other.lon-this.lon);

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(this.lat)) * Math.cos(Math.toRadians(other.lat)) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return earthRadius * c;
    }

    public static double distanceBetweenCenters(Collection<Location> loc1, Collection<Location> loc2) {
        var center1 = centerOfCollection(loc1);
        var center2 = centerOfCollection(loc2);
        return center1.distanceToInKm(center2);
    }

    private static Location centerOfCollection(Collection<Location> locs) {
        double xsum, ysum, zsum;
        xsum = ysum = zsum = 0;
        for (var loc : locs) {
            var vec = Vector3D.fromLatLong(loc.lat, loc.lon, earthRadius).normalized();
            xsum += vec.getx();
            ysum += vec.gety();
            zsum += vec.getz();
        }
        Vector3D center = new Vector3D(xsum, ysum, zsum).normalized();

        var lat = Math.acos(center.getz() / earthRadius);
        var lon = Math.atan2(center.getx(), center.gety());
        return new Location("", "", lat, lon);
    }
}
