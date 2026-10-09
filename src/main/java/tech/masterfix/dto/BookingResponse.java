package tech.masterfix.dto;

import tech.masterfix.model.BookingStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class BookingResponse {

    private Long id;
    private String fullName;
    private String phone;
    private String email;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String applianceType;
    private String description;
    private LocalDate preferredDate;
    private LocalTime preferredTime;
    private BookingStatus status;
    private LocalDateTime createdAt;
    private String confirmationCode;
    private boolean ownerNotified;
    private String notifyChannel;
    private boolean emailSent;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }

    public String getApplianceType() { return applianceType; }
    public void setApplianceType(String applianceType) { this.applianceType = applianceType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getPreferredDate() { return preferredDate; }
    public void setPreferredDate(LocalDate preferredDate) { this.preferredDate = preferredDate; }

    public LocalTime getPreferredTime() { return preferredTime; }
    public void setPreferredTime(LocalTime preferredTime) { this.preferredTime = preferredTime; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getConfirmationCode() { return confirmationCode; }
    public void setConfirmationCode(String confirmationCode) { this.confirmationCode = confirmationCode; }

    public boolean isOwnerNotified() { return ownerNotified; }
    public void setOwnerNotified(boolean ownerNotified) { this.ownerNotified = ownerNotified; }

    public String getNotifyChannel() { return notifyChannel; }
    public void setNotifyChannel(String notifyChannel) { this.notifyChannel = notifyChannel; }

    public boolean isEmailSent() { return emailSent; }
    public void setEmailSent(boolean emailSent) { this.emailSent = emailSent; }
}
