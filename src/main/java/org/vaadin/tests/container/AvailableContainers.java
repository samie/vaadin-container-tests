package org.vaadin.tests.container;

/**
 * Created by se on 19/06/15.
 */
import com.vaadin.data.Container;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.ExtensibleBeanContainer;
import com.vaadin.data.util.IndexedContainer;
import org.vaadin.data.collectioncontainer.CollectionContainer;
import org.vaadin.tests.container.backend.Contact;
import org.vaadin.tests.container.backend.ContactService;
import org.vaadin.viritin.ListContainer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AvailableContainers {


    private static List<Contact> all;

    private static Map<String, Container> containerMap;

    public static class Person {
        private String firstName;
        private String lastName;

        public Person(String firstName, String lastName) {
            this.setFirstName(firstName);
            this.setLastName(lastName);
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
    }

    /** Initialize all containers to be tested.
     *
     * @param service
     */
    public static void initializeData(ContactService service) {


        all = service.findAll(null);
        containerMap = new HashMap<>();

        containerMap.put("BeanItemContainer", new BeanItemContainer<Contact>(Contact.class, all));

        containerMap.put("IndexedContainer", new IndexedContainer(all));

        ExtensibleBeanContainer<Contact, Contact> ec = new ExtensibleBeanContainer<Contact, Contact>(Contact.class);
        for (Contact c : all) {
            ec.addItem(c, c);
        }
        containerMap.put("ExtensibleBeanContainer", ec);

        CollectionContainer cc = new CollectionContainer(all, true, CollectionContainer.ITEM_ID_MODE_OBJECT);
        containerMap.put("CollectionContainer", cc);

        ListContainer<Contact> lc = new ListContainer<Contact>(Contact.class,all);
        containerMap.put("ListContainer", lc);
    }

    public static Map<String, Container> getContainerMap(ContactService service) {
        if (containerMap == null) {
            initializeData(service);
        }
        return containerMap;
    }


}