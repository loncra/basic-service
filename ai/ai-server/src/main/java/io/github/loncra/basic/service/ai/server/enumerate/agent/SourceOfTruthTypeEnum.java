package io.github.loncra.basic.service.ai.server.enumerate.agent;

import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum SourceOfTruthTypeEnum implements NameValueEnum<String> {

    LOCAL("local", "本机文件落地"),

    ResourceServer("resourceServer", "资源服务对象存储");

    private final String value;

    private final String name;
}
