package org.dachuang_team.dc_backend_services.services;

import org.dachuang_team.dc_backend_services.repository.AttractionRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(1)
public class AttractionHotelCleanupRunner implements ApplicationRunner {
    private final AttractionRepository attractionRepository;

    public AttractionHotelCleanupRunner(AttractionRepository attractionRepository) {
        this.attractionRepository = attractionRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        var hotels = attractionRepository.findByAttractionTag("酒店");
        if (hotels != null && !hotels.isEmpty()) {
            attractionRepository.deleteAll(hotels);
        }
    }
}
