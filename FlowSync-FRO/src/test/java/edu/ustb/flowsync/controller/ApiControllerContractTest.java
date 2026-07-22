package edu.ustb.flowsync.controller;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ApiControllerContractTest {

    @Test
    void controllersUseRequiredApiPrefixes() throws Exception {
        assertControllerPrefix("AuthController", "/api/auth");
        assertControllerPrefix("ProjectController", "/api/projects");
        assertControllerPrefix("TaskController", "/api/tasks");
        assertControllerPrefix("TaskLogController", "/api/task-logs");
        assertControllerPrefix("TaskSummaryController", "/api/summaries");
        assertControllerPrefix("OverviewController", "/api/overview");
        assertControllerPrefix("UserController", "/api/users");
    }

    private void assertControllerPrefix(String simpleName, String expectedPrefix) throws Exception {
        Class<?> controllerClass = Class.forName("edu.ustb.flowsync.controller." + simpleName);

        RestController restController = controllerClass.getAnnotation(RestController.class);
        assertNotNull(restController, simpleName + " should be a REST controller");

        RequestMapping mapping = controllerClass.getAnnotation(RequestMapping.class);
        assertNotNull(mapping, simpleName + " should declare a base request mapping");
        assertArrayEquals(new String[]{expectedPrefix}, mapping.value());
    }
}
