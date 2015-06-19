package org.vaadin.tests.container;

/**
 * Created by se on 19/06/15.
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.github.jamm.MemoryMeter;
import org.vaadin.data.collectioncontainer.CollectionContainer;
import org.vaadin.viritin.ListContainer;

import com.vaadin.data.Container;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.ExtensibleBeanContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Table;


public class TestData {
    public static final String[][] TEST_DATA = { { "First", "Last" }, { "John", "Doe" }, { "Jane", "Doe" } };

    public static final int NUMBER_OF_PERSONS = 1000;

    public static Person[] persons;

    public static List<Person> personList;

    public static BeanItemContainer<Person> beanItemContainer;

    public static IndexedContainer indexedContainer;

    public static ExtensibleBeanContainer<Person, Person> extensibleBeanContainer;

    public static CollectionContainer collectionContainer;

    public static ListContainer<Person> listContainer;

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

    public static void main(String[] args) {
        initializeData();

        System.out.println("Raw test data array:");
        printSize(TEST_DATA);

        System.out.println("Array of Person:");
        printSize(persons);
        System.out.println("ArrayList of Person:");
        printSize(personList);

        System.out.println("BeanItemContainer<Person> (no listeners):");
        printSize(beanItemContainer);

        System.out.println("IndexedContainer (no listeners):");
        printSize(indexedContainer);

        Table table = new Table();
        table.setContainerDataSource(beanItemContainer);

        System.out.println("Table with BeanItemContainer<Person> (not displayed):");
        printSize(table);

        System.out.println("MCont ExtensibleBeanContainer<Person> (no listeners):");
        printSize(extensibleBeanContainer);

        System.out.println("CollectionContainer (no listeners):");
        printSize(collectionContainer);

        System.out.println("ListContainer (no listeners):");
        printSize(listContainer);
    }

    public static void initializeData() {
        persons = new Person[NUMBER_OF_PERSONS];
        personList = new ArrayList<Person>();
        // for (int i=0; i<TEST_DATA.length * 1000; ++i) {
        for (int i=0; i<NUMBER_OF_PERSONS; ++i) {
            // Person person = new Person(TEST_DATA[i][0], TEST_DATA[i][1]);
            Person person = new Person("First" + i, "Last" + i);
            persons[i] = person;
            personList.add(person);
        }

        beanItemContainer = new BeanItemContainer<Person>(Person.class);
        for (Person person : persons) {
            beanItemContainer.addBean(person);
        }

        indexedContainer = new IndexedContainer();
        indexedContainer.addContainerProperty("First", String.class, null);
        indexedContainer.addContainerProperty("Last", String.class, null);
        for (Person person : persons) {
            Object id = indexedContainer.addItem();
            indexedContainer.getItem(id).getItemProperty("First").setValue(person.getFirstName());
            indexedContainer.getItem(id).getItemProperty("Last").setValue(person.getLastName());
        }

        extensibleBeanContainer = new ExtensibleBeanContainer<Person, Person>(Person.class);
        for (Person person : persons) {
            extensibleBeanContainer.addItem(person, person);
        }

        collectionContainer = new CollectionContainer(persons, true, CollectionContainer.ITEM_ID_MODE_OBJECT);

        listContainer = new ListContainer<TestData.Person>(Person.class);
        listContainer.addAll(personList);
    }

    public static Map<String, Container> getContainerMap() {
        if (containerMap == null) {
            if (persons == null) {
                initializeData();
            }

            containerMap = new HashMap();
            containerMap.put("BeanItemContainer", beanItemContainer);
            containerMap.put("IndexedContainer", indexedContainer);
            containerMap.put("ExtensibleBeanContainer", extensibleBeanContainer);
            containerMap.put("CollectionContainer", collectionContainer);
            containerMap.put("ListContainer", listContainer);
        }
        return containerMap;
    }

    public static void printSize(Object object) {
        MemoryMeter meter = new MemoryMeter();
        System.out.println(" shallow size: " + meter.measure(object));
        System.out.println(" deep size: " + meter.measureDeep(object));
        System.out.println(" number of children: " + meter.countChildren(object));
    }
}