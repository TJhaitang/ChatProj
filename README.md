# 更新日志

## 下一步工作

* 待发送消息保存在服务器
* 加好友
* 群组功能

## 待实现功能

* 待发送消息队列
* ~~服务器与本地的文件创建~~
* 用户界面的map
* 本地消息的处理
* 服务器消息的转发
* 加好友
* 建群
* 依时间排列的消息列表
* 打开聊天窗时再次点击使得窗口弹出（获得焦点）

***
## 210523xjz
> 增加
1. 增加了服务器端在用户未在线时的消息暂存功能
> 改动
1. 重写了服务器端HandleAsession的代码
> 修复
1. 修复了接收消息格式问题
> 其他
1. secondPerson消息记录打开时位置不在最下方
2. 转发消息队列不生效

***
## 210522xjz
> 增加
1. 增加了消息展示框对双方消息的不同对齐展示
2. 增加了消息展示框对日期与内容的不同颜色展示
3. 增加了创建文件失败时的提示框
4. 增加了聊天窗的html支持，现在可以在里面放图片了（但还不能发图片）
> 改动
1. 在creatNewUser方法中加入了文件与图片文件夹
2. 将聊天记录格式加上了一个标签(TEXT，IMG，FILE)
3. 将服务器端用户密码保存方式改为MD5码保存（不安全，后续可能换为PBKDF2）
> 修复
1. 修复了聊天框没法使用换行的问题：将\n明文保存
> 其他
1. 现在图片格式无法调整：对齐方式、大小、文字环绕等
2. 信息过互联网延迟过高，应该简化与服务器的交互流程
3. 在加载图片的时候如果没有找到图片应该加入一个default
4. 内网穿透软件稳定性不高，而且挂国外节点网速不高
5. 要不要把发信息也放到HandleAsession里面？

***
## 210521xjz
> 增加
1. 实现了局域网内的连接
2. 实现了互联网的连接
3. 增加了服务器端对消息(TEXT)的转发
4. 增加了对用户退出行为服务器端的应对
> 改动
1. 将发送信息的反馈取消，后续将放在用户端的接收器中
> 修复
1. 修复其他账号登录时好友列表加载错误的问题
2. 修复了局域网内连接不上的问题：关闭防火墙
> 其他
1. 有关历史记录在聊天框中展示的问题：   
如果聊天记录过多的话应该不能全部展示，那便需要“仅仅展示五十条”之类的操作，为了降低复杂度这里应该从最新的消息向前搜索五十条，这样的话就需要把消息[反向插入展示框中](https://blog.csdn.net/qq_21682469/article/details/78808713)
2. 对于用户的账号密码存储问题，咱们可以加上服务器端的安全措施，就是将账号密码的[哈希值存在服务器端](https://www.cnblogs.com/milantgh/p/3612318.html)。

***
## 210521nyz

> 增加

本地创建新用户;补充服务器创建新用户

服务器发送消息接受初步

> 改动

修改创建窗体位置，使其适用于不同设备

***
## 210519nyz

> 增加

采用map存储打开聊天窗，便于对聊天窗口进行操作
> 改动

修改删除聊天窗机制，现在关闭聊天窗即可释放资源

README以后在下面更新吧，最上面放待完成功能/问题
> 修复

可以无限打开同一聊天窗的bug

***
## 210518nyz

> 增加

1. 初步建立用户主窗口，与聊天窗口建立连接，可多开聊天窗
2. 主窗口初步与服务器相连

> 改动

修改文件读取方式：System.getProperty("user.dir") + "/src/client/users/"
在login中获取用户姓名 优化创建选项卡中滚动窗的代码，提供拓展能力

***
## 210516xjz

> 增加

1. 增加了用户聊天窗口的父类，对其中的大体结构进行了确定
2. 增加了聊天窗口的历史信息读取，消息记录文件应在加好友时创建
3. 增加了服务器端的用户登录输出

> 改动

> 修复

> 其他

1.

服务器与本地文件匹配时，保证服务器数据先于本地，同步时为服务器端数据下载到本地-匹配方式：[md5HashCode](https://blog.csdn.net/qq_25646191/article/details/78863110)

2. 聊天框内可以加入[9-patch](https://github.com/freeseawind/NinePatch#readme)图片背景
3. 聊天信息展示框使用JTextPane,可以实现[html语言插入](https://blog.csdn.net/sujudz/article/details/7928384)从而展示图片(包括gif).
4. 美化等后面再做吧我现在是做不下去了
5. 本地文件中群组名由服务器确定
6. 目前输入框无法输入回行

***
## 210515xjz

> 增加

1. 增加了连接类，创建了sever:TargetConnection与client:ServerConnection来建立连接，实现了其中的对应逻辑.
2. 增加了服务器端的HashMap用以存储用户

> 改动

1. 将之前的信号全部换为了Flag接口中的常量

> 修复

1. 修复了用户端关闭后偶尔出现的服务器崩溃问题

> 其他

1. 很多类都写成了内部类，不知道这样做是好是坏
2. 以后在服务器端加一点用户的状态输出

***
## 210514xjz

* 会不会出现同时向服务器发送多个请求的情况，这时候服务器端该怎么接收呢？

***
***

# 一个可联网的网上聊天系统

## 设想功能：

* 用户：用户的注册、登录、退出、建立与删除好友关系、（查看对方是否在线）
* 文本对话：好友间进行即时聊天
* 群组对话：用户发起与删除对话群组、用户接受、拒绝群组邀请、用户退出群组
* 朋友圈功能：用户发送好友可见的广播式信息（朋友圈）、（可设置消息“不让ta看”、“不看ta”）——非即时、需用户主动刷新 可能扩展：
* 文件：文件的发送、云端暂存与接收
* 聊天扩展：图片与音频的发送与接收、展示

## 实现方法

### 服务器端

1. 存储（文件）

> 所有用户 ID，密码；朋友圈内容   
好友关系矩阵   
群组ID，群主，用户ID

2. 准备消息队列：目标ID，内容

> 逻辑：   
与用户交换信息：
>1. 一对一：
>> 接受 - 找对象 - 在线：发 / 没有在线：存文件   
> > 收什么发什么
>2. 群：
>> 接收 - 找群ID - 给群中所有人发信息 - 在线/不在线
>3. 朋友圈：
>> 接受 - 存文件  
> > 用户端给信号 - 用户的所有好友的朋友圈按时间顺序发 （对比标记更新）

### 本地端

1. 存储（文件）

> 本地文件存储结构
>1. 聊天记录（本地/服务器）：
>2. 索引文件夹：
>> 交互对象文件：<时间><对象(群)><已读？><内容地址>
>3. 资源文件夹： 资源：<id>文件名：内容
>4. 用户配置信息：

2. 本地逻辑：

> 1. GUI ：
>2. 登陆：
    >>* 注册：用户名/密码
    >>* 登陆：用户名/密码
    >>* 构建用户界面：读本地 - 更新信息（对比标记） - 接受信息 界面内容：…………
>3. 新窗口：对象
>4. 向服务器发信息：   
    ……
>> * 内容：
     >>1. 一对一：<时间><来源><对象><内容索引(大体积内容：图片)> //先发索引再发图片
     >>2. 群： <时间><来源><群ID><内容索引>
     >>3. 朋友圈：<时间><内容>
>5. 由服务器接受信息：
    >>* 处理





。。