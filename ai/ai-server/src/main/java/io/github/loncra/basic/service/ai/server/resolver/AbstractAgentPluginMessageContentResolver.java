package io.github.loncra.basic.service.ai.server.resolver;

import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.commons.domain.metadata.chat.InstructionMessageMetadata;
import io.github.loncra.framework.commons.id.metadata.IdValueMetadata;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public abstract class AbstractAgentPluginMessageContentResolver
        implements AgentPluginMessageContentResolver {

    protected abstract String getPrefix();

    @Override
    public List<InstructionMessageMetadata> getHits(AgentMessageEntity userMessage) {
        if (userMessage == null) {
            return List.of();
        }
        return InstructionMessageMetadata.ofList(userMessage.getContent()).stream()
                .filter(s -> Objects.equals(getPrefix(), s.getPrefix()))
                .toList();
    }

    protected Long parsePackageId(InstructionMessageMetadata hit) {
        if (hit == null) {
            return null;
        }
        IdValueMetadata<String, String> value = hit.getValue();
        if (value == null || StringUtils.isBlank(value.getId())) {
            return null;
        }
        String id = value.getId().trim();
        if (!NumberUtils.isDigits(id)) {
            return null;
        }
        long packageId = NumberUtils.toLong(id);
        return packageId > 0 ? packageId : null;
    }

    protected Set<Long> getPackageIds(List<InstructionMessageMetadata> hits) {
        Set<Long> packageIds = new LinkedHashSet<>();
        for (InstructionMessageMetadata hit : hits) {
            Long packageId = parsePackageId(hit);
            if (packageId != null) {
                packageIds.add(packageId);
            }
        }
        return packageIds;
    }
}
