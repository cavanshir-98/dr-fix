package tech.masterfix.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.masterfix.model.ApplianceService;
import tech.masterfix.repository.ApplianceServiceRepository;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(ApplianceServiceRepository repository) {
        return args -> {
            if (repository.count() > 0) return;

            String[][] services = {
                {"Refrigerator", "🧊", "Cooling issues, leaks, compressor repair", "residential"},
                {"Washer", "🫧", "Drain problems, spin cycle, motor repair", "residential"},
                {"Dryer", "🌀", "Heating issues, drum problems, vent cleaning", "residential"},
                {"Dishwasher", "🍽️", "Not draining, leaks, cleaning issues", "residential"},
                {"Oven", "🔥", "Heating elements, temperature control", "residential"},
                {"Range", "🍳", "Burner repair, ignition problems", "residential"},
                {"Microwave", "📡", "Not heating, turntable, door issues", "residential"},
                {"Freezer", "❄️", "Frost buildup, temperature issues", "residential"},
                {"Ice Machine", "🧊", "No ice production, water leaks", "residential"},
                {"Cooktop", "🔥", "Burner repair, glass replacement", "residential"},
                {"Vent Hood", "💨", "Fan motor, filter, lighting repair", "residential"},
                {"Garbage Disposal", "🗑️", "Jammed, leaking, motor issues", "residential"},
                {"Commercial Refrigerator", "🏢", "Walk-in coolers, display cases", "commercial"},
                {"Commercial Oven", "🏢", "Industrial baking, convection repair", "commercial"},
                {"Commercial Dishwasher", "🏢", "High-volume kitchen equipment", "commercial"},
                {"Ice Machine (Commercial)", "🏢", "Restaurant & bar ice systems", "commercial"},
            };

            for (String[] s : services) {
                repository.save(new ApplianceService(s[0], s[1], s[2], s[3]));
            }
        };
    }
}
