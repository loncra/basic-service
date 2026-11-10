package io.github.loncra.basic.service.ai.server.resolver.sandbox;

import io.agentscope.harness.agent.sandbox.ExecResult;
import io.github.loncra.basic.service.ai.server.resolver.filesystem.RpcFilesystemInvoker;
import io.github.loncra.basic.service.ai.server.resolver.filesystem.RpcFilesystemScope;
import io.github.loncra.framework.commons.exception.SystemException;
import org.apache.commons.lang3.SystemUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public final class SandboxRecords {

    private static final int TIMEOUT_SECONDS = 60;

    private static final String MISSING = "__MISSING__";

    private final AbstractCommandSandbox sandbox;

    private final boolean powershell;

    private SandboxRecords(AbstractCommandSandbox sandbox, boolean powershell) {
        this.sandbox = sandbox;
        this.powershell = powershell;
    }

    public static SandboxRecords cloud(Path home) {
        return new SandboxRecords(new DirectorySandbox(home), SystemUtils.IS_OS_WINDOWS);
    }

    public static SandboxRecords client(RpcFilesystemInvoker invoker, String deviceIdentified) {
        // ponytail: 客户端记录命令固定 PowerShell，因为 shell_cmd.rs 始终启动 powershell。runner 改跟 system_shell 时这里再分支。
        return new SandboxRecords(
                new ClientSandbox(invoker, deviceIdentified, new RpcFilesystemScope.Store()),
                true
        );
    }

    public String readText(String path) {
        ExecResult result = exec(readCommand(path), null);
        String output = result.stdout() == null ? "" : result.stdout().strip();
        if (MISSING.equals(output)) {
            return null;
        }
        if (!result.ok()) {
            throw new SystemException("读取沙箱文件 [" + path + "] 失败: " + result.stderr());
        }
        if (output.isEmpty()) {
            return "";
        }
        byte[] bytes = Base64.getMimeDecoder().decode(output.replaceAll("\\s", ""));
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public void writeText(String path, String content) {
        byte[] stdin = Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8)).getBytes(StandardCharsets.UTF_8);
        ExecResult result = exec(writeCommand(path), stdin);
        if (!result.ok()) {
            throw new SystemException("写入沙箱文件 [" + path + "] 失败: " + result.stdout() + result.stderr());
        }
    }

    public void delete(String path) {
        ExecResult result = exec(deleteCommand(path), null);
        if (!result.ok()) {
            throw new SystemException("删除沙箱文件 [" + path + "] 失败: " + result.stderr());
        }
    }

    public void deleteTree(String path) {
        ExecResult result = exec(deleteTreeCommand(path), null);
        if (!result.ok()) {
            throw new SystemException("删除沙箱目录 [" + path + "] 失败: " + result.stderr());
        }
    }

    public boolean exists(String path) {
        ExecResult result = exec(existsCommand(path), null);
        return result.ok() && result.stdout() != null && result.stdout().strip().startsWith("yes");
    }

    public List<String> listNames(String directory) {
        ExecResult result = exec(listCommand(directory), null);
        if (!result.ok()) {
            throw new SystemException("列出沙箱目录 [" + directory + "] 失败: " + result.stderr());
        }
        if (result.stdout() == null || result.stdout().isBlank()) {
            return List.of();
        }
        return Arrays.stream(result.stdout().split("\\R"))
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .toList();
    }

    private ExecResult exec(String command, byte[] stdin) {
        try {
            ExecResult result = sandbox.execWithInput(null, command, TIMEOUT_SECONDS, stdin);
            if (result.truncated()) {
                throw new SystemException("沙箱输出被截断");
            }
            return result;
        } catch (SystemException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new SystemException("沙箱执行失败: " + exception.getMessage());
        }
    }

    private String readCommand(String path) {
        String quoted = quote(path);
        if (powershell) {
            return "if (-not (Test-Path -LiteralPath " + quoted + " -PathType Leaf)) { '" + MISSING + "' } else { [Convert]::ToBase64String([IO.File]::ReadAllBytes(" + quoted + ")) }";
        }
        return "if [ ! -f " + quoted + " ]; then echo " + MISSING + "; else base64 " + quoted + "; fi";
    }

    private String writeCommand(String path) {
        String quoted = quote(path);
        if (powershell) {
            return "$p=" + quoted
                    + "; $d=Split-Path -Parent $p; if ($d -and -not (Test-Path -LiteralPath $d)) { New-Item -ItemType Directory -Force -Path $d | Out-Null }; "
                    + "$b=[Console]::In.ReadToEnd().Trim(); if ([string]::IsNullOrEmpty($b)) { [IO.File]::WriteAllBytes($p, [byte[]]@()) } else { [IO.File]::WriteAllBytes($p, [Convert]::FromBase64String($b)) }";
        }
        return "mkdir -p \"$(dirname " + quoted + ")\" && base64 -d > " + quoted;
    }

    private String deleteCommand(String path) {
        String quoted = quote(path);
        if (powershell) {
            return "if (Test-Path -LiteralPath " + quoted + " -PathType Leaf) { [IO.File]::Delete(" + quoted + ") }";
        }
        return "rm -f " + quoted;
    }

    private String deleteTreeCommand(String path) {
        String quoted = quote(path);
        if (powershell) {
            return "if (Test-Path -LiteralPath " + quoted + ") { [IO.Directory]::Delete(" + quoted + ", $true) }";
        }
        return "rm -rf " + quoted;
    }

    private String existsCommand(String path) {
        String quoted = quote(path);
        if (powershell) {
            return "if (Test-Path -LiteralPath " + quoted + ") { 'yes' } else { 'no' }";
        }
        return "if [ -e " + quoted + " ]; then echo yes; else echo no; fi";
    }

    private String listCommand(String directory) {
        String quoted = quote(directory);
        if (powershell) {
            return "if (Test-Path -LiteralPath " + quoted + " -PathType Container) { Get-ChildItem -LiteralPath " + quoted + " -Name }";
        }
        return "if [ -d " + quoted + " ]; then ls -1 " + quoted + "; fi";
    }

    private static String quote(String path) {
        return "'" + path + "'";
    }
}
