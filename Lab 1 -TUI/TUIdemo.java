package com.mybank.tui;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.util.Locale;

import jexer.TAction;
import jexer.TApplication;
import jexer.TField;
import jexer.TText;
import jexer.TWindow;
import jexer.event.TMenuEvent;
import jexer.menu.TMenu;

import com.mybank.domain.Bank;
import com.mybank.domain.Customer;
import com.mybank.domain.Account;
import com.mybank.domain.CheckingAccount;
import com.mybank.domain.SavingsAccount;
import com.mybank.data.DataSource;
/**
 *
 * @author Yaroslav Pichugin
 */
public class TUIdemo extends TApplication {

    private static final int ABOUT_APP = 2000;
    private static final int CUST_INFO = 2010;

    public static void main(String[] args) throws Exception {
        TUIdemo tdemo = new TUIdemo();
        (new Thread(tdemo)).start();
    }

    public TUIdemo() throws Exception {
        super(BackendType.SWING);

        Locale.setDefault(Locale.US);

        addToolMenu();
        //custom 'File' menu
        TMenu fileMenu = addMenu("&File");
        fileMenu.addItem(CUST_INFO, "&Customer Info");
        fileMenu.addDefaultItem(TMenu.MID_SHELL);
        fileMenu.addSeparator();
        fileMenu.addDefaultItem(TMenu.MID_EXIT);
        //end of 'File' menu  

        addWindowMenu();

        //custom 'Help' menu
        TMenu helpMenu = addMenu("&Help");
        helpMenu.addItem(ABOUT_APP, "&About...");
        //end of 'Help' menu 

        setFocusFollowsMouse(true);
        //Customer window
        ShowCustomerDetails();
    }

    @Override
    protected boolean onMenu(TMenuEvent menu) {
        if (menu.getId() == ABOUT_APP) {
            messageBox("About", "\t\t\t\t\t   Just a simple Jexer demo.\n\nCopyright \u00A9 2019 Alexander \'Taurus\' Babich").show();
            return true;
        }
        if (menu.getId() == CUST_INFO) {
            ShowCustomerDetails();
            return true;
        }
        return super.onMenu(menu);
    }

    private void ShowCustomerDetails() {
    TWindow custWin = addWindow("Customer Window", 2, 1, 40, 10, TWindow.NOZOOMBOX);
    custWin.newStatusBar("Enter valid customer number and press Show...");
    custWin.addLabel("Enter customer number: ", 2, 2);
    TField custNo = custWin.addField(24, 2, 3, false);
    TText details = custWin.addText("Owner Name: \nAccount Type: \nAccount Balance: ", 2, 4, 38, 8);

    custWin.addButton("&Show", 28, 2, new TAction() {
    @Override
    public void DO() {
        try {
            // Копіюємо test.dat з ресурсу JAR до тимчасового файлу
            InputStream inputStream = TUIdemo.class.getResourceAsStream("test.dat");
            if (inputStream == null) {
                messageBox("Error", "Cannot find test.dat resource!").show();
                return;
            }

            // Створюємо тимчасовий файл
            File tempFile = File.createTempFile("test", ".dat");
            tempFile.deleteOnExit();

            try (OutputStream outStream = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outStream.write(buffer, 0, bytesRead);
                }
            }

            // Завантажуємо дані
            DataSource dataSource = new DataSource(tempFile.getPath());
            dataSource.loadData();

            int custNum = Integer.parseInt(custNo.getText());
            Bank bank = Bank.getBank();

            if (custNum < 0 || custNum >= bank.getNumberOfCustomers()) {
                messageBox("Error", "Customer not found!").show();
                return;
            }

            Customer customer = bank.getCustomer(custNum);
            Account account = customer.getAccount(0);

            String accountType;
            if (account instanceof CheckingAccount) {
                accountType = "Checking";
            } else if (account instanceof SavingsAccount) {
                accountType = "Savings";
            } else {
                accountType = "Unknown";
            }

            details.setText(
                "Owner Name: " + customer.getFirstName() + " " + customer.getLastName() + "\n" +
                "Account Type: " + accountType + "\n" +
                "Account Balance: $" + String.format("%.2f", account.getBalance())
            );

        } catch (Exception e) {
            e.printStackTrace(); // для діагностики
            messageBox("Error", "You must provide a valid customer number!").show();
        }
    }
});

}
}
