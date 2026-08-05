package com.appventurez.EcoGov.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {HospitalModel.class}, version = 2)
public abstract class MyDatabase extends RoomDatabase {
    public abstract HospitalDao hospitalDao();
}
