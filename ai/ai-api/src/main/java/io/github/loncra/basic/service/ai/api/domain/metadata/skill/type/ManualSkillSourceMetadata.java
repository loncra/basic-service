package io.github.loncra.basic.service.ai.api.domain.metadata.skill.type;

import io.github.loncra.basic.service.ai.api.domain.metadata.skill.AbstractSkillSourceMetadata;
import io.github.loncra.basic.service.ai.api.enumerate.hub.SkillSourceTypeEnum;
import io.github.loncra.framework.commons.tree.Tree;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.AntPathMatcher;

import java.io.Serial;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 广场托管目录（对象存储前缀）
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ManualSkillSourceMetadata extends AbstractSkillSourceMetadata implements Tree<String, List<ManualSkillSourceMetadata>> {

    @Serial
    private static final long serialVersionUID = 3950614278305961423L;

    private static final String FILE_SUFFIX_REGEX = ".+\\.[^./\\\\]+";

    private String title;

    private String path;

    private String content;

    private List<Tree<String, List<ManualSkillSourceMetadata>>> children = new LinkedList<>();

    @Override
    public SkillSourceTypeEnum getType() {
        return SkillSourceTypeEnum.MANUAL;
    }

    @Override
    public List<Tree<String, List<ManualSkillSourceMetadata>>> getChildren() {
        return children;
    }

    @Override
    public String getParent() {
        return Stream.of(StringUtils.splitByWholeSeparator(path, AntPathMatcher.DEFAULT_PATH_SEPARATOR))
                .filter(StringUtils::isNotBlank)
                .filter(s -> !s.matches(FILE_SUFFIX_REGEX))
                .collect(Collectors.joining(AntPathMatcher.DEFAULT_PATH_SEPARATOR));
    }
}
