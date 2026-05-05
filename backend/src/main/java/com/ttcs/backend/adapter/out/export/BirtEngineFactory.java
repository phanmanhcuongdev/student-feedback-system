package com.ttcs.backend.adapter.out.export;

import jakarta.annotation.PreDestroy;
import org.eclipse.birt.chart.model.ModelPackage;
import org.eclipse.birt.chart.model.attribute.AttributePackage;
import org.eclipse.birt.chart.model.component.ComponentPackage;
import org.eclipse.birt.chart.model.data.DataPackage;
import org.eclipse.birt.chart.model.layout.LayoutPackage;
import org.eclipse.birt.chart.model.type.TypePackage;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.EngineConstants;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class BirtEngineFactory {

    private static final Logger log = LoggerFactory.getLogger(BirtEngineFactory.class);

    private volatile IReportEngine engine;
    private volatile boolean platformStarted;

    public IReportEngine getEngine() {
        IReportEngine current = engine;
        if (current == null) {
            synchronized (this) {
                current = engine;
                if (current == null) {
                    engine = current = createEngine();
                }
            }
        }
        return current;
    }

    private IReportEngine createEngine() {
        try {
            EngineConfig config = new EngineConfig();
            config.getAppContext().put(
                    EngineConstants.APPCONTEXT_CLASSLOADER_KEY,
                    BirtEngineFactory.class.getClassLoader()
            );
            registerChartModelPackages();
            Platform.startup(config);
            platformStarted = true;
            IReportEngineFactory factory = (IReportEngineFactory) Platform.createFactoryObject(
                    IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY
            );
            if (factory == null) {
                throw new IllegalStateException("BIRT report engine factory is not available");
            }
            return factory.createReportEngine(config);
        } catch (Throwable exception) {
            throw new BirtRenderException("Unable to start BIRT report engine", exception);
        }
    }

    private void registerChartModelPackages() {
        ModelPackage.eINSTANCE.eClass();
        AttributePackage.eINSTANCE.eClass();
        ComponentPackage.eINSTANCE.eClass();
        DataPackage.eINSTANCE.eClass();
        LayoutPackage.eINSTANCE.eClass();
        TypePackage.eINSTANCE.eClass();
    }

    @PreDestroy
    public void shutdown() {
        synchronized (this) {
            if (engine != null) {
                try {
                    engine.destroy();
                } catch (Throwable exception) {
                    log.warn("Failed to destroy BIRT report engine cleanly", exception);
                } finally {
                    engine = null;
                }
            }
            if (platformStarted) {
                try {
                    Platform.shutdown();
                } catch (Throwable exception) {
                    log.warn("Failed to shutdown BIRT platform cleanly", exception);
                } finally {
                    platformStarted = false;
                }
            }
        }
    }
}
