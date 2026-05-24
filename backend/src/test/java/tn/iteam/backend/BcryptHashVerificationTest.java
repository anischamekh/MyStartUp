package tn.iteam.backend;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Keeps the documented seed password ("password") in sync with {@code sql/emp_db_full.sql}.
 * Run {@code mvn test -Dtest=BcryptHashVerificationTest#printFreshHashForSqlScript} to print a new hash if you rotate the seed password.
 */
class BcryptHashVerificationTest {

    /** BCrypt of plain text "password" — must match INSERTs in sql/emp_db_full.sql */
    static final String PASSWORD_BCRYPT =
            "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi";

    @Test
    void sqlScriptHashMatchesPassword() {
        BCryptPasswordEncoder enc = new BCryptPasswordEncoder();
        assertTrue(enc.matches("password", PASSWORD_BCRYPT), "Seed hash must verify for 'password'");
    }

    @Test
    @Disabled("Enable locally to print a new BCrypt hash for sql/emp_db_full.sql")
    void printFreshHashForSqlScript() {
        String h = new BCryptPasswordEncoder().encode("password");
        System.out.println("UPDATE users SET password = '" + h + "' WHERE ... -- for sql/emp_db_full.sql");
        assertTrue(new BCryptPasswordEncoder().matches("password", h));
    }
}
