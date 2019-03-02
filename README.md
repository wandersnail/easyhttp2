## 代码托管
[![](https://jitpack.io/v/wandersnail/network-requester.svg)](https://jitpack.io/#wandersnail/network-requester)
[![Download](https://api.bintray.com/packages/wandersnail/android/network-requester/images/download.svg) ](https://bintray.com/wandersnail/android/network-requester/_latestVersion)

## 使用

1. module的build.gradle中的添加依赖，自行修改为最新版本，同步后通常就可以用了：
```
dependencies {
	...
	implementation 'com.github.wandersnail:network-requester:1.0.0'
}
```

2. 如果从jcenter下载失败。在project的build.gradle里的repositories添加内容，最好两个都加上，有时jitpack会抽风，同步不下来。添加完再次同步即可。
```
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
		maven { url 'https://dl.bintray.com/wandersnail/android/' }
	}
}
```

## 功能
- 普通get和post请求
- 带进度下载
- 带进度上传
