package io.github.loncra.basic.service.auth.server.controller.user;

import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.basic.service.commons.domain.metadata.ExportDataMetadata;
import io.github.loncra.basic.service.commons.enumerate.ImportExportTypeEnum;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;

/**
 * 用户导出请求构造
 *
 * @author maurice.chen
 */
@UtilityClass
public class UserExportSupport {

    public ExportDataMetadata createExportData(
            HttpServletRequest request,
            AuditAuthenticationToken token,
            ImportExportTypeEnum type
    ) {
        ExportDataMetadata dto = new ExportDataMetadata();
        dto.setFilename(type.getName() + CastUtils.UNDERSCORE + System.currentTimeMillis() + SystemConstants.EXCEL_SUFFIX_NAME);
        dto.setType(type);
        dto.getMetadata().put(SystemConstants.QUERY_KEY, request.getParameterMap());
        dto.setPrincipal(token.getName());
        return dto;
    }
}
