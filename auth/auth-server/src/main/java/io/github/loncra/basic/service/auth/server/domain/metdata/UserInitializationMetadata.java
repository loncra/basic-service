package io.github.loncra.basic.service.auth.server.domain.metdata;

import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户初始化元数据信息
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
public class UserInitializationMetadata implements Serializable {

    @Serial
    private static final long serialVersionUID = -8419236109054403628L;

    /**
     * 是否可更新密码：1.是、0.否
     */
    private YesOrNo randomPassword = YesOrNo.Yes;

    /**
     * 是否可更新登录账户：1.是、0.否
     */
    private YesOrNo randomUsername = YesOrNo.Yes;
}
