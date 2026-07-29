package io.github.loncra.basic.service.ai.server.domain.metadata;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.AgentTokenUsageMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentChatTypeEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageContentTypeEnum;
import io.github.loncra.basic.service.commons.domain.metadata.chat.TextMessageMetadata;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.enumerate.ValueEnum;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.id.IdEntity;
import io.github.loncra.framework.commons.id.metadata.TypeIdNameMetadata;
import io.github.loncra.framework.commons.minio.ObjectWriteResult;
import io.github.loncra.framework.mybatis.handler.JacksonJsonTypeHandler;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class AgentChatMetadata implements Serializable {


    public static final String TOKEN_USAGE_KEY = "tokenUsage";

    @Serial
    private static final long serialVersionUID = 5795604118691592427L;

    /**
     * 会话 id
     */
    private Long agentConversationId;

    /**
     * 应答类型:10.ask,20.plan,30.agent
     */
    @NotNull
    private AgentChatTypeEnum type;
    /**
     * 消息词槽格式内容
     */
    @NotEmpty
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private List<Map<String, Object>> content = new LinkedList<>();

    /**
     * 附件媒体内容
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private List<ObjectWriteResult> attachment = new LinkedList<>();

    /**
     * 附加 元数据内容
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private Map<String, Object> metadata = new LinkedHashMap<>();

    @JsonIgnore
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @TableField(exist = false)
    private String contentJson;

    @JsonIgnore
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @TableField(exist = false)
    private String metadataJson;

    public <T extends AbstractAssistantMessageContentMetadata> List<T> obtainBlock(String id) {
        List<Map<String, Object>> contents = getContent().stream()
                .filter(block -> Objects.equals(id, block.get(IdEntity.ID_FIELD_NAME)))
                .toList();
        List<T> result = new LinkedList<>();

        for (Map<String, Object> content : contents) {
            String type = Objects.toString(content.get(TypeIdNameMetadata.TYPE_FIELD_NAME), StringUtils.EMPTY);
            AgentMessageContentTypeEnum typeEnum = ValueEnum.ofEnum(AgentMessageContentTypeEnum.class, type);
            T block = (T) CastUtils.convertValue(content, typeEnum.getTargetClass());
            result.add(block);
        }

        return result;

    }

    public <T extends AbstractAssistantMessageContentMetadata> T obtainBlock(String id, AgentMessageContentTypeEnum type) {
        return (T) getContent().stream()
                .filter(block -> Objects.equals(id, block.get(IdEntity.ID_FIELD_NAME)))
                .filter(block -> block.get(TypeIdNameMetadata.TYPE_FIELD_NAME).equals(type.getValue()))
                .map(s -> CastUtils.convertValue(s, type.getTargetClass()))
                .findFirst()
                .orElse(null);
    }

    public String obtainAssistantAnswerText() {
        return obtainMessageContents()
                .stream()
                .filter(s -> s.getType().equals(AgentMessageContentTypeEnum.ANSWER.getValue()))
                .map(s -> CastUtils.cast(s, AbstractBlockDeltaContentMetadata.class))
                .map(AbstractBlockDeltaContentMetadata::getValue)
                .collect(Collectors.joining());
    }

    public String obtainUserText() {
        return TextMessageMetadata.ofString(getContent());
    }

    public List<AbstractAssistantMessageContentMetadata> obtainMessageContents() {
        if (CollectionUtils.isEmpty(content)) {
            return List.of();
        }
        List<AbstractAssistantMessageContentMetadata> result = new LinkedList<>();
        for (Map<String, Object> block : content) {
            String id = Objects.toString(block.get(TypeIdNameMetadata.ID_FIELD_NAME), StringUtils.EMPTY);
            if (StringUtils.isEmpty(id)) {
                continue;
            }

            String type = Objects.toString(block.get(TypeIdNameMetadata.TYPE_FIELD_NAME), StringUtils.EMPTY);
            if (StringUtils.isEmpty(type)) {
                continue;
            }
            AgentMessageContentTypeEnum typeEnum = ValueEnum.ofEnum(AgentMessageContentTypeEnum.class, type);
            AbstractAssistantMessageContentMetadata messageContent = obtainBlock(id, typeEnum);
            result.add(messageContent);
        }
        return result;
    }

    public String obtainContentJsonString() {
        return StringUtils.defaultIfEmpty(contentJson, StringUtils.EMPTY);
    }

    public void updateContent(AbstractAssistantMessageContentMetadata item) {
        Optional<Map<String, Object>> optional = getContent().stream()
                .filter(block -> Objects.equals(item.getId(), block.get(IdEntity.ID_FIELD_NAME)))
                .filter(s -> s.get(TypeIdNameMetadata.TYPE_FIELD_NAME).equals(item.getType()))
                .findFirst();
        Map<String, Object> block;
        if (optional.isEmpty()) {
            block = new LinkedHashMap<>();
            getContent().add(block);
        } else {
            block = optional.get();
        }

        block.putAll(CastUtils.convertValue(item, CastUtils.MAP_TYPE_REFERENCE));
        contentJson = SystemException.convertSupplier(() -> CastUtils.getObjectMapper().writeValueAsString(content));
    }

    public String getLastSseEventId() {
        if (CollectionUtils.isEmpty(content)) {
            return null;
        }
        Map<String, Object> last = content.getLast();
        if (MapUtils.isEmpty(last)) {
            return null;
        }
        AbstractAssistantMessageContentMetadata content = obtainMessageContents().stream()
                .filter(s -> StringUtils.isNotEmpty(s.getSseEventId()))
                .reduce((first, second) -> second)
                .orElse(null);
        if (content == null) {
            return null;
        }
        return content.getSseEventId();
    }

    public List<AgentTokenUsageMetadata> obtainTokenUsageMetadata() {
        Object tokenUsage = metadata.get(TOKEN_USAGE_KEY);
        if (Objects.isNull(tokenUsage)) {
            return new LinkedList<>();
        }
        return CastUtils.convertValue(tokenUsage, new TypeReference<>() { });
    }

    public void saveAgentTokenUsageMetadata(AgentTokenUsageMetadata tokenUsageMetadata) {
        List<AgentTokenUsageMetadata> agentTokenUsageMetadata = obtainTokenUsageMetadata();
        Optional<AgentTokenUsageMetadata> optional = agentTokenUsageMetadata.stream()
                .filter(s -> s.getUsageType().equals(tokenUsageMetadata.getUsageType()))
                .findFirst();
        if (optional.isEmpty()) {
            agentTokenUsageMetadata.add(tokenUsageMetadata);
        } else {
            AgentTokenUsageMetadata exist = optional.get();
            exist.setCachedTokens(exist.getCachedTokens() + tokenUsageMetadata.getCachedTokens());
            exist.setInputTokens(exist.getInputTokens() + tokenUsageMetadata.getInputTokens());
            exist.setOutputTokens(exist.getOutputTokens() + tokenUsageMetadata.getOutputTokens());
        }

        metadata.put(TOKEN_USAGE_KEY, agentTokenUsageMetadata);
        metadataJson = SystemException.convertSupplier(() -> CastUtils.getObjectMapper().writeValueAsString(metadata));
    }

    public String obtainMetadataJsonString() {
        return StringUtils.defaultIfEmpty(metadataJson, StringUtils.EMPTY);
    }
}
