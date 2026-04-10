package pragmatech.digital.workshops.lab1;

import org.springframework.boot.SpringApplication;

public class LocalApplication {
  public static void main(String[] args) {
    SpringApplication.from(Lab1Application::main).with(LocalDevTestcontainersConfig.class).run(args);
  }
}
