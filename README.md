# Configurer
通过注解编译时生成关系的配置绑定，无需手动配置


## Get Start

1. Add  repository to root `build.gradle`:

    ```gradle
    buildscript {
      repositories {
        maven { url 'https://www.jitpack.io' }
        maven {url 'https://dl.bintray.com/taoszu/maven'}
      }
    }
    
    allprojects {
        repositories {
              maven { url 'https://www.jitpack.io' }
              maven {url 'https://dl.bintray.com/taoszu/maven'}
        }
    }
    ```

2. Add annotationProcessor to project's `build.gradle`:

    ```gradle
    dependencies {
        annotationProcessor "com.taoszu.configurer:processor:1.0.0"
    }
    ```
    
3. Add plugin to project's `build.gradle`:

    ```gradle
    apply plugin: 'com.taoszu.configurer'
    ```
    
## Use

1. 在类增加注解Wokrer 
   ```java
   @Worker(key = "taoszu", module = "Worker")
   Class Worker {
   }
   ```

2. 调用 FactoryHub.load() 初始化

3. 获取Worker的工厂
    ```java
    BaseFactory workerFactory = FactoryHub.getFactoryInstance(module)
   ```
