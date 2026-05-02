package io.github.loncra.basic.service.commons.domain.metadata.address;

import io.github.loncra.framework.commons.CastUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.Serial;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 待国家的区域元数据信息
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CountryRegionMetadata extends RegionMetadata {

    @Serial
    private static final long serialVersionUID = -6071004096403468504L;

    /**
     * 国家
     */
    private String country;

    /**
     * 国家代码
     */
    private String countryCode;

    @Override
    public String getFullyRegionName() {
        return StringUtils.join(Stream.of(getCountry(), getProvince(), getCity(), getDistrict())
                                        .filter(Objects::nonNull)
                                        .collect(Collectors.toList()), CastUtils.COMMA);
    }

    @Override
    public String getRegionName() {
        List<String> all = Stream.of(getCountry(), getProvince(), getCity())
                .filter(Objects::nonNull)
                .toList();
        return StringUtils.join(all, CastUtils.COMMA);
    }
}
