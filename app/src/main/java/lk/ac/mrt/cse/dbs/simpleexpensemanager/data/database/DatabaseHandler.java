package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.exception.InvalidAccountException;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Account;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Transaction;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final String DATABASE = "180536X";
    public static final String table_account = "Account_table";
    public static final String table_transaction = "Transaction_table";

    public DatabaseHandler(Context context) {
        super(context, DATABASE, null, 3);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableAcc = "CREATE TABLE " + table_account + "(accountNo TEXT PRIMARY KEY, bankName TEXT, accountHolderName TEXT, balance REAL )";
        String createTableTransact = "CREATE TABLE " + table_transaction + "(accountNo TEXT, expenseType TEXT,amount REAL, date DATE , FOREIGN KEY (accountNo) REFERENCES "+table_account+ "(accountNo))";
        db.execSQL(createTableAcc);
        db.execSQL(createTableTransact);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS "+table_account);
        db.execSQL("DROP TABLE IF EXISTS "+table_transaction);
        onCreate(db);
    }

    public void addAccount(Account account) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("accountNo", account.getAccountNo());
        contentValues.put("bankName", account.getBankName());
        contentValues.put("accountHolderName", account.getAccountHolderName());
        contentValues.put("balance", account.getBalance());
        db.insert(table_account, null, contentValues);
    }

    public ArrayList<String> getAccountNumbersList() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<String> accList = new ArrayList<String>();
        Cursor cursor = db.rawQuery( "SELECT accountNo FROM "+table_account, null );
        cursor.moveToFirst();
        while(cursor.isAfterLast() == false) {
            accList.add(cursor.getString(0));
            cursor.moveToNext();
        }
        return accList;
    }

    public ArrayList<Account> getAccountsList() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Account> accountList = new ArrayList<Account>();
        Cursor cursor = db.rawQuery( "SELECT * FROM "+ table_account , null );
        cursor.moveToFirst();
        while(cursor.isAfterLast() == false) {
            String account_no = cursor.getString(0);
            String bankName = cursor.getString(1);
            String accountHolderName = cursor.getString(2);
            Double balance = cursor.getDouble(3);
            Account account = new Account(account_no,bankName,accountHolderName,balance);
            accountList.add(account);
            cursor.moveToNext();
        }
        return accountList;
    }

    public Account getAccount(String accountNo) throws InvalidAccountException {
        SQLiteDatabase db = this.getReadableDatabase();
        String [] accNum = {accountNo} ;
        Cursor res = db.rawQuery( "SELECT * FROM "+ table_account +" WHERE accountNo = ?", accNum );

        if (res.moveToFirst()){
            String accountNum = res.getString(0);
            String bankName = res.getString(1);
            String accountHolderName = res.getString(2);
            Double balance = res.getDouble(3);
            Account account = new Account(accountNum,bankName,accountHolderName,balance);
            return account;
        }
        else {
            throw new InvalidAccountException("Account is invalid");
        }
    }

    public void updateBalance(String accountNo, ExpenseType expenseType, double amount) throws InvalidAccountException {
        Account account = getAccount(accountNo);

        switch (expenseType) {
            case INCOME:
                account.setBalance(account.getBalance() + amount);
                break;
            case EXPENSE:
                account.setBalance(account.getBalance() - amount);
                break;
        }
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("balance", account.getBalance());
        db.update(table_account, contentValues, "accountNo = ? ", new String[] {account.getAccountNo()});
    }

    public void removeAccount(String accountNo) throws InvalidAccountException {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] account_No = {accountNo};
        Cursor cursor = db.rawQuery( "SELECT * FROM "+ table_account +" WHERE accountNo = ?", account_No );

        if(cursor.moveToFirst()){
            db.delete(table_account, "accountNo = ? ", account_No);
        }
        else{
            throw new InvalidAccountException("Account is invalid");
        }
    }

    public void logTransaction(Date date, String accountNo, ExpenseType expenseType, double amount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        Transaction transaction = new Transaction(date, accountNo, expenseType, amount);
        contentValues.put("accountNo", transaction.getAccountNo());
        contentValues.put("expenseType", String.valueOf(transaction.getExpenseType()));
        contentValues.put("amount", transaction.getAmount());
        contentValues.put("date", new SimpleDateFormat().format(transaction.getDate()));
        db.insert(table_transaction, null, contentValues);
    }

    public List<Transaction> getAllTransactionLogs() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Transaction> transactList = new ArrayList<Transaction>();
        Cursor cursor = db.rawQuery( "SELECT * FROM "+ table_transaction , null );
        cursor.moveToFirst();
        try {
            while(cursor.isAfterLast() == false) {
                String account_no = cursor.getString(0);
                String expense_type_db = cursor.getString(1);
                Double amount = cursor.getDouble(2);
                Date date = new SimpleDateFormat("dd/mm/yyyy").parse(cursor.getString(3));

                ExpenseType expenseType = ExpenseType.EXPENSE;
                if(expense_type_db.equals("INCOME")){
                    expenseType = ExpenseType.INCOME;
                }
                Transaction transaction = new Transaction(date,account_no,expenseType,amount);
                transactList.add(transaction);
                cursor.moveToNext();
            }
        }catch (ParseException e) {
            e.printStackTrace();
        }
        return transactList;
    }

    public List<Transaction> getPaginatedTransactionLogs(int limit) {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Transaction> transactionList = new ArrayList<Transaction>();
        Cursor cursor = db.rawQuery( "SELECT * FROM "+table_transaction+" ORDER BY date DESC LIMIT ?",new String[]{Integer.toString(limit)});
        cursor.moveToFirst();
        try {
            while(cursor.isAfterLast() == false) {
                String accNo = cursor.getString(0);
                Double amount = cursor.getDouble(2);

                Date date = new SimpleDateFormat("dd/mm/yyyy").parse(cursor.getString(3));
                String expense_type_db = cursor.getString(1);
                ExpenseType expenseType = ExpenseType.EXPENSE;
                if (expense_type_db.equals("INCOME")) {
                    expenseType = ExpenseType.INCOME;
                }
                Transaction transaction = new Transaction(date, accNo, expenseType, amount);
                transactionList.add(transaction);
                cursor.moveToNext();
            }
        }catch (ParseException e) {
            e.printStackTrace();
        }
        return transactionList;
    }
}
