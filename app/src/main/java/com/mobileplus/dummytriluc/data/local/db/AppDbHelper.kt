package com.mobileplus.dummytriluc.data.local.db

import com.mobileplus.dummytriluc.bluetooth.request.BleErrorRequest
import com.mobileplus.dummytriluc.data.model.entity.DataBluetoothRetryEntity
import com.mobileplus.dummytriluc.data.model.entity.TableConfig
import com.mobileplus.dummytriluc.data.model.entity.TableLevel
import com.mobileplus.dummytriluc.data.model.entity.TableSubject
import io.reactivex.Observable

class AppDbHelper constructor(private val appDatabase: AppDatabase) : DbHelper {
    override fun saveLevels(levels: List<TableLevel>): Observable<List<Long>> =
        Observable.fromCallable {
            appDatabase.utilsDao().saveListLevel(levels)
        }

    override fun saveSubjects(subjects: List<TableSubject>): Observable<List<Long>> =
        Observable.fromCallable {
            appDatabase.utilsDao().saveListSubject(subjects)
        }

    override fun getAllLevel(): Observable<List<TableLevel>> = Observable.fromCallable {
        appDatabase.utilsDao().getAllLevel()
    }

    override fun getAllSubject(): Observable<List<TableSubject>> = Observable.fromCallable {
        appDatabase.utilsDao().getAllSubject()
    }

    override fun saveConfigs(configs: List<TableConfig>): Observable<List<Long>> =
        Observable.fromCallable { appDatabase.utilsDao().saveListConfig(configs) }

    override fun getAllConfig(): Observable<List<TableConfig>> = Observable.fromCallable {
        appDatabase.utilsDao().getAllConfig()
    }

    override fun getAllBluetoothDataRetry(): Observable<List<DataBluetoothRetryEntity>> {
        return Observable.fromCallable {
            appDatabase.bluetoothDao().getAllDataBluetoothRetry()
        }
    }

    override fun saveBluetoothDataRetry(data: DataBluetoothRetryEntity): Observable<Long> {
        return Observable.fromCallable {
            appDatabase.bluetoothDao().saveDataBluetoothRequest(data)
        }
    }

    override fun deleteAllDataBluetoothRetry(): Observable<Boolean> {
        return Observable.fromCallable {
            appDatabase.bluetoothDao().deleteAllBluetoothRequest()
            true
        }
    }

    override fun deleteDataBluetoothRetryByContent(content: String): Observable<Boolean> {
        return Observable.fromCallable {
            appDatabase.bluetoothDao().deleteDataBluetoothRequestByContent(content)
            true
        }
    }

    override fun getAllBluetoothDataError(): Observable<List<BleErrorRequest>> {
        return Observable.fromCallable {
            appDatabase.bluetoothDao().getAllDataBluetoothError()

        }
    }

    override fun saveBluetoothDataError(data: BleErrorRequest): Observable<Long> {
        return Observable.fromCallable {
            appDatabase.bluetoothDao().saveDataBluetoothError(data)
        }
    }

    override fun deleteAllDataBluetoothError(): Observable<Boolean> {
        return Observable.fromCallable {
            appDatabase.bluetoothDao().deleteAllBluetoothError()
            true
        }
    }

    override fun deleteDataBluetoothErrorByContent(content: String): Observable<Boolean> {
        return Observable.fromCallable {
            appDatabase.bluetoothDao().deleteDataBluetoothErrorByContent(content)
            true
        }
    }

    override fun deleteDataBluetoothError(data: BleErrorRequest): Observable<Boolean> {
        return Observable.fromCallable {
            appDatabase.bluetoothDao().deleteDataBluetoothError(data)
            true
        }
    }
}