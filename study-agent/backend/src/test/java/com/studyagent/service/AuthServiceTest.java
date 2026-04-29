package com.studyagent.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthServiceTest {

    @Test
    @DisplayName("相同密码加密后结果一致")
    void encryptPassword_sameInput_producesSameOutput() {
        String password = "Admin@123456";
        AuthService authService = new AuthService(null, null, null);

        String result1 = authService.encryptPassword(password);
        String result2 = authService.encryptPassword(password);

        assertThat(result1).isEqualTo(result2);
    }

    @Test
    @DisplayName("不同密码加密后结果不同")
    void encryptPassword_differentInputs_producesDifferentOutputs() {
        AuthService authService = new AuthService(null, null, null);

        String result1 = authService.encryptPassword("password1");
        String result2 = authService.encryptPassword("password2");

        assertThat(result1).isNotEqualTo(result2);
    }

    @Test
    @DisplayName("加密结果为32位十六进制字符串")
    void encryptPassword_returns32CharHexString() {
        AuthService authService = new AuthService(null, null, null);

        String result = authService.encryptPassword("anyPassword");

        assertThat(result).hasSize(32);
        assertThat(result).matches("^[0-9a-f]{32}$");
    }

    @Test
    @DisplayName("空密码加密正常处理")
    void encryptPassword_emptyPassword_works() {
        AuthService authService = new AuthService(null, null, null);

        String result = authService.encryptPassword("");

        assertThat(result).hasSize(32);
        assertThat(result).matches("^[0-9a-f]{32}$");
    }
}
