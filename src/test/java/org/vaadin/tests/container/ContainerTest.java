package org.vaadin.tests.container;


import com.vaadin.data.Container;
import com.vaadin.testbench.TestBench;
import com.vaadin.testbench.TestBenchTestCase;
import com.vaadin.testbench.elements.ButtonElement;
import com.vaadin.testbench.elements.OptionGroupElement;
import org.github.jamm.MemoryMeter;
import org.junit.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.vaadin.tests.container.backend.Contact;
import org.vaadin.tests.container.backend.ContactService;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * WebDriver tests against the demo/example app.
 */
public class ContainerTest extends TestBenchTestCase {

    // TODO: This value should be tested with different machines
    private static final long UI_THRESHOLD_VALUE = 2048;
    private static final int TEST_LOOPS = 10;

    private static WebDriver commonDriver;
    private static ContactService service;

    @BeforeClass
    public static void beforeAllTests() {
        // Start the server
        UITestServer.runUIServer(ContainerTestUI.class);

        //TODO:
        System.setProperty("phantomjs.binary.path", "/Users/se/tools/phantomjs-2.0.0-macosx/bin/phantomjs");

        // Create a single webdriver
        commonDriver = TestBench.createDriver(new PhantomJSDriver());
        commonDriver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
        
        service = ContactService.createDemoService();
        List<Contact> all = service.findAll(null);
        printSize("List size", all, true);



        // Calculate initial sizes
        Map<String,MemorySize> initialSize = new HashMap<>();
        Map<String,Container> map = AvailableContainers.getContainerMap(service);
        map.keySet().stream().forEach(k -> {
            initialSize.put(k,getMemorySize(k,map.get(k)));
        });


        System.out.println("\n---- INITIAL CONTAINER SIZE ----");
        printSizes(map.values(), true);
        System.out.println("--------------------------------\n");


        // Visit every container
        map.keySet().stream().forEach(k -> {
            Container c = map.get(k);
            c.getItemIds().stream().forEach(iid -> {
                c.getContainerPropertyIds().forEach(pid -> {
                    String v = c.getContainerProperty(iid, pid).getValue() + "";
                });
            });
        });

        // Calculate resulting size
        Map<String,MemorySize> currentSize = new HashMap<>();
        map.keySet().stream().forEach(k -> {
            currentSize.put(k, getMemorySize(k, map.get(k)));
        });


        System.out.println("\n---- VISITED CONTAINER SIZE ----");
        printSizes(map.values(), true);
        System.out.println("--------------------------------\n");

    }

    @AfterClass
    public static void afterAllTests() {
        // Stop the browser
        if (commonDriver != null) {
            commonDriver.quit();
        }

        // Stop the server
        UITestServer.shutdown();
    }

    @Before
    public void beforeTest() {
        if (getDriver() == null) {
            setDriver(commonDriver);
        }

        // Reload to make sure we have a clean start
        reloadPage();
    }

    @Test
    public void testContainerAddition() {

        clickButton(ContainerTestUI.NOP_BTN_ID);
        String startValue = findNotification().getText();


        AvailableContainers.getContainerMap(service).keySet().stream().forEach(k -> {

            $(OptionGroupElement.class).id(ContainerTestUI.CONTAINER_OPTIONS_ID).setValue(k);

            // Click the cancel button in dialog
            clickButton(ContainerTestUI.ADD_BTN_ID);
        });

        clickButton(ContainerTestUI.NOP_BTN_ID);
        String endValue = findNotification().getText();


        long diff = Long.parseLong(endValue) - Long.parseLong(startValue);
       // assertThat(diff, Matchers.lessThan(UI_THRESHOLD_VALUE));
    }



    public void testUIMemoryLeak() {

        clickButton(ContainerTestUI.NOP_BTN_ID);
        String startValue = findNotification().getText();


        for(int i =0 ; i<TEST_LOOPS; i++) {
            AvailableContainers.getContainerMap(service).keySet().stream().forEach(k -> {

                $(OptionGroupElement.class).id(ContainerTestUI.CONTAINER_OPTIONS_ID).setValue(k);

                // Click the cancel button in dialog
                clickButton(ContainerTestUI.ADD_BTN_ID);
                clickButton(ContainerTestUI.REMOVE_BTN_ID);

            });
        }

        clickButton(ContainerTestUI.NOP_BTN_ID);
        String endValue = findNotification().getText();


        long diff = Long.parseLong(endValue) - Long.parseLong(startValue);
        Assert.assertTrue(diff < UI_THRESHOLD_VALUE);
    }

    private void clickButton(String id, WebElement inContext) {
        $(ButtonElement.class).context(inContext).id(id).click();
    }

    private void clickButton(String id) {
        $(ButtonElement.class).id(id).click();
    }

    /**
     * Get the last notification.
     *
     * @return
     */
    private WebElement findNotification() {
        List<WebElement> n = getDriver().findElements(
                By.className("v-Notification"));
        WebElement last = n.get(n.size() - 1);
        return last;
    }

    /**
     * Reloads the page. Depending on UI configuration this might re-init the UI
     * or keep the state.
     *
     * @see #restartApplication()
     */
    protected void reloadPage() {
        getDriver().get(UITestServer.getServerUrl());
    }

    /**
     * Restarts the Vaadin application using ?restartApplication parameter.
     *
     * @see #reloadPage()
     */
    protected void restartApplication() {
        getDriver().get(UITestServer.getServerUrl() + "?restartApplication");
    }


    public static void printSize(String name, Object object, boolean printHeader) {
        if (printHeader) {
            System.out.println(MemorySize.getMemorySizeHeader());
        }
        if (object != null && name != null) {
            MemorySize size = getMemorySize(name, object);
            System.out.println(size.getMemorySizeString());
        }
    }

    public static void printSizes(Collection<? extends Object> objects, boolean printHeader) {
        if (printHeader) {
            System.out.println(MemorySize.getMemorySizeHeader());
        }
        if (objects != null) {
            objects.forEach(o -> System.out.println(getMemorySize(o.getClass().getSimpleName(), o).getMemorySizeString()));
        }
    }

    public static MemorySize getMemorySize(String name, Object object) {
        MemoryMeter meter = new MemoryMeter();
        meter.ignoreKnownSingletons();
        MemorySize s = new MemorySize();
        s.name = name;
        s.className = object.getClass().getCanonicalName();
        s.shallow = meter.measure(object);
        s.deep = meter.measureDeep(object);
        s.children = meter.countChildren(object);
        return s;
    }

    public static class MemorySize {
        private String name;
        private String className;
        private long shallow;
        private long deep;
        private long children;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public long getShallow() {
            return shallow;
        }

        public void setShallow(long shallow) {
            this.shallow = shallow;
        }

        public long getDeep() {
            return deep;
        }

        public void setDeep(long deep) {
            this.deep = deep;
        }

        public long getChildren() {
            return children;
        }

        public void setChildren(long children) {
            this.children = children;
        }

        public static String getMemorySizeHeader() {
            return "Name\tClass\tShallow\tDeep\tChildren";
        }
        public String getMemorySizeString() {
            return name+"\t"+ className+"\t"+ shallow+"\t" + deep+"\t" + children;
        }

    }
}

