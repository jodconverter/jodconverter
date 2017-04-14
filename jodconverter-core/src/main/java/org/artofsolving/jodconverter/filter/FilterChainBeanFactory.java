/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2017 Simon Braconnier and contributors
 *
 * This file is part of JODConverter - Java OpenDocument Converter.
 *
 * JODConverter is an Open Source software: you can redistribute it and/or
 * modify it under the terms of either (at your option) of the following
 * licenses:
 *
 * 1. The GNU Lesser General Public License v3 (or later)
 *    http://www.gnu.org/licenses/lgpl-3.0.txt
 * 2. The Apache License, Version 2.0
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package org.artofsolving.jodconverter.filter;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.configuration2.beanutils.BeanCreationContext;
import org.apache.commons.configuration2.beanutils.BeanDeclaration;
import org.apache.commons.configuration2.beanutils.BeanHelper;
import org.apache.commons.configuration2.beanutils.DefaultBeanFactory;
import org.apache.commons.configuration2.beanutils.XMLBeanDeclaration;
import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;

public class FilterChainBeanFactory extends DefaultBeanFactory {

  @Override
  protected void initBeanInstance(final Object bean, final BeanCreationContext bcc)
      throws Exception {

    if (!(bean instanceof FilterChain)) {
      throw new ConfigurationRuntimeException(
          "A filter chain must implement the FilterChain interface.");
    }

    final FilterChain filterChain = (FilterChain) bean;
    final BeanDeclaration data = bcc.getBeanDeclaration();
    BeanHelper.initBeanProperties(bean, data);

    final Map<String, Object> nestedBeans = data.getNestedBeanDeclarations();
    if (nestedBeans == null || nestedBeans.size() == 0) {
      throw new ConfigurationRuntimeException(
          "A filter chain must contain at least one nested filter.");
    }

    if (nestedBeans.size() > 1) {
      throw new ConfigurationRuntimeException(
          "A filter chain only supports nested filter element named 'filter'.");
    }

    final Map.Entry<String, Object> entry = nestedBeans.entrySet().iterator().next();
    if (!"filter".equalsIgnoreCase(entry.getKey())) {
      throw new ConfigurationRuntimeException(
          "Expected filter chain nested filter element names to be 'filter' but was '"
              + entry.getKey()
              + "'.");
    }

    final Object prop = entry.getValue();
    if (prop instanceof Collection) {
      for (Object elemDef : (Collection<?>) prop) {
        if (!(elemDef instanceof XMLBeanDeclaration)) {
          throw new ConfigurationRuntimeException(
              "Expected filter element to be a bean declaration but was not.");
        }

        Object beanFilter = BeanHelper.INSTANCE.createBean((XMLBeanDeclaration) elemDef);
        if (!(beanFilter instanceof Filter)) {
          throw new ConfigurationRuntimeException("A filter must implement the Filter interface.");
        }

        filterChain.addFilter((Filter) beanFilter);
      }
    } else {
      if (!(prop instanceof XMLBeanDeclaration)) {
        throw new ConfigurationRuntimeException(
            "Expected filter element to be a bean declaration but was not.");
      }
      
      Object beanFilter = BeanHelper.INSTANCE.createBean((XMLBeanDeclaration) prop);
      if (!(beanFilter instanceof Filter)) {
        throw new ConfigurationRuntimeException("A filter must implement the Filter interface.");
      }

      filterChain.addFilter((Filter) beanFilter);
    }
  }
}
