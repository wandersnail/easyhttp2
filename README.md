## 推荐很好用的调试工具——调试全能王，各大应用市场都已上架，欢迎下载使用。
[![](https://img.shields.io/badge/下载-%E5%8D%8E%E4%B8%BA%E5%BA%94%E7%94%A8%E5%B8%82%E5%9C%BA-red)](https://appgallery.huawei.com/app/C105371117)

## 代码托管
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/cn.wandersnail/easyhttp/badge.svg)](https://maven-badges.herokuapp.com/maven-central/cn.wandersnail/easyhttp)
[![Release](https://jitpack.io/v/wandersnail/easyhttp.svg)](https://jitpack.io/#wandersnail/easyhttp)

## 使用
1. 因为使用了jdk8的一些特性，需要在module的build.gradle里添加如下配置：
```
//纯java的项目
android {
	compileOptions {
		sourceCompatibility JavaVersion.VERSION_1_8
		targetCompatibility JavaVersion.VERSION_1_8
	}
}

//有kotlin的项目还需要在project的build.gradle里添加
allprojects {
    tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile).all {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8

        kotlinOptions {
            jvmTarget = '1.8'
            apiVersion = '1.3'
            languageVersion = '1.3'
        }
    }
}
```

2. module的build.gradle中的添加依赖，自行修改为最新版本，同步后通常就可以用了：
```
dependencies {
	...
	implementation 'cn.wandersnail:easyhttp:latestVersion'
	implementation 'com.squareup.retrofit2:retrofit:latestVersion'
	implementation 'com.squareup.retrofit2:adapter-rxjava2:latestVersion'
	implementation 'io.reactivex.rxjava2:rxandroid:latestVersion'
	implementation 'com.alibaba:fastjson:latestVersion'
}
```

2. 如果从jcenter下载失败。在project的build.gradle里的repositories添加内容，最好两个都加上，添加完再次同步即可。
```
allprojects {
	repositories {
		...
		mavenCentral()
		maven { url 'https://dl.bintray.com/wandersnail/androidx/' }
	}
}
```

## 功能
- 普通get和post请求
- 带进度下载
- 带进度上传