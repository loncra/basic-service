package io.github.loncra.basic.service.message.server.resolver.support.sms.alibaba;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.*;
import io.github.loncra.basic.service.commons.enumerate.CloudChannelEnum;
import io.github.loncra.basic.service.message.server.domain.body.sms.SmsSignResponseBody;
import io.github.loncra.basic.service.message.server.resolver.support.sms.SmsSignResolver;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.id.StringIdEntity;
import io.github.loncra.framework.commons.page.Page;
import io.github.loncra.framework.commons.page.PageRequest;
import io.github.loncra.framework.commons.page.TotalPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlibabaCloudSmsSignResolver implements SmsSignResolver {

    private final Client alibabaClient;

    @Override
    public CloudChannelEnum getType() {
        return CloudChannelEnum.ALIBABA_CLOUD;
    }

    @Override
    public GetSmsSignResponseBody get(String id) {
        GetSmsSignRequest getSmsSignRequest = new GetSmsSignRequest();
        getSmsSignRequest.setSignName(id);
        GetSmsSignResponse response = SystemException.convertSupplier(
                () -> alibabaClient.getSmsSign(getSmsSignRequest),
                "[" + getType().getName() + "] 获取短信签名错误"
        );
        SystemException.isTrue(
                HttpStatus.OK.getReasonPhrase().equals(response.getBody().getCode()),
                response.getBody().getMessage()
        );
        return response.getBody();
    }

    @Override
    public Page<SmsSignResponseBody> page(
            PageRequest request,
            Map<String, Object> query
    ) {
        QuerySmsSignListRequest querySmsSignListRequest = new QuerySmsSignListRequest();

        querySmsSignListRequest.setPageIndex(request.getNumber());
        querySmsSignListRequest.setPageSize(request.getSize());

        QuerySmsSignListResponseBody response = find(querySmsSignListRequest);
        List<SmsSignResponseBody> elements = new LinkedList<>();
        if(CollectionUtils.isNotEmpty(response.getSmsSignList())) {
            elements = response
                    .getSmsSignList()
                    .stream()
                    .map(this::createSmsSignResponseBody)
                    .sorted(Comparator.comparing(StringIdEntity::getCreationTime).reversed())
                    .toList();
        }
        return new TotalPage<>(request, elements, response.getTotalCount());
    }

    private QuerySmsSignListResponseBody find(
            QuerySmsSignListRequest querySmsSignListRequest
    ) {
        QuerySmsSignListResponse response = SystemException.convertSupplier(
                () -> alibabaClient.querySmsSignList(querySmsSignListRequest),
                "[" + getType().getName() + "] 获取短信签名列表出现错误"
        );
        SystemException.isTrue(
                HttpStatus.OK.getReasonPhrase().equals(response.getBody().getCode()),
                response.getBody().getMessage()
        );

        return response.getBody();
    }

    @Override
    public List<SmsSignResponseBody> find(Map<String, Object> query) {
        QuerySmsSignListRequest querySmsSignListRequest = new QuerySmsSignListRequest();

        querySmsSignListRequest.setPageIndex(1);
        querySmsSignListRequest.setPageSize(AlibabaCloudSmsChannelSender.DEFAULT_PAGE_SIZE);

        QuerySmsSignListResponseBody response = find(querySmsSignListRequest);

        if(CollectionUtils.isNotEmpty(response.getSmsSignList())) {
            return response
                    .getSmsSignList()
                    .stream()
                    .map(this::createSmsSignResponseBody)
                    .sorted(Comparator.comparing(StringIdEntity::getCreationTime).reversed())
                    .toList();
        }
        return Collections.emptyList();
    }

    private SmsSignResponseBody createSmsSignResponseBody(
            QuerySmsSignListResponseBody.QuerySmsSignListResponseBodySmsSignList smsSign
    ) {
        SmsSignResponseBody body = new SmsSignResponseBody();
        body.setName(smsSign.getSignName());
        body.setChannel(getType());
        body.setStatus(AlibabaCloudSmsChannelSender.createAuditStatus(smsSign.getAuditStatus()));
        body.setCreationTime(AlibabaCloudSmsChannelSender.ofDateString(smsSign.getCreateDate()));
        body.setId(smsSign.getSignName());

        body.getMetadata().putAll(CastUtils.convertValue(smsSign.getReason(), CastUtils.MAP_TYPE_REFERENCE));

        return body;
    }
}
