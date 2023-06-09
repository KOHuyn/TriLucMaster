package com.mobileplus.dummytriluc.transceiver.observer

/**
 * Created by KO Huyn on 09/06/2023.
 */
interface ISubjectMachine {
    fun registerObserver(observer: IObserverMachine)
    fun removeObserver(observer: IObserverMachine)
}