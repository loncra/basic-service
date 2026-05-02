package io.github.loncra.basic.service.commons.domain.metadata.address;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * ip 区域原数据
 *
 * @author maurice.chen
 */
@Data
@EqualsAndHashCode
@NoArgsConstructor(force = true)
@RequiredArgsConstructor(staticName = "of")
public class IpRegionMetadata implements Serializable {

    @Serial
    private static final long serialVersionUID = -357706294703499044L;

    public static final String IP_ADDRESS_NAME = "ipAddress";

    /**
     * ip 地址
     */
    @NonNull
    private String ipAddress;

    /**
     * 区域信息
     */
    private RegionMetadata regionMetadata;

}
