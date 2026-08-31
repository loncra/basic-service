package io.github.loncra.basic.service.ai.api.domain.metadata.skill.type;

import io.github.loncra.basic.service.ai.api.domain.metadata.skill.AbstractSkillSourceMetadata;
import io.github.loncra.basic.service.ai.api.enumerate.hub.SkillSourceTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * Git 仓库来源
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GitSkillSourceMetadata extends AbstractSkillSourceMetadata {

    @Serial
    private static final long serialVersionUID = 1738492056183749201L;

    /**
     * 公开 HTTPS 仓库地址
     */
    private String url;

    /**
     * 要跟踪的 Git 引用：分支名或 tag（如 main、v1.2.0）。
     * 可空，表示使用仓库默认分支。
     * 同一 ref 的 tip 会随远端推送变化；具体钉在哪一次提交看 {@link #sha}。
     */
    private String ref;
    /**
     * 已物化内容对应的 commit SHA（完整 40 位）。
     * 用于复现与日后对比是否有新提交；ingest 成功后由后端写入，不宜作为日常必填项。
     * 若运营显式填写，则按该提交检出（冻结版本，不再跟随 ref 移动）。
     */
    private String sha;

    /**
     * 仓库内 Skill 根目录，相对仓库根，可空表示整个仓库即为该 Skill。
     * 合集仓必填，例如 skills/pdf、template；物化时只上传该目录下的文件到 ai/skill/{id}/。
     */
    private String path;

    @Override
    public String getType() {
        return SkillSourceTypeEnum.GIT.toString();
    }
}
