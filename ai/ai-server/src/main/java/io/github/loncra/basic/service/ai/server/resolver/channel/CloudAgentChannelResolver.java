package io.github.loncra.basic.service.ai.server.resolver.channel;

import io.agentscope.core.skill.repository.FileSystemSkillRepository;
import io.agentscope.core.state.AgentStateStore;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.IsolationScope;
import io.agentscope.harness.agent.filesystem.AbstractFilesystem;
import io.agentscope.harness.agent.filesystem.RoutedSandboxFilesystem;
import io.agentscope.harness.agent.filesystem.local.LocalFilesystem;
import io.github.loncra.basic.service.ai.server.domain.body.AgentChatBasicResponseBody;
import io.github.loncra.basic.service.ai.server.domain.body.AgentResumeRequestBody;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentConversationEntity;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.AgentExecuteMessageMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.AgentExecutionMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.BasicAgentExecutionMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.model.ModelResolverMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentConversationTypeEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.ExecutionChannelEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.SandboxTypeEnum;
import io.agentscope.harness.agent.filesystem.local.LocalFilesystemWithShell;
import io.agentscope.harness.agent.filesystem.sandbox.AbstractSandboxFilesystem;
import io.agentscope.harness.agent.filesystem.spec.LocalFilesystemSpec;
import io.agentscope.harness.agent.skill.runtime.ShellPathPolicy;
import io.github.loncra.basic.service.ai.server.resolver.sandbox.AgentSandboxLayout;
import io.github.loncra.basic.service.ai.server.resolver.sandbox.SandboxFileAgentStateStore;
import io.github.loncra.basic.service.ai.server.service.agent.AgentHubSkillService;
import io.github.loncra.basic.service.ai.server.service.agent.AgentUserPluginToolkitService;
import io.github.loncra.basic.service.ai.server.service.agent.HubSkillRepository;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import lombok.RequiredArgsConstructor;
import nl.basjes.parse.useragent.yauaa.shaded.org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.SystemProperties;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class CloudAgentChannelResolver extends AbstractAgentChannelResolver {

    private final AgentUserPluginToolkitService agentUserPluginToolkitService;

    private final AgentHubSkillService agentHubSkillService;

    @Override
    public ExecutionChannelEnum getChannel() {
        return ExecutionChannelEnum.CLOUD;
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
                true
        );
        return responseBody;
    }

    @Override
    public HarnessAgent createHarnessAgent(AgentExecutionMetadata execution) {
        AgentMessageEntity assistant = execution.getAssistantMessage();
        AgentConversationEntity conversation = Objects.requireNonNull(
                execution.getConversation(),
                "找不到 ID 为 [" + assistant.getAgentConversationId() + "] 的会话内容"
        );
        SystemException.isTrue(
                AgentConversationTypeEnum.WORKSPACE_CONVERSATION.equals(conversation.getType()),
                "当前会话类型不支持执行 agent"
        );

        AgentConversationEntity workspace = Objects.requireNonNull(
                getAgentRecordStore().getConversation(assistant.getPrincipal(), conversation.getParentId(), execution.getDeviceIdentified()),
                "找不到 ID 为 [" + conversation.getParentId() + "] 的工作空间"
        );
        SystemException.isTrue(
                !AgentConversationTypeEnum.WORKSPACE_CONVERSATION.equals(workspace.getType()),
                "当前会话类型不支持执行 agent"
        );

        AgentMessageEntity userMessage = Objects.requireNonNull(
                execution.getUserMessage(),
                "找不到 ID 为 [" + assistant.getParentId() + "] 的用户消息记录"
        );

        ModelResolverMetadata modelResolverMetadata = modelSettingService.getModelMetadata(
                assistant.getModel(),
                userMessage.getMetadata()
        );
        Path userSkillDir = agentUserPluginToolkitService.registerUserMcp(
                modelResolverMetadata.getToolkit(),
                assistant.getPrincipal(),
                workspace.getId()
        );
        HubSkillRepository hubSkillRepository = agentHubSkillService.ensureCached(
                assistant.getPrincipal(),
                workspace.getId()
        );

        String workspacePath = AgentSandboxLayout.workspaceDirectory(
                Path.of(aiAppConfig.getWorkspacePath()),
                workspace.getId()
        ).toString();

        HarnessAgent.Builder builder = baseBuilder(execution, modelResolverMetadata, workspacePath)
                .skillRepository(new FileSystemSkillRepository(userSkillDir))
                .skillRepository(hubSkillRepository);

        return finishAgent(builder, execution);
    }

    @Override
    protected HarnessAgent.Builder attachFilesystem(
            HarnessAgent.Builder builder,
            AgentExecutionMetadata execution,
            String workspacePath
    ) {
        Function<String, AbstractFilesystem> storeBackend = storeBackend(execution.getAssistantMessage().getPrincipal());
        if (SandboxTypeEnum.DOCKER.equals(aiAppConfig.getSandboxType())) {
            userLevelRouteMap(storeBackend).forEach(builder::filesystemRoute);
            return builder.filesystem(aiAppConfig.getDocker().toSpec());
        }
        Path directory = Path.of(workspacePath);
        return builder.abstractFilesystem(new RoutedSandboxFilesystem(
                primaryFilesystem(directory, directory),
                userLevelRouteMap(storeBackend)
        ));
    }

    private AbstractSandboxFilesystem primaryFilesystem(Path workspace, Path project) {
        AbstractFilesystem filesystem = new LocalFilesystemSpec()
                .project(project)
                .toFilesystem(workspace, IsolationScope.USER.toNamespaceFactory());
        if (filesystem instanceof AbstractSandboxFilesystem sandbox) {
            return sandbox;
        }
        throw new SystemException("本地文件系统规格未产出沙箱文件系统: " + filesystem.getClass().getName());
    }

    private Function<String, AbstractFilesystem> storeBackend(String principal) {
        Path userRoot = Path.of(aiAppConfig.getWorkspacePath(), aiAppConfig.getDataDir(), AgentSandboxLayout.segment(principal))
                .toAbsolutePath()
                .normalize();
        return dir -> new LocalFilesystem(userRoot.resolve(dir), true, aiAppConfig.getStoreMaxFileSizeMb());
    }

    @Override
    protected String createEnvironmentMemory(String deviceIdentified, String principal) {
        if (SandboxTypeEnum.DOCKER.equals(aiAppConfig.getSandboxType())) {
            String root = StringUtils.defaultIfBlank(aiAppConfig.getDocker().getWorkspaceRoot(), ShellPathPolicy.SANDBOX_WORKSPACE_PREFIX);
            return "云端环境：Docker 容器，执行命令的终端为 bash，工作区根为 " + root + "。执行命令时请使用 bash 语法。";
        }
        String shell = SystemUtils.IS_OS_WINDOWS ? "cmd.exe /c" : "sh -c";
        return "云端环境：操作系统 " + SystemProperties.getOsName() + "，执行命令的终端为 " + shell + "，工作区是服务器本机目录。文件工具不经过终端。执行命令时请使用与 " + shell + " 匹配的语法。";
    }

    @Override
    protected AgentStateStore resolveStateStore(BasicAgentExecutionMetadata execution) {
        return new SandboxFileAgentStateStore(openRecords(execution.getDeviceIdentified()), recordHome());
    }

    @Override
    protected AbstractSandboxFilesystem openRecords(String deviceIdentified) {
        return new LocalFilesystemWithShell(recordDirectory());
    }

    @Override
    protected String recordHome() {
        return recordDirectory().toString();
    }

    private Path recordDirectory() {
        return Path.of(aiAppConfig.getWorkspacePath(), aiAppConfig.getDataDir()).toAbsolutePath().normalize();
    }
}
