package io.github.loncra.basic.service.message.server.resolver.support.sms.alibaba;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.*;
import io.github.loncra.basic.service.commons.enumerate.CloudChannelEnum;
import io.github.loncra.basic.service.message.server.domain.body.sms.SmsTemplateResponseBody;
import io.github.loncra.basic.service.message.server.resolver.support.sms.SmsTemplateResolver;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.id.StringIdEntity;
import io.github.loncra.framework.commons.page.Page;
import io.github.loncra.framework.commons.page.PageRequest;
import io.github.loncra.framework.commons.page.TotalPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlibabaCloudSmsTemplateResolver implements SmsTemplateResolver {

    private final Client alibabaClient;

    @Override
    public CloudChannelEnum getType() {
        return CloudChannelEnum.ALIBABA_CLOUD;
    }

    @Override
    public List<SmsTemplateResponseBody> find(Map<String, Object> query) {
        QuerySmsTemplateListRequest request = new QuerySmsTemplateListRequest();

        request.setPageIndex(1);
        request.setPageSize(AlibabaCloudSmsChannelSender.DEFAULT_PAGE_SIZE);

        QuerySmsTemplateListResponseBody response = find(request);
        if (CollectionUtils.isNotEmpty(response.getSmsTemplateList())) {
            return response.getSmsTemplateList()
                    .stream()
                    .map(this::createSmsTemplateResponseBody)
                    .sorted(Comparator.comparing(StringIdEntity::getCreationTime).reversed())
                    .toList();
        }
        return Collections.emptyList();
    }

    private QuerySmsTemplateListResponseBody find(
            QuerySmsTemplateListRequest request
    ) {
        QuerySmsTemplateListResponse response = SystemException.convertSupplier(
                () -> alibabaClient.querySmsTemplateList(request),
                "[" + getType().getName() + "] 获取短信模版集合错误"
        );
        SystemException.isTrue(
                HttpStatus.OK.getReasonPhrase().equals(response.getBody().getCode()),
                response.getBody().getMessage()
        );
        return response.getBody();
    }

    private SmsTemplateResponseBody createSmsTemplateResponseBody(
            QuerySmsTemplateListResponseBody.QuerySmsTemplateListResponseBodySmsTemplateList item
    ) {
        SmsTemplateResponseBody body = new SmsTemplateResponseBody();

        body.setName(item.getTemplateName());
        body.setChannel(getType());
        body.setContent(item.getTemplateContent());
        body.setType(AlibabaCloudSmsChannelSender.createMessageType(item.getOuterTemplateType()));
        body.setCreationTime(AlibabaCloudSmsChannelSender.ofDateString(item.getCreateDate()));
        body.setStatus(AlibabaCloudSmsChannelSender.createAuditStatus(item.getAuditStatus()));
        body.setId(item.getTemplateCode());
        body.setMetadata(CastUtils.convertValue(item.getReason(), CastUtils.MAP_TYPE_REFERENCE));

        return body;
    }

    @Override
    public Page<SmsTemplateResponseBody> page(
            PageRequest pageRequest,
            Map<String, Object> query
    ) {
        QuerySmsTemplateListRequest request = new QuerySmsTemplateListRequest();

        request.setPageIndex(pageRequest.getNumber());
        request.setPageSize(pageRequest.getSize());

        QuerySmsTemplateListResponseBody response = find(request);
        List<SmsTemplateResponseBody> elements = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(response.getSmsTemplateList())) {
            elements = response.getSmsTemplateList()
                    .stream()
                    .map(this::createSmsTemplateResponseBody)
                    .sorted(Comparator.comparing(StringIdEntity::getCreationTime).reversed())
                    .toList();
        }
        return new TotalPage<>(pageRequest, elements, response.getTotalCount());
    }

    @Override
    public GetSmsTemplateResponseBody get(String id) {
        GetSmsTemplateRequest request = new GetSmsTemplateRequest();
        request.setTemplateCode(id);
        GetSmsTemplateResponse response = SystemException.convertSupplier(
                () -> alibabaClient.getSmsTemplate(request),
                "[" + getType().getName() + "] 获取短信模版错误"
        );
        SystemException.isTrue(
                HttpStatus.OK.getReasonPhrase().equals(response.getBody().getCode()),
                response.getBody().getMessage()
        );

        return response.getBody();
    }
}
