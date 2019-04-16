[![Release](https://jitpack.io/v/djrain/permission.svg)](https://jitpack.io/#djrain/permission)


# What is permission?

After Marshmallow in Android<br/>
안드로이드에서는 권한을 요청합니다.

This library is a simple library designed to help you with authorization requests.<br/>
이 라이브러리는 권한 요청 작업 도움을 주고자 만들어진 심플한 라이브러리 입니다.

For more information about Android privilege requests, please see:<br/>
안드로이드 권한 요청에 대한 상세한 설명은 아래 주소를 참고 하시면 됩니다.<br/>
([See permissions overview](https://developer.android.com/guide/topics/permissions/overview))<br/>

You can make check function yourself.<br/>
([How to Requesting Permissions at RunTime](http://developer.android.com/intl/ko/training/permissions/requesting.html))<br/>

안드로이드 권한 요청작업을 위해서는 다음과 같은 함수들을 사용해야 하며<br/>
(`checkSelfPermission()`, `requestPermissions()`, `onRequestPermissionsResult()`, `onActivityResult()` ...)
보다 친절한 기능을 위해서는 반복적인 추가 작업이 필요로 합니다.

permission is most of simple and smallest permission check helper.


<br/><br/>



## Demo


![Screenshot](https://github.com/djrain/permission/blob/readme/demo.gif?raw=true)    
           

sample RESULTDLG
1. Request permission
2. Show message dialog for setting when denied permission



<br/><br/>




## How...

### Gradle with jitpack

#### Add it in your root build.gradle at the end of repositories:
```javascript

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

```
#### Add the dependency
```javascript

	dependencies {
	        implementation 'com.github.djrain:permission:2.1.2'
	}


```



If you think this library is useful, please press star button at upside.
<br/>


<br/><br/>

## How to use


### 1. Make PermissionListener
We will use PermissionListener for Permission Result.
You will get result to `onPermissionGranted()`, `onPermissionDenied()`

```javascript
  var request =  PermissionRequest.builder(this, Manifest.permission.WRITE_EXTERNAL_STORAGE
                                                , Manifest.permission.READ_CALENDAR)
          .setRequestMessage("for contact photo save image")
          .setRequestPositiveButtonText("OK")
          .setRequestNegativeButtonText("cancel")
          .run()
```

<br/>

##Customize
You can customize something ...<br />

* `setRequestMessage(R.string.xxx or CharSequence)`
* `setRequestPositiveButtonText(R.string.xxx or CharSequence) (default: confirm / 확인)`
* `setRequestNegativeButtonText(R.string.xxx or CharSequence) (default: close / 닫기)`
* `setDenyMessage(R.string.xxx or CharSequence)`
* `setDenyPositiveButtonText(R.string.xxx or CharSequence) (default: close / 닫기)`
* `setDenyNegativeButtonText(R.string.xxx or CharSequence) (default: setting / 설정)`

<br/><br/>





## Thanks 





<br/><br/>


## License 
 ```code
Copyright 2016 Eastar Jeong

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.```
