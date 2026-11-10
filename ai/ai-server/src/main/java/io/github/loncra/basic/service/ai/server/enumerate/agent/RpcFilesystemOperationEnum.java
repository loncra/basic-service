package io.github.loncra.basic.service.ai.server.enumerate.agent;

import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum RpcFilesystemOperationEnum implements NameValueEnum<String> {

    LS("ls", "列出目录"),

    READ("read", "读取文件"),

    WRITE("write", "写入文件"),

    EDIT("edit", "编辑文件"),

    GREP("grep", "搜索内容"),

    GLOB("glob", "匹配文件"),

    UPLOAD("upload", "上传文件"),

    DOWNLOAD("download", "下载文件"),

    DELETE("delete", "删除"),

    MOVE("move", "移动"),

    EXISTS("exists", "是否存在"),

    EXECUTE("execute", "执行命令");

    private final String value;

    private final String name;
}
