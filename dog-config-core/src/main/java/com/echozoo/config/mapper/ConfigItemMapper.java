package com.echozoo.config.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.echozoo.config.domain.ConfigItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ConfigItemMapper extends BaseMapper<ConfigItem> {

    /**
     * 按 key 查询，包含已软删除记录。
     *
     * <p>自定义 SQL 不经 MyBatis-Plus 逻辑删除过滤，用于唯一性查重识别「已删除占用」。
     */
    @Select("SELECT * FROM config_item WHERE `key` = #{key} LIMIT 1")
    ConfigItem selectByKeyIncludeDeleted(@Param("key") String key);

    /**
     * 按 id 查询，包含已软删除记录。
     *
     * <p>自定义 SQL 不经 MyBatis-Plus 逻辑删除过滤，用于恢复已软删除配置项前的校验。
     */
    @Select("SELECT * FROM config_item WHERE id = #{id} LIMIT 1")
    ConfigItem selectByIdIncludeDeleted(@Param("id") Long id);

    /**
     * 解除软删除（deleted = 0），显式 SQL 绕过逻辑删除过滤。
     */
    @Update("UPDATE config_item SET deleted = 0 WHERE id = #{id}")
    int restoreById(@Param("id") Long id);
}
