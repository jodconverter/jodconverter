package org.artofsolving.jodconverter.boot.autoconfigure;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerBuilder;
import org.artofsolving.jodconverter.office.OfficeManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass({ OfficeDocumentConverter.class })
@ConditionalOnProperty(prefix = "jodconverter",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = false)
@EnableConfigurationProperties(JodConverterProperties.class)
public class JodConverterAutoConfiguration {

    private final JodConverterProperties properties;

    public JodConverterAutoConfiguration(JodConverterProperties properties) {
        this.properties = properties;
    }

    // Create a set of port numbers from a string
    private Set<Integer> buildPortNumbers(String str) {

        Set<Integer> iports = new HashSet<Integer>();

        if (StringUtils.isBlank(str)) {
            return iports;
        }

        String[] portNumbers = StringUtils.split(str, ", ");
        if (portNumbers.length == 0) {
            return iports;
        }

        for (String portNumber : portNumbers) {
            if (!StringUtils.isBlank(portNumber)) {
                iports.add(Integer.parseInt(StringUtils.trim(portNumber)));
            }
        }

        return iports;
    }

    // Creates the OfficeManager bean.
    private OfficeManager createOfficeManager() {

        DefaultOfficeManagerBuilder builder = new DefaultOfficeManagerBuilder();

        if (!StringUtils.isBlank(properties.getOfficeHome())) {
            builder.setOfficeHome(properties.getOfficeHome());
        }

        if (!StringUtils.isBlank(properties.getOfficeHome())) {
            builder.setWorkingDir(new File(properties.getWorkingDir()));
        }

        if (!StringUtils.isBlank(properties.getPortNumbers())) {
            Set<Integer> ports = buildPortNumbers(properties.getPortNumbers());
            if (!ports.isEmpty()) {
                builder.setPortNumbers(ArrayUtils.toPrimitive(ports.toArray(new Integer[]{})));
            }
        }

        if (!StringUtils.isBlank(properties.getTemplateProfileDir())) {
            builder.setTemplateProfileDir(new File(properties.getTemplateProfileDir()));
        }

        builder.setRetryTimeout(properties.getRetryTimeout());
        builder.setRetryInterval(properties.getRetryInterval());
        builder.setKillExistingProcess(properties.isKillExistingProcess());
        builder.setTaskQueueTimeout(properties.getTaskQueueTimeout());
        builder.setTaskExecutionTimeout(properties.getTaskExecutionTimeout());
        builder.setMaxTasksPerProcess(properties.getMaxTasksPerProcess());
        builder.setRetryInterval(properties.getRetryInterval());

        // Starts the manager
        return builder.build();
    }

    @Bean(initMethod = "start",
            destroyMethod = "stop")
    @ConditionalOnMissingBean
    public OfficeManager officeManager() {

        return createOfficeManager();
    }

    // Must appear after the OfficeManager bean creation. Do not reorder this class by name.
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(OfficeManager.class)
    public OfficeDocumentConverter jodConverter(OfficeManager officeManager) {

        return new OfficeDocumentConverter(officeManager);
    }
}
