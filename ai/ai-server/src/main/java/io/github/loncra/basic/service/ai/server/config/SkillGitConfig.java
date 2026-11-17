package io.github.loncra.basic.service.ai.server.config;

import io.github.loncra.framework.commons.TimeProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Data
@Component
@NoArgsConstructor
@ConfigurationProperties("loncra.basic-service.ai.app.skill.git")
public class SkillGitConfig {

    private String cloneTempDirectory = "skill-source-git-";
    private TimeProperties timeout = new TimeProperties(3, TimeUnit.MINUTES);
}
