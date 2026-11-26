package com.fillthefridge.powerups;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PowerUpPoolTest {

    @Test
    void allHeroPowerUpsCanBeInstantiatedById() {
        List<String> heroIds = List.of(
            "hero_cold_storage_veteran",
            "hero_fifo_master",
            "hero_produce_whisperer",
            "hero_space_optimizer",
            "hero_trap_hunter",
            "hero_deep_freeze_specialist",
            "hero_reinforced_stock",
            "hero_time_bender"
        );

        for (String id : heroIds) {
            PowerUp powerUp = PowerUpPool.createPowerUp(id);
            assertNotNull(powerUp, "PowerUp for ID " + id + " should not be null");
            assertEquals(id, powerUp.getId());
        }
    }

    @Test
    void standardPowerUpsCanBeInstantiatedById() {
        List<String> standardIds = List.of(
            "cold_snap", "cold_snap_upgrade", "deep_freeze", "deep_freeze_upgrade",
            "ghost_fit", "sniff_test", "trash_compactor", "crisper_drawer",
            "produce_saver", "produce_saver_upgrade", "reinforced_shelves",
            "smart_thermostat", "meal_prep_plan", "efficient_packer",
            "trap_scout", "fifo_caddy", "shelf_rails", "reusable_containers",
            "second_wind"
        );

        for (String id : standardIds) {
            PowerUp powerUp = PowerUpPool.createPowerUp(id);
            assertNotNull(powerUp, "PowerUp for ID " + id + " should not be null");
            assertEquals(id, powerUp.getId());
        }
    }

    @Test
    void unknownPowerUpIdThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> PowerUpPool.createPowerUp("non_existent_id"));
    }
}
