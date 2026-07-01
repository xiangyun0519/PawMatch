package com.petadopt.common.exception;

import com.petadopt.common.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Result / BusinessException / ErrorCode 单元测试")
class ResultAndExceptionTest {

    @Test
    @DisplayName("Result.success(null)")
    void result_success_null() {
        Result<String> r = Result.success(null);
        assertThat(r.getCode()).isEqualTo(200);
        assertThat(r.getMessage()).isEqualTo("success");
        assertThat(r.getData()).isNull();
    }

    @Test
    @DisplayName("Result.success(data)")
    void result_success_data() {
        Result<String> r = Result.success("ok");
        assertThat(r.getData()).isEqualTo("ok");
        assertThat(r.getCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("Result.error(message) 默认 500")
    void result_error_default_500() {
        Result<String> r = Result.error("oops");
        assertThat(r.getCode()).isEqualTo(500);
        assertThat(r.getMessage()).isEqualTo("oops");
    }

    @Test
    @DisplayName("Result.error(code, message)")
    void result_error_custom_code() {
        Result<String> r = Result.error(404, "not found");
        assertThat(r.getCode()).isEqualTo(404);
        assertThat(r.getMessage()).isEqualTo("not found");
    }

    @Test
    @DisplayName("BusinessException 默认错误码 500")
    void business_exception_default() {
        BusinessException ex = new BusinessException("系统繁忙");
        assertThat(ex.getCode()).isEqualTo(500);
        assertThat(ex.getMessage()).isEqualTo("系统繁忙");
    }

    @Test
    @DisplayName("BusinessException 指定错误码")
    void business_exception_with_code() {
        BusinessException ex = new BusinessException(ErrorCode.NOT_FOUND, "宠物不存在");
        assertThat(ex.getCode()).isEqualTo(404);
        assertThat(ex.getMessage()).isEqualTo("宠物不存在");
    }

    @Test
    @DisplayName("ErrorCode 枚举值存在")
    void error_code_values() {
        assertThat(ErrorCode.SUCCESS.getCode()).isEqualTo(200);
        assertThat(ErrorCode.UNAUTHORIZED.getCode()).isEqualTo(401);
        assertThat(ErrorCode.FORBIDDEN.getCode()).isEqualTo(403);
        assertThat(ErrorCode.NOT_FOUND.getCode()).isEqualTo(404);
        assertThat(ErrorCode.INTERNAL_ERROR.getCode()).isEqualTo(500);
    }
}