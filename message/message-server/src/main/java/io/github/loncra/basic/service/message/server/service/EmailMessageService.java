package io.github.loncra.basic.service.message.server.service;

import io.github.loncra.basic.service.message.server.dao.EmailMessageDao;
import io.github.loncra.basic.service.message.server.domain.entity.EmailMessageEntity;
import io.github.loncra.basic.service.resource.api.service.AttachmentServiceClient;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.id.IdEntity;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;

/**
 * tb_email_message 的业务逻辑
 *
 * <p>Table: tb_email_message - 邮件消息</p>
 *
 * @author maurice.chen
 * @see EmailMessageEntity
 * @since 2021-12-10 09:02:07
 */
@Service
@RequiredArgsConstructor
public class EmailMessageService extends BasicService<EmailMessageDao, EmailMessageEntity> {

    private final AttachmentServiceClient attachmentServiceClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Collection<? extends Serializable> ids, boolean errorThrow, boolean useFill) {
        int result = ids.stream().mapToInt(id -> deleteById(id, useFill)).sum();
        if (result != ids.size() && errorThrow) {
            String msg = "删除 id 为 [" + ids + "] 的 [邮箱信息] 数据不成功";
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
    public int deleteByEntity(Collection<EmailMessageEntity> entities, boolean errorThrow) {
        int result = entities.stream().mapToInt(this::deleteByEntity).sum();
        if (result != entities.size() && errorThrow) {
            String msg = "删除 id 为 [" + entities.stream().map(IdEntity::getId).toList() + "] 的 [邮箱信息] 数据不成功";
            throw new SystemException(msg);
        }
        return result;
    }

    @Override
    public int deleteByEntity(EmailMessageEntity entity) {
        attachmentServiceClient.deleteAttachment(new LinkedList<>(entity.getAttachmentList()), new LinkedHashMap<>());
        return super.deleteByEntity(entity);
    }
}
