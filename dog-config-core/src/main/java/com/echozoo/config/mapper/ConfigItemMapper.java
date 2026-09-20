package com.echozoo.config.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.echozoo.config.domain.ConfigItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ConfigItemMapper extends BaseMapper<ConfigItem> {
}
