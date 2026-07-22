package edu.ustb.flowsync.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MD5UtilTest {

    @Test
    void encryptReturnsStableLowercaseMd5() {
        assertEquals("e10adc3949ba59abbe56e057f20f883e", MD5Util.encrypt("123456"));
    }

    @Test
    void matchesComparesRawPasswordWithEncryptedPassword() {
        String encrypted = MD5Util.encrypt("123456");

        assertTrue(MD5Util.matches("123456", encrypted));
        assertFalse(MD5Util.matches("654321", encrypted));
    }
}
