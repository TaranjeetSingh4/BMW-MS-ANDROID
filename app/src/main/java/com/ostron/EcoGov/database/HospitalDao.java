package com.ostron.EcoGov.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface HospitalDao {

    @Query("SELECT * FROM hospitalmodel")
    LiveData<List<HospitalModel>> getAllData();

    @Insert
    void insertAllData(HospitalModel... hospitalModel);

    @Query("UPDATE hospitalmodel SET waste_weight = :weight")
    void updateData(String weight);

    @Query("DELETE FROM hospitalmodel WHERE uid = :id")
    void deleteData(int id);

}
