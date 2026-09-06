package io.github.loncra.basic.service.commons.config;

import io.github.loncra.basic.service.commons.enumerate.RuntimeModeEnum;
import io.github.loncra.framework.crypto.algorithm.Base64;
import io.github.loncra.framework.crypto.algorithm.ByteSource;
import io.github.loncra.framework.crypto.algorithm.cipher.AesCipherService;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * 公共配置信息
 *
 * @author maurice.chen
 */
@Data
@Slf4j
@Component
@NoArgsConstructor
@ConfigurationProperties("loncra.basic-service.commons.app")
public class CommonsConfig {

    private static AesCipherService AES_CIPHER_SERVICE = null;

    /**
     * 数据加解密密钥
     */
    private String dataCryptoKey = "MqnRT2o0hbNUT67Upcby7AUNWGuZnddl7GXQ2HwdwFs=";

    private String host = "http://localhost:8080";

    /**
     * 操作数据审计存储分表数量
     */
    private Integer operationDataTraceEntityStorageCount = 32;

    /**
     * 重置密码长度
     */
    private int adminRestPasswordLength = 8;

    /**
     * 随机登陆账户长度
     */
    private int randomUsernameLength = 6;

    private int randomNicknameLength = 8;

    private Integer randomNumberCount = 4;

    private int bigDecimalScale = 2;

    private RoundingMode bigDecimalRoundingMode = RoundingMode.HALF_UP;

    private String pdfTemplate = "<!DOCTYPE html><html><head><style>{0}</style></head><body><div class='ql-snow'><div class='ql-editor'>{1}</div></div></body></html>";

    private String pdfCssPath = "./assert/platform-agreement-pdf.css";

    private String pdfFontPath = "./assert/simsun.ttc";

    /**
     * 默认短信发送渠道名称
     */
    private String defaultSmsChannel = "alibabaCloud";

    private RuntimeModeEnum runtimeMode = RuntimeModeEnum.MONOLITH;

    public String decrypt(String cipherText) {
        return decrypt(cipherText, getDataCryptoKey());
    }

    public String decrypt(
            String cipherText,
            String key
    ) {
        try {
            ByteSource byteSource = getDefaultAesCipherService().decrypt(
                    Base64.decode(cipherText),
                    Base64.decode(key)
            );
            return byteSource.obtainString();
        }
        catch (Exception e) {
            log.warn("解密出现异常, cipherText:{}, dataCryptoKey:{}", cipherText, key, e);
        }
        return StringUtils.EMPTY;
    }

    public String encrypt(String plaintext) {
        if (StringUtils.isEmpty(plaintext)) {
            return null;
        }
        ByteSource byteSource = getDefaultAesCipherService().encrypt(
                plaintext.getBytes(),
                Base64.decode(getDataCryptoKey())
        );
        return byteSource.getBase64();
    }

    public String encrypt(
            String plaintext,
            String key
    ) {

        ByteSource byteSource = getDefaultAesCipherService().encrypt(
                plaintext.getBytes(StandardCharsets.UTF_8),
                Base64.decode(key)
        );

        return byteSource.getBase64();

    }

    public static AesCipherService getDefaultAesCipherService() {

        if (Objects.isNull(AES_CIPHER_SERVICE)) {
            AES_CIPHER_SERVICE = new AesCipherService();
        }
        return AES_CIPHER_SERVICE;
    }

    public String generateRandomPassword() {
        String key = RandomStringUtils.secure()
                .nextAlphanumeric(adminRestPasswordLength) + System.currentTimeMillis();
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    public String generateRandomUsername(String prefix) {
        return Objects.toString(prefix, StringUtils.EMPTY) + RandomStringUtils.secure()
                .nextAlphanumeric(randomUsernameLength);
    }

    public String generateRandomNickName() {
        return RandomStringUtils.secure()
                .nextAlphanumeric(randomNicknameLength);
    }

}
