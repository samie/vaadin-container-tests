package org.vaadin.tests.container;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.Container;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import org.github.jamm.MemoryMeter;

import javax.servlet.annotation.WebServlet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 *
 */
@Theme("valo")
public class MyUI extends UI {

    private Label sizeLabel1 = new Label();
    private Label sizeLabel2 = new Label();
    private Table table = null;

    OptionGroup containerOptions;

    @Override
    protected void init(VaadinRequest request) {
        TestData.initializeData();

        final VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        setContent(layout);

        layout.addComponent(sizeLabel1);
        layout.addComponent(sizeLabel2);

        Button nop = new Button("Do nothing");
        nop.addClickListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                // just trigger a server call
            }
        });
        layout.addComponent(nop);

        Button button = new Button("Add table and measure");
        button.addClickListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                if (table != null) return;

                table = new Table();
                table.setWidth("500px");
                table.setHeight("500px");
                table.setContainerDataSource(createDataSource());

                MemoryMeter meter = createMeter();

                long detachedTableSize = meter.measureDeep(table);

                long deepSizeBefore = meter.measureDeep(MyUI.this);

                layout.addComponent(table);

                long deepSizeAfter = meter.measureDeep(MyUI.this);

                updateSizeLabel(sizeLabel1, "Add table ("+table.getContainerDataSource().getClass().getSimpleName()+")", deepSizeBefore, deepSizeAfter, detachedTableSize);
            }
        });
        layout.addComponent(button);

        Button button2 = new Button("Remove table and measure");
        button2.addClickListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                if (table == null) return;

                MemoryMeter meter = createMeter();

                long deepSizeBefore = meter.measureDeep(MyUI.this);

                layout.removeComponent(table);

                long deepSizeAfter = meter.measureDeep(MyUI.this);

                long detachedTableSize = meter.measureDeep(table);

                updateSizeLabel(sizeLabel2, "Remove table ("+table.getContainerDataSource().getClass().getSimpleName()+")", deepSizeBefore, deepSizeAfter, detachedTableSize);

                table = null;
            }

        });
        layout.addComponent(button2);

        containerOptions = new OptionGroup("Container", new ArrayList<String>(TestData.getContainerMap().keySet()));
        containerOptions.setValue(TestData.getContainerMap().keySet().iterator().next());
        layout.addComponent(containerOptions);

    }

    private MemoryMeter createMeter() {
        MemoryMeter meter = new MemoryMeter().ignoreKnownSingletons().ignoreNonStrongReferences().withGuessing(MemoryMeter.Guess.FALLBACK_BEST);
        meter = meter.withTrackerProvider(new Callable<Set<Object>>() {
            public Set<Object> call() throws Exception {
                Set<Object> set = Collections
                        .newSetFromMap(new IdentityHashMap<Object, Boolean>());
                set.add(VaadinSession.getCurrent());
                return set;
            }
        });
        return meter;
    }

    protected Container createDataSource() {
        return TestData.getContainerMap().get(containerOptions.getValue());
    }

    private void updateSizeLabel(Label sizeLabel, String prefix, long before, long after, long detachedSize) {
        String sizes = prefix + " (Initial size / diff / detached size): ";
        sizes = sizes + before + " / " + (after - before) + " / " + detachedSize;
        System.out.println(sizes);
        sizeLabel.setValue(sizes);
    }

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = MyUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }
}
