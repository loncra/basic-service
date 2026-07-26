package io.github.loncra.basic.service.ai.server.utils;

import io.github.loncra.framework.commons.tenant.TenantContext;
import io.github.loncra.framework.commons.tenant.holder.TenantContextHolder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.ContextView;

import java.util.function.Supplier;

public class ReactorContextUtils {

    private ReactorContextUtils() {}

    public static <T> Flux<T> captureContext(Flux<T> source) {
        TenantContext ctx = TenantContextHolder.get();
        if (ctx == null) {
            return source;
        }
        return source.contextWrite(c -> c.put(TenantContext.class, ctx));
    }

    public static <T> Mono<T> monoWithContext(ContextView ctxView, Supplier<T> block) {
        TenantContext tenantCtx = ctxView.getOrDefault(TenantContext.class, null);
        if (tenantCtx == null) {
            return Mono.fromSupplier(block);
        }
        return Mono.fromSupplier(() -> {
            TenantContext prev = TenantContextHolder.get();
            TenantContextHolder.set(tenantCtx);
            try {
                return block.get();
            } finally {
                if (prev == null) {
                    TenantContextHolder.clear();
                } else {
                    TenantContextHolder.set(prev);
                }
            }
        });
    }

    public static <T> Flux<T> fluxWithContext(ContextView ctxView, Supplier<Flux<T>> block) {
        TenantContext tenantCtx = ctxView.getOrDefault(TenantContext.class, null);
        if (tenantCtx == null) {
            return block.get();
        }
        return Flux.using(
                () -> {
                    TenantContext prev = TenantContextHolder.get();
                    TenantContextHolder.set(tenantCtx);
                    return prev;
                },
                prev -> block.get(),
                prev -> {
                    if (prev == null) {
                        TenantContextHolder.clear();
                    } else {
                        TenantContextHolder.set(prev);
                    }
                }
        );
    }

}
