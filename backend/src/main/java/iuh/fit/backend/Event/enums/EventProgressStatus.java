package iuh.fit.backend.Event.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum EventProgressStatus {
    UPCOMING,    // Chưa diễn ra
    ONGOING,     // Đang diễn ra
    COMPLETED;    // Đã diễn ra
    @JsonCreator
    public static EventProgressStatus fromString(String value) {
        if (value == null) return null;

        // Xử lý trim và chuyển đổi
        String normalizedValue = value.trim().toUpperCase();
        try {
            return EventProgressStatus.valueOf(normalizedValue);
        } catch (IllegalArgumentException e) {
            return null; // hoặc throw exception tùy yêu cầu
        }
    }
}