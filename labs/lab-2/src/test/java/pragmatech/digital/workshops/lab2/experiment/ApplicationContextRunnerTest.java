package pragmatech.digital.workshops.lab2.experiment;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import pragmatech.digital.workshops.lab2.config.ConditionalBookImportConfig;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationContextRunnerTest {

  private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
    .withUserConfiguration(ConditionalBookImportConfig.class);

  @Test
  void shouldNotHaveBookImportBeanWhenPropertyIsAbsent() {
    contextRunner.run(context ->
      assertThat(context).doesNotHaveBean("bookImportEnabled")
    );
  }

  @Test
  void shouldHaveBookImportBeanWhenPropertyIsEnabled() {
    contextRunner
      .withPropertyValues("bookshelf.import.enabled=true")
      .run(context ->
        assertThat(context).hasSingleBean(String.class)
      );
  }

  @Test
  void shouldNotHaveBookImportBeanWhenPropertyIsFalse() {
    contextRunner
      .withPropertyValues("bookshelf.import.enabled=false")
      .run(context ->
        assertThat(context).doesNotHaveBean("bookImportEnabled")
      );
  }
}
