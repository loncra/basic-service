package io.github.loncra.basic.service.ai.server.resolver.channel;

import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.filesystem.AbstractFilesystem;
import io.agentscope.harness.agent.filesystem.RoutedSandboxFilesystem;
import io.agentscope.harness.agent.filesystem.sandbox.AbstractSandboxFilesystem;
import io.agentscope.harness.agent.skill.runtime.ShellPathPolicy;
import io.github.loncra.basic.service.ai.server.domain.body.AgentChatBasicResponseBody;
import io.github.loncra.basic.service.ai.server.domain.body.AgentChatRequestBody;
import io.github.loncra.basic.service.ai.server.domain.body.AgentChatResponseBody;
import io.github.loncra.basic.service.ai.server.domain.body.AgentResumeRequestBody;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentConversationEntity;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.AgentExecuteMessageMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.AgentExecutionMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.model.ModelResolverMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentConversationTypeEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.ExecutionChannelEnum;
import io.github.loncra.basic.service.ai.server.resolver.sandbox.AgentSandboxLayout;
import io.github.loncra.basic.service.ai.server.resolver.sandbox.ClientRpcSandboxFilesystem;
import io.github.loncra.basic.service.ai.server.resolver.sandbox.RpcFilesystemScope;
import io.github.loncra.basic.service.ai.server.service.agent.AgentSocketSessionResolver;
import io.github.loncra.basic.service.message.api.domian.metadata.SocketSessionMetadata;
import io.github.loncra.basic.service.message.api.service.SocketServiceClient;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import io.github.loncra.framework.spring.web.device.DeviceUtils;
import lombok.RequiredArgsConstructor;
import nl.basjes.parse.useragent.UserAgent;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class ClientAgentChannelResolver extends AbstractAgentChannelResolver {

    private final AgentSocketSessionResolver agentSocketSessionResolver;

    private final SocketServiceClient socketServiceClient;

    @Override
    public ExecutionChannelEnum getChannel() {
        return ExecutionChannelEnum.CLIENT;
    }

    @Override
    public AgentChatResponseBody chat(
            AgentChatRequestBody body,
            AuditAuthenticationToken token,
            String deviceIdentified
    ) {
        SocketSessionMetadata session = agentSocketSessionResolver.getSession(token.getName(), deviceIdentified);
        SystemException.isTrue(Objects.nonNull(session), "客户端工作空间执行需要桌面客户端在线");
        String executeShell = Objects.toString(session.getMetadata().get(SocketSessionMetadata.EXECUTE_SHELL_METADATA_NAME), null);
        SystemException.isTrue(StringUtils.isNotBlank(executeShell), "客户端 [" + deviceIdentified + "] 未上报 executeShell，无法执行客户端任务");

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
    protected void preCreateWorkspace(
            AgentConversationEntity workspace,
            AuditAuthenticationToken token,
            String deviceIdentified
    ) {
        if (!AgentConversationTypeEnum.DEFAULT_WORKSPACE.equals(workspace.getType())) {
            return ;
        }

        SocketSessionMetadata session = agentSocketSessionResolver.getSession(token.getName(), deviceIdentified);
        if (Objects.isNull(session)) {
            return;
        }
        String clientHome = Objects.toString(session.getMetadata().get(SocketSessionMetadata.CLIENT_HOME_METADATA_NAME), null);
        if (StringUtils.isBlank(clientHome)) {
            return;
        }
        workspace.getMetadata().put(WORKSPACE_ROOTS_METADATA_KEY, List.of(clientHome));
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

        return finishAgent(baseBuilder(execution, modelResolverMetadata, clientWorkspaceScratch()), execution);
    }

    private String clientWorkspaceScratch() {
        Path scratch = Path.of(aiAppConfig.getWorkspace().getPath(), "client-scratch");
        try {
            Files.createDirectories(scratch);
        } catch (IOException exception) {
            throw new SystemException("创建客户端会话暂存目录失败: " + scratch, exception);
        }
        return scratch.toString();
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
        ClientRpcSandboxFilesystem workspacePipe = new ClientRpcSandboxFilesystem(
                socketServiceClient,
                execution.getDeviceIdentified(),
                workspaceScope
        );

        String storeBase = AgentSandboxLayout.storePrincipal(aiAppConfig.getSourceOfTruth().getDataDir(), AgentSandboxLayout.segment(principal));
        Function<String, AbstractFilesystem> storeBackend = dir -> new ClientRpcSandboxFilesystem(
                socketServiceClient,
                execution.getDeviceIdentified(),
                new RpcFilesystemScope.Store(String.join(AntPathMatcher.DEFAULT_PATH_SEPARATOR, storeBase, dir))
        );
        return builder.abstractFilesystem(new RoutedSandboxFilesystem(workspacePipe, userLevelRouteMap(storeBackend)));
    }

    @Override
    protected AbstractSandboxFilesystem openRecords(String deviceIdentified) {
        return new ClientRpcSandboxFilesystem(socketServiceClient, deviceIdentified, new RpcFilesystemScope.Store(""));
    }

    @Override
    protected String recordHome() {
        return aiAppConfig.getSourceOfTruth().getDataDir();
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
                ShellPathPolicy.SANDBOX_WORKSPACE_PREFIX
        );
    }
}
