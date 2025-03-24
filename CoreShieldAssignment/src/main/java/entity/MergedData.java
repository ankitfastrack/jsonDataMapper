package entity;

public class MergedData {
    public String id;
    public double latitude;
    public double longitude;
    public String type;
    public Double rating;
    public Integer reviews;

    public MergedData(Location location, Metadata metadata) {
        this.id = location.id;
        this.latitude = location.latitude;
        this.longitude = location.longitude;
        this.type = metadata.type;
        this.rating = metadata.rating;
        this.reviews = metadata.reviews;
    }
}
