package agpar.multifacet.data_interface.data_classes;

public class Region {
    private String city;
    private String state;
    private int hash = 0;

    public Region(String city, String state) {
        this.city = city.toLowerCase().strip();
        this.state = state.toLowerCase().strip();
    }

    public int hashCode() {
        if (hash != 0)
            return hash;
        hash = (this.city.concat(this.state)).hashCode();
        return hash;
    }

    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other == null || this.getClass() != other.getClass())
            return false;

        Region otherRegion = (Region) other;
        return (this.city.equals(otherRegion.city) && this.state.equals(otherRegion.state));
    }
}
