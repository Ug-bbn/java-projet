package com.sgpa.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    @Test
    void fromString_validAdmin_returnsAdmin() {
        assertEquals(Role.ADMIN, Role.fromString("ADMIN"));
    }

    @Test
    void fromString_validUser_returnsUser() {
        assertEquals(Role.USER, Role.fromString("USER"));
    }

    @Test
    void fromString_caseInsensitive() {
        assertEquals(Role.ADMIN, Role.fromString("admin"));
    }

    @Test
    void fromString_null_returnsUser() {
        assertEquals(Role.USER, Role.fromString(null));
    }

    @Test
    void fromString_invalidValue_returnsUser() {
        assertEquals(Role.USER, Role.fromString("SUPERADMIN"));
    }

    @Test
    void getLabel_returnsCorrectLabel() {
        assertEquals("ADMIN", Role.ADMIN.getLabel());
        assertEquals("USER", Role.USER.getLabel());
    }

    @Test
    void toString_returnsLabel() {
        assertEquals("ADMIN", Role.ADMIN.toString());
    }
}
