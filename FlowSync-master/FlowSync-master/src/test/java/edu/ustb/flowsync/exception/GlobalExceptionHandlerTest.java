package edu.ustb.flowsync.exception;

import edu.ustb.flowsync.common.ApiResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class GlobalExceptionHandlerTest {

    @Test
    void handlesBusinessExceptionAsUnifiedFailureResponse() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ApiResponse<Object> response = handler.handleBusinessException(new BusinessException(400, "参数错误"));

        assertFalse(response.isSuccess());
        assertEquals(400, response.getCode());
        assertEquals("参数错误", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void handlesUnknownExceptionAsUnifiedFailureResponse() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ApiResponse<Object> response = handler.handleException(new RuntimeException("database down"));

        assertFalse(response.isSuccess());
        assertEquals(500, response.getCode());
        assertEquals("服务器异常，请联系管理员", response.getMessage());
        assertNull(response.getData());
    }
}
