package io.github.loncra.basic.service.ai.server.domain.body;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * Skill 目录打不可变版本
 *
 * @author maurice.chen
 */
@Data
public class SkillPackageSnapshotRequestBody implements Serializable {

    @Serial
    private static final long serialVersionUID = 4182736501928475611L;

    @NotBlank
    private String releaseVersion;

    private String changelog;
}
