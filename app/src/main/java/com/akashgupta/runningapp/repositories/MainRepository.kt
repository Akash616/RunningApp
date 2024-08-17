package com.akashgupta.runningapp.repositories

import com.akashgupta.runningapp.db.Run
import com.akashgupta.runningapp.db.RunDAO
import javax.inject.Inject

class MainRepository @Inject constructor(
    val runDAO: RunDAO //(Only one parameter)
) {

    suspend fun insertRun(run: Run) = runDAO.insertRun(run)

    suspend fun deleteRun(run: Run) = runDAO.deleteRun(run)

    //LiveData execute by default asynchronously, so we do not need suspend
    fun getAllRunSortedByDate() = runDAO.getAllRunSortedByDate()

    fun getAllRunSortedByTimeInMillis() = runDAO.getAllRunSortedByTimeInMillis()

    fun getAllRunSortedByCaloriesBurned() = runDAO.getAllRunSortedByCaloriesBurned()

    fun getAllRunSortedByAvgSpeedInKMH() = runDAO.getAllRunSortedByAvgSpeedInKMH()

    fun getAllRunSortedByDistanceInMeters() = runDAO.getAllRunSortedByDistanceInMeters()

    fun getTotalTimeInMillis() = runDAO.getTotalTimeInMillis()

    fun getTotalCaloriesBurned() = runDAO.getTotalCaloriesBurned()

    fun getTotalDistance() = runDAO.getTotalDistance()

    fun getTotalAvgSpeed() = runDAO.getTotalAvgSpeed()

}