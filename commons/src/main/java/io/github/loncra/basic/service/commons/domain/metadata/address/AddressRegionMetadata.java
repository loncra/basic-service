package io.github.loncra.basic.service.commons.domain.metadata.address;

import io.github.loncra.framework.commons.CastUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.Serial;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 带地址信息的区域元数据
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AddressRegionMetadata extends RegionMetadata {

    @Serial
    private static final long serialVersionUID = -2107580568101381048L;

    /**
     * 地址
     */
    private String address;

    @Override
    public String getFullyRegionName() {
        return StringUtils.join(Stream.of(getProvince(), getCity(), getDistrict(), getAddress())
                                        .filter(StringUtils::isNotEmpty)
                                        .collect(Collectors.toList()), CastUtils.COMMA);
    }

    public void copyTo(AddressRegionMetadata dest) {
        super.copyTo(dest);

        if (StringUtils.isEmpty(getAddress())) {
            setAddress(dest.getAddress());
        }
    }
}
