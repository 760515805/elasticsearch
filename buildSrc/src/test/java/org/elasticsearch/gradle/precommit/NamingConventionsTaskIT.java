package org.elasticsearch.gradle.precommit;

import org.elasticsearch.gradle.GradleIntegrationTests;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


public class NamingConventionsTaskIT extends GradleIntegrationTests {

    @Test
    public void pluginCanBeApplied() {
        BuildResult result = GradleRunner.create()
            .withProjectDir(getProjectDir("namingConventionsSelfTest"))
            .withArguments("hello", "-s", "-PcheckForTestsInMain=false")
            .withPluginClasspath()
            .build();

        assertEquals(TaskOutcome.SUCCESS, result.task(":hello").getOutcome());
        assertTrue(result.getOutput().contains("build plugin can be applied"));
    }

    @Test
    public void nameCheckFailsAsItShould() {
        BuildResult result = GradleRunner.create()
            .withProjectDir(getProjectDir("namingConventionsSelfTest"))
            .withArguments("namingConventions", "-s", "-PcheckForTestsInMain=false")
            .withPluginClasspath()
            .buildAndFail();

        assertNotNull("task did not run", result.task(":namingConventions"));
        assertEquals(TaskOutcome.FAILED, result.task(":namingConventions").getOutcome());
        for (String line : Arrays.asList(
            "Found inner classes that are tests, which are excluded from the test runner:",
            "* org.elasticsearch.test.NamingConventionsCheckInMainIT$InternalInvalidTests",
            "Classes ending with [Tests] must subclass [UnitTestCase]:",
            "* org.elasticsearch.test.NamingConventionsCheckInMainTests",
            "* org.elasticsearch.test.NamingConventionsCheckInMainIT",
            "Not all subclasses of UnitTestCase match the naming convention. Concrete classes must end with [Tests]:",
            "* org.elasticsearch.test.WrongName")) {
            assertTrue(
                "expected:  '" + line + "' but it was not found in the output",
                result.getOutput().contains(line)
            );
        }
    }

    @Test
    public void nameCheckFailsAsItShouldWithMain() {
        BuildResult result = GradleRunner.create()
            .withProjectDir(getProjectDir("namingConventionsSelfTest"))
            .withArguments("namingConventions", "-s", "-PcheckForTestsInMain=true")
            .withPluginClasspath()
            .buildAndFail();

        assertNotNull("task did not run", result.task(":namingConventions"));
        assertEquals(TaskOutcome.FAILED, result.task(":namingConventions").getOutcome());

        for (String line : Arrays.asList(
            "Classes ending with [Tests] or [IT] or extending [UnitTestCase] must be in src/test/java:",
            "* org.elasticsearch.test.NamingConventionsCheckBadClasses$DummyInterfaceTests",
            "* org.elasticsearch.test.NamingConventionsCheckBadClasses$DummyAbstractTests",
            "* org.elasticsearch.test.NamingConventionsCheckBadClasses$InnerTests",
            "* org.elasticsearch.test.NamingConventionsCheckBadClasses$NotImplementingTests",
            "* org.elasticsearch.test.NamingConventionsCheckBadClasses$WrongNameTheSecond",
            "* org.elasticsearch.test.NamingConventionsCheckBadClasses$WrongName")) {
            assertTrue(
                "expected:  '" + line + "' but it was not found in the output",
                result.getOutput().contains(line)
            );
        }
    }

}
