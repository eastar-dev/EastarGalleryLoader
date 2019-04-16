package android.galleryloader

import java.util.*

object GalleryLoaderObserver : Observable() {
    override fun notifyObservers(arg: Any?) {
        setChanged()
        super.notifyObservers(arg)
    }

    fun onceUpdate(observer: Observer) {
//        Log.e(countObservers())
        clearChanged()
        deleteObservers()
//        Log.e("=>")
//        Log.w(countObservers())

        addObserver(object : Observer {
            override fun update(o: Observable?, arg: Any?) {
                o?.deleteObserver(this)
                observer.update(o, arg)
            }
        })
    }
}
