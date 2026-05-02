package io.github.loncra.basic.service.commons.domain.metadata.address;

import io.github.loncra.framework.commons.CastUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.Serial;
import java.util.List;
import java.util.stream.Stream;

/**
 * 待国家的区域元数据信息
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CountryAddressRegionMetadata extends CountryRegionMetadata {

    @Serial
    private static final long serialVersionUID = -6071004096403468504L;

    /**
     * 地址
     */
    private String address;

    @Override
    public String getFullyRegionName() {
        List<String> fullyRegionName = Stream.of(getCountry(), getProvince(), getCity(), getDistrict(), getAddress())
                .filter(StringUtils::isNotEmpty)
                .toList();
        return StringUtils.join(fullyRegionName, CastUtils.COMMA);
    }

}
