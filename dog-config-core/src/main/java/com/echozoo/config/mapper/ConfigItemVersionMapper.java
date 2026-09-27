package com.echozoo.config.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.echozoo.config.domain.ConfigItemVersion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ConfigItemVersionMapper extends BaseMapper<ConfigItemVersion> {

    /**
     * 按 item_id 查询版本历史，按 version_no 升序。
     */
    @Select("SELECT * FROM config_item_version WHERE item_id = #{itemId} ORDER BY version_no ASC")
    List<ConfigItemVersion> selectByItemIdOrderByVersionNo(@Param("itemId") Long itemId);

    /**
     * 查询某配置项指定版本号的一条版本记录。
     */
    @Select("SELECT * FROM config_item_version WHERE item_id = #{itemId} AND version_no = #{versionNo} LIMIT 1")
    ConfigItemVersion selectByItemIdAndVersionNo(@Param("itemId") Long itemId, @Param("versionNo") Integer versionNo);

    /**
     * 查询某配置项当前最大版本号，无版本时返回 0。
     */
    @Select("SELECT COALESCE(MAX(version_no), 0) FROM config_item_version WHERE item_id = #{itemId}")
    Integer selectMaxVersionNo(@Param("itemId") Long itemId);
}
