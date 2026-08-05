package io.github.loncra.basic.service.ai.server.domain;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import io.agentscope.core.tool.mcp.McpClientWrapper;
import io.agentscope.core.util.JsonUtils;
import io.github.loncra.basic.service.ai.server.enumerate.McpClientResultAssertionTypeEnum;
import io.github.loncra.framework.commons.id.metadata.IdValueMetadata;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class ContentAwareMcpClientWrapper extends McpClientWrapper {

    private final McpClientWrapper delegate;

    @Getter
    private final Map<String, List<IdValueMetadata<String, String>>> failureAssertions;

    public ContentAwareMcpClientWrapper(
            McpClientWrapper delegate,
            Map<String, List<IdValueMetadata<String, String>>> failureAssertions
    ) {
        super(delegate.getName());
        this.delegate = delegate;
        this.failureAssertions = failureAssertions;
    }

    // ---------- 关键：拦截 callTool，修正 isError ----------

    @Override
    public Mono<McpSchema.CallToolResult> callTool(
            String toolName,
            Map<String, Object> arguments
    ) {
        return delegate.callTool(toolName, arguments)
                .map(result -> correctErrorFlag(toolName, result));
    }

    @Override
    public Mono<McpSchema.CallToolResult> callTool(
            String toolName,
            Map<String, Object> arguments,
            Map<String, Object> meta
    ) {
        return delegate.callTool(toolName, arguments, meta)
                .map(result -> correctErrorFlag(toolName, result));
    }

    private McpSchema.CallToolResult correctErrorFlag(
            String toolName,
            McpSchema.CallToolResult result
    ) {
        // 如果 MCP server 已经标记 isError=true，不需要再检查
        if (Boolean.TRUE.equals(result.isError())) {
            return result;
        }
        // 检查内容是否包含通用业务错误
        if (MapUtils.isEmpty(failureAssertions)) {
            return result;
        }

        if (assertionError(result.content())) {
            log.warn("MCP tool '{}' returned business-level error in content, correcting isError to true", toolName);
            return McpSchema.CallToolResult.builder()
                    .content(result.content())
                    .isError(true)
                    .meta(result.meta())
                    .build();
        }
        return result;
    }

    /**
     * 通用的业务错误检测，不针对任何特定服务。
     * 通过 JSON 结构特征判断，而非硬编码字符串。
     */
    private boolean assertionError(List<McpSchema.Content> contents) {
        for (McpSchema.Content content : contents) {
            if (content instanceof McpSchema.TextContent textContent) {
                String text = textContent.text();
                if (log.isDebugEnabled()) {
                    log.debug("MCP tool '{}' content: {}", getName(), text);
                }
                if (StringUtils.isBlank(text)) {
                    continue;
                }

                if (JsonUtils.isValidJsonObject(text)) {
                    List<IdValueMetadata<String, String>> assertions = failureAssertions.get(McpClientResultAssertionTypeEnum.JSON.getName());
                    if (CollectionUtils.isEmpty(assertions)) {
                        continue;
                    }

                    for (IdValueMetadata<String, String> assertion : assertions) {
                        if (evaluateJsonAssertion(assertion, text)) {
                            log.warn("MCP tool '{}' JSON assertion matched: path={}, expected={}, content={}",
                                    getName(), assertion.getId(), assertion.getValue(), text);
                            return true;
                        }
                    }
                } else {
                    List<IdValueMetadata<String, String>> assertions =
                            failureAssertions.get(McpClientResultAssertionTypeEnum.TEXT.getName());
                    if (CollectionUtils.isEmpty(assertions)) {
                        continue;
                    }

                    for (IdValueMetadata<String, String> assertion : assertions) {
                        if (evaluateTextAssertion(assertion, text)) {
                            log.warn("MCP tool '{}' TEXT assertion matched: desc={}, expression={}, content={}",
                                    getName(), assertion.getId(), assertion.getValue(), text);
                            return true;
                        }
                    }
                }

            }
        }
        return false;
    }

    private static boolean evaluateJsonAssertion(
            IdValueMetadata<String, String> assertion,
            String jsonText
    ) {
        String path = assertion.getId();
        String expression = assertion.getValue();

        try {
            Object actual = JsonPath.read(jsonText, path);
            if (actual == null) {
                return false;
            }
            // 统一转字符串比较
            return evaluateAssertion(expression, Objects.toString(actual, StringUtils.EMPTY));
        } catch (PathNotFoundException e) {
            // 路径不存在 → 未命中
            return false;
        } catch (Exception e) {
            log.warn("JSON assertion evaluation error: path={}, error={}",
                    path, e.getMessage());
            return false;
        }
    }

    private static boolean evaluateTextAssertion(
            IdValueMetadata<String, String> assertion,
            String text
    ) {
        String expression = assertion.getValue();

        try {
            return evaluateAssertion(expression, text);
        } catch (Exception e) {
            log.warn("TEXT assertion evaluation error: expression={}, desc={}, error={}",
                    expression, assertion.getId(), e.getMessage());
            return false;
        }
    }

    private static boolean evaluateAssertion(String regex, String value) {
        return RegExUtils.dotAll(regex).matcher(value).find();
    }

    // ---------- 其余方法全部委托 ----------

    @Override
    public Mono<Void> initialize() {
        return delegate.initialize();
    }

    @Override
    public Mono<List<McpSchema.Tool>> listTools() {
        return delegate.listTools();
    }

    @Override
    public boolean isInitialized() {
        return delegate.isInitialized();
    }

    @Override
    public McpSchema.Tool getCachedTool(String toolName) {
        return delegate.getCachedTool(toolName);
    }

    @Override
    public void close() {
        delegate.close();
    }

}
