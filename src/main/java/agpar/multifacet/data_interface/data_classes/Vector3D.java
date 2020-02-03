package agpar.multifacet.data_interface.data_classes;

public class Vector3D {
    private double[] points;

    public Vector3D(double x, double y, double z) {
        points = new double[3];
        points[0] = x;
        points[1] = y;
        points[2] = z;
    }

    public static Vector3D fromLatLong(double lat, double lon, double radius) {
        lat = Math.toRadians(lat);
        lon = Math.toRadians(lon);
        var x = radius * Math.cos(lat) * Math.cos(lon);
        var y = radius * Math.cos(lat) * Math.sin(lon);
        var z = radius * Math.sin(lat);
        return new Vector3D(x, y, z);
    }

    public Vector3D normalized() {
        var tot = getx() + gety() + getz();
        return new Vector3D(getx() / tot, gety() / tot, getz() / tot);
    }

    public double getx() {
        return points[0];
    }

    public double gety() {
        return points[1];
    }

    public double getz() {
        return points[2];
    }
}
