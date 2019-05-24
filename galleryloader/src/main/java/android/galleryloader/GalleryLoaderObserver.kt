/*
 * Copyright 2019 copyright eastar Jeong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


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
