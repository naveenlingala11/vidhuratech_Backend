package com.vidhuratech.jobs;

import com.vidhuratech.jobs.user.entity.User;
import com.vidhuratech.jobs.user.enums.UserRole;
import com.vidhuratech.jobs.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class JobsApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobsApplication.class, args);
	}

	@Bean
	CommandLineRunner seedUsers(
			UserRepository repo,
			PasswordEncoder encoder
	) {
		return args -> {

			seedIfNotExists(repo, encoder, "Admin", "admin@test.com", UserRole.ADMIN);
			seedIfNotExists(repo, encoder, "Trainer", "trainer@test.com", UserRole.TRAINER);
			seedIfNotExists(repo, encoder, "HR", "hr@test.com", UserRole.HR);
			seedIfNotExists(repo, encoder, "Manager", "manager@test.com", UserRole.MANAGER);
			seedIfNotExists(repo, encoder, "Super Admin", "superadmin@test.com", UserRole.SUPER_ADMIN);
			seedIfNotExists(repo, encoder, "Mentor", "mentor@test.com", UserRole.MENTOR);
			seedIfNotExists(repo, encoder, "Student", "student@test.com", UserRole.STUDENT);
		};
	}

	private void seedIfNotExists(
			UserRepository repo,
			PasswordEncoder encoder,
			String name,
			String email,
			UserRole role
	) {
		if (repo.existsByEmail(email)) {
			return;
		}

		User user = new User();
		user.setName(name);
		user.setEmail(email);
		user.setPhone("9999999999");
		user.setPassword(encoder.encode("Admin@123"));
		user.setRole(role);
		user.setActive(true);

		repo.save(user);
	}
}