package io.github.loncra.basic.service.ai.server.domain;

import io.agentscope.core.state.State;
import io.github.loncra.basic.service.ai.api.enumerate.ClarifyModeStatusEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedList;
import java.util.List;

@Data
@NoArgsConstructor
public class ClarifyModeContextState implements State {

    private ClarifyModeStatusEnum status = ClarifyModeStatusEnum.READY;

    private List<String> questions = new LinkedList<>();

    private int maxClarifyRounds;
}
