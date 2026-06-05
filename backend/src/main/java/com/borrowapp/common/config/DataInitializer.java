package com.borrowapp.common.config;

import com.borrowapp.common.constants.Role;
import com.borrowapp.equipment.entity.Equipment;
import com.borrowapp.equipment.repository.EquipmentRepository;
import com.borrowapp.user.entity.User;
import com.borrowapp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final EquipmentRepository equipmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Seed Users
        User admin = userRepository.findByEmail("admin@gmail.com").orElse(null);
        if (admin == null) {
            log.info("Seeding default admin...");
            admin = User.builder()
                    .fullName("Quản Trị Viên")
                    .studentCode("ADMIN001")
                    .email("admin@gmail.com")
                    .password(passwordEncoder.encode("123456"))
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(admin);
            log.info("Default admin seeded successfully.");
        } else {
            log.info("Updating default admin password...");
            admin.setPassword(passwordEncoder.encode("123456"));
            userRepository.save(admin);
        }

        User student = userRepository.findByEmail("student@ptit.edu.vn").orElse(null);
        if (student == null) {
            log.info("Seeding default student...");
            student = User.builder()
                    .fullName("Nguyễn Văn A")
                    .studentCode("B20DCCN001")
                    .email("student@ptit.edu.vn")
                    .password(passwordEncoder.encode("123456"))
                    .role(Role.STUDENT)
                    .build();
            userRepository.save(student);
            log.info("Default student seeded successfully.");
        } else {
            log.info("Updating default student password...");
            student.setPassword(passwordEncoder.encode("123456"));
            userRepository.save(student);
        }

        // Seed Equipments
        if (equipmentRepository.count() == 0) {
            log.info("Seeding default equipments...");
            List<Equipment> defaultEquipments = Arrays.asList(
                    Equipment.builder()
                            .name("Laptop Dell XPS 15")
                            .description("Laptop cao cấp cấu hình khủng Core i7, RAM 16GB, thích hợp lập trình đồ án.")
                            .totalQuantity(5)
                            .availableQuantity(5)
                            .imageUrl("https://images.unsplash.com/photo-1593642632823-8f785ba67e45?w=500&auto=format&fit=crop&q=60")
                            .isDeleted(false)
                            .build(),
                    Equipment.builder()
                            .name("Máy Chiếu Epson Full HD")
                            .description("Máy chiếu Epson hỗ trợ giảng dạy, hội thảo câu lạc bộ sắc nét.")
                            .totalQuantity(3)
                            .availableQuantity(3)
                            .imageUrl("https://images.unsplash.com/photo-1535016120720-40c646be5580?w=500&auto=format&fit=crop&q=60")
                            .isDeleted(false)
                            .build(),
                    Equipment.builder()
                            .name("Bộ Vi Xử Lý Raspberry Pi 4")
                            .description("Bo mạch Raspberry Pi cho các dự án nghiên cứu nhúng, IoT.")
                            .totalQuantity(10)
                            .availableQuantity(10)
                            .imageUrl("https://images.unsplash.com/photo-1555664424-778a1e5e1b48?w=500&auto=format&fit=crop&q=60")
                            .isDeleted(false)
                            .build(),
                    Equipment.builder()
                            .name("Máy Ảnh Canon EOS 80D")
                            .description("Máy ảnh DSLR chuyên nghiệp cho các hoạt động truyền thông và sự kiện của câu lạc bộ.")
                            .totalQuantity(2)
                            .availableQuantity(2)
                            .imageUrl("https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=500&auto=format&fit=crop&q=60")
                            .isDeleted(false)
                            .build(),
                    Equipment.builder()
                            .name("Loa Bluetooth JBL PartyBox")
                            .description("Loa kéo di động công suất lớn phục vụ sinh hoạt ngoài trời.")
                            .totalQuantity(8)
                            .availableQuantity(8)
                            .imageUrl("https://images.unsplash.com/photo-1589003077984-894e133dabab?w=500&auto=format&fit=crop&q=60")
                            .isDeleted(false)
                            .build(),
                    Equipment.builder()
                            .name("iPad Pro M2 12.9 inch")
                            .description("Máy tính bảng Apple màn hình lớn, hỗ trợ vẽ phác thảo, ghi chú sự kiện nhanh chóng.")
                            .totalQuantity(4)
                            .availableQuantity(4)
                            .imageUrl("https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=500&auto=format&fit=crop&q=60")
                            .isDeleted(false)
                            .build()
            );

            equipmentRepository.saveAll(defaultEquipments);
            log.info("Default equipments seeded successfully.");
        }
    }
}
