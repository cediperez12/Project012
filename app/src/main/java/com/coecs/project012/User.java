package com.coecs.project012;

public class User {
    private String email;
    private String password;

    private String firstName;
    private String lastName;

    private String uid;

    private String profileImagePath;

    private boolean workerMode;

    private EducationalAttainment[] educations;
    private Experiences[] experiences;
    private String[] skills;
    private Location userLocation;

    public User(String email, String password, String firstName, String lastName, String uid, String profileImagePath) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.uid = uid;
        this.profileImagePath = profileImagePath;
    }

    public User(String uid){
        this.uid = uid;
    }

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public User(){

    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getProfileImagePath() {
        return profileImagePath;
    }

    public void setProfileImagePath(String profileImagePath) {
        this.profileImagePath = profileImagePath;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public boolean isWorkerMode() {
        return workerMode;
    }

    public void setWorkerMode(boolean workerMode) {
        this.workerMode = workerMode;
    }

    public EducationalAttainment[] getEducations() {
        return educations;
    }

    public void setEducations(EducationalAttainment[] educations) {
        this.educations = educations;
    }

    public Experiences[] getExperiences() {
        return experiences;
    }

    public void setExperiences(Experiences[] experiences) {
        this.experiences = experiences;
    }

    public String[] getSkills() {
        return skills;
    }

    public void setSkills(String[] skills) {
        this.skills = skills;
    }

    public Location getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(Location userLocation) {
        this.userLocation = userLocation;
    }

    private class Location{
        private long lat;
        private long lng;
    }

    private class EducationalAttainment{
        private String school;
        private String course;
        private String attainment;
        private String year;
    }

    private class Experiences{
        private String years;
        private String company;
        private String jobTitle;
    }
}