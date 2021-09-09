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