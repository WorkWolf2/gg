package com.minegolem.hypingNations.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class TaxHistory {

    @Getter
    @AllArgsConstructor
    public static class TaxEntry {
        private final UUID nationId;
        private final LocalDateTime timestamp;
        private final double amount;
        private final int chunks;
        private final boolean success;
    }
}