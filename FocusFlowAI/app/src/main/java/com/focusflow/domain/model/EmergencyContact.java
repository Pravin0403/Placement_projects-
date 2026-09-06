package com.focusflow.domain.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

/**
 * Represents an emergency contact configured by the user.
 */
public class EmergencyContact {

    private final long id;
    @NonNull
    private final String name;
    @NonNull
    private final String contactType; // "phone", "message", etc.
    @NonNull
    private final String contactValue;
    private final boolean isPriority;

    public EmergencyContact(
            long id,
            @NonNull String name,
            @NonNull String contactType,
            @NonNull String contactValue,
            boolean isPriority
    ) {
        this.id = id;
        this.name = name;
        this.contactType = contactType;
        this.contactValue = contactValue;
        this.isPriority = isPriority;
    }

    public long id() {
        return id;
    }

    @NonNull
    public String name() {
        return name;
    }

    @NonNull
    public String contactType() {
        return contactType;
    }

    @NonNull
    public String contactValue() {
        return contactValue;
    }

    public boolean isPriority() {
        return isPriority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmergencyContact that = (EmergencyContact) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}