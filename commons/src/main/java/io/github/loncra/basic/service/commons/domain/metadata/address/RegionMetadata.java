package io.github.loncra.basic.service.commons.domain.metadata.address;

import io.github.loncra.framework.commons.CastUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * 区域元数据
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode
public class RegionMetadata implements Serializable {

    @Serial
    private static final long serialVersionUID = 3470046783490160867L;

    public static final String SIMPLE_NAME = "region";
    /**
     * 省
     */
    private String province;

    /**
     * 市
     */
    private String city;

    /**
     * 区域
     */
    private String district;

    /**
     * 省代码
     */
    private String provinceCode;

    /**
     * 城市代码
     */
    private String cityCode;

    /**
     * 行政区域代码
     */
    private String districtCode;

    /**
     * 区号
     */
    private String areaCode;

    public String getFullyRegionName() {
        List<String> regions = Stream.of(province, city, district)
                .filter(Objects::nonNull)
                .toList();
        return StringUtils.join(regions, CastUtils.COMMA);
    }

    public String getRegionName() {
        List<String> regions = Stream.of(province, city)
                .filter(Objects::nonNull)
                .toList();
        return StringUtils.join(regions, CastUtils.COMMA);
    }

    public List<String> getFullyRegionCode() {
        List<String> result = new ArrayList<>();
        if (StringUtils.isNotBlank(provinceCode)) {
            result.add(provinceCode);
        }
        if (StringUtils.isNotBlank(cityCode)) {
            result.add(cityCode);
        }
        if (StringUtils.isNotBlank(districtCode)) {
            result.add(districtCode);
        }
        return result;
    }

    public void copyTo(RegionMetadata dest) {

        if (StringUtils.isEmpty(getCityCode()) && StringUtils.isEmpty(getCity())) {
            setCityCode(dest.getCityCode());
            setCity(dest.getCity());
        }

        if (StringUtils.isEmpty(getProvinceCode()) && StringUtils.isEmpty(getProvince())) {
            setProvinceCode(dest.getProvinceCode());
            setProvince(dest.getProvince());
        }

        if (StringUtils.isEmpty(getDistrict()) && StringUtils.isEmpty(getDistrictCode())) {
            setDistrictCode(dest.getDistrictCode());
            setDistrict(dest.getDistrict());
        }

        if (StringUtils.isEmpty(getAreaCode())) {
            setAreaCode(dest.getAreaCode());
        }
    }

}
