package io.github.loncra.basic.service.ai.api.constants;

import io.github.loncra.framework.commons.CastUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

public class AiConstants {

    public static final String MQ_USER_AUTH_SUCCESS_QUEUE_NAME = "ai.server.user.auth.success";

    public static final String MQ_AGENT_EXECUTE_QUEUE = "ai.server.agent.execute";

    public static final String MQ_SKILL_SOURCE_INGEST_QUEUE = "ai.server.skill.source.ingest";

    public static String newReplyId() {
        return UUID.randomUUID().toString().replace(CastUtils.NEGATIVE, StringUtils.EMPTY);
    }
}
