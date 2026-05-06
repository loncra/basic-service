package io.github.loncra.basic.service.auth.server.domain.metdata;

import io.github.loncra.basic.service.auth.server.domain.entity.ResourceEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SyncPluginResourceMetadata implements Serializable {

    private List<ResourceEntity> resources = new LinkedList<>();

    private List<Long> deleteIds = new LinkedList<>();

    private List<String> applicationNames = new LinkedList<>();
}
