package com.coecs.project012;

import java.util.List;
import java.util.Map;

public class User {
    private String email;
    private String password;

    private String firstName;
    private String lastName;

    private String uid;

    private String profileImagePath;

    private boolean workerMode;

    private WorkerProfile workerProfile;

    private Map<String, Conversation.Message> conversationIds;

    private Map<String,Review> reviews;

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

    public WorkerProfile getWorkerProfile() {
        return workerProfile;
    }

    public void setWorkerProfile(WorkerProfile workerProfile) {
        this.workerProfile = workerProfile;
    }

    public Map<String, Review> getReviews() {
        return reviews;
    }

    public void setReviews(Map<String, Review> reviews) {
        this.reviews = reviews;
    }

    public static class Review{
        private String uid;
        private String reviewContent;
        private int stars;
        private long submitDate;

        public Review() {
        }

        public Review(String uid, String reviewContent, int stars, long submitDate) {
            this.uid = uid;
            this.reviewContent = reviewContent;
            this.stars = stars;
            this.submitDate = submitDate;
        }

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public String getReviewContent() {
            return reviewContent;
        }

        public void setReviewContent(String reviewContent) {
            this.reviewContent = reviewContent;
        }

        public int getStars() {
            return stars;
        }

        public void setStars(int stars) {
            this.stars = stars;
        }

        public long getSubmitDate() {
            return submitDate;
        }

        public void setSubmitDate(long submitDate) {
            this.submitDate = submitDate;
        }
    }

    public Map<String, Conversation.Message> getConversationIds() {
        return conversationIds;
    }

    public void setConversationIds(Map<String, Conversation.Message> conversationIds) {
        this.conversationIds = conversationIds;
    }

    public static class WorkerProfile{

        private String mainService;
        private List<String> otherService;

        private List<EducationalAttainment> educations;
        private List<Experiences> experiences;
        private List<String> skills;
        private Location userLocation;

        private boolean termsAndAgreement = false;

        public WorkerProfile() {
        }

        public WorkerProfile(String mainService, List<String> otherService, List<EducationalAttainment> educations, List<Experiences> experiences, List<String> skills, Location userLocation) {
            this.mainService = mainService;
            this.otherService = otherService;
            this.educations = educations;
            this.experiences = experiences;
            this.skills = skills;
            this.userLocation = userLocation;
        }

        public boolean isTermsAndAgreement() {
            return termsAndAgreement;
        }

        public void setTermsAndAgreement(boolean termsAndAgreement) {
            this.termsAndAgreement = termsAndAgreement;
        }

        public String getMainService() {
            return mainService;
        }

        public void setMainService(String mainService) {
            this.mainService = mainService;
        }

        public List<String> getOtherService() {
            return otherService;
        }

        public void setOtherService(List<String> otherService) {
            this.otherService = otherService;
        }

        public List<EducationalAttainment> getEducations() {
            return educations;
        }

        public void setEducations(List<EducationalAttainment> educations) {
            this.educations = educations;
        }

        public List<Experiences> getExperiences() {
            return experiences;
        }

        public void setExperiences(List<Experiences> experiences) {
            this.experiences = experiences;
        }

        public List<String> getSkills() {
            return skills;
        }

        public void setSkills(List<String> skills) {
            this.skills = skills;
        }

        public Location getUserLocation() {
            return userLocation;
        }

        public void setUserLocation(Location userLocation) {
            this.userLocation = userLocation;
        }

    }

    public static class Location{
        private double lat;
        private double lng;

        public Location() {
        }

        public Location(double lat, double lng) {
            this.lat = lat;
            this.lng = lng;
        }

        public double getLat() {
            return lat;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public double getLng() {
            return lng;
        }

        public void setLng(double lng) {
            this.lng = lng;
        }
    }

    public static class EducationalAttainment{
        private String school;
        private String course;
        private String year;

        public EducationalAttainment() {
        }

        public EducationalAttainment(String school, String course, String year) {
            this.school = school;
            this.course = course;
            this.year = year;
        }

        public String getSchool() {
            return school;
        }

        public void setSchool(String school) {
            this.school = school;
        }

        public String getCourse() {
            return course;
        }

        public void setCourse(String course) {
            this.course = course;
        }

        public String getYear() {
            return year;
        }

        public void setYear(String year) {
            this.year = year;
        }

        public String toString(){
            return school + " - " + course + " - " + year;
        }
    }

    public static class Experiences{
        private String years;
        private String company;
        private String jobTitle;

        public Experiences() {
        }

        public Experiences(String years, String company, String jobTitle) {
            this.years = years;
            this.company = company;
            this.jobTitle = jobTitle;
        }

        public String getYears() {
            return years;
        }

        public void setYears(String years) {
            this.years = years;
        }

        public String getCompany() {
            return company;
        }

        public void setCompany(String company) {
            this.company = company;
        }

        public String getJobTitle() {
            return jobTitle;
        }

        public void setJobTitle(String jobTitle) {
            this.jobTitle = jobTitle;
        }

        public String toString(){
            return company + " - " + jobTitle + " - " + years;
        }
    }
}
