package io.github.loncra.basic.service.commons.domain.metadata.address;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.awt.geom.Point2D;
import java.io.Serial;
import java.util.Objects;

/**
 * 带有经纬度坐标点的区域元数据信息
 *
 * @author maurice.chen
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LocationRegionMetadata extends AddressRegionMetadata {

    @Serial
    private static final long serialVersionUID = -6283041190317541850L;

    /**
     * 坐标点
     */
    private Point2D.Double location;

    public void copyTo(LocationRegionMetadata dest) {
        super.copyTo(dest);
        if (Objects.isNull(getLocation())) {
            setLocation(dest.getLocation());
        }
    }
}
