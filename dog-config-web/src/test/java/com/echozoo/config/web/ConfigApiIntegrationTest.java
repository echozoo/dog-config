package com.echozoo.config.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

    @Autowired
    private ObjectMapper objectMapper;

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

    @Test
    void createItemProducesInitialVersion() throws Exception {
        long id = createItem("t.version.create", "10");

        mockMvc.perform(get("/api/items/" + id + "/versions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].versionNo").value(1))
                .andExpect(jsonPath("$.data[0].changeType").value("CREATE"))
                .andExpect(jsonPath("$.data[0].value").value("10"));
    }

    @Test
    void valueChangeAppendsUpdateVersion() throws Exception {
        long id = createItem("t.version.update", "10");

        mockMvc.perform(patch("/api/items/" + id + "/value")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\":\"20\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/api/items/" + id + "/versions"))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[1].versionNo").value(2))
                .andExpect(jsonPath("$.data[1].changeType").value("UPDATE"))
                .andExpect(jsonPath("$.data[1].value").value("20"));
    }

    @Test
    void nonValueChangeDoesNotAppendVersion() throws Exception {
        long id = createItem("t.version.novalue", "10");

        mockMvc.perform(put("/api/items/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"key\":\"t.version.novalue\",\"name\":\"改名不改值\",\"value\":\"10\","
                                + "\"valueType\":\"INTEGER\",\"componentType\":\"NUMBER\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/api/items/" + id + "/versions"))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void versionDetailAndNotFound() throws Exception {
        long id = createItem("t.version.detail", "10");

        mockMvc.perform(get("/api/items/" + id + "/versions/1"))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.versionNo").value(1));

        mockMvc.perform(get("/api/items/" + id + "/versions/99"))
                .andExpect(jsonPath("$.code").value(40400));
    }

    @Test
    void versionsForMissingItemNotFound() throws Exception {
        mockMvc.perform(get("/api/items/999999/versions"))
                .andExpect(jsonPath("$.code").value(40400));
    }

    @Test
    void rollbackRestoresValueAndKeepsHistory() throws Exception {
        mockMvc.perform(patch("/api/items/1/value")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\":\"60\"}"))
                .andExpect(jsonPath("$.code").value(0));
        mockMvc.perform(patch("/api/items/1/value")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\":\"90\"}"))
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(post("/api/items/1/versions/1/rollback"))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.value").value("60"));

        mockMvc.perform(get("/api/items/1/versions"))
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[2].changeType").value("ROLLBACK"))
                .andExpect(jsonPath("$.data[2].value").value("60"));

        mockMvc.perform(get("/api/configs/order.timeout"))
                .andExpect(jsonPath("$.data").value(60));
    }

    @Test
    void rollbackToMissingVersionNotFound() throws Exception {
        mockMvc.perform(post("/api/items/1/versions/99/rollback"))
                .andExpect(jsonPath("$.code").value(40400));
    }

    @Test
    void restoreSoftDeletedItem() throws Exception {
        mockMvc.perform(delete("/api/items/1"))
                .andExpect(jsonPath("$.code").value(0));
        mockMvc.perform(get("/api/configs/order.timeout"))
                .andExpect(jsonPath("$.code").value(40400));

        mockMvc.perform(post("/api/items/1/restore"))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(1));

        mockMvc.perform(get("/api/configs/order.timeout"))
                .andExpect(jsonPath("$.data").value(30));

        mockMvc.perform(get("/api/items/1/versions"))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].changeType").value("RESTORE"));
    }

    @Test
    void restoreNonDeletedItemNotFound() throws Exception {
        mockMvc.perform(post("/api/items/1/restore"))
                .andExpect(jsonPath("$.code").value(40400));
    }

    private long createItem(String key, String value) throws Exception {
        String body = mockMvc.perform(post("/api/groups/1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"key\":\"" + key + "\",\"name\":\"版本测试项\",\"value\":\"" + value + "\","
                                + "\"valueType\":\"INTEGER\",\"componentType\":\"NUMBER\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn().getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(body);
        return node.path("data").path("id").asLong();
    }
}
