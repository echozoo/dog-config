package com.echozoo.config.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 管理端 + 业务读取 API 集成测试。
 * 使用独立测试库 dog_config_test（本地 MySQL，createDatabaseIfNotExist 自动建库）。
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:mysql://localhost:3306/dog_config_test?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true",
        "spring.datasource.username=admin",
        "spring.datasource.password=123456",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:db/schema.sql",
        "spring.sql.init.data-locations=classpath:db/data.sql",
})
class ConfigApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void readSingleTypedConfig() throws Exception {
        mockMvc.perform(get("/api/configs/order.auto.cancel.minutes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(30));
    }

    @Test
    void readBatchByKeys() throws Exception {
        mockMvc.perform(get("/api/configs")
                        .param("keys", "order.timeout,order.allow.over.sell"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data['order.timeout']").value(30))
                .andExpect(jsonPath("$.data['order.allow.over.sell']").value(false));
    }

    @Test
    void readByPrefix() throws Exception {
        mockMvc.perform(get("/api/configs")
                        .param("prefix", "order."))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data['order.timeout']").exists())
                .andExpect(jsonPath("$.data['product.max.buy.quantity']").doesNotExist());
    }

    @Test
    void notFoundWhenKeyMissing() throws Exception {
        mockMvc.perform(get("/api/configs/not.exists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40400));
    }

    @Test
    void createPageAndConflict() throws Exception {
        mockMvc.perform(post("/api/pages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"MEMBER\",\"name\":\"会员配置\",\"sort\":4}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.code").value("MEMBER"));

        mockMvc.perform(post("/api/pages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"ORDER\",\"name\":\"重复\",\"sort\":9}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40900));
    }

    @Test
    void updateItemValueReflectedInRead() throws Exception {
        mockMvc.perform(patch("/api/items/4/value")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\":\"60\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/api/configs/order.auto.cancel.minutes"))
                .andExpect(jsonPath("$.data").value(60));
    }

    @Test
    void createItemWithInvalidValueRejected() throws Exception {
        mockMvc.perform(post("/api/groups/1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"key\":\"t.invalid.value\",\"name\":\"非法值\",\"value\":\"abc\","
                                + "\"valueType\":\"INTEGER\",\"componentType\":\"NUMBER\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40000));
    }

    @Test
    void createItemWithIncompatibleComponentRejected() throws Exception {
        mockMvc.perform(post("/api/groups/1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"key\":\"t.bad.component\",\"name\":\"组件不兼容\",\"value\":\"true\","
                                + "\"valueType\":\"BOOLEAN\",\"componentType\":\"INPUT\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40000));
    }

    @Test
    void createSelectWithoutOptionsRejected() throws Exception {
        mockMvc.perform(post("/api/groups/1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"key\":\"t.select.no.options\",\"name\":\"缺选项\",\"value\":\"SF\","
                                + "\"valueType\":\"STRING\",\"componentType\":\"SELECT\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40000));
    }

    @Test
    void createRequiredWithoutValueRejected() throws Exception {
        mockMvc.perform(post("/api/groups/1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"key\":\"t.required.empty\",\"name\":\"必填空\","
                                + "\"valueType\":\"INTEGER\",\"componentType\":\"NUMBER\",\"required\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40000));
    }

    @Test
    void createItemWithKeyOfDeletedItemConflicts() throws Exception {
        mockMvc.perform(delete("/api/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(post("/api/groups/1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"key\":\"order.timeout\",\"name\":\"重建同名\",\"value\":\"30\","
                                + "\"valueType\":\"INTEGER\",\"componentType\":\"NUMBER\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40900));
    }

    @Test
    void deletePageWithChildrenConflicts() throws Exception {
        mockMvc.perform(delete("/api/pages/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40900));
    }

    @Test
    void deleteGroupWithChildrenConflicts() throws Exception {
        mockMvc.perform(delete("/api/groups/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40900));
    }
}
