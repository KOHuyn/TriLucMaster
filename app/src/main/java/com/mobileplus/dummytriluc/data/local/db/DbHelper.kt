package com.mobileplus.dummytriluc.data.local.db

import com.mobileplus.dummytriluc.bluetooth.request.BleErrorRequest
import com.mobileplus.dummytriluc.data.model.entity.DataBluetoothRetryEntity
import com.mobileplus.dummytriluc.data.model.entity.TableConfig
import com.mobileplus.dummytriluc.data.model.entity.TableLevel
import com.mobileplus.dummytriluc.data.model.entity.TableSubject
import io.reactivex.Observable

interface DbHelper {
    fun saveLevels(levels: List<TableLevel>): Observable<List<Long>>
    fun saveSubjects(subjects: List<TableSubject>): Observable<List<Long>>
    fun getAllLevel(): Observable<List<TableLevel>>
    fun getAllSubject(): Observable<List<TableSubject>>
    fun saveConfigs(configs: List<TableConfig>): Observable<List<Long>>
    fun getAllConfig(): Observable<List<TableConfig>>

    fun getAllBluetoothDataRetry(): Observable<List<DataBluetoothRetryEntity>>
    fun saveBluetoothDataRetry(data: DataBluetoothRetryEntity): Observable<Long>
    fun deleteAllDataBluetoothRetry(): Observable<Boolean>
    fun deleteDataBluetoothRetryByContent(content: String): Observable<Boolean>

    fun getAllBluetoothDataError(): Observable<List<BleErrorRequest>>
    fun saveBluetoothDataError(data: BleErrorRequest): Observable<Long>
    fun deleteAllDataBluetoothError(): Observable<Boolean>
    fun deleteDataBluetoothErrorByContent(content: String): Observable<Boolean>
    fun deleteDataBluetoothError(data: BleErrorRequest): Observable<Boolean>
}