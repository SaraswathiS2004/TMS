package com.tms;

import com.tms.sheet.SheetConfig;
import com.tms.sheet.SheetSyncManager;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String classesDir = sce.getServletContext().getRealPath("/WEB-INF/classes");
        SheetConfig.init(classesDir);
        SheetSyncManager.getInstance().init();
        System.out.println("[AppContextListener] SheetConfig loaded. Configured: " +
            SheetConfig.getInstance().isConfigured());
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        SheetSyncManager.getInstance().shutdown();
    }
}
