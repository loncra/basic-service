package io.github.loncra.basic.service.ai.server.dao.hub;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.loncra.basic.service.ai.server.domain.entity.hub.AiMcpPackageEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_ai_mcp_package 的数据访问
 *
 * <p>Table: tb_ai_mcp_package - MCP 连接器目录</p>
 *
 * @see AiMcpPackageEntity
 *
 * @author maurice.chen
 *
 * @since 2026-08-04 09:21:08
 */
@Mapper
@Repository
public interface AiMcpPackageDao extends BaseMapper<AiMcpPackageEntity> {

}
