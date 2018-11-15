# Configurer
通过注解在编译时自动生成工厂与生产者关系的配置绑定


## 开始配置

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
       classpath "com.taoszu.configurer:plugin:1.1.7.3"
    }
    ```
    
3. Add plugin to project's `build.gradle`:

    ```gradle
    apply plugin: 'com.taoszu.configurer'
    ```
    
## 使用
1. 定义基础接口类
  ```java
    public interface BaseProgramer {
      void doProgram();
    }
   ```
   
2. 配置注解(两种方式) 

     2.1 在类增加注解Wokrer 
   ```java
   @Worker(key = "android", module = "IT", baseClass = BaseProgramer.class )
   Class AndroidProgramer implements BaseProgramer {
         @Override
         public void doProgram() {
           Log.e("Programer", "我是安卓程序🐒")
         }
   }
   ```
   
     2.2 在类增加注解Factory
     
     ```java
     @Factory(module = "IT")
     public class ITFactory implements BaseFactory<BaseProgramer> {
        Map<String, BaseProgramer> baseProgramerMap;
        
        public ITFactory() {
           baseProgramerMap = new HashMap<>();
           baseProgramerMap.put("android", new BaseProgramer() {
            @Override
            public void doProgram() {
            Log.e("Programer", "我是安卓程序🐒")
            }
          });
        }

       @Override
       public BaseProgramer getWorker(String key) {
        return baseProgramerMap.get(key);
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
