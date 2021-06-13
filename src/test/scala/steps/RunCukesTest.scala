package steps

import io.cucumber.junit.{Cucumber, CucumberOptions}
import org.junit.runner.RunWith

@RunWith(classOf[Cucumber])
@CucumberOptions(
  plugin = Array("pretty", "html:target/cucumber-reports.html")
)
class RunCukesTest
