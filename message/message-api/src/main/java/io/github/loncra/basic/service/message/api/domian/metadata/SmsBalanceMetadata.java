package io.github.loncra.basic.service.message.api.domian.metadata;

import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.id.IdEntity;
import io.github.loncra.framework.commons.id.metadata.IdValueMetadata;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

/**
 * 短信余额实体
 *
 * @author maurice
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor(staticName = "of")
public class SmsBalanceMetadata extends IdEntity<String> {

    @Serial
    private static final long serialVersionUID = 4834851659384448629L;

    /**
     * 据道名称
     */
    private String channel;

    /**
     * 余额
     */
    private List<RestResult<IdValueMetadata<String, BigDecimal>>> balances = new LinkedList<>();
}
