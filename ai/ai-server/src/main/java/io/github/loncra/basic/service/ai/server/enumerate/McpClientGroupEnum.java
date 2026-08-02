package io.github.loncra.basic.service.ai.server.enumerate;

import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum McpClientGroupEnum implements NameValueEnum<String> {

    EXPLORE("探索", "explore" ),

    SEARCH_WEB("互联网搜索", "searchWeb")
    ;

    private final String name;
    private final String value;
}
