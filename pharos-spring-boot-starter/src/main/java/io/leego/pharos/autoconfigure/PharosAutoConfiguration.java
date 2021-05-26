package io.leego.pharos.autoconfigure;

import io.leego.pharos.config.PharosConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Yihleego
 */
@Configuration
@Import(PharosConfiguration.class)
public class PharosAutoConfiguration {
}
