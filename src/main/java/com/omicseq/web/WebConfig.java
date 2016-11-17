package com.omicseq.web;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

/**
 * 
 * 
 * @author zejun.du
 */
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = { "com.omicseq.web.*" })
public class WebConfig extends WebMvcConfigurerAdapter {

    /**
     * 添加对静态文件访问的支持
     * 
     * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter#addResourceHandlers(org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry)
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        ResourceHandlerRegistration handler = registry.addResourceHandler("/static/**", "/tpl/**");
        handler.addResourceLocations("/static/", "/tpl/");
    }

    /**
     * 添加拦截器
     * @return
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 添加切换语言标记 index.htm?locale=zh_CN
        LocaleChangeInterceptor changeInterceptor = new LocaleChangeInterceptor();
        InterceptorRegistration interceptor = registry.addInterceptor(changeInterceptor);
        interceptor.addPathPatterns("/**").excludePathPatterns("/static/**", "/tpl/**");
    }

    /**
     * 如果不配置此bean 返回jsp需要指定相对webapp目录下的绝对路径如/WEB-INF/views/test.jsp </br>
     * 添加此配置后,则只需要返回test
     * 
     * @return
     */
    @Bean
    public ViewResolver viewResolver() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setViewClass(JstlView.class);
        viewResolver.setPrefix("/WEB-INF/views/");
        viewResolver.setSuffix(".jsp");
        return viewResolver;
    }

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
        super.configureDefaultServletHandling(configurer);
    }

    /**
     * 国际化资源文件
     * 
     * @return
     */
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasenames("i18n.messages", "i18n.errors");
        return messageSource;
    }

    /**
     * 国际化语言信息记录
     * 
     * @return
     */
    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver localeResolver = new CookieLocaleResolver();
        localeResolver.setCookieName("language");
        localeResolver.setCookieMaxAge(Integer.MAX_VALUE);
        return localeResolver;
    }

}
