package com.borrowapp.common.ports;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Primary
public class NoopAccountLockedNotifier implements AccountLockedNotifier {

    @Override
    public void notify(Long userId, int totalPoints) {
        log.info("[AccountLocked] userId={} totalPoints={} — (noop, chờ Thành implement)",
                userId, totalPoints);
    }
}