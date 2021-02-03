package lk.ac.mrt.cse.dbs.simpleexpensemanager.control;

import android.content.Context;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.AccountDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.TransactionDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.database.DatabaseHandler;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl.PersistentAccountDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl.PersistentTransactionDAO;

public class PersistentExpenseManager extends ExpenseManager{
    final Context context;
    public PersistentExpenseManager(Context context) {
        this.context = context;
        setup();
    }
//implementation of setup method
    @Override
    public void setup() {
        DatabaseHandler databaseHandler = new DatabaseHandler(context);
        TransactionDAO persistentTransactionDAO = new PersistentTransactionDAO(databaseHandler);
        setTransactionsDAO(persistentTransactionDAO);

        AccountDAO persistentAccountDAO = new PersistentAccountDAO(databaseHandler);
        setAccountsDAO(persistentAccountDAO);
    }
}
