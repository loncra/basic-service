package io.github.loncra.basic.service.auth.server.domain;

import io.github.loncra.basic.service.auth.server.domain.metdata.UserInitializationMetadata;

import java.io.Serializable;

/**
 * 用户初始化信息
 *
 * @author maurice.chen
 */
public interface UserInitialization extends Serializable {

    /**
     * 获取用户初始化元数据内容
     *
     * @return 用户初始化元数据内容
     */
    UserInitializationMetadata getInitialization();
}
