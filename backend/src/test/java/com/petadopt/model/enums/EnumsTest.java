package com.petadopt.model.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("枚举转换单元测试")
class EnumsTest {

    @Test
    @DisplayName("UserRole.fromCode")
    void user_role_from_code() {
        assertThat(UserRole.fromCode("ADOPTER")).isEqualTo(UserRole.ADOPTER);
        assertThat(UserRole.fromCode("shelter")).isEqualTo(UserRole.SHELTER);
        assertThat(UserRole.fromCode(null)).isEqualTo(UserRole.ADOPTER);
        assertThat(UserRole.fromCode("UNKNOWN")).isEqualTo(UserRole.ADOPTER);
    }

    @Test
    @DisplayName("PetSpecies.fromCode")
    void pet_species_from_code() {
        assertThat(PetSpecies.fromCode("DOG")).isEqualTo(PetSpecies.DOG);
        assertThat(PetSpecies.fromCode("Cat")).isEqualTo(PetSpecies.CAT);
        assertThat(PetSpecies.fromCode(null)).isEqualTo(PetSpecies.OTHER);
        assertThat(PetSpecies.fromCode("WHALE")).isEqualTo(PetSpecies.OTHER);
    }

    @Test
    @DisplayName("PetSize.fromCode")
    void pet_size_from_code() {
        assertThat(PetSize.fromCode("SMALL")).isEqualTo(PetSize.SMALL);
        assertThat(PetSize.fromCode(null)).isEqualTo(PetSize.MEDIUM);
    }

    @Test
    @DisplayName("PetGender.fromCode")
    void pet_gender_from_code() {
        assertThat(PetGender.fromCode("MALE")).isEqualTo(PetGender.MALE);
        assertThat(PetGender.fromCode(null)).isEqualTo(PetGender.MALE);
    }

    @Test
    @DisplayName("HousingType.fromCode")
    void housing_type_from_code() {
        assertThat(HousingType.fromCode("SMALL_APARTMENT")).isEqualTo(HousingType.SMALL_APARTMENT);
        assertThat(HousingType.fromCode(null)).isEqualTo(HousingType.OTHER);
    }

    @Test
    @DisplayName("ActivityLevel.fromCode")
    void activity_level_from_code() {
        assertThat(ActivityLevel.fromCode("HIGH")).isEqualTo(ActivityLevel.HIGH);
        assertThat(ActivityLevel.fromCode(null)).isEqualTo(ActivityLevel.MODERATE);
    }

    @Test
    @DisplayName("PetExperience.fromCode")
    void pet_experience_from_code() {
        assertThat(PetExperience.fromCode("EXPERT")).isEqualTo(PetExperience.EXPERT);
        assertThat(PetExperience.fromCode(null)).isEqualTo(PetExperience.NONE);
    }

    @Test
    @DisplayName("ApplicationStatus.fromCode")
    void application_status_from_code() {
        assertThat(ApplicationStatus.fromCode("PENDING")).isEqualTo(ApplicationStatus.PENDING);
        assertThat(ApplicationStatus.fromCode(null)).isEqualTo(ApplicationStatus.PENDING);
    }

    @Test
    @DisplayName("ShelterType.fromCode")
    void shelter_type_from_code() {
        assertThat(ShelterType.fromCode("NGO")).isEqualTo(ShelterType.NGO);
        assertThat(ShelterType.fromCode(null)).isEqualTo(ShelterType.OTHER);
    }

    @Test
    @DisplayName("PetStatus.fromCode")
    void pet_status_from_code() {
        assertThat(PetStatus.fromCode("AVAILABLE")).isEqualTo(PetStatus.AVAILABLE);
        assertThat(PetStatus.fromCode(null)).isEqualTo(PetStatus.AVAILABLE);
    }
}