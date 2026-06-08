package io.github.loncra.basic.service.message.server.service;

import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import io.github.loncra.basic.service.commons.constants.PrincipalDetailsConstants;
import io.github.loncra.basic.service.message.api.enumerate.MessageTypeEnum;
import io.github.loncra.basic.service.message.server.dao.SiteMessageDao;
import io.github.loncra.basic.service.message.server.domain.body.site.ReadSiteMessageResponseBody;
import io.github.loncra.basic.service.message.server.domain.entity.BasicMessageEntity;
import io.github.loncra.basic.service.message.server.domain.entity.SiteMessageEntity;
import io.github.loncra.basic.service.resource.api.service.AttachmentServiceClient;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.id.IdEntity;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import io.github.loncra.framework.security.audit.SimpleAuditPrincipal;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import lombok.RequiredArgsConstructor;
import nl.basjes.parse.useragent.utils.springframework.util.Assert;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * tb_site_message 的业务逻辑
 *
 * <p>Table: tb_site_message - 站内信消息</p>
 *
 * @author maurice.chen
 * @see SiteMessageEntity
 * @since 2021-12-10 09:02:07
 */
@Service
@RequiredArgsConstructor
public class SiteMessageService extends BasicService<SiteMessageDao, SiteMessageEntity> {

    private final AttachmentServiceClient attachmentServiceClient;

    /**
     * 计数站内信未读数量
     *
     * @return 按类型分组的未读数量
     */
    public Map<Long, Long> countUnreadQuantity(AuditAuthenticationToken token) {
        List<SiteMessageEntity> list = lambdaQuery()
                .select(IdEntity::getId, BasicMessageEntity::getType)
                .eq(SiteMessageEntity::getReadable, YesOrNo.Yes.getValue())
                .eq(SiteMessageEntity::getToUser, token.getName())
                .list();
        return list.stream()
                .collect(Collectors.groupingBy(e -> e.getType().getValue().longValue(), Collectors.counting()));
    }

    /**
     * 阅读站内信
     *
     * @param types 站内信类型集合
     */
    @Transactional(rollbackFor = Exception.class)
    public void read(List<Long> types, AuditAuthenticationToken token) {
        Date now = new Date();

        LambdaUpdateChainWrapper<SiteMessageEntity> wrapper = lambdaUpdate()
                .set(SiteMessageEntity::getReadable, YesOrNo.No.getValue())
                .set(SiteMessageEntity::getReadTime, now)
                .eq(SiteMessageEntity::getToUser, token.getName())
                .eq(SiteMessageEntity::getReadable, YesOrNo.Yes.getValue());

        if (CollectionUtils.isNotEmpty(types)) {
            wrapper = wrapper.in(SiteMessageEntity::getType, types);
        }

        wrapper.update();
    }

    @Transactional(rollbackFor = Exception.class)
    public SiteMessageEntity read(Long id, AuditAuthenticationToken token) {
        SiteMessageEntity entity = get(id);

        Assert.notNull(entity, "找不到 ID 为 [" + id + "] 的站内信消息");
        PrincipalDetailsConstants.equals(new SimpleAuditPrincipal(entity.getToUser()), token);
        YesOrNo beforeReadable = entity.getReadable();

        entity.setReadable(YesOrNo.No);
        entity.setReadTime(Instant.now());

        updateById(entity);

        ReadSiteMessageResponseBody body = CastUtils.of(entity, ReadSiteMessageResponseBody.class);
        body.setBeforeReadable(beforeReadable);

        return body;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteRead(List<Long> types, AuditAuthenticationToken token) {
        LambdaUpdateChainWrapper<SiteMessageEntity> wrapper = lambdaUpdate()
                .eq(SiteMessageEntity::getReadable, YesOrNo.No.getValue())
                .eq(SiteMessageEntity::getToUser, token.getName());

        if (CollectionUtils.isNotEmpty(types)) {
            wrapper.in(BasicMessageEntity::getType, types);
        }

        wrapper.remove();
    }

    @Transactional(rollbackFor = Exception.class)
    public SiteMessageEntity getForFrontEnd(Long id, YesOrNo read) {
        SiteMessageEntity entity = get(id);
        if (YesOrNo.Yes.equals(read) && YesOrNo.No.equals(entity.getReadable())) {
            entity.setReadable(YesOrNo.No);
            updateById(entity);
        }
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Collection<? extends Serializable> ids, boolean errorThrow, boolean useFill) {
        int result = ids.stream().mapToInt(id -> deleteById(id, useFill)).sum();
        if (result != ids.size() && errorThrow) {
            String msg = "删除 id 为 [" + ids + "] 的 [站内信信息] 失败";
            throw new SystemException(msg);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Serializable id, boolean useFill) {
        return deleteByEntity(get(id));
    }

    @Override
    public int deleteByEntity(Collection<SiteMessageEntity> entities, boolean errorThrow) {
        int result = entities.stream().mapToInt(this::deleteByEntity).sum();
        if (result != entities.size() && errorThrow) {
            String msg = "删除 id 为 [" + entities.stream().map(IdEntity::getId).toList() + "] 的 [站内信信息] 失败";
            throw new SystemException(msg);
        }
        return result;
    }

    @Override
    public int deleteByEntity(SiteMessageEntity entity) {
        if (CollectionUtils.isNotEmpty(entity.getAttachmentList())) {
            attachmentServiceClient.deleteAttachment(new LinkedList<>(entity.getAttachmentList()), new LinkedHashMap<>());
        }
        return super.deleteByEntity(entity);
    }

    public List<MessageTypeEnum> getMessageTypeList(AuditAuthenticationToken token) {
        // FIXME 这里优化把每个类型的用户对应的消息类型写在 ResourceSourceEnum 字段里
        return List.of(MessageTypeEnum.SYSTEM, MessageTypeEnum.NOTICE, MessageTypeEnum.WARNING);
    }


}
