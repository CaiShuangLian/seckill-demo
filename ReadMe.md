# 秒杀系统

## 一、搭建项目

### 1.依赖

使用spring initializr模板创建项目，勾选以下依赖

<img src="F:\秋招学习\项目\myproject\image-20210907185326276.png" alt="image-20210907185326276" style="zoom:60%;" />

导入依赖Mybatis Plus 依赖，在Mybatis Plus官网安装模块，选择spring boot的maven依赖包

```xml
<!--        Mybatis Plus组件-->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
            <version>3.4.0</version>
        </dependency>
```

### 2.配置文件

```yml
spring:
  #thymeleaf配置
  thymeleaf:
    # 关闭缓存
    cache: false
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/seckill?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
    hikari:
      # 连接池名（据说是最快的连接池）
      pool-name: DataHikariCP
      # 最小空闲连接数
      minimum-idle: 5
      # 空闲连接存活最大时间，默认600000（10分钟）
      idle-timeout: 18000
      # 最大连接数，默认10
      maximum-pool-size: 10
      # 从连接池返回的连接自动提交
      auto-commit: true
      # 连接最大存活时间，0表示永久存活，默认1800000（30分钟）
      max-lifetime: 1800000
      # 连接超时时间，默认30000（30秒）
      connection-timeout: 30000
      # 测试连接是否可用的查询语句
      connection-test-query: SELECT 1

#Mybatis-plus配置
mybatis-plus:
  #配置Mapper.xml映射文件
  mapper-locations: classpath*:/mapper/*Mapper.xml
  #配置Mybatis数据返回数据别名（默认别名是类名）
  type-aliases-package: com.csl.seckill.pojo

logging:
  level:
    com.csl.seckill.mapper: debug
```

### 3.创建包

在java包下面的seckill包下，创建controller，service，pojo，mapper包，在service包下创建impl包

在resource包下创建mapper包，放置mapper文件

其中templates包下放置前端页面

### 4.测试

①在启动类中添加注解：@MapperScan("com.csl.seckill.pojo")，添加此注解以后，com.csl.seckill.pojo包下面的接口类，在编译之后都会生成相应的实现类。

②写一个页面跳转controller

首先声明是controller层（@controller）

请求地址时http://localhost:8080/demo/hello 使用@RequestMapping注解进行映射，返回映射的页面名称

```java
@Controller
@RequestMapping("/demo")
public class DemoController {

    /**
     * 测试页面跳转
     * @param model
     * @return
     */
    @RequestMapping("/hello")
    public String hello(Model model){
        model.addAttribute("name","csl");
        return "hello";
    }
}
```

③创建一个hello页面：

```HTML
<!DOCTYPE html>
<html lang="en"
        xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>测试</title>
</head>
<body>
    <P th:text="'hello'+${name}"></P> <!-- thymeleaf语法 controller层中使用Model进行传值-->
</body>
</html>
```

xmlns:th="http://www.thymeleaf.org"：引入thymeleaf，可以使用thymeleaf语法进行传值

此时输入地址：http://localhost:8080/demo/hello 即可显示：hellocsl

### 5.创建数据库

创建数据库seckill，选择字符集为utf8mb4，可以多存emoji表情

```mysql
CREATE TABLE t_user(
    id BIGINT(20) NOT NULL COMMENT '用户ID，手机号码',
    nickname VARCHAR(255) NOT NULL,
    pwd VARCHAR(32) NOT NULL COMMENT 'MD5（MD5（pass明文+固定salt）+salt）',
    salt VARCHAR(10) DEFAULT NULL,
    head VARCHAR(128) DEFAULT NULL COMMENT '头像',
    register_date  datetime DEFAULT NULL COMMENT '注册时间',
    last_login_date datetime DEFAULT NULL COMMENT '最后一次登录时间',
    login_count int(11)DEFAULT '0' COMMENT '登录次数',
    PRIMARY KEY(id)
)
```

### 6.MD5加密

两次MD5加密：保证安全

第一次MD5加密：用户端前端输入密码时，将密码进行一次加密传输到后端，避免明文在网络中传输，避免明文直接被截获

第二次MD5加密：后端接收到第一次加密密码后，进行第二次MD5加密然后传输到数据库，防止数据库被盗用后，根据加密秘文和盐值反推出密码

引入MD5依赖

```xml
<!--        MD5依赖-->
        <dependency>
            <groupId>commons-codec</groupId>
            <artifactId>commons-codec</artifactId>
        </dependency>
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-lang3</artifactId>
            <version>3.6</version>
        </dependency>
```

创建一个工具包utils，创建MD5工具类

```java
@Component
public class MD5Util {

    /**
     * 将数据进行MD5加密
     * @param src
     * @return
     */
    public static String md5(String src){
        return DigestUtils.md5Hex(src);
    }

    private static final String salt="1a2b3c4d";

    /**
     * 第一次MD5加密（即将前台输入的数据加密后传到后台）
     * @param inputPass
     * @return
     */
    public static String inputPassToFormPass(String inputPass){
        //为了安全性，取部分盐值
        String str=salt.charAt(0)+salt.charAt(2)+inputPass+salt.charAt(5)+salt.charAt(4);
        return md5(str);
    }

    /**
     * 第二次MD5加密（将后台的数据加密后传到数据库）
     * @param formPass
     * @param salt
     * @return
     */
    public static String formPassToDBPass(String formPass,String salt){
        String str=salt.charAt(0)+salt.charAt(2)+formPass+salt.charAt(5)+salt.charAt(4);
        return md5(str);
    }

    /**
     * 将输入的数据进行两次加密后传到数据库
     * @param inputPass
     * @param salt
     * @return
     */
    public static String inputPassToDBPass(String inputPass,String salt){
        String formPass=inputPassToFormPass(inputPass);
        String dbPass=formPassToDBPass(formPass,salt);
        return dbPass;
    }

    /**
     * 测试
     * @param args
     */
    public static void main(String[] args) {
//        ce21b747de5af71ab5c2e20ff0a60eea
        System.out.println(inputPassToFormPass("123456"));
//        b640b74f58005bb70b1c963a025b7549
        System.out.println(formPassToDBPass("ce21b747de5af71ab5c2e20ff0a60eea","1a2c3b4d"));
//       b640b74f58005bb70b1c963a025b7549
        System.out.println(inputPassToDBPass("123456","1a2c3b4d"));
    }
}
```

### 7.逆向工程

①对于Mybatis的逆向工程，可以生成到pojo，mapper和mapper.xml，但是对于Mybatis Plus来说，可以更多地生成到service，impl等

②建议将逆向工程单独做一个项目，这样以后只需要改配置即可

创建流程同创建项目一致

③导入依赖：

```xml
<!--        Mybatis Plus组件-->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
            <version>3.4.0</version>
        </dependency>
<!--        自动代码生成器-->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-generator</artifactId>
            <version>3.4.0</version>
        </dependency>
<!--        freemarker 依赖-->
        <dependency>
            <groupId>org.freemarker</groupId>
            <artifactId>freemarker</artifactId>
            <version>2.3.30</version>
        </dependency>
<!--        mysql数据库 运行时有效-->
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <scope>runtime</scope>
        </dependency>
```

创建代码生成器类

```java
public class CodeGenerator {
    /**
     *    * <p>
     *    * 读取控制台内容
     *    * </p>
     *    
     */
    public static String scanner(String tip) {
        Scanner scanner = new Scanner(System.in);
        StringBuilder help = new StringBuilder();
        help.append("请输入" + tip + "：");
      System.out.println(help.toString());
      if (scanner.hasNext()) {
        String ipt = scanner.next();
        if (StringUtils.isNotBlank(ipt)) {
            return ipt;
        }
    }
      throw new MybatisPlusException("请输入正确的" + tip + "！");
  }
  

    public static void main(String[] args) {
      // 代码生成器
      AutoGenerator mpg = new AutoGenerator();
      // 全局配置
      GlobalConfig gc = new GlobalConfig();
      String projectPath = System.getProperty("user.dir");
      gc.setOutputDir(projectPath + "/src/main/java");
      //作者
      gc.setAuthor("CaiShuangLian");
      //打开输出目录
      gc.setOpen(false);
      //xml开启 BaseResultMap
      gc.setBaseResultMap(true);
      //xml 开启BaseColumnList
      gc.setBaseColumnList(true);
      //日期格式，采用Date
      gc.setDateType(DateType.ONLY_DATE);
      mpg.setGlobalConfig(gc);
      // 数据源配置
      DataSourceConfig dsc = new DataSourceConfig();
      
        dsc.setUrl("jdbc:mysql://localhost:3306/seckill? useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia" + "/Shanghai");
      dsc.setDriverName("com.mysql.cj.jdbc.Driver");
      dsc.setUsername("root");
      dsc.setPassword("1234");
      mpg.setDataSource(dsc);
      // 包配置
      PackageConfig pc = new PackageConfig();
      pc.setParent("com.csl.seckill")
           .setEntity("pojo")
           .setMapper("mapper")
           .setService("service")
           .setServiceImpl("service.impl")
           .setController("controller");
      mpg.setPackageInfo(pc);
      // 自定义配置
      InjectionConfig cfg = new InjectionConfig() {
        @Override
            public void initMap() {
            // to do nothing
            Map<String, Object> map = new HashMap<>();
            map.put("date1", "1.0.0");
            this.setMap(map);
        }
    
        };
      // 如果模板引擎是 freemarker
      String templatePath = "/templates/mapper.xml.ftl";
      // 如果模板引擎是 velocity
      // String templatePath = "/templates/mapper.xml.vm";
      // 自定义输出配置
      List<FileOutConfig> focList = new ArrayList<>();
      // 自定义配置会被优先输出
      focList.add(new FileOutConfig(templatePath) {
        @Override
            public String outputFile(TableInfo tableInfo) {
            // 自定义输出文件名 ， 如果你 Entity 设置了前后缀、此处注意 xml 的名称会跟着发生变化！！
            return projectPath + "/src/main/resources/mapper/" +
                        tableInfo.getEntityName() + "Mapper"
                  +StringPool.DOT_XML;
        }
    
        });
      cfg.setFileOutConfigList(focList);
      mpg.setCfg(cfg);
      // 配置模板
      TemplateConfig templateConfig = new TemplateConfig()
           .setEntity("templates/entity2.java")
           .setMapper("templates/mapper2.java")
           .setService("templates/service2.java")
           .setServiceImpl("templates/serviceImpl2.java")
           .setController("templates/controller2.java");
      templateConfig.setXml(null);
      mpg.setTemplate(templateConfig);
        // 策略配置
      StrategyConfig strategy = new StrategyConfig();
      //数据库表映射到实体的命名策略
      strategy.setNaming(NamingStrategy.underline_to_camel);
      //数据库表字段映射到实体的命名策略
      strategy.setColumnNaming(NamingStrategy.underline_to_camel);
      //lombok模型
      strategy.setEntityLombokModel(true);
      //生成 @RestController 控制器
      // strategy.setRestControllerStyle(true);
      strategy.setInclude(scanner("表名，多个英文逗号分割").split(","));
      strategy.setControllerMappingHyphenStyle(true);
      //表前缀
      strategy.setTablePrefix("t_");
      mpg.setStrategy(strategy);
      mpg.setTemplateEngine(new FreemarkerTemplateEngine());
      mpg.execute();
  }
}
```

注意配置模板setEntity("templates/entity2.java")，在Maven的templates相关文件复制到resources下templates，注意改名字与代码中相关

<img src="F:\秋招学习\项目\myproject\image-20210909171057752.png" alt="image-20210909171057752" style="zoom:80%;" /><img src="F:\秋招学习\项目\myproject\image-20210909171019715.png" alt="image-20210909171019715" style="zoom:80%;" />

运行程序，得到自动生成代码，将生成的代码复制到需要的程序中，注意不要忘记复制resources下的mapper.xml



逆向工程的另一种使用：在database中选择mysql进行generate，注意时区问题：jdbc:mysql://localhost:3306/seckill? serverTimezone=GMT



## 二、登录功能

### 1.前端页面

引入静态资源：

<img src="F:\秋招学习\项目\myproject\image-20210909174203942.png" alt="image-20210909174203942" style="zoom:80%;" />

写一个前端页面：login.html

```java
<!DOCTYPE html>
<html lang="en"
           xmlns:th="http://www.thymeleaf.org">
<head>
       <meta charset="UTF-8">
       <title>登录</title>
       <!-- jquery -->
       <script type="text/javascript" th:src="@{/js/jquery.min.js}"></script>
       <!-- bootstrap -->
       <link rel="stylesheet" type="text/css"
             th:href="@{/bootstrap/css/bootstrap.min.css}"/>
       <script type="text/javascript" th:src="@{/bootstrap/js/bootstrap.min.js}">
</script>
       <!-- jquery-validator 做校验的-->
       <script type="text/javascript" th:src="@{/jquery-validation/jquery.validate.min.js}"></script>
       <script type="text/javascript" th:src="@{/jquery-validation/localization/messages_zh.min.js}"></script>
       <!-- layer -->
       <script type="text/javascript" th:src="@{/layer/layer.js}"></script>
       <!-- md5.js -->
       <script type="text/javascript" th:src="@{/js/md5.min.js}"></script>
       <!-- common.js -->
       <script type="text/javascript" th:src="@{/js/common.js}"></script>
</head>
<body>
<form name="loginForm" id="loginForm" method="post" style="width:50%; margin:0 
auto">
       <h2 style="text-align:center; margin-bottom: 20px">用户登录</h2>
       <div class="form-group">
           <div class="row">
               <label class="form-label col-md-4">请输入手机号码</label>
               <div class="col-md-5">
                   <input id="mobile" name="mobile" class="form-control"
                          type="text" placeholder="手机号码" required="true"
                                                minlength="11" maxlength="11"/>
               </div>
               <div class="col-md-1">
               </div>
           </div>
       </div>
       <div class="form-group">
           <div class="row">
               <label class="form-label col-md-4">请输入密码</label>
               <div class="col-md-5">
                   <input id="password" name="password" class="form-control"
                          type="password" placeholder="密码"
                                                required="true" minlength="6" maxlength="16"/>
               </div>
           </div>
       </div>
       <div class="row">
           <div class="col-md-5">
               <button class="btn btn-primary btn-block" type="reset"
                       onclick="reset()">重置</button>
           </div>
           <div class="col-md-5">
               <button class="btn btn-primary btn-block" type="submit"
                       onclick="login()">登录</button>
           </div>
       </div>
</form>
</body>
<script>
    function login() {
        $("#loginForm").validate({
            submitHandler: function (form) {
                doLogin();
            }
        });
    }
    function doLogin() {
        g_showLoading();
        var inputPass = $("#password").val();
        var salt = g_passsword_salt;
        var str = "" + salt.charAt(0) + salt.charAt(2) + inputPass +
            salt.charAt(5) + salt.charAt(4);
        var password = md5(str);
        $.ajax({
            url: "/login/doLogin",
            type: "POST",
            data: {
                mobile: $("#mobile").val(),
                password: password
            },
            success: function (data) {
                layer.closeAll();
                if (data.code == 200) {
                    layer.msg("成功");
                } else {
                    layer.msg(data.message);
                }
            },
            error: function () {
                layer.closeAll();
            }
        });
    }
</script>
</html>
```

点击登录，先做validate校验，校验成功后调用doLogin方法，页面URL请求变成/login/doLogin

### 2.返回结果集

新建一个vo包放返回结果集

新建两个类：RespBean和RespBeanEnum，RespBean主要实现公共返回对象，RespBeanEnum主要实现公共返回对象枚举

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RespBean {

    private long code;
    private String message;
    private Object obj;

    /**
     * 成功返回结果
     * @return
     */
    public static RespBean success(){
        return new RespBean(RespBeanEnum.SUCCESS.getCode(),RespBean.success().getMessage(),null);
    }

    /**
     * 重载方法：成功返回结果
     * @param obj
     * @return
     */
    public static RespBean success(Object obj){
        return new RespBean(RespBeanEnum.SUCCESS.getCode(),RespBeanEnum.SUCCESS.getMessage(),obj);
    }

    /**
     * 失败返回结果
     * @param respBeanEnum
     * @return
     */
    public static RespBean error(RespBeanEnum respBeanEnum){
        return new RespBean(respBeanEnum.getCode(),respBeanEnum.getMessage(),null );
    }

    /**
     * 重载方法：失败返回结果
     * @param respBeanEnum
     * @param obj
     * @return
     */
    public static RespBean error(RespBeanEnum respBeanEnum,Object obj){
        return new RespBean(respBeanEnum.getCode(),respBeanEnum.getMessage(),obj);
    }
}
```

```java
@Getter
@ToString
@AllArgsConstructor
public enum  RespBeanEnum {
    SUCCESS(200,"SUCCESS"),
    ERROR(500,"服务端异常");

    private final Integer code;
    private final String message;
}
```

注意：使用Lombok注解（@Data,@Getter）时需要下载Lombok并启动Lombok

<img src="F:\秋招学习\项目\myproject\image-20210909175130803.png" alt="image-20210909175130803" style="zoom:80%;" />



### 3.登录后端实现

①先测试一下页面跳转是否成功：

```java
@Controller
@RequestMapping("/login")
@Slf4j
public class LoginController {
    @RequestMapping("/toLogin")
    public String toLogin(){
        return "login";
    }
}
```

注意：@RestController和@Controller的区别，@RestController会默认给下面的方法加上ResponseBody，那么返回的就是对象，而不是做页面跳转

页面跳转成功后做实现的逻辑



②由于登录需要mobile（手机号）和password，需要在vo包下创建一个LoginVo类封装登录参数

```java
@Data
public class LoginVo {
    private String mobile;
    private String password;
}
```

在controller层中，由于点击按钮触发页面跳转到/login/doLogin，故

```java
    @RequestMapping("/doLogin")
    @ResponseBody
    public RespBean doLogin(LoginVo loginVo){
        log.info("{}",loginVo);//使用的是Lombok的@Slf4j
        return null;
    }
```

注意地址的映射

结果成功打印



③具体实现：

- 首先增加登录结果枚举：

```java
//    登录
    LOGIN_ERROR(500210,"用户名或密码错误！"),
    MOBILE_ERROR(500211,"手机号码格式不正确")
```

- 增加手机号校验工具

```java
public class ValidatorUtil {
    private static final Pattern mobile_pattern=Pattern.compile("[1]([3-9])[0-9]{9}$");//正则表达式

    public static boolean isMobile(String mobile){
        if(StringUtils.isEmpty(mobile))
            return false;
        Matcher matcher=mobile_pattern.matcher(mobile);
        return matcher.matches();
    }
}
```

- 在数据库中录入需要验证的数据

- 在controller层中修改doLogin方法

  ```java
  @Autowired
  IUserService userService;
  
  @RequestMapping("/doLogin")
  @ResponseBody
  public RespBean doLogin(LoginVo loginVo){
  	log.info("{}",loginVo);//使用的是Lombok的@Slf4j
  	return userService.doLogin(loginVo);//具体逻辑在service层实现
  }
  ```

  注意：不要忘了@Autowired注解

- 实现service的doLogin的接口

  ```java
  @Autowired
  private UserMapper userMapper;
  
  /**
   * 登录功能
   * @param loginVo
   * @return
   */
  @Override
  public RespBean doLogin(LoginVo loginVo) {
      String mobile = loginVo.getMobile();
      String password = loginVo.getPassword();
      //判断用户名或者密码是否为空
      if(StringUtils.isEmpty(mobile)||StringUtils.isEmpty(password)){
          return RespBean.error(RespBeanEnum.LOGIN_ERROR);
      }
      //判断手机号输入是否正确
      if(!ValidatorUtil.isMobile(mobile)){
          return RespBean.error(RespBeanEnum.MOBILE_ERROR);
      }
      //根据手机号获取用户
      User user = userMapper.selectById(mobile);
      if(user==null)
          return RespBean.error(RespBeanEnum.LOGIN_ERROR);
  
      //判断密码是否正确
      if(!MD5Util.formPassToDBPass(password,user.getSalt()).equals(user.getPwd())){
          return RespBean.error(RespBeanEnum.LOGIN_ERROR);
      }
      return RespBean.success();
  }
  ```

  测试手机号格式不正确，密码不正确，手机号和密码均正确时的结果
  
  

### 4.自定义注解参数校验

在上面的学习中，多次进行了参数校验，如电话号码，密码的校验，为简化代码

①首先导入依赖

```xml
<!--        validation组件-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
```

依赖导入后就可以使用注解@Valid，@NotNull，@Length

首先controller层中，对传入的参数添加@Valid注解，对LoginVo中的参数添加@Null，@Length注解

```java
@RequestMapping("/doLogin")
    @ResponseBody
    public RespBean doLogin(@Valid LoginVo loginVo){
        log.info("{}",loginVo);//使用的是Lombok的@Slf4j
        return userService.doLogin(loginVo);//具体逻辑在service层实现
//        return null;
    }
```

```java
@Data
public class LoginVo {
    @NotNull
    private String mobile;

    @NotNull
    @Length(min = 32)
    private String password;
}
```

在validation依赖包下可以查看更多的注解



②对于手机号的校验，单独自定义注解

可以参照例如@NotNull注解，首先创建一个注解类（@Annotation）

```java
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(
        validatedBy = {IsMobileValidator.class}
)
public @interface IsMobile {

    boolean required() default true;//手机号是否是必填项

    String message() default "手机号码格式错误";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
```

规则

```java
public class IsMobileValidator implements ConstraintValidator<IsMobile,String> {

    //获取手机号是否为必填项
    private boolean required=false;

    @Override
    public void initialize(IsMobile constraintAnnotation) {
        required=constraintAnnotation.required();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        //手机号是否是必填项
        if (required) {
            if (StringUtils.isEmpty(value)) {
                return false;//空值无效
            } else
                return ValidatorUtil.isMobile(value);//判断手机号是否正确
        } 
        //手机号不是必填项则直接返回true，表示输入的数据有效
        else {
            return true;
        }
    }
}
```

此时可以在LoginVo中的mobile参数中添加注解

```java
@IsMobile(required = true,message = "不存在该号码")
private String mobile;
```



### 5.异常处理

> 异常包括：编译时异常和运行异常
>
> 编译时异常：捕获异常，获取异常信息
>
> 运行时异常：编码规范，测试等减少异常
>
> 
>
> springboot全局异常处理方式：1.使用RestControllerAdvice注解和ExceptionHandler注解：只能处理控制														器抛出的异常，因为请求已经进入控制器中了
>
> ​														2.使用error controller类实现：可以处理所有的异常，包括没有进入控制器														中的异常，可以定义多个拦截方法拦截不同异常，抛出异常信息

在参数校验中，运行过程中控制台报出错误信息如下：

Resolved[**org.springframework.validation**.**BindException**:org.springframework.validation.BeanPropertyBindingResult: **1 errors**

具体信息：

Field error in object 'loginVo' on field 'mobile': rejected value [11111111111]; 

codes [IsMobile.loginVo.mobile,IsMobile.mobile,IsMobile.java.lang.String,IsMobile]; 

arguments [org.springframework.context.support.DefaultMessageSourceResolvable: 

codes [loginVo.mobile,mobile]; arguments []; default message [mobile],true]; 

default message [不存在该号码]]

可以看出：异常为绑定异常：BindException，是org.springframework.validation的异常，错误信息为1个



新建一个异常处理包：exception，然后创建一个运行时异常类

```java
@Data
@NoArgsConstructor      //空参构造
@AllArgsConstructor     //全参构造
public class GlobalException extends RuntimeException{
    private RespBeanEnum respBeanEnum;
}
```

```java
@RestControllerAdvice	//统一异常处理
public class GlobalExceptionHandler {
    
    @ExceptionHandler(Exception.class)
    public RespBean ExceptionHandler(Exception e){
        if(e instanceof GlobalException){
            GlobalException ex=(GlobalException)e;
            return RespBean.error(ex.getRespBeanEnum());
        }
        //在登录时产生的参数校验异常是BindException异常
        else if(e instanceof BindException){
            BindException ex=(BindException)e;
            RespBean respBean = RespBean.error(RespBeanEnum.BIN_ERROR);
            //具体异常信息从控制台中得知
            respBean.setMessage("参数校验异常"+ex.getBindingResult().getAllErrors().get(0).getDefaultMessage());
            return respBean;
        }
        return RespBean.error(RespBeanEnum.ERROR);
    }
    
}
```

需要注意的是包的导入，import org.springframework.validation.BindException，因为异常是在org.springframework.validation里产生的，不要错导成：import java.net.BindException;



此时，可以在service层中适当部分添加异常抛出

```java
       //根据手机号获取用户
        User user = userMapper.selectById(mobile);
        if(user==null){
//            return RespBean.error(RespBeanEnum.LOGIN_ERROR);
            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
        }
        //判断密码是否正确
        if(!MD5Util.formPassToDBPass(password,user.getSalt()).equals(user.getPwd())){
//            return RespBean.error(RespBeanEnum.LOGIN_ERROR);
            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
        }
```



### 6.登录功能完善

判断是否登录成功，使用分布式session

首先导入cookie工具类：

```java
package com.csl.seckill.utils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * @Author:CaiShuangLian
 * @FileName:
 * @Date:Created in  2021/9/11 11:18
 * @Version:
 * @Description:Cookie工具类
 */

public final class CookieUtil {
    /**
     * 得到Cookie的值, 不编码
     *
     * @param request
     * @param cookieName
     * @return
     */
    public static String getCookieValue(HttpServletRequest request, String cookieName) {
        return getCookieValue(request, cookieName, false);
    }

    /**
     * 得到Cookie的值,
     *
     * @param request
     * @param cookieName
     * @return
     */
    public static String getCookieValue(HttpServletRequest request, String cookieName, boolean isDecoder) {
        Cookie[] cookieList = request.getCookies();
        if (cookieList == null || cookieName == null) {
            return null;
        }
        String retValue = null;
        try {
            for (int i = 0; i < cookieList.length; i++) {
                if (cookieList[i].getName().equals(cookieName)) {
                    if (isDecoder) {
                        retValue = URLDecoder.decode(cookieList[i].getValue(),
                                "UTF-8");
                    } else {
                        retValue = cookieList[i].getValue();
                    }
                    break;
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return retValue;
    }

    /**
     * 得到Cookie的值,
     *
     * @param request
     * @param cookieName
     * @return
     */
    public static String getCookieValue(HttpServletRequest request, String cookieName, String encodeString) {
        Cookie[] cookieList = request.getCookies();
        if (cookieList == null || cookieName == null) {
            return null;
        }
        String retValue = null;
        try {
            for (int i = 0; i < cookieList.length; i++) {
                if (cookieList[i].getName().equals(cookieName)) {
                    retValue = URLDecoder.decode(cookieList[i].getValue(),
                            encodeString);
                    break;
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return retValue;
    }

    /**
     * 设置Cookie的值 不设置生效时间默认浏览器关闭即失效,也不编码
     */
    public static void setCookie(HttpServletRequest request, HttpServletResponse response,
                                 String cookieName, String cookieValue) {
            setCookie(request, response, cookieName, cookieValue, -1);
    }

    /**
     * 设置Cookie的值 在指定时间内生效,但不编码
     */
    public static void setCookie(HttpServletRequest request, HttpServletResponse response,
                                 String cookieName, String cookieValue, int cookieMaxage) {
            setCookie(request, response, cookieName, cookieValue, cookieMaxage, false);
    }

    /**
     * 设置Cookie的值 不设置生效时间,但编码
     */
    public static void setCookie(HttpServletRequest request, HttpServletResponse response,
                                 String cookieName, String cookieValue, boolean isEncode) {
            setCookie(request, response, cookieName, cookieValue, -1, isEncode);

    }

    /**
     * 设置Cookie的值 在指定时间内生效, 编码参数
     */
    public static void setCookie(HttpServletRequest request, HttpServletResponse response,
                                 String cookieName, String cookieValue, int cookieMaxage, boolean isEncode) {
            doSetCookie(request, response, cookieName, cookieValue, cookieMaxage, isEncode);
    }

    /**
     * 设置Cookie的值 在指定时间内生效, 编码参数(指定编码)
     */
    public static void setCookie(HttpServletRequest request, HttpServletResponse response, String cookieName,
                                 String cookieValue, int cookieMaxage, String encodeString) {
            doSetCookie(request, response, cookieName, cookieValue, cookieMaxage, encodeString);
    }

    /**
     * 删除Cookie带cookie域名
     */
    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String cookieName) {
            doSetCookie(request, response, cookieName, "", -1, false);
    }

    /**
     * 设置Cookie的值，并使其在指定时间内生效
     *
     * @param cookieMaxage cookie生效的最大秒数
     */
    private static final void doSetCookie(HttpServletRequest request,
                                          HttpServletResponse response,
                                          String cookieName, String cookieValue,
                                          int cookieMaxage, boolean isEncode) {
        try {
            if (cookieValue == null) {
                cookieValue = "";
            } else if (isEncode) {
                cookieValue = URLEncoder.encode(cookieValue, "utf-8");
            }
            Cookie cookie = new Cookie(cookieName, cookieValue);
            if (cookieMaxage > 0)
                cookie.setMaxAge(cookieMaxage);
            if (null != request) {// 设置域名的cookie
                String domainName = getDomainName(request);
                System.out.println(domainName);
                if (!"localhost".equals(domainName)) {
                    cookie.setDomain(domainName);
                }
            }
            cookie.setPath("/");
            response.addCookie(cookie);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置Cookie的值，并使其在指定时间内生效
     *
     * @param cookieMaxage cookie生效的最大秒数
     */
    private static final void doSetCookie(HttpServletRequest request,
                                          HttpServletResponse response,
                                          String cookieName, String cookieValue,
                                          int cookieMaxage, String encodeString) {
        try {
            if (cookieValue == null) {
                cookieValue = "";
            } else {
                cookieValue = URLEncoder.encode(cookieValue, encodeString);
            }
            Cookie cookie = new Cookie(cookieName, cookieValue);
            if (cookieMaxage > 0) {
                cookie.setMaxAge(cookieMaxage);
            }
            if (null != request) {// 设置域名的cookie
                String domainName = getDomainName(request);
                System.out.println(domainName);
                if (!"localhost".equals(domainName)) {
                    cookie.setDomain(domainName);
                }
            }
            cookie.setPath("/");
            response.addCookie(cookie);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 得到cookie的域名
     */
    private static final String getDomainName(HttpServletRequest request) {
        String domainName = null;
        // 通过request对象获取访问的url地址
        String serverName = request.getRequestURL().toString();
        if (serverName == null || serverName.equals("")) {
            domainName = "";
        } else {
            // 将url地下转换为小写
            serverName = serverName.toLowerCase();
            // 如果url地址是以http://开头 将http://截取
            if (serverName.startsWith("http://")) {
                serverName = serverName.substring(7);
            }
            int end = serverName.length();
            // 判断url地址是否包含"/"
            if (serverName.contains("/")) {
                //得到第一个"/"出现的位置
                end = serverName.indexOf("/");
            }
            // 截取
            serverName = serverName.substring(0, end);
            // 根据"."进行分割
            final String[] domains = serverName.split("\\.");
            int len = domains.length;
            if (len > 3) {
                // www.xxx.com.cn
                domainName = domains[len - 3] + "." + domains[len - 2] + "." +
                        domains[len - 1];
            } else if (len <= 3 && len > 1) {
                // xxx.com or xxx.cn
                domainName = domains[len - 2] + "." + domains[len - 1];
            } else {
                domainName = serverName;
            }
        }
        if (domainName != null && domainName.indexOf(":") > 0) {
            String[] ary = domainName.split("\\:");
            domainName = ary[0];
        }
        return domainName;
    }
}
```

导入UUID工具类：

```java
package com.csl.seckill.utils;

import java.util.UUID;

/**
 * @Author:CaiShuangLian
 * @FileName:
 * @Date:Created in  2021/9/11 11:31
 * @Version:
 * @Description:TODO
 */

public class UUIDUtil {
     public static String uuid() {
            return UUID.randomUUID().toString().replace("-", "");
     }
}
```

现在可以通过session进行页面跳转

在service层中，当手机号与密码匹配正确后，

```java
//生成cookie
String ticket= UUIDUtil.uuid();
//从浏览器中获取session
request.getSession().setAttribute(ticket,user);
//将session存储在cookie中
CookieUtil.setCookie(request,response,"userTicket",ticket);
return RespBean.success();
```

注意：因为是从浏览器中获取session值，所以在controller传值时，需要增加HttpServletRequest和HttpServletResponse

```java 
 	@RequestMapping("/doLogin")
    @ResponseBody
    public RespBean doLogin(@Valid LoginVo loginVo, HttpServletRequest request, HttpServletResponse response){
        log.info("{}",loginVo);//使用的是Lombok的@Slf4j
        return userService.doLogin(loginVo,request,response);//具体逻辑在service层实现
//        return null;
    }
```

先准备一个商品列表页goodsList.html做测试：

```html
<!DOCTYPE html>
<html lang="en"
    xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>商品列表</title>
</head>
<body>
<p th:text="'Hello:'+${user.nickname}"></p>
</body>
</html>
```

登录成功，跳转到商品列表页，增加跳转页面代码：

```JavaScript
success: function (data) {
    layer.closeAll();
    if (data.code == 200) {
        layer.msg("成功");
        window.location.href='/goods/toList';
    } else {
        layer.msg(data.message);
    }
},
```

此时请求地址是：/goods/toList

故有GoodsController

```java
@Controller
@RequestMapping("/goods")
public class GoodsController {

    /**
     * 跳转到商品列表页
     * @param session
     * @param model
     * @param ticket
     * @return
     */
    @RequestMapping("/toList")
    public String toList(HttpSession session, Model model, @CookieValue("userTicket") String ticket){
        //浏览器中session为空，则返回登录页面
        if(StringUtils.isEmpty(ticket)){
            return "login";
        }
        User user=(User)session.getAttribute(ticket);
        
        if(null==user){
            return "login";
        }
        //用model在前后端进行传值
        model.addAttribute("user",user);
        return "goodsList";
    }
}
```

## 三、分布式session

> 在前的代码是将将所有的操作部署在一台Tomcat中，如果部署多台系统，配合Nginx的时候会出现用户登录的问题。
>
> 
>
> 原因：由于Nginx使用默认 负载均衡策略（轮询），请求将会按照时间顺序逐一分发到后端应用上。
>
> 也就是说，我们刚开始在Tomcat1登录后，用户信息会放在Tomcat1的session中。过了一会儿，请求又被Nginx分发到Tomcat2上，这时Tomcat2上session里还没有用户信息，于是又要登录
>
> 
>
> 解决方案：
>
> 1.session复制：
>
> ​	优点：无需修改代码，知足要修改Tomcat配置
>
> ​	缺点：session同步传输占用内网带宽
>
> ​				多台Tomcat同步性能指数级下降
>
> ​				session占用内存，无法有效水平扩展
>
> 2.前端存储：
>
> ​	优点：不占用服务器内存
>
> ​	缺点：存在安全风险
>
> ​				数据大小受cookie限制
>
> ​				占用外网带宽
>
> 3.session粘滞：
>
> ​	优点：无需修改代码
>
> ​				服务端可以水平拓展
>
> ​	缺点：增加新机器，会重新Hash，导致重新登陆
>
> ​				应用重启，需要重新登陆
>
> 4.后端集中存储：
>
> ​	优点：安全
>
> ​				容易水平扩展
>
> ​	缺点：增加复杂度
>
> ​				需要修改代码

Redis：通常用来做缓存

可以用来做数据库，缓存，消息中间件，比关系型数据库更快，支持string，hashes，lists，sets，sorted sets等数据类型



总结：



## 四、Redis

### 1.在远程服务器上部署Redis

- 先将redis-5.0.5.tar.gz包上传到root目录下：

![image-20210912173327895](F:\秋招学习\项目\myproject\seckill-demo\image-20210912173327895.png)

- 解压：tar zxvf  redis-5.0.5.tar.gz
- 进入到解压的文件夹里：cd redis-5.0.5
- 显示目录内文件：ll
- 由于Redis是由C语言编写的，需要在安装之前安装依赖，直接安装会报错，安装前需要编译
- 编译指令：make，发现直接make报错
- 安装依赖：yum -y install gcc-c++ automake autoconf
- 可能会报错：![image-20210912174649994](F:\秋招学习\项目\myproject\seckill-demo\image-20210912174649994.png)

- 编译完成，安装到指定目录：make PREFIX=/user/local/redis install

- 安装成功，进入到安装目录下：

  先到根目录cd ~，再cd /user/local/redis，查看目录下文件ll，只有一个bin文件夹，查看bin文件夹

- bin文件下的redis-server即为启动redis，输入命令./redis-server，启动redis

  ![image-20210912175615502](F:\秋招学习\项目\myproject\seckill-demo\image-20210912175615502.png)

  可以看出端口号为6379，但是这是前台启动

- 改成后台启动:

  - 先Ctrl+C先退出，后台启动要修改对应的配置文件，配置文件在解压的redis目录里，先进入redis-5.0.5目录
  - 然后将redis.conf拷贝到/uer/local/redis/bin/目录下，拷贝的目的是避免配置文件修改错误而失去的原件，拷贝指令：cp redis.conf /user/local/redis/bin/
  - 回到bin目录下，修改redis.conf文件，修改指令：vim redis.conf
  - 注释bind，表示取消绑定；保护模式改成no：protected-mode no；后台启动改成yes：daemonize yes，其余的暂时不需要改动
  - esc后输入:wq保存并退出

- 配置文件修改完后，如果直接./redis-server依旧是前台启动，因为它会加载默认配置，此时需要加载我们修改的配置，因此命令为：./redis-server redis.conf   （注意此时是在需要的配置的目录下，也就是bin目录下）

- 查看是否启动成功：

  - ps -ef|grep redis
  - 或者：./redis-cli，然后输入ping，正常输出PONG，即为启动成功

- 一些命令：

  - select 3：在第三号库去执行，库的索引从0开始

- 可视化工具测试连接：

  ![image-20210912182637250](F:\秋招学习\项目\myproject\seckill-demo\image-20210912182637250.png)

  连接成功

### 2.操作基本数据类型

1）操作string

![image-20210912183134232](F:\秋招学习\项目\myproject\seckill-demo\image-20210912183134232.png)

设置值和取值

![image-20210912183252520](F:\秋招学习\项目\myproject\seckill-demo\image-20210912183252520.png)

设置多个值

2）hash

![image-20210912183727754](F:\秋招学习\项目\myproject\seckill-demo\image-20210912183727754.png)

![image-20210912184037627](F:\秋招学习\项目\myproject\seckill-demo\image-20210912184037627.png)

![image-20210912184340907](F:\秋招学习\项目\myproject\seckill-demo\image-20210912184340907.png)

在string类型，只能使用del进行删除，del可以删除所有类型，是Redis的通用删除

3）list

list加入数据分为左加和右加

![image-20210912184737783](F:\秋招学习\项目\myproject\seckill-demo\image-20210912184737783.png)

![image-20210912185021284](F:\秋招学习\项目\myproject\seckill-demo\image-20210912185021284.png)

4）set

无序（内部排序）不可重复

![image-20210912185348345](F:\秋招学习\项目\myproject\seckill-demo\image-20210912185348345.png)

5）sorted set

有序不可重复

![image-20210912185915875](F:\秋招学习\项目\myproject\seckill-demo\image-20210912185915875.png)

操作的所有数据都可以在可视化工具中查看



6）设置失效时间

![image-20210912190819624](F:\秋招学习\项目\myproject\seckill-demo\image-20210912190819624.png)

ttl表示剩余秒数，pttl表示剩余毫秒数

![image-20210912191038442](F:\秋招学习\项目\myproject\seckill-demo\image-20210912191038442.png)

pexpire表示设置剩余毫秒数

![image-20210912191346725](F:\秋招学习\项目\myproject\seckill-demo\image-20210912191346725.png)

xx：表示已设置过设置成功

nx：表示未设置则设置成功

xx和nx可以用于用作锁

### 3.遇到的问题

在首次连接redis后，redis一旦断开，第二次连接就会错误

根据查到的信息：

可能情况有：

1.端口号6379为开放

2.配置文件bind127.0.0.1未注释

3.没有设置密码，即配置文件中的requiredpass 被注释了

最后是问题1的原因

开放端口号：

①首先启动防火墙：systemctl start firewalld  

如果centOS没有安装firewalld，则使用yum install firewalle firewalled-config进行安装

② sudo firewall-cmd --add-port=6379/tcp --permanent 

③sudo firewall-cmd --reload 

④重启一下redis即可

## 五、分布式session实现

### 1.通过spring session实现

首先添加依赖：

通过Redis实现分布式session

```xml
<!--        spring data redis 依赖-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
<!--        commons-pool2对象池依赖-->
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-pool2</artifactId>
        </dependency>
<!--        spring session依赖-->
        <dependency>
            <groupId>org.springframework.session</groupId>
            <artifactId>spring-session-data-redis</artifactId>
        </dependency>
```

由于引入了spring data redis依赖，需要添加相关配置：

```yml
redis:
  #服务器地址
  host: 47.100.109.45
  #端口号
  port: 6379
  #数据库
  database: 0
  #超时时间
  timeout: 10000ms
  lettuce:
    pool:
      #最大连接数，默认8
      max-active: 8
      #最大连接阻塞等待时间，默认-1
      max-wait: 10000ms
      #最大空闲连接，默认8
      max-idle: 200
      #最小空闲连接，默认0
      min-idle: 0
```

至此，分布式session完成

输入网址，成功登录后，查看session，发现session已经存储在redis上了：

![image-20210913103932222](F:\秋招学习\项目\myproject\seckill-demo\image-20210913103932222.png)

发现值都是二进制数，需要进行序列化操作

### 2.将信息存储到Redis

先将之前的session删除，再注释掉springsession依赖，不需要修改配置文件

将用户信息提取出来，然后存储到Redis，之前是将用户信息存到session，然后从不同的redis提取信息。



首先将redis序列化

```java
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String,Object>redisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        //key序列化
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        //value序列化
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        //hash类型 key序列化
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        //hash类型 value序列化
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        //注入连接工厂
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

}
```

```
//GenericJackson2JsonRedisSerializer比较通用的
//jdk产生的是二进制
//Jackson2JsonRedisSerializer产生的是java字符串，需要传对象
```

然后修改登录功能代码，之前是将session存到cookie中，现在如下：

```java
 //生成cookie
        String ticket= UUIDUtil.uuid();
        //将用户信息存入redis中
        redisTemplate.opsForValue().set("user:"+ticket,user);
        //获取浏览器的session
//        request.getSession().setAttribute(ticket,user);
        //将session存储在cookie中
        CookieUtil.setCookie(request,response,"userTicket",ticket);
        return RespBean.success();
```

用户信息存到redis后，取出信息就不能使用之前的session了，修改代码如下：

故取出用户信息为：

```java
/**
 * 根据cookie获取用户
 * @param userTicket
 * @param httpServletRequest
 * @param httpServletResponse
 * @return
 */
@Override
public User getUserByCookie(String userTicket, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    if(StringUtils.isEmpty(userTicket))
        return null;
    User user = (User) redisTemplate.opsForValue().get("user:" + userTicket);
    if(user!=null){
        //重新设置cookie，处于安全考量
        CookieUtil.setCookie(httpServletRequest,httpServletResponse,"userTicket",userTicket);
    }
    return user;
}
```

对应的controller层有：

```java
//        User user=(User)session.getAttribute(ticket);
        User user =userService.getUserByCookie(ticket,request,response);
```

## 六、优化登录功能

每一个接口都需要根据ticket判断用户是否存在，对此问题进行优化。

### 1.自定义用户参数

```java
@Component
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

    @Autowired
    private IUserService userService;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        //判断类型是否是User
        Class<?> clazz=parameter.getParameterType();
        return clazz== User.class;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request=webRequest.getNativeRequest(HttpServletRequest.class);
        HttpServletResponse response=webRequest.getNativeResponse(HttpServletResponse.class);
        String ticket= CookieUtil.getCookieValue(request,"userTicket");
        if(StringUtils.isEmpty(ticket)){
            return null;
        }
        return userService.getUserByCookie(ticket,request,response);
    }
}
```

注意使用的是组件

### 2.MVC配置类

```java
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private UserArgumentResolver userArgumentResolver;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {

        resolvers.add(userArgumentResolver);
    }
}
```

### 3.修改controller层代码

```java
@RequestMapping("/toList")
public String toList(Model model, User user){
    model.addAttribute("user",user);
    return "goodsList";
}
```

## 七、秒杀商品

### 1.创建表

先在数据库中创建表

创建商品表：

```mysql
DROP TABLE IF EXISTS `t_goods`;
CREATE TABLE `t_goods` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `goods_name` varchar(16) CHARACTER SET utf8 DEFAULT NULL COMMENT '商品名称',
  `goods_title` varchar(64) CHARACTER SET utf8 DEFAULT NULL COMMENT '商品标题',
  `goods_img` varchar(64) CHARACTER SET utf8 DEFAULT NULL,
  `goods_detail` longtext CHARACTER SET utf8 COMMENT '商品详情',
  `goods_price` decimal(10,2) DEFAULT '0.00' COMMENT '商品价格',
  `goods_stock` int(11) DEFAULT '0' COMMENT '商品库存，-1表示没有限制',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

创建订单表：

```mysql
DROP TABLE IF EXISTS `t_order`;
CREATE TABLE `t_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户id',
  `goods_id` bigint(20) DEFAULT NULL COMMENT '商品id',
  `delivery_addr_id` bigint(20) DEFAULT NULL COMMENT '收获地址id',
  `goods_name` VARCHAR(16) DEFAULT NULL COMMENT '冗余过来的商品名称',
  `goods_count` INT(11) DEFAULT '0' COMMENT '商品数量',
  `goods_price` DECIMAL(10,2) DEFAULT '0.00' COMMENT '商品单价',
	`order_channel` TINYINT(4) DEFAULT '0' COMMENT '1pc,2android,3ios',
	`status` TINYINT(4) DEFAULT '0' COMMENT '订单状态，0新建未支付，1已支付，2已发货，3已收货，4已退款，5已完成',
	`create_date` datetime DEFAULT null COMMENT '订单创建时间',
	`pay_date` datetime DEFAULT null COMMENT '支付时间',	
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

创建秒杀商品：

```mysql
DROP TABLE IF EXISTS `t_seckill_goods`;
CREATE TABLE `t_seckill_goods` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '秒杀商品ID',
  `goods_id` bigint(20) DEFAULT NULL COMMENT '商品id',
  `seckill_price` DECIMAL(10,2) DEFAULT '0.00' COMMENT '秒杀价',
  `goods_count` INT(10) DEFAULT '0' COMMENT '库存数量',
	`start_date` datetime DEFAULT null COMMENT '秒杀开始时间',
	`end_date` datetime DEFAULT null COMMENT '秒杀结束时间',	
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

创建秒杀订单表：

```mysql
DROP TABLE IF EXISTS `t_seckill_order`;
CREATE TABLE `t_seckill_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '秒杀订单id',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID',
  `goods_id` bigint(20) DEFAULT NULL COMMENT '商品id',
  `order_id` bigint(20) DEFAULT NULL  COMMENT '订单id',	
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

通过逆向工程生成相关代码，逆向工程见上

逆向工程技术开始后端代码，注意修改类名为驼峰命名法

### 2.商品列表前端代码

```HTML
<!DOCTYPE html>
<html lang="en"
       xmlns:th="http://www.thymeleaf.org">
<head>
       <meta charset="UTF-8">
       <title>商品列表</title>
       <!-- jquery -->
       <script type="text/javascript" th:src="@{/js/jquery.min.js}"></script>
       <!-- bootstrap -->
       <link rel="stylesheet" type="text/css"
             th:href="@{/bootstrap/css/bootstrap.min.css}"/>
       <script type="text/javascript" th:src="@{/bootstrap/js/bootstrap.min.js}">
</script>
       <!-- layer -->
       <script type="text/javascript" th:src="@{/layer/layer.js}"></script>
       <!-- common.js -->
       <script type="text/javascript" th:src="@{/js/common.js}"></script>
</head>
<body>
<div class="panel panel-default">
       <div class="panel-heading">秒杀商品列表</div>
       <table class="table" id="goodslist">
           <tr>
               <td>商品名称</td>
               <td>商品图片</td>
               <td>商品原价</td>
               <td>秒杀价</td>
               <td>库存数量</td>
               <td>详情</td>
           </tr>
           
    <tr th:each="goods,goodsStat : ${goodList}">
               <td th:text="${goods.goodsName}"></td>
               <td><img th:src="@{${goods.goodsImg}}" width="100" height="100"/></td>
               <td th:text="${goods.goodsPrice}"></td>
               <td th:text="${goods.seckillPrice}"></td>
               <td th:text="${goods.goodsStock}"></td>
               <td><a th:href="'/goods/toDetail/'+${goods.id}">详情</a></td>
           </tr>
       </table>
</div>
</body>
</html>
```

### 3.商品列表后端代码

首先，商品的公共信息封装成商品返回对象，商品前端可见信息在多处使用

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoodsVo extends Goods {

    private BigDecimal seckillPrice;
    private Integer goodsCount;
    private Date startDate;
    private Date endDate;
}
```

此处由于和商品信息实体类有许多相同之处，故采用继承，需要什么信息补充即可

在contr层中，实现跳转页面：

```java
    @Autowired
    private IGoodsService goodsService;

/**
 * 跳转到商品列表
 * @param model
 * @param user
 * @return
 */
@RequestMapping("/toList")
public String toList(Model model, User user){
    model.addAttribute("user",user);
    model.addAttribute("goodList",goodsService.findGoodsVo());
    return "goodsList";
}
```

对应的service层有：

```java
@Autowired
private GoodsMapper goodsMapper;
/**
 * 获取商品列表
 * @return
 */
@Override
public List<GoodsVo> findGoodsVo() {
    return goodsMapper.findGoodsVo();
}
```

对应的mapper有：

```java
 * 获取商品列表
 * @return
 */
List<GoodsVo> findGoodsVo();
```

故在xml中写对应的查询语句：

```xml
<!--    获取商品列表-->
    <select id="findGoodsVo" resultType="com.csl.seckill.vo.GoodsVo">
        select
            g.id,
            g.goods_name,
            g.goods_title,
            g.goods_img,
            g.goods_detail,
            g.goods_price,
            g.goods_stock,
            sg.seckill_price,
            sg.goods_count,
            sg.start_date,
            sg.end_date
        from t_goods g
            left join t_seckill_goods as sg on g.id=sg.goods_id
    </select>
```

可以先在Navicat中进行查询，避免查询语句错误

> 约定大于配置，配置类大于配置文件

在配置类中扫描不到static目录，故静态资源获取失败，控制台显示404，故在WebConfig配置类中：

```java
@Override
public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
}
```

### 4.商品详情前端代码

goodsDetail.html

```HTML
<!DOCTYPE html>
<html lang="en"
      xmlns:th="http://www.thymeleaf.org">
<head>
       <meta charset="UTF-8">
       <title>商品详情</title>
       <!-- jquery -->
       <script type="text/javascript" th:src="@{/js/jquery.min.js}"></script>
       <!-- bootstrap -->
       <link rel="stylesheet" type="text/css" th:href="@{/bootstrap/css/bootstrap.min.css}"/>
       <script type="text/javascript" th:src="@{/bootstrap/js/bootstrap.min.js}"></script>
       <!-- layer -->
       <script type="text/javascript" th:src="@{/layer/layer.js}"></script>
       <!-- common.js -->
       <script type="text/javascript" th:src="@{/js/common.js}"></script>
</head>
<body>
<div class="panel panel-default">
       <div class="panel-heading">秒杀商品详情</div>
       <div class="panel-body">
           <span th:if="${user eq null}"> 您还没有登录，请登陆后再操作<br/></span>
           <span>没有收货地址的提示。。。</span>
       </div>
       <table class="table" id="goods">
           <tr>
               <td>商品名称</td>
               <td colspan="3" th:text="${goods.goodsName}"></td>
           </tr>
           <tr>
               <td>商品图片</td>
               <td colspan="3"><img th:src="@{${goods.goodsImg}}" width="200" height="200"/></td>
           </tr>
           <tr>
               <td>秒杀开始时间</td>
               
           </tr>
           <tr>
               <td>商品原价</td>
               <td colspan="3" th:text="${goods.goodsPrice}"></td>
           </tr>
           <tr>
               <td>秒杀价</td>
               <td colspan="3" th:text="${goods.seckillPrice}"></td>
           </tr>
           <tr>
               <td>库存数量</td>
               <td colspan="3" th:text="${goods.goodsCount}"></td>
           </tr>
       </table>
</div>
</body>
<script>
    
</script>
</html>
```

### 5.商品详情后端代码

从前端得到商品的id，根据id查找商品信息

controller层中：

```java
@RequestMapping("/toDetail/{goodsId}")
public String toDetail(Model model,User user, @PathVariable Long goodsId){
    model.addAttribute("user",user);
    model.addAttribute("goods",goodsService.findGoodsVoByGoodsId(goodsId));
    return "goodsDetail";
}
```

service层：

```java
@Override
public GoodsVo findGoodsVoByGoodsId(Long goodsId) {
    return goodsMapper.findGoodsVoByGoodsId(goodsId);
}
```

对应的SQL语句为

```xml
<!--    获取商品详情-->
    <select id="findGoodsVoByGoodsId" resultType="com.csl.seckill.vo.GoodsVo">
        select
            g.id,
            g.goods_name,
            g.goods_title,
            g.goods_img,
            g.goods_detail,
            g.goods_price,
            g.goods_stock,
            sg.seckill_price,
            sg.goods_count,
            sg.start_date,
            sg.end_date
        from t_goods g
            left join t_seckill_goods as sg on g.id=sg.goods_id
        where
            g.id=#{goodsId}
    </select>
```

### 6.秒杀功能详解

#### 6.1秒杀倒计时处理

首先从数据库中获取开始时间：

```HTML
<tr>
	<td>秒杀开始时间</td>
	<td th:text="${#dates.format(goods.startDate,'yyyy-MM-dd HH:mm:ss')}"></td>
</tr>
```

此处使用的是comm.js的格式化时间方法，经过测试，显示正确

![image-20210916214110409](F:\秋招学习\项目\myproject\seckill-demo\image-20210916214110409.png)

在倒计时业务中，首先需要获得开始，结束和现在的时间，判断是否开始秒杀了。

在后端代码有：

```java
@RequestMapping("/toDetail/{goodsId}")
    public String toDetail(Model model,User user, @PathVariable Long goodsId){
        model.addAttribute("user",user);
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
        Date startDate=goodsVo.getStartDate();
        Date endDate=goodsVo.getEndDate();
        Date nowDate=new Date();
        //秒杀状态
        int seckillStatus=0;
        int remainSeconds=0;
//        判断状态
        if(nowDate.before(startDate)){
            //秒杀倒计时
            remainSeconds= (int) ((startDate.getTime()-nowDate.getTime())/1000);
        }else if(nowDate.after(endDate)){
            //秒杀已结束
            seckillStatus=2;
            remainSeconds=-1;
        }else {
            //秒杀进行中
            seckillStatus=1;
            remainSeconds=0;
        }
        model.addAttribute("remainSeconds",remainSeconds);
        model.addAttribute("seckillStatus",seckillStatus);
        model.addAttribute("goods",goodsVo);
        return "goodsDetail";
    }
```

将剩余时间和秒杀状态传到前台

在前端中

```html
<tr>
	<td>秒杀开始时间</td>
	<td th:text="${#dates.format(goods.startDate,'yyyy-MM-dd HH:mm:ss')}"></td>
	<!--倒计时-->
	<td id="seckillTip">
		<input type="hidden" id="remainSeconds" th:value="${remainSeconds}">
		<span th:if="${seckillStatus eq 0}">秒杀倒计时：
			<span id="countDown" th:text="${remainSeconds}"></span>秒</span>
		<span th:if="${seckillStatus eq 1}">秒杀进行中</span>
		<span th:if="${seckillStatus eq 2}">秒杀已结束</span>
	</td>
</tr>
```

对应的JavaScript有：

```JavaScript
$(function () {

    countDown();
});

function countDown() {
    var remainSeconds=$("#remainSeconds").val();
    var timeout;
    //秒杀未开始
    if(remainSeconds>0){
        timeout=setTimeout(function () {
            $("#countDown").text(remainSeconds-1);
            $("#remainSeconds").val(remainSeconds-1);
            countDown();
        },1000);
    }
    //秒杀进行中
    else if(remainSeconds==0){
        if(timeout){
            clearTimeout(timeout);
        }
        $("#seckillTip").html("秒杀进行中")
    }else {
        $("#seckillTip").html("秒杀结束")
    }

};
```

#### 6.2秒杀按钮处理

在秒杀前和秒杀结束后，按钮置灰，不可使用

在秒杀开始部分，设置按钮：

```html
 <td>秒杀开始时间</td>
           <td th:text="${#dates.format(goods.startDate,'yyyy-MM-dd HH:mm:ss')}"></td>
            <!--倒计时-->
           <td id="seckillTip">
               <input type="hidden" id="remainSeconds" th:value="${remainSeconds}">
               <span th:if="${seckillStatus eq 0}">秒杀倒计时：
                   <span id="countDown" th:text="${remainSeconds}"></span>秒</span>
               <span th:if="${seckillStatus eq 1}">秒杀进行中</span>
               <span th:if="${seckillStatus eq 2}">秒杀已结束</span>
           </td>
            <td>
                <form id="seckillForm" action="/seckill/doSeckill">
                    <input type="hidden" name="goodsId" th:value="${goodsId}">
                    <button class="btn btn-primary btn-block" type="submit" id="buyButton">立即秒杀</button>
                </form>
            </td>
```

对应的JavaScript有：

```JavaScript
function countDown() {
    var remainSeconds=$("#remainSeconds").val();
    var timeout;
    //秒杀未开始
    if(remainSeconds>0){
        $("#buyButton").attr("disabled",true);
        timeout=setTimeout(function () {
            $("#countDown").text(remainSeconds-1);
            $("#remainSeconds").val(remainSeconds-1);
            countDown();
        },1000);
    }
    //秒杀进行中
    else if(remainSeconds==0){
        $("#buyButton").attr("disabled",false);
        if(timeout){
            clearTimeout(timeout);
        }
        $("#seckillTip").html("秒杀进行中")
    }else {
        $("#buyButton").attr("disabled",true);
        $("#seckillTip").html("秒杀结束")
    }

};
```

#### 6.3秒杀功能实现

功能实现：1.有库存是才能秒杀

​					2.一个用户只能秒杀一件商品

后端实现：

```java
@Controller
@RequestMapping("/seckill")
public class SeckillController {

    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private ISeckillOrderService seckillOrderService;
    @Autowired
    private IOrderService orderService;

    @RequestMapping("/doSeckill")
    public String doSeckill(Model model, User user, Long goodsId){

        if(user==null)
        {
            return "login";
        }
        model.addAttribute("user",user);
        GoodsVo goods  = goodsService.findGoodsVoByGoodsId(goodsId);
        //判断库存
        if(goods.getGoodsCount()<1){
            model.addAttribute("errmsg", RespBeanEnum.EMPTY_STOCK.getMessage());
            return "seckillFail";
        }
        //判断是否重复抢购
        //MybatisPlus的一个写法
        SeckillOrder seckillOrder = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().eq("user_id", user.getId()).eq("goods_id", goodsId));
        if(seckillOrder!=null){
            model.addAttribute("errmsg",RespBeanEnum.REPEATE_ERROR.getMessage());
            return "seckillFail";
        }
        Order order=orderService.seckill(user,goods);
        model.addAttribute("order",order);
        model.addAttribute("goods",goods);
        return "orderDetail";

    }
}
```

```java
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    @Autowired
    private ISeckillOrderService seckillOrderService;
    @Autowired
    private ISeckillGoodsService seckillGoodsService;
    @Autowired
    private OrderMapper orderMapper;
    /**
     * 秒杀
     * @param user
     * @param goods
     * @return
     */
    @Override
    public Order seckill(User user, GoodsVo goods) {
        //秒杀商品表减库存
        SeckillGoods seckillGoods = seckillGoodsService.getOne(new QueryWrapper<SeckillGoods>().eq("goods_id", goods.getId()));
        seckillGoods.setGoodsCount(seckillGoods.getGoodsCount()-1);
        seckillGoodsService.updateById(seckillGoods);
        //生成订单
        Order order=new Order();
        order.setUserId(user.getId());
        order.setGoodsId(goods.getId());
        order.setDeliveryAddrId(0L);
        order.setGoodsName(goods.getGoodsName());
        order.setGoodsCount(1);
        order.setGoodsPrice(seckillGoods.getSeckillPrice());
        order.setOrderChannel(1);
        order.setStatus(0);
        order.setCreateDate(new Date());
        orderMapper.insert(order);
        //生成秒杀订单
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setUserId(user.getId());
        seckillOrder.setOrderId(order.getId());
        seckillOrder.setGoodsId(goods.getId());
        seckillOrderService.save(seckillOrder);

        return order;
    }
}
```

前端页面：

orderDetail.html

```html
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head>
       <title>订单详情</title>
       <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
       <!-- jquery -->
       <script type="text/javascript" th:src="@{/js/jquery.min.js}"></script>
       <!-- bootstrap -->
       <link rel="stylesheet" type="text/css"
             th:href="@{/bootstrap/css/bootstrap.min.css}" />
       <script type="text/javascript" th:src="@{/bootstrap/js/bootstrap.min.js}">
</script>
       <!-- layer -->
       <script type="text/javascript" th:src="@{/layer/layer.js}"></script>
       <!-- common.js -->
       <script type="text/javascript" th:src="@{/js/common.js}"></script>
</head>
<body>
<div class="panel panel-default">
       <div class="panel-heading">秒杀订单详情</div>
       <table class="table" id="order">
           <tr>
               <td>商品名称</td>
               <td th:text="${goods.goodsName}" colspan="3"></td>
           </tr>
           <tr>
               <td>商品图片</td>
               <td colspan="2"><img th:src="@{${goods.goodsImg}}" width="200"
                                    height="200" /></td>
           </tr>
           <tr>
               <td>订单价格</td>
               <td colspan="2" th:text="${order.goodsPrice}"></td>
           </tr>
           <tr>
               <td>下单时间</td>
               <td th:text="${#dates.format(order.createDate, 'yyyy-MM-dd HH:mm:ss')}" colspan="2"></td>
           </tr>
           <tr>
               <td>订单状态</td>
               <td >
                   <span th:if="${order.status eq 0}">未支付</span>
                   <span th:if="${order.status eq 1}">待发货</span>
                   <span th:if="${order.status eq 2}">已发货</span>
                   <span th:if="${order.status eq 3}">已收货</span>
                   <span th:if="${order.status eq 4}">已退款</span>
                   <span th:if="${order.status eq 5}">已完成</span>
               </td>
               <td>
                   <button class="btn btn-primary btn-block" type="submit"
                           id="payButton">立即支付</button>
               </td>
           </tr>
           <tr>
               <td>收货人</td>
               <td colspan="2">XXX 18012345678</td>
           </tr>
           <tr>
               <td>收货地址</td>
               <td colspan="2">上海市浦东区世纪大道</td>
           </tr>
       </table>
</div>
</body>
</html>
```

秒杀失败则直接显示失败信息即可：

```html
<!DOCTYPE html>
<html lang="en"
    xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>秒杀失败</title>
</head>
<body>
秒杀失败：<p th:text="${errmsg}"></p>
</body>
</html>
```

## 八、系统压测

#### 1.简单使用JMeter

安装解压后，打开jmeter.bat即可，修改jmeter.properties文件

> language=zh_CN
>
> sampleresult.default.encoding=UTF-8

第二句为防止乱码问题

然后重启jmeter.bat

![image-20210917145159971](F:\秋招学习\项目\myproject\seckill-demo\image-20210917145159971.png)

描述压测使用下面的指标

QPS：每秒的查询率：query per second

> QPS一般是针对一个特定的查询，服务器在规定时间内（1秒）所处理流量多少的衡量标准，在因特网上，作为域名系统服务器的机器的性能经常用每秒查询率来衡量。

TPS：transition per second

> TPS经常是业务核心逻辑测试结果的衡量单位。系统整体处理能力取决于处理能力最低模块的TPS值，就像是**木桶效应**。

#### 2.远程安装MySql

https://www.cnblogs.com/lan-blue/p/14176075.html

参考系统：centOS8 

