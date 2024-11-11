package com.example.easypark.admin.ManageLocations;

public class ParkingLocation {
    private String imageUrl;
    private String latitude;
    private String longitude;
    private String locationDetails;
    private String locationName;
    private String locationType;
    private String numberOfSlots;

    // Default constructor required for Firebase
    public ParkingLocation() {
    }

    // Parameterized constructor
    public ParkingLocation(String imageUrl, String latitude, String longitude, String locationDetails, String locationName, String locationType, String numberOfSlots) {
        this.imageUrl = imageUrl;
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationDetails = locationDetails;
        this.locationName = locationName;
        this.locationType = locationType;
        this.numberOfSlots = numberOfSlots;
    }

    // Getters and Setters
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLocationDetails() {
        return locationDetails;
    }

    public void setLocationDetails(String locationDetails) {
        this.locationDetails = locationDetails;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getLocationType() {
        return locationType;
    }

    public void setLocationType(String locationType) {
        this.locationType = locationType;
    }

    public String getNumberOfSlots() {
        return numberOfSlots;
    }

    public void setNumberOfSlots(String numberOfSlots) {
        this.numberOfSlots = numberOfSlots;
    }
}
