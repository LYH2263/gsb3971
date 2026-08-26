package com.hanyu.learning;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldRegisterAndLogin() throws Exception {
        Map<String, Object> register = new HashMap<>();
        register.put("phone", "13800009999");
        register.put("password", "Pass1234");
        register.put("realName", "测试用户");
        register.put("age", 30);
        register.put("gender", 1);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.phone").value("13800009999"));

        Map<String, String> login = new HashMap<>();
        login.put("phone", "13800009999");
        login.put("password", "Pass1234");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    void shouldRejectWrongPassword() throws Exception {
        Map<String, String> login = new HashMap<>();
        login.put("phone", "13800000001");
        login.put("password", "wrong123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(40101));
    }

    @Test
    void shouldReturn401ForProtectedApiWithoutToken() throws Exception {
        mockMvc.perform(get("/api/customers"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(40102));
    }

    @Test
    void shouldCheckinAndRejectOccupiedBed() throws Exception {
        String token = loginAndGetToken("13800000002", "Staff@123");

        Map<String, Object> createCustomer = new HashMap<>();
        createCustomer.put("name", "王五");
        createCustomer.put("phone", "13900000003");
        createCustomer.put("age", 68);
        createCustomer.put("gender", 1);
        createCustomer.put("note", "测试入营");

        MvcResult createResult = mockMvc.perform(post("/api/customers")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createCustomer)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andReturn();

        Long customerId = objectMapper.readTree(createResult.getResponse().getContentAsString())
            .path("data").path("id").asLong();

        Map<String, Object> checkin1 = new HashMap<>();
        checkin1.put("action", "checkin");
        checkin1.put("actionDate", "2026-02-24");
        checkin1.put("bedId", 1);

        mockMvc.perform(patch("/api/customers/1/lifecycle")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkin1)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("RESIDENT"));

        Map<String, Object> checkin2 = new HashMap<>();
        checkin2.put("action", "checkin");
        checkin2.put("actionDate", "2026-02-24");
        checkin2.put("bedId", 1);

        mockMvc.perform(patch("/api/customers/" + customerId + "/lifecycle")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkin2)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value(40902))
            .andExpect(jsonPath("$.message").value("铺位已占用"));
    }

    @Test
    void shouldRejectOutingForDischargedCustomer() throws Exception {
        String token = loginAndGetToken("13800000002", "Staff@123");

        Map<String, Object> outing = new HashMap<>();
        outing.put("action", "outing");
        outing.put("actionDate", "2026-02-24");

        mockMvc.perform(patch("/api/customers/2/lifecycle")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(outing)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value(40903));
    }

    @Test
    void shouldSaveAndLoadWeeklyMenu() throws Exception {
        String token = loginAndGetToken("13800000002", "Staff@123");
        Map<String, String> menu = new HashMap<>();
        menu.put("mon", "米饭+鱼");
        menu.put("tue", "面条");
        menu.put("wed", "饺子");
        menu.put("thu", "粥");
        menu.put("fri", "鸡肉");
        menu.put("sat", "牛肉");
        menu.put("sun", "面包");

        mockMvc.perform(put("/api/meals/weekly-menus/2026-02-23")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(menu)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.mon").value("米饭+鱼"));

        mockMvc.perform(get("/api/meals/weekly-menus/2026-02-23")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.sun").value("面包"));
    }

    @Test
    void shouldCreateCareRecordAndSortByDateDesc() throws Exception {
        String token = loginAndGetToken("13800000002", "Staff@123");

        Map<String, Object> record1 = new HashMap<>();
        record1.put("customerId", 1);
        record1.put("careDate", "2026-02-24 10:00:00");
        record1.put("content", "测量血压");

        Map<String, Object> record2 = new HashMap<>();
        record2.put("customerId", 1);
        record2.put("careDate", "2026-02-24 11:00:00");
        record2.put("content", "潮间带采样辅导");

        mockMvc.perform(post("/api/care-records")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(record1)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.performerName").value("导师甲"));

        mockMvc.perform(post("/api/care-records")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(record2)))
            .andExpect(status().isOk());

        MvcResult result = mockMvc.perform(get("/api/care-records")
                .header("Authorization", "Bearer " + token)
                .queryParam("customerId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andReturn();

        JsonNode list = objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
        assertThat(list.get(0).path("content").asText()).isEqualTo("潮间带采样辅导");
        assertThat(list.get(1).path("content").asText()).isEqualTo("测量血压");
    }

    @Test
    void shouldAssignServiceObjectAndCreateServiceFocus() throws Exception {
        String adminToken = loginAndGetToken("13800000001", "Admin@123");
        Map<String, Object> assignPayload = new HashMap<>();
        assignPayload.put("managerUserId", 2);

        mockMvc.perform(put("/api/services/objects/1")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(assignPayload)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.customerId").value(1))
            .andExpect(jsonPath("$.data.managerUserId").value(2));

        String staffToken = loginAndGetToken("13800000002", "Staff@123");

        Map<String, Object> focusPayload = new HashMap<>();
        focusPayload.put("customerId", 1);
        focusPayload.put("serviceName", "夜潮观测拓展包");
        focusPayload.put("purchaseDate", "2026-02-25");
        focusPayload.put("expireDate", "2026-08-25");
        focusPayload.put("serviceStatus", "ACTIVE");
        focusPayload.put("note", "每周三次上门");

        mockMvc.perform(post("/api/services/focuses")
                .header("Authorization", "Bearer " + staffToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(focusPayload)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.customerId").value(1))
            .andExpect(jsonPath("$.data.serviceName").value("夜潮观测拓展包"))
            .andExpect(jsonPath("$.data.createdByName").value("导师甲"));

        mockMvc.perform(get("/api/services/focuses")
                .header("Authorization", "Bearer " + staffToken)
                .queryParam("customerId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data[0].serviceStatus").value("ACTIVE"));
    }

    private String loginAndGetToken(String phone, String password) throws Exception {
        Map<String, String> payload = new HashMap<>();
        payload.put("phone", phone);
        payload.put("password", password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isOk())
            .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("token").asText();
    }
}
