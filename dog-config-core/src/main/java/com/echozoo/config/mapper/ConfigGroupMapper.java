package com.echozoo.config.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.echozoo.config.domain.ConfigGroup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ConfigGroupMapper extends BaseMapper<ConfigGroup> {

    /**
     * 按 page_id + code 查询，包含已软删除记录。
     *
     * <p>自定义 SQL 不经 MyBatis-Plus 逻辑删除过滤，用于唯一性查重识别「已删除占用」。
     */
    @Select("SELECT * FROM config_group WHERE page_id = #{pageId} AND code = #{code} LIMIT 1")
    ConfigGroup selectByPageIdAndCodeIncludeDeleted(@Param("pageId") Long pageId, @Param("code") String code);
}
