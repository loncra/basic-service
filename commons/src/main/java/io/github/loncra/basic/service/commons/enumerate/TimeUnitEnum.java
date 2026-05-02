package io.github.loncra.basic.service.commons.enumerate;

import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.concurrent.TimeUnit;

/**
 * 时间单位枚举
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum TimeUnitEnum implements NameValueEnum<String> {

    /**
     * 天
     */
    DAYS("天", TimeUnit.DAYS.toString()),

    /**
     * 小时
     */
    HOURS("小时", TimeUnit.HOURS.toString()),

    /**
     * 分钟
     */
    MINUTES("分钟", TimeUnit.MINUTES.toString()),

    /**
     * 秒
     */
    SECONDS("秒", TimeUnit.SECONDS.toString()),

    /**
     * 毫秒
     */
    MILLISECONDS("毫秒", TimeUnit.MILLISECONDS.toString()),

    /**
     * 微秒
     */
    MICROSECONDS("微秒", TimeUnit.MICROSECONDS.toString()),

    /**
     * 纳秒
     */
    NANOSECONDS("纳秒", TimeUnit.NANOSECONDS.toString()),

    ;

    private final String name;

    private final String value;

    public TimeUnit toTimeUnit() {
        return TimeUnit.valueOf(getName());
    }

}
