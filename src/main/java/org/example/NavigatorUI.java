package org.example;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.external.org.slf4j.Logger;
import com.vaadin.external.org.slf4j.LoggerFactory;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.*;
import com.vaadin.ui.*;

import java.io.File;

import static com.vaadin.ui.Notification.Type.HUMANIZED_MESSAGE;

/**
 * This UI is the application entry point. A UI may either represent a browser window
 * (or tab) or some part of a html page where a Vaadin application is embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is intended to be
 * overridden to add component to the user interface and initialize non-component functionality.
 */
@Theme("mytheme")
public class NavigatorUI extends UI {

    // allows switching of views in a part of an app
    Navigator navigator;
    protected static final String MAINVIEW = "main";
    private static final Logger LOGGER = LoggerFactory.getLogger(NavigatorUI.class);

    /**
     * Initializes the user interface
     * Here components can be added and non-component functionality initialized
     *
     * @param vaadinRequest Generic request to the server
     */
    @Override
    protected void init(VaadinRequest vaadinRequest) {
        navigator = new Navigator(this, this);
        navigator.addView("", new StartView());
        navigator.addView(MAINVIEW, new MainView());

    }

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = NavigatorUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }

    /**
     * Initial view
     * Displays a welcome message and gives the opportunity to navigate to the MainView
     */
    public class StartView extends VerticalLayout implements View {

        /**
         * Constructor
         * Initializes the view content
         */
        public StartView() {
            setSizeFull();
            // Navigate from StartView to MainView using a button
            Button button = new Button("Go to Main View", new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    navigator.navigateTo(MAINVIEW);
                }
            });
            addComponent(button);
            setComponentAlignment(button, Alignment.MIDDLE_CENTER);
        }

        /**
         * Runs when the application is opened with the URI associated with the StartView or the User navigates to the StartView
         *
         * @param event ViewChangeEvent
         */
        @Override
        public void enter(ViewChangeListener.ViewChangeEvent event) {
            Notification.show("Welcome to the Animal Farm", HUMANIZED_MESSAGE);
        }

    } // end of StartView

    /**
     * Main view with the main content
     * Is displayed when the user clicks the 'Go to MainView' button on the StartView
     */
    public class MainView extends VerticalLayout implements View {
        // Menu navigation button listener
        class ButtonListener implements Button.ClickListener {
            String menuitem;

            public ButtonListener(String menuitem) {
                this.menuitem = menuitem;
            }

            @Override
            public void buttonClick(Button.ClickEvent event) {
                navigator.navigateTo(MAINVIEW + "/" + menuitem);
            }
        }

        Panel equalPanel = new Panel(); // Contains the labels and the image of the selected animal
        Button logout = new Button("Go back to the start");

        /**
         * Constructor
         * Initializes the view content
         */
        public MainView() {
            setCaption("MainView");

            // Add buttons to the MainView
            addComponent(new Button("Pig",
                    new ButtonListener("pig")));
            addComponent(new Button("Cat",
                    new ButtonListener("cat")));

            // Allow going back to the start
            logout.addClickListener(event ->
                    navigator.navigateTo(""));
            addComponent(logout);
        }

        /**
         * Sets the content of the panel when a animal is selected over a button
         *
         * @param event ViewChangeEvent (here: ButtonClickEvent)
         */
        @Override
        public void enter(ViewChangeListener.ViewChangeEvent event) {
            // If there are no view changes detected return nothing
            if (event.getParameters() == null
                    || event.getParameters().isEmpty()) {
                return;
            } else { // else add panel with labels and animal image to the MainView
                addComponent(equalPanel);
                equalPanel.setContent(new AnimalViewer(
                        event.getParameters()));
            }

        }

        /**
         * Panel that contains the labels and the image of the selected animal
         */
        class AnimalViewer extends VerticalLayout {
            Label watching = new Label();
            Embedded pic = new Embedded(); // embedded image of the animal
            Label back = new Label();

            public AnimalViewer(String animal) { //
                // Detect Webapp base folder
                // String basePath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();

                FileResource resource = new FileResource(new File("img/" + animal + ".png")); // image file
                LOGGER.info("Loaded: " + resource.getFilename());

                // set labels and image
                watching.setValue("You are currently watching a " +
                        animal);
                pic.setSource(resource);
                back.setValue("And " + animal + " is watching you back");

                // Add labels and image to the MainView
                addComponent(watching);
                addComponent(pic);
                addComponent(back);
            }
        } // end of AnimalViewer

    } // end of MainView

} // end of NavigatorUI