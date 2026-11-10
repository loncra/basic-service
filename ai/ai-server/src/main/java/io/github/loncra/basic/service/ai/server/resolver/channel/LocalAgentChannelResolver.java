package io.github.loncra.basic.service.ai.server.resolver.channel;

import io.agentscope.core.state.AgentStateStore;
import io.agentscope.harness.agent.HarnessAgent;
import io.github.loncra.basic.service.ai.server.domain.body.AgentChatBasicResponseBody;
import io.github.loncra.basic.service.ai.server.domain.body.AgentChatRequestBody;
import io.github.loncra.basic.service.ai.server.domain.body.AgentChatResponseBody;
import io.github.loncra.basic.service.ai.server.domain.body.AgentResumeRequestBody;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentConversationEntity;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.AgentExecuteMessageMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.AgentExecutionMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.BasicAgentExecutionMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.model.ModelResolverMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentConversationTypeEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.ExecutionChannelEnum;
import io.github.loncra.basic.service.message.api.domian.metadata.SocketSessionMetadata;
import io.github.loncra.basic.service.message.api.service.SocketServiceClient;
import io.github.loncra.basic.service.ai.server.resolver.rpc.RpcFilesystemScope;
import io.github.loncra.basic.service.ai.server.resolver.rpc.ClientRpcFilesystem;
import io.agentscope.harness.agent.filesystem.sandbox.AbstractSandboxFilesystem;
import io.github.loncra.basic.service.ai.server.resolver.sandbox.SandboxFileAgentStateStore;
import io.github.loncra.basic.service.ai.server.service.agent.AgentSocketSessionResolver;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.id.metadata.IdNameMetadata;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import io.github.loncra.framework.spring.web.device.DeviceUtils;
import lombok.RequiredArgsConstructor;
import nl.basjes.parse.useragent.UserAgent;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class LocalAgentChannelResolver extends AbstractAgentChannelResolver {

    private final AgentSocketSessionResolver agentSocketSessionResolver;

    private final SocketServiceClient socketServiceClient;

    @Override
    public ExecutionChannelEnum getChannel() {
        return ExecutionChannelEnum.LOCAL;
    }

    @Override
    public AgentChatResponseBody chat(
            AgentChatRequestBody body,
            AuditAuthenticationToken token,
            String deviceIdentified
    ) {
        SocketSessionMetadata session = agentSocketSessionResolver.getSession(token.getName(), deviceIdentified);
        SystemException.isTrue(Objects.nonNull(session), "本地工作空间执行需要桌面客户端在线");
        String executeShell = Objects.toString(session.getMetadata().get(SocketSessionMetadata.EXECUTE_SHELL_METADATA_NAME), null);
        SystemException.isTrue(StringUtils.isNotBlank(executeShell), "客户端 [" + deviceIdentified + "] 未上报 executeShell，无法执行本地任务");

        return super.chat(body, token, deviceIdentified);
    }

    @Override
    public AgentChatBasicResponseBody resume(
            AgentResumeRequestBody body,
            AuditAuthenticationToken token,
            String deviceIdentified
    ) {
        AgentChatBasicResponseBody responseBody = super.resume(body, token, deviceIdentified);
        agentSseStreamPublishResolver.sendExecuteMessage(
                AgentExecuteMessageMetadata.of(getChannel(), responseBody.getAssistantMessageId(), deviceIdentified),
                false
        );
        return responseBody;
    }

    @Override
    protected void preCreateDefaultWorkspace(
            IdNameMetadata body,
            AuditAuthenticationToken token,
            String deviceIdentified
    ) {
        SocketSessionMetadata session = agentSocketSessionResolver.getSession(token.getName(), deviceIdentified);
        if (Objects.isNull(session)) {
            return;
        }
        String clientHome = Objects.toString(session.getMetadata().get(SocketSessionMetadata.CLIENT_HOME_METADATA_NAME), null);
        if (StringUtils.isBlank(clientHome)) {
            return;
        }
        body.getMetadata().put(WORKSPACE_ROOTS_METADATA_KEY, List.of(clientHome));
    }

    @Override
    public HarnessAgent createHarnessAgent(AgentExecutionMetadata execution) {
        AgentMessageEntity assistant = execution.getAssistantMessage();
        AgentMessageEntity userMessage = Objects.requireNonNull(
                execution.getUserMessage(),
                "找不到 ID 为 [" + assistant.getParentId() + "] 的用户消息记录"
        );

        ModelResolverMetadata modelResolverMetadata = modelSettingService.getModelMetadata(
                assistant.getModel(),
                userMessage.getMetadata()
        );

        String workspacePath = workspaceRoot(
                execution.getConversation(),
                execution.getAssistantMessage().getPrincipal(),
                execution.getDeviceIdentified()
        );

        return finishAgent(baseBuilder(execution, modelResolverMetadata, workspacePath), execution);
    }

    @Override
    protected HarnessAgent.Builder attachFilesystem(
            HarnessAgent.Builder builder,
            AgentExecutionMetadata execution,
            String workspacePath
    ) {
        String principal = execution.getAssistantMessage().getPrincipal();
        AgentConversationEntity conversation = execution.getConversation();
        RpcFilesystemScope workspaceScope = Objects.isNull(conversation)
                ? new RpcFilesystemScope.DefaultWorkspace()
                : resolveWorkspaceScope(conversation, principal, execution.getDeviceIdentified());
        return builder.abstractFilesystem(new ClientRpcFilesystem(
                socketServiceClient,
                execution.getDeviceIdentified(),
                workspaceScope
        ));
    }

    @Override
    protected AgentStateStore resolveStateStore(BasicAgentExecutionMetadata execution) {
        return new SandboxFileAgentStateStore(openRecords(execution.getDeviceIdentified()), recordHome());
    }

    @Override
    protected AbstractSandboxFilesystem openRecords(String deviceIdentified) {
        return new ClientRpcFilesystem(socketServiceClient, deviceIdentified, new RpcFilesystemScope.Store());
    }

    @Override
    protected String recordHome() {
        return ClientRpcFilesystem.VIRTUAL_ROOT;
    }

    private RpcFilesystemScope resolveWorkspaceScope(
            AgentConversationEntity conversation,
            String principal,
            String deviceIdentified
    ) {
        if (AgentConversationTypeEnum.DEFAULT_WORKSPACE.equals(conversation.getType())) {
            return new RpcFilesystemScope.DefaultWorkspace();
        }
        String key = Objects.toString(conversation.getMetadata().get(WORKSPACE_KEY_METADATA_KEY), null);
        if (AgentConversationTypeEnum.CUSTOMIZE_WORKSPACE.equals(conversation.getType()) && StringUtils.isNotBlank(key)) {
            return new RpcFilesystemScope.CustomWorkspace(key);
        }
        AgentConversationEntity parent = Objects.isNull(conversation.getParentId())
                ? null
                : getAgentRecordStore().getConversation(principal, conversation.getParentId(), deviceIdentified);
        if (Objects.isNull(parent)) {
            return new RpcFilesystemScope.DefaultWorkspace();
        }
        return resolveWorkspaceScope(parent, principal, deviceIdentified);
    }

    private String workspaceRoot(
            AgentConversationEntity conversation,
            String principal,
            String deviceIdentified
    ) {
        if (Objects.isNull(conversation)) {
            return null;
        }
        Object roots = conversation.getMetadata().get(WORKSPACE_ROOTS_METADATA_KEY);
        if (roots instanceof List<?> list) {
            for (Object item : list) {
                String root = Objects.toString(item, null);
                if (StringUtils.isNotBlank(root)) {
                    return root;
                }
            }
        }
        if (Objects.isNull(conversation.getParentId())) {
            return null;
        }
        AgentConversationEntity parent = getAgentRecordStore().getConversation(principal, conversation.getParentId(), deviceIdentified);
        return workspaceRoot(parent, principal, deviceIdentified);
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
                "客户端环境：操作系统 {0}（{1}），默认 shell 为 {2}，文件系统根路径为 {3}，所有文件路径以 {3} 为根，执行命令时请使用与 {2} 匹配的语法。",
                operatingSystemName,
                operatingSystemClass,
                executeShell,
                ClientRpcFilesystem.VIRTUAL_ROOT
        );
    }
}
