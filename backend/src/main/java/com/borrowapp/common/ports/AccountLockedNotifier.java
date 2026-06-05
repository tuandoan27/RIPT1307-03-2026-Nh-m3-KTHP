package com.borrowapp.common.ports;

public interface AccountLockedNotifier {
    void notify(Long userId, int totalPoints);
}