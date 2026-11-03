package io.github.loncra.basic.service.ai.server.enumerate;

import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum RpcFilesystemOperationEnum implements NameValueEnum<String> {

    LS("列目录", "ls"),

    READ("读文件", "read"),

    WRITE("写文件", "write"),

    EDIT("编辑文件", "edit"),

    GREP("搜索内容", "grep"),

    GLOB("搜索文件", "glob"),

    EXISTS("判断存在", "exists"),

    DELETE("删除", "delete"),

    MOVE("移动", "move"),

    UPLOAD("上传", "upload"),

    DOWNLOAD("下载", "download"),

    EXECUTE("执行命令", "execute"),

    ;

    private final String name;

    private final String value;

}
