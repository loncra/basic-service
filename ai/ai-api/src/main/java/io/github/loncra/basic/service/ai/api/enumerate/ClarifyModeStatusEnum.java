package io.github.loncra.basic.service.ai.api.enumerate;

import io.github.loncra.framework.commons.enumerate.NameEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ClarifyModeStatusEnum implements NameEnum {

    READY("准备完成"),

    PENDING("待执行"),

    RUNNING("进行中"),

    DONE("完成"),

    SKIP("失败"),
    ;

    private final String name;

    public static final List<ClarifyModeStatusEnum> ACTIVE_STATUS = List.of(PENDING, RUNNING);
}
