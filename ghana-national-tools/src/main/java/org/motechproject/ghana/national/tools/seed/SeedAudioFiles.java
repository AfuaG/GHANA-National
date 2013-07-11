package org.motechproject.ghana.national.tools.seed;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SeedAudioFiles {
    public static final String APPLICATION_CONTEXT_XML = "applicationContext-tools.xml";

    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext(APPLICATION_CONTEXT_XML);
        SeedLoader seedLoader = (SeedLoader) context.getBean("seedAudioFiles");
        seedLoader.load();
    }

    public static void newaudio(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext(APPLICATION_CONTEXT_XML);
        SeedLoader seedLoader = (SeedLoader) context.getBean("newSeedAudioFiles");
        seedLoader.load();
    }

}
