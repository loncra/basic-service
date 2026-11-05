package io.github.loncra.basic.service.commons.service;

import io.github.loncra.framework.commons.generator.twitter.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class SimpleSnowflakeIdGenerateService implements SnowflakeIdGenerateService {

    private final SnowflakeIdGenerator snowflakeIdGenerator;

    @Override
    public String generateSnowflakeId() {
        return snowflakeIdGenerator.generateId();
    }
}
