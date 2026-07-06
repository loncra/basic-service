package io.github.loncra.basic.service.message.server.resolver.support.chat.call;

import com.google.protobuf.util.JsonFormat;
import io.github.loncra.basic.service.commons.constants.PrincipalDetailsConstants;
import io.github.loncra.basic.service.message.server.config.UserChatCallConfig;
import io.github.loncra.basic.service.message.server.domain.body.chat.UserChatCallResponseBody;
import io.github.loncra.basic.service.message.server.domain.entity.chat.call.UserChatCallEntity;
import io.github.loncra.basic.service.message.server.domain.entity.chat.call.UserChatCallParticipantEntity;
import io.github.loncra.basic.service.message.server.enumerate.chat.call.UserChatCallSceneEnum;
import io.github.loncra.basic.service.message.server.resolver.CallMediaServerResolver;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.id.metadata.IdValueMetadata;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import io.livekit.server.*;
import livekit.LivekitModels;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import retrofit2.Call;
import retrofit2.Response;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class LiveKitCallMediaServerResolver implements CallMediaServerResolver {

    public static final String DEFAULT_TYPE = "liveKit";

    private final UserChatCallConfig userChatCallConfig;

    @Override
    public String getType() {
        return DEFAULT_TYPE;
    }

    @Override
    public void create(
            UserChatCallEntity call,
            UserChatCallParticipantEntity caller,
            List<UserChatCallParticipantEntity> callParticipants
    ) {
        RoomServiceClient roomServiceClient = RoomServiceClient.createClient(
                userChatCallConfig.getLivekit().getHost(),
                userChatCallConfig.getLivekit().getSecret().getSecretId(),
                userChatCallConfig.getLivekit().getSecret().getSecretKey()
        );
        Call<LivekitModels.Room> roomServiceCall;
        if (UserChatCallSceneEnum.PRIVATE.equals(call.getScene())) {
            roomServiceCall = roomServiceClient.createRoom(call.getRoomId(), (int)userChatCallConfig.getPrivateCallingExpirationTime().toSeconds());
        } else if (UserChatCallSceneEnum.GROUP.equals(call.getScene())) {
            roomServiceCall = roomServiceClient.createRoom(call.getRoomId(), (int)userChatCallConfig.getGroupCallingExpirationTime().toSeconds());
        } else if (UserChatCallSceneEnum.MEETING.equals(call.getScene())) {
            roomServiceCall = roomServiceClient.createRoom(call.getRoomId(), (int)userChatCallConfig.getMeetingCallingExpirationTime().toSeconds());
        } else {
            throw new UnsupportedOperationException("不支持" + call.getScene().getName() + "类型的媒体房间创建");
        }

        Response<LivekitModels.Room> response = SystemException.convertSupplier(roomServiceCall::execute, StringUtils.EMPTY);
        SystemException.isTrue(response.isSuccessful(), response.message());
        Map<String, Object> metadata = SystemException.convertSupplier(() -> CastUtils.getObjectMapper().readValue(JsonFormat.printer().print(response.body()),CastUtils.MAP_TYPE_REFERENCE), StringUtils.EMPTY);
        call.setMediaServer(getType());
        call.getMetadata().put(getType(), metadata);
    }

    private RoomServiceClient getRoomServiceClient() {
        return RoomServiceClient.createClient(
                userChatCallConfig.getLivekit().getHost(),
                userChatCallConfig.getLivekit().getSecret().getSecretId(),
                userChatCallConfig.getLivekit().getSecret().getSecretKey()
        );
    }

    private AccessToken createAccessToken() {
        return new AccessToken(
                userChatCallConfig.getLivekit().getSecret().getSecretId(),
                userChatCallConfig.getLivekit().getSecret().getSecretKey()
        );
    }

    @Override
    public void completed(UserChatCallResponseBody body) {
        SystemException.convertSupplier(() -> getRoomServiceClient().deleteRoom(body.getRoomId()).execute());
    }

    @Override
    public void accept(
            UserChatCallEntity call,
            UserChatCallParticipantEntity callee
    ) {
        Map<String, Object> calleeDetails = CastUtils.convertValue(callee.getMetadata().get(AuditAuthenticationToken.DETAILS_KEY), CastUtils.MAP_TYPE_REFERENCE);
        String token = createJwtToken(calleeDetails, new RoomJoin(true), new RoomName(call.getRoomId()));
        String id = userChatCallConfig.getLivekit().getHost();
        callee.getMetadata().put(getType(), IdValueMetadata.of(id, token));
    }

    private String createJwtToken(
            Map<String, Object> participantDetails,
            VideoGrant ...grants
    ) {
        AccessToken token = createAccessToken();
        token.setName(PrincipalDetailsConstants.getPrincipalName(participantDetails));
        token.setIdentity(Objects.requireNonNull(participantDetails.get(PrincipalDetailsConstants.SYSTEM_NAME_KEY)).toString());
        token.addGrants(grants);
        return token.toJwt();
    }

    @Override
    public void privateSceneRejected(
            UserChatCallEntity call,
            UserChatCallParticipantEntity caller,
            UserChatCallParticipantEntity callee
    ) {
        SystemException.convertSupplier(() -> getRoomServiceClient().deleteRoom(call.getRoomId()).execute());
    }
}
