package io.github.loncra.basic.service.ai.server.resolver.skill;

import cn.hutool.core.io.FileUtil;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.harness.agent.filesystem.AbstractFilesystem;
import io.agentscope.harness.agent.filesystem.model.FileUploadResponse;
import io.github.loncra.basic.service.ai.api.domain.metadata.skill.type.GitSkillSourceMetadata;
import io.github.loncra.basic.service.ai.api.enumerate.hub.SkillSourceTypeEnum;
import io.github.loncra.basic.service.ai.server.config.SkillGitConfig;
import io.github.loncra.basic.service.ai.server.domain.entity.hub.AiSkillPackageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.SkillPackageMetadata;
import io.github.loncra.basic.service.ai.server.resolver.SkillSourceResolver;
import io.github.loncra.basic.service.ai.server.service.hub.AiSkillPackageService;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.exception.ServiceException;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.id.metadata.TypeIdNameMetadata;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

/**
 * 公开 Git 仓 clone 后上传到 {@code ai/skill/{id}/}。
 *
 * @author maurice.chen
 */
@Component
@RequiredArgsConstructor
public class GitSkillSourceResolver implements SkillSourceResolver {

    private static final String GIT_DIR = ".git";

    private final AiSkillPackageService aiSkillPackageService;

    private final SkillGitConfig skillGitConfig;

    @Override
    public boolean isSupport(SkillSourceTypeEnum sourceType) {
        return SkillSourceTypeEnum.GIT.equals(sourceType);
    }

    @Override
    public void ingest(AiSkillPackageEntity entity) {
        SkillPackageMetadata packageMetadata = entity.obtainMetadata();
        GitSkillSourceMetadata source = Objects.isNull(packageMetadata) ? null : packageMetadata.obtainSource();
        SystemException.isTrue(Objects.nonNull(source), () -> new ServiceException("Skill 目录 [" + entity.getPackageKey() + "] 缺少 Git 来源配置"));
        SystemException.isTrue(StringUtils.isNotBlank(source.getUrl()), () -> new ServiceException("Skill 目录 [" + entity.getPackageKey() + "] 未填写 Git 地址"));

        Path local = SystemException.convertSupplier(
                () -> Files.createTempDirectory(skillGitConfig.getCloneTempDirectory()),
                "[git skill ingest] 创建临时目录失败"
        );
        try {
            String ref = StringUtils.trimToNull(source.getRef());
            String sha = StringUtils.trimToNull(source.getSha());
            CloneCommand cloneCommand = Git.cloneRepository()
                    .setURI(source.getUrl().trim())
                    .setDirectory(local.toFile())
                    .setTimeout((int)skillGitConfig.getTimeout().toSeconds());
            if (Objects.nonNull(ref)) {
                cloneCommand.setBranch(ref);
            }
            try (Git git = cloneCommand.call()) {
                if (Objects.nonNull(sha)) {
                    git.checkout().setName(sha).call();
                } else if (Objects.nonNull(ref)) {
                    git.checkout().setName(ref).call();
                }
                ObjectId head = git.getRepository().resolve(Constants.HEAD);
                SystemException.isTrue(
                        Objects.nonNull(head),
                        () -> new ServiceException("Skill 目录 [" + entity.getPackageKey() + "] 无法解析 Git HEAD")
                );
                writeResolvedSha(entity, source, head.getName());
                Path uploadRoot = resolveSkillRoot(local, source.getPath(), entity.getPackageKey());
                uploadTree(uploadRoot, entity.getId());
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("拉取 Git 仓库失败: " + e.getMessage(), e);
        } finally {
            FileUtil.del(local.toFile());
        }
    }

    private void writeResolvedSha(AiSkillPackageEntity entity, GitSkillSourceMetadata source, String sha) {
        source.setSha(sha);
        Map<String, Object> metadata = entity.getMetadata();
        if (Objects.isNull(metadata)) {
            metadata = new LinkedHashMap<>();
            entity.setMetadata(metadata);
        }
        Map<String, Object> sourceMap = new LinkedHashMap<>(CastUtils.convertValue(source, CastUtils.MAP_TYPE_REFERENCE));
        sourceMap.put(TypeIdNameMetadata.TYPE_FIELD_NAME, source.getType().getValue());
        metadata.put(AiSkillPackageEntity.SOURCE_FIELD, sourceMap);
    }

    private Path resolveSkillRoot(Path local, String path, String packageKey) {
        if (StringUtils.isBlank(path)) {
            return local;
        }
        String relative = toUnixPath(Path.of(path.trim()))
                .replaceAll("^/+", StringUtils.EMPTY)
                .replaceAll("/+$", StringUtils.EMPTY);
        if (StringUtils.isBlank(relative)) {
            return local;
        }
        SystemException.isTrue(
                !relative.contains(".."),
                () -> new ServiceException("Skill 目录 [" + packageKey + "] 的 path 非法")
        );
        Path base = local.toAbsolutePath().normalize();
        Path root = base.resolve(relative).normalize();
        SystemException.isTrue(
                root.startsWith(base),
                () -> new ServiceException("Skill 目录 [" + packageKey + "] 的 path 非法")
        );
        SystemException.isTrue(
                Files.isDirectory(root),
                () -> new ServiceException("仓库内找不到目录 [" + relative + "]")
        );
        return root;
    }

    private void uploadTree(Path root, Long packageId) {
        List<Path> files = SystemException.convertSupplier(
                () -> {
                    try (Stream<Path> walk = Files.walk(root)) {
                        return walk.filter(Files::isRegularFile)
                                .filter(path -> !isGitPath(root.relativize(path)))
                                .toList();
                    }
                },
                "[skill] 遍历 Git 工作区失败"
        );

        List<Map.Entry<String, byte[]>> entries = new ArrayList<>(files.size());
        for (Path file : files) {
            String relative = toUnixPath(root.relativize(file));
            byte[] content = SystemException.convertSupplier(
                    () -> Files.readAllBytes(file),
                    "[skill] 读取文件失败: " + relative
            );
            entries.add(Map.entry(packageId + AntPathMatcher.DEFAULT_PATH_SEPARATOR + relative, content));
        }

        AbstractFilesystem filesystem = aiSkillPackageService.getSourceOfTruthFileSystem();
        List<FileUploadResponse> responses = filesystem.uploadFiles(RuntimeContext.empty(), entries);
        for (FileUploadResponse response : responses) {
            SystemException.isTrue(
                    response.isSuccess(),
                    () -> new ServiceException("[skill] 文件上传失败 [" + response.path() + "]: " + response.error())
            );
        }
    }

    private boolean isGitPath(Path relative) {
        for (Path part : relative) {
            if (GIT_DIR.equals(part.toString())) {
                return true;
            }
        }
        return false;
    }

    private String toUnixPath(Path relative) {
        return Strings.CS.replace(relative.toString(), String.valueOf(IOUtils.DIR_SEPARATOR_WINDOWS), AntPathMatcher.DEFAULT_PATH_SEPARATOR);
    }
}
