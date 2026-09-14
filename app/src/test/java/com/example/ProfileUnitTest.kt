package com.example

import com.example.data.model.UserProfileEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileUnitTest {

    @Test
    fun testUserProfileProtection() {
        val unprotectedUser = UserProfileEntity(
            id = 1L,
            name = "Misafir",
            password = "",
            pin = "",
            isGuest = true
        )
        assertFalse(unprotectedUser.isProtected)
        assertEquals("Misafir", unprotectedUser.displayName)

        val pinProtectedUser = UserProfileEntity(
            id = 2L,
            name = "Ahmet",
            pin = "1234",
            password = "",
            isGuest = false
        )
        assertTrue(pinProtectedUser.isProtected)
        assertEquals("Ahmet", pinProtectedUser.displayName)

        val passwordProtectedUser = UserProfileEntity(
            id = 3L,
            name = "Mehmet",
            pin = "",
            password = "SecurePassword",
            isGuest = false
        )
        assertTrue(passwordProtectedUser.isProtected)
    }

    @Test
    fun testGuestModeProperties() {
        val guest = UserProfileEntity(
            id = 4L,
            name = "Misafir Kullanıcı",
            isGuest = true
        )
        assertTrue(guest.isGuest)
        assertFalse(guest.isProtected)
    }
}
