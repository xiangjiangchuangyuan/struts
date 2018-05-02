# struts
[![license](https://img.shields.io/github/license/mashape/apistatus.svg)](https://github.com/xiangjiangchuangyuan/struts/blob/master/LICENSE)
![Jar Size](https://img.shields.io/badge/jar--size-63.67k-blue.svg)

### 运行条件
* tomcat(8.0.47+)
* JDK(1.7+)

### 框架特色
* 0配置，无需任何配置文件，自动扫描所有类文件
* 自动判断是否返回压缩后的JSON和文本
* 根据[~]标记自动解析为根目录，不受类注解影响
* 框架返回上传的文件流和信息，可以直接上传oss或本地存储
* 根据@Order自动判断拦截器的执行顺序
* 根据@Resource自动注册实现类，无需指定
* 基础方法getPostData可将页面Form直接获取为Entity对象
* 开放StrutsInit类，系统启动或销毁时自动执行其内部函数
* 拦截器只对已注解的Action有效
* Action的任意参数都可用getParameter(name)获取
* 框架自动处理json转换，极速性能
* 支持Linux环境下自动编译所有jsp文件  
* 支持jsp文件修改后自动重新编译，无需重启服务

### WIKI
https://github.com/xiangjiangchuangyuan/struts/wiki

### 反馈
* [提交issue](https://github.com/xiangjiangchuangyuan/struts/issues/new)
* 交流群616698275 答案easy
* email：441430565@qq.com