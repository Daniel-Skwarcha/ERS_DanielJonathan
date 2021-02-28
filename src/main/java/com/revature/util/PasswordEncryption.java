package com.revature.util;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class PasswordEncryption {
    public static String encryptString(String str) {
        String bcryptHashString = BCrypt.withDefaults().hashToString(12, str.toCharArray());
        return bcryptHashString;
    }

    public static boolean verifyPassword(String pw, String encryptedPassword) {
        BCrypt.Result result = BCrypt.verifyer().verify(pw.toCharArray(), encryptedPassword);
        return result.verified;
    }
}
