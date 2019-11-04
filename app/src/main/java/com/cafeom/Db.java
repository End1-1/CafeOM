package com.cafeom;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class Db extends SQLiteOpenHelper {
    public static final int dbversion = 7;
    private ContentValues contentValues;
    private SQLiteDatabase dbLite;
    private Cursor cursor;

    public Db(@Nullable Context context) {
        super(context, "cafeom", null, dbversion);
        dbLite = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table rem (rec text primary key, table_name text, staff text, dish text, qty text, state_id text, started text, comments text) ");
        db.execSQL("create table his (rec text primary key, table_name text, staff text, dish text, qty text, state_id text, started text, comments text) ");
    }

    public void noException(SQLiteDatabase db, String sql) {
        try {
            db.execSQL(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            noException(db, "drop table rem");
            noException(db, "drop table his");
            onCreate(db);
        }
    }

    public ContentValues getContentValues() {
        if (contentValues == null) {
            contentValues = new ContentValues();
        }
        contentValues.clear();
        return contentValues;
    }

    public boolean exec(String sql) {
        try {
            dbLite.execSQL(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public Cursor select(String sql) {
        cursor = dbLite.rawQuery(sql, null);
        return cursor;
    }

    public boolean moveToNext() {
        if (cursor == null) {
            return false;
        }
        return cursor.moveToNext();
    }

    public int getInt(String columnName) {
        return cursor.getInt(cursor.getColumnIndex(columnName));
    }

    public String getString(String columnName) {
        return cursor.getString(cursor.getColumnIndex(columnName));
    }

    public boolean insert(String table) {
        try {
            dbLite.insertOrThrow(table, null, contentValues);
            contentValues.clear();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public int insertWithId(String table) {
        if (insert(table)) {
            Cursor c = dbLite.rawQuery("select last_insert_rowid()", null);
            if (c.moveToLast()) {
                return c.getInt(0);
            }
        }
        return 0;
    }

    public void close() {
        dbLite.close();
    }
}
