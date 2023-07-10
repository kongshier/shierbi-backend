package com.shier.shierbi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shier.shierbi.model.entity.Chart;

import java.util.List;
import java.util.Map;

/**
* @author Shier
* @description 针对表【chart(图表信息表)】的数据库操作Mapper
* @createDate 2023-05-14 19:20:33
* @Entity com.shier.shierbi.model.entity.Chart
*/
public interface ChartMapper extends BaseMapper<Chart> {

    /**
     * @param querySql
     * @return
     */
    List<Map<String, Object>> queryChartData(String querySql);
}




