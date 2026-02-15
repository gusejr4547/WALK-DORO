package com.walkdoro.global.error;

import com.walkdoro.global.error.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    @DisplayName("BusinessException을 처리하여 ErrorResponse를 반환한다")
    void handleBusinessException_ShouldReturnErrorResponse() {
        // given
        ErrorCode errorCode = ErrorCode.USER_NOT_FOUND;
        BusinessException exception = new BusinessException(errorCode);

        // when
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBusinessException(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(errorCode.getStatus());
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(errorCode.getCode());
        assertThat(response.getBody().getMessage()).isEqualTo(errorCode.getMessage());
    }

    @Test
    @DisplayName("MethodArgumentNotValidException을 처리하여 400 에러를 반환한다")
    void handleMethodArgumentNotValidException_ShouldReturnBadRequest() {
        // given
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Collections.singletonList(
                new FieldError("objectName", "field", "defaultMessage")));

        // when
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleMethodArgumentNotValidException(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(ErrorCode.INVALID_INPUT_VALUE.getCode());
    }

    @Test
    @DisplayName("IllegalArgumentException을 처리하여 400 에러를 반환한다")
    void handleIllegalArgumentException_ShouldReturnBadRequest() {
        // given
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");

        // when
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgumentException(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(ErrorCode.INVALID_INPUT_VALUE.getCode());
    }
}
