package com.appdynamicspilot.views;

/**
 * Created by aleftik on 8/28/16.
 */
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.el.MethodExpression;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;


import com.appdynamicspilot.model.Item;
import com.appdynamicspilot.service.ItemService;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuModel;

import com.appdynamicspilot.controllers.RegistrationController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author aleftik
 */
@Named
@ApplicationScoped
public class MenuBean implements Serializable {

    private MenuModel model = null;

    @Inject
    RegistrationController registrationController;

    @Inject
    ItemService itemService;
//
    public MenuBean() {

    }
//
    private void buildMenu() {
        DefaultSubMenu homeSub = new DefaultSubMenu();
        homeSub.setLabel("Home");
        homeSub.setIcon("ui-icon ui-icon-home");
        DefaultMenuItem  home = new DefaultMenuItem();
        home.setValue("Home");
        home.setIcon("ui-icon ui-icon-home");
        home.setUrl("/index.xhtml");
        homeSub.addElement(home);
        model.addElement(homeSub);

        DefaultSubMenu categorySub = new  DefaultSubMenu();
        categorySub.setLabel("Categories");
        categorySub.setIcon("ui-icon ui-icon-list");


        Item.ItemType[] types = Item.ItemType.values();
        for (Item.ItemType t:types) {
            DefaultSubMenu typeMenu = new DefaultSubMenu();
            typeMenu.setLabel(t.toString());
//            typeMenu.setUrl ("/index.xhtml?t=" + t.toString());

            List<String> categories = itemService.getCategoriesByType(t);
            for (String i: categories) {
                DefaultMenuItem categoryMenu = new DefaultMenuItem();
                categoryMenu.setValue(i);
                categoryMenu.setUrl ("/index.xhtml?t=" + t.toString() + "&c=" +i);
                typeMenu.addElement(categoryMenu);
            }
            categorySub.addElement(typeMenu);
        }

        model.addElement(categorySub);

    }

    @PostConstruct
    public void init() {
        model = new DefaultMenuModel();
        buildMenu();
    }


    private void buildLoggedInMenu() {
//        DefaultMenuItem categoriesMenu = new DefaultMenuItem();
//        categoriesMenu.setValue("Categories");
//        categoriesMenu.setIcon("fa fa-fw fa-folder-open-o");
//        model.addElement(categoriesMenu);

        if (isLoggedIn()) {


//
//            Submenu MyScheduleSub = new Submenu();
//            MyScheduleSub.setLabel("My Schedule");
//            MyScheduleSub.setIcon("ui-icon");
//
//            MenuItem editSchedule = new MenuItem();
//            editSchedule.setValue("Edit Schedule");
//            editSchedule.setUrl("/myschedule.xhtml");
//            MyScheduleSub.getChildren().add(editSchedule);
//            model.addSubmenu(MyScheduleSub);
//
//            Submenu MyProfileSub = new Submenu();
//            MyProfileSub.setLabel("My Profile");
//            MyProfileSub.setIcon("ui-icon");
//
//            MenuItem editProfile = new MenuItem();
//            editProfile.setValue("Edit Profile");
//            editProfile.setUrl("/myprofile.xhtml");
//            MyProfileSub.getChildren().add(editProfile);
//
//            MenuItem logout = new MenuItem();
//            logout.setValue("Logout");
//            logout.setAjax(false);
//            MethodExpression logoutME = FacesContext.getCurrentInstance().getApplication().getExpressionFactory().
//                    createMethodExpression(FacesContext.getCurrentInstance().getELContext(), "#{registrationController.logout}", null,
//                            new Class<?>[0]);
//            logout.setActionExpression(logoutME);
//
//            MyProfileSub.getChildren().add(logout);
//            model.addSubmenu(MyProfileSub);
        } else {
//
//            Submenu loginSub = new Submenu();
//            loginSub.setLabel("Register/Login");
//            loginSub.setIcon("ui-icon");
//            MenuItem login = new MenuItem();
//            login.setValue("Login");
//            login.setUrl("/login.xhtml");
//            loginSub.getChildren().add(login);
//            model.addSubmenu(loginSub);
//
//            Submenu registerSub = new Submenu();
//            registerSub.setLabel("Register");
//            registerSub.setIcon("ui-icon");
//            MenuItem register = new MenuItem();
//            register.setValue("Register");
//            register.setUrl("/register.xhtml");
//            loginSub.getChildren().add(register);
//            model.addSubmenu(loginSub);
        }
    }

    private boolean isLoggedIn() {

        RegistrationController controller = FacesContext.getCurrentInstance().getApplication()
                .evaluateExpressionGet(FacesContext.getCurrentInstance(), "#{registrationController}", RegistrationController.class);
        if (controller == null) {
            return false;
        }
        return controller.getIsLoggedIn();
    }
//
    public MenuModel getModel() {
        return this.model;
    }

    public ItemService getItemService() {
        return itemService;
    }

    public void setItemService(ItemService itemService) {
        this.itemService = itemService;
    }

    public RegistrationController getRegistrationController() {
        return registrationController;
    }

    public void setRegistrationController(RegistrationController registrationController) {
        this.registrationController = registrationController;
    }
}

