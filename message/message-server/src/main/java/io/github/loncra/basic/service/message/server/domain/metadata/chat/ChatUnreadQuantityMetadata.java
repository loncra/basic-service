package io.github.loncra.basic.service.message.server.domain.metadata.chat;

import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class ChatUnreadQuantityMetadata implements Serializable {

    private Long count;

    private YesOrNo muted;
}
