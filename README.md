# Configurer
通过注解在编译时自动生成工厂与生产者关系的配置绑定


## Get Start

1. Add  repository to root `build.gradle`:

    ```gradle
    buildscript {
      repositories {
         jcenter()
      }
    }
    
    allprojects {
        repositories {
            jcenter()
        }
    }
    ```

2. Add classpath to root  `build.gradle`:

    ```gradle
    dependencies {
       classpath "com.taoszu.configurer:plugin:1.1.4.1"
    }
    ```
    
3. Add plugin to project's `build.gradle`:

    ```gradle
    apply plugin: 'com.taoszu.configurer'
    ```
    
## Use
1. 定义基础接口类
  ```java
    public interface BaseProgramer {
      void doProgram();
    }
   ```
   
2. 在类增加注解Wokrer 
   

   ```java
   @Worker(key = "android", module = "IT", baseClass = BaseProgramer.class )
   Class AndroidProgramer implements BaseProgramer {
         @Override
         public void doProgram() {
           Log.e("Programer", "我是安卓程序🐒")
         }
   }
   ```

3. 调用 FactoryHub.load() 初始化

4. 获取Worker
    ```java
    ITFactory itFactory = (ITFactory) FactoryHub.getFactoryInstance("IT");
    BaseProgramer androidProgramer = itFactory.getWorker("android");
    androidProgramer.doProgram()
   ```
