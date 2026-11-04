package io.github.loncra.basic.service.ai.server.resolver.channel;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.filesystem.AbstractFilesystem;
import io.github.loncra.basic.service.ai.api.domain.metadata.ModelSettingMetadata;
import io.github.loncra.basic.service.ai.server.domain.body.AgentChatRequestBody;
import io.github.loncra.basic.service.ai.server.domain.body.AgentChatResponseBody;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentConversationEntity;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.model.ModelResolverMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.AgentExecutionMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.AgentExecuteMessageMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.LocalChatSourceMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentConversationTypeEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.ExecutionChannelEnum;
import io.github.loncra.basic.service.ai.server.resolver.filesystem.RemoteRpcFilesystem;
import io.github.loncra.basic.service.ai.server.resolver.filesystem.RpcFilesystemInvoker;
import io.github.loncra.basic.service.ai.server.service.agent.AgentExecutionStore;
import io.github.loncra.basic.service.ai.server.service.agent.AgentSocketSessionResolver;
import io.github.loncra.basic.service.message.api.domian.metadata.SocketSessionMetadata;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import io.github.loncra.framework.spring.web.device.DeviceUtils;
import lombok.RequiredArgsConstructor;
import nl.basjes.parse.useragent.UserAgent;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.text.MessageFormat;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class LocalAgentChannelResolver extends AbstractAgentChannelResolver {

    private final AgentExecutionStore agentExecutionStore;

    private final AgentSocketSessionResolver agentSocketSessionResolver;

    private final RpcFilesystemInvoker rpcFilesystemInvoker;

    @Override
    public ExecutionChannelEnum channel() {
        return ExecutionChannelEnum.REDIS;
    }

    @Override
    public AgentChatResponseBody chat(
            AgentChatRequestBody body,
            AuditAuthenticationToken token,
            String deviceIdentified
    ) {
        LocalChatSourceMetadata source = body.getLocalSource();
        SystemException.isTrue(StringUtils.isNotBlank(source.getWorkspaceKey()), "本地工作空间标识不能为空");
        SystemException.isTrue(StringUtils.isNotBlank(source.getConversationId()), "本地会话标识不能为空");

        SocketSessionMetadata session = agentSocketSessionResolver.getSession(token.getName(), deviceIdentified);
        SystemException.isTrue(Objects.nonNull(session), "本地工作空间执行需要桌面客户端在线");
        String executeShell = Objects.toString(session.getMetadata().get(SocketSessionMetadata.EXECUTE_SHELL_METADATA_NAME), null);
        SystemException.isTrue(StringUtils.isNotBlank(executeShell), "客户端 [" + deviceIdentified + "] 未上报 executeShell，无法执行本地任务");

        ModelSettingMetadata modelMetadata = resolveModelMetadata(body.getModelId());

        AgentConversationEntity conversation = new AgentConversationEntity();
        conversation.setId(IdWorker.getId());
        conversation.setType(AgentConversationTypeEnum.WORKSPACE_CONVERSATION);
        conversation.setPrincipal(token.getName());
        prepareConversation(conversation, body, modelMetadata);

        AgentMessageEntity userMessage = buildUserMessage(body, modelMetadata, conversation.getId(), token.getName());
        userMessage.setId(IdWorker.getId());

        AgentMessageEntity assistantMessage = buildAssistantMessage(userMessage, conversation.getId());
        assistantMessage.setId(IdWorker.getId());

        AgentExecutionMetadata execution = new AgentExecutionMetadata();
        execution.setWorkspaceKey(source.getWorkspaceKey());
        execution.setClientConversationId(source.getConversationId());
        execution.setDeviceIdentified(deviceIdentified);
        execution.setChannel(ExecutionChannelEnum.REDIS);
        execution.setConversation(conversation);
        execution.setUserMessage(userMessage);
        execution.setAssistantMessage(assistantMessage);
        String executionKey = agentExecutionStore.create(execution);

        agentSseStreamPublishResolver.sendExecuteMessageNow(
                AgentExecuteMessageMetadata.of(ExecutionChannelEnum.REDIS, executionKey, deviceIdentified)
        );

        AgentChatResponseBody responseBody = buildResponseBody(conversation, userMessage, assistantMessage);
        responseBody.setStreamKey(executionKey);

        agentSseStreamPublishResolver.publishStreamStart(executionKey, assistantMessage.getId().toString());

        return responseBody;
    }

    @Override
    public HarnessAgent createHarnessAgent(AgentMessageEntity assistant) {
        AgentExecutionMetadata execution = agentExecutionStore.get(assistant.getExecutionRef());
        SystemException.isTrue(
                Objects.nonNull(execution),
                "执行引用 [" + assistant.getExecutionRef() + "] 不存在或已过期"
        );

        AgentMessageEntity userMessage = execution.getUserMessage();
        ModelResolverMetadata modelResolverMetadata = modelSettingService.getModelMetadata(
                assistant.getModel(),
                userMessage.getMetadata()
        );

        String workspacePath = aiAppConfig.getWorkspacePath() + File.separator + assistant.getExecutionRef();

        return finishAgent(baseBuilder(assistant, modelResolverMetadata, workspacePath), assistant);
    }

    @Override
    public AbstractFilesystem createFilesystem(AgentMessageEntity assistant, String workspacePath) {
        return new RemoteRpcFilesystem(rpcFilesystemInvoker, assistant.getDeviceIdentified());
    }

    @Override
    protected String createEnvironmentMemory(String deviceIdentified, String principal) {

        SocketSessionMetadata session = agentSocketSessionResolver.getSession(principal, deviceIdentified);

        if (Objects.isNull(session)) {
            return null;
        }

        String userAgentValue = Objects.toString(session.getMetadata().get(DeviceUtils.USER_AGENT_HEADER_NAME.toLowerCase()), null);
        if (StringUtils.isBlank(userAgentValue)) {
            return null;
        }

        UserAgent userAgent = DeviceUtils.getDevice(userAgentValue);
        String operatingSystemName = userAgent.get(UserAgent.OPERATING_SYSTEM_NAME).getValue();
        String operatingSystemClass = userAgent.get(UserAgent.OPERATING_SYSTEM_CLASS).getValue();

        if (StringUtils.isBlank(operatingSystemName)) {
            return null;
        }

        String executeShell = Objects.toString(session.getMetadata().get(SocketSessionMetadata.EXECUTE_SHELL_METADATA_NAME), null);
        if (StringUtils.isBlank(executeShell)) {
            return null;
        }

        return MessageFormat.format(
                "客户端环境：操作系统 {0}（{1}），默认 shell 为 {2}，文件系统根路径为 /workspace，所有文件路径以 /workspace 为根，执行命令时请使用与 {2} 匹配的语法。",
                operatingSystemName,
                operatingSystemClass,
                executeShell
        );
    }
}
