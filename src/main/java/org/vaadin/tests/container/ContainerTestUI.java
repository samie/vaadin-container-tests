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
import org.vaadin.tests.container.backend.ContactService;

import javax.servlet.annotation.WebServlet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.concurrent.Callable;

@Theme("valo")
public class ContainerTestUI extends UI {

    static final String NOP_BTN_ID = "nop";
    static final String CLEAR_BTN_ID = "clear";
    static final String ADD_BTN_ID = "add";
    static final String REMOVE_BTN_ID = "remove";
    static final String CONTAINER_OPTIONS_ID = "container";

    private Table table = null;

    ContactService service = ContactService.createDemoService();

    OptionGroup containerOptions;

    @Override
    protected void init(VaadinRequest request) {

        final VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.setSpacing(true);
        setContent(layout);

        final VerticalLayout labels = new VerticalLayout();
        layout.addComponents(labels);


        Button nop = new Button("Do nothing");
        nop.setId(NOP_BTN_ID);
        nop.addClickListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                // just trigger a server call
                MemoryMeter meter = createMeter();

                long uiSize = meter.measureDeep(ContainerTestUI.this);
                Notification.show("" + uiSize);
            }
        });
        layout.addComponent(nop);

        Button clear = new Button("Clear");
        clear.setId(CLEAR_BTN_ID);
        clear.addClickListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                labels.removeAllComponents();
            }
        });
        layout.addComponent(clear);

        Button btnAdd = new Button("Add table and measure");
        btnAdd.setId(ADD_BTN_ID);
        btnAdd.addClickListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                if (table != null) return;

                table = new Table();
                table.setWidth("500px");
                table.setHeight("500px");
                table.setContainerDataSource(createDataSource());

                MemoryMeter meter = createMeter();

                long detachedTableSize = meter.measureDeep(table);

                long deepSizeBefore = meter.measureDeep(ContainerTestUI.this);

                layout.addComponent(table);

                long deepSizeAfter = meter.measureDeep(ContainerTestUI.this);

                String str = formatSizeString("Add table (" + table.getContainerDataSource().getClass().getSimpleName() + ")", deepSizeBefore, deepSizeAfter, detachedTableSize);
                labels.addComponents(new Label(str));

            }
        });
        layout.addComponent(btnAdd);

        Button btnRemove = new Button("Remove table and measure");
        btnRemove.setId(REMOVE_BTN_ID);
        btnRemove.addClickListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                if (table == null) return;

                MemoryMeter meter = createMeter();

                long deepSizeBefore = meter.measureDeep(ContainerTestUI.this);

                layout.removeComponent(table);

                long deepSizeAfter = meter.measureDeep(ContainerTestUI.this);

                long detachedTableSize = meter.measureDeep(table);

                String str = formatSizeString("Remove table (" + table.getContainerDataSource().getClass().getSimpleName() + ")", deepSizeBefore, deepSizeAfter, detachedTableSize);
                labels.addComponents(new Label(str));

                table = null;
            }

        });
        layout.addComponent(btnRemove);

        containerOptions = new OptionGroup("Container", new ArrayList<String>(AvailableContainers.getContainerMap(service).keySet()));
        containerOptions.setId(CONTAINER_OPTIONS_ID);
        containerOptions.setValue(AvailableContainers.getContainerMap(service).keySet().iterator().next());
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
        return AvailableContainers.getContainerMap(service).get(containerOptions.getValue());
    }

    private String formatSizeString(String prefix, long before, long after, long detachedSize) {
        String sizes = prefix + " (Initial size / diff / detached size): ";
        sizes = sizes + before + " / " + (after - before) + " / " + detachedSize;
        return sizes;
    }

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = ContainerTestUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }
}
