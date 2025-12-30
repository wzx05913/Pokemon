# 项目概述
## 1.1 项目基本内容
本项目旨在开发一个基于 Java/JavaFX 的宠物养成与对战系统，以解决传统宠物游戏在数据管理、交互体验和对战逻辑方面存在的不足。系统主要面向游戏玩家，支持单人游戏模式，功能涵盖宠物获取与属性管理、宠物对战与技能系统、背包道具使用、存档与读档、用户信息维护以及游戏界面交互。通过这些功能，系统可提升宠物养成的趣味性、保障游戏数据的稳定性，并改善玩家的游戏体验。

## 1.2 工作任务分解及人员分工

| 工作内容 | 详细任务 | 负责人 |
| --- | --- | --- |
| 需求分析与业务建模 | 分析系统业务需求，整理功能需求和用例流程 | 全体成员 |
| 系统总体架构设计 | 确定系统架构（MVC）、模块划分及主要类/接口、FXML 与资源管理方案 |万子茜|
| 数据库结构设计 | 设计数据库表结构及表关系，确保数据完整性与一致性 |陈徽汝 |
| 编码实现| 完成迷宫生成算法、玩家移动与碰撞检测、遇敌与事件触发逻辑|陈徽汝|
|-| 实现战斗功能，完成回合流程、技能伤害计算与捕获判定、战斗结果处理与状态更新 |整体搭建：陈徽汝；细化：万子茜|
|-| 实现商品目录、购买流程、货币校验与背包更新|陈徽汝|
|-| 实现宝可梦基类及其子类、宝可梦与宠物的转化、经验与升级逻辑、属性成长曲线与技能分配。 |万子茜|
|- | 完成存档/读档流程、剧情编写及其界面加载与交互。 | 万子茜 |
| 系统测试 | 编写测试用例、执行系统功能测试，记录测试结果 | 全体成员 |
| 文档整理与课程报告撰写 | 整理项目文档、撰写课程报告及项目总结| 全体成员 |

## 1.3 创新点以及技术难点
- 战斗伤害计算机制：结合基础属性与概率性暴击设计伤害公式，基础伤害基于攻击者攻击力并受目标防御值部分抵消，同时概率触发暴击伤害，平衡战斗策略性与随机性。
- 特殊技能机制：在基础攻击之外，设计了多样化的特殊技能效果，丰富了战斗策略选择。
- 状态同步与反馈机制：实时记录并输出战斗事件，保证界面与日志的一致性，便于调试与回放。
- 稳健性与异常处理：入口空值校验、异常捕获与回滚策略提升系统健壮性。
- 迷宫生成：结合随机算法与障碍约束生成可通行的迷宫，提高重玩价值。
- 数据库连接池：使用 HikariCP 管理 MySQL 连接，合理配置连接池参数以提升连接复用率与稳定性，减少频繁创建连接的开销。
- 存档与缓存优化：设计结构化存档模型，运行时通过GameDataManager缓存核心数据，仅在存档/读档时使用数据库事务读写，以减少数据库访问并保证数据一致性。

# 2. 业务需求分析和基本功能
## 2.1 游戏策划
宝可梦 + 迷宫 + Roguelike（肉鸽）。玩家在随机或算法生成的迷宫中探索、触发事件、与野生宝可梦战斗并捕捉，获取资源后在家园进行养成与恢复。玩法以探索与回合制战斗交替，支持剧情触发与多存档管理。
## 2.2 业务需求分析
- **玩家与存档**：账号管理、存档/读档与多存档支持。
- **迷宫探索**：地图生成、玩家移动、碰撞检测与事件触发（遇敌）。
- **战斗系统**：回合流程、技能释放、伤害计算、捕获判定与战果记录。
- **养成与背包**：宠物属性/经验/升级、道具使用与商店交易。
- **数据持久化**：DAO 与事务保证数据一致性与性能优化。
## 2.3 基本功能
- **存档与读取**：存档/读档流程、数据一致性
- **剧情与过场**：剧情触发、对话与分支选择
- **迷宫生成与探索**：迷宫生成算法、地图表示、玩家移动与战斗事件触发
- **回合制战斗与捕获**：战斗回合流程、技能效果、捕获判定
- **角色与宠物养成**：宠物属性、经验与升级
- **商店与交易系统**：物品购买、货币与库存管理、物品效果
- **家园系统**：宠物养成、休息复活与界面展示
- **数据管理与持久化**：DAO 层与全局变量管理

# 3. 系统设计与实现
## 3.1 计算机体系设计
### 3.1.1 系统总体架构
系统采用MVC架构模式：  
- **视图层（View）**：JavaFX + FXML，负责 UI 布局与资源
- **控制器层（Controller）**：处理用户交互并协调视图与业务逻辑
- **模型层（Model）**：包含核心实体类与业务逻辑，封装数据与行为。  
- **数据持久层**：通过DAO组件结合MySQL数据库存储游戏状态，使用HikariCP管理数据库连接池，确保数据读写效率。  
### 3.1.2 开发与运行平台
- 语言/界面：Java + JavaFX（FXML），采用视图与控制器分离设计。
- 构建：Maven，包含单元测试与资源打包。
- 数据库：MySQL，通过DBConnection配置连接参数，使用 HikariCP 管理连接池优化性能。
- 开发工具：IDEA/Eclipse；数据库客户端：Navicat以及MySQL客户端。

## 3.2 功能详细设计
### 3.2.1 存档与读取
- **职责**：实现玩家进度与游戏状态的持久化与恢复，保证数据一致性。
- **主要类/文件**：SaveLoadController、SaveData、DBConnection、UserDAO、PetDAO、BagDAO。
- **要点**：采用 DAO 封装数据库操作，保存时使用事务保证多表一致性；异常与回滚处理；提供手动保存入口。

### 3.2.2 剧情与过场
- **职责**：在关键事件触发时展示剧情、对话与玩家选择分支。
- **主要类/文件**：enter1.fxml/enter2.fxml、相关 *Controller、MainController 事件分发。
- **要点**：剧情资源可外部化为文本/配置，按事件 ID 加载对应 FXML，支持分支控制。

### 3.2.3 迷宫生成与地图表示
- **职责**：生成迷宫地图、处理玩家移动与碰撞检测、触发地图内事件（遇敌/抵达终点等）。
- **主要类/文件**：Maze、Point、MazePlayer、MazeController、maze.fxml。
- **要点**：支持算法生成；提供邻接与可通行检查接口；控制器负责输入映射与事件触发和回调。

### 3.2.4 战斗系统设计
- **职责**：实现回合制战斗流程、技能释放、伤害计算、捕获判定与战斗结束处理。
- **主要类/文件**：BattleManager、BattleStepResult、BattleResult、BattleController、Pokemon、Move。
- **要点**：将战斗流程与 UI 分离，BattleManager 返回 BattleStepResult 以供界面渲染；捕获逻辑固定概率在胜利结算时触发；若成功捕获则更新实体与全局变量。

### 3.2.5 角色与养成
- **职责**：管理玩家与宠物属性、经验/升级机制、技能分配与成长曲线。
- **主要类/文件**：User、Pet、PokemonFactory/PetFactory、PetManageController。
- **要点**：定义属性成长规则，实现经验累积与升级触发，支持技能解锁与分配，同步更新实体数据与持久化存储。

### 3.2.6 商店系统
- **职责**：提供商品展示、购买/出售与交易校验功能。
- **主要类/文件**：ShopController、商店商品数据结构、Bag 更新接口。
- **要点**：交易需校验玩家货币与背包空间，操作完成后同步更新全局变量并记录日志以便回滚。

### 3.2.7 家园系统
- **职责**：家园/卧室界面用于宠物互动、状态查看与快速恢复。
- **主要类/文件**：BedroomHintController、BedroomSelectController、bedroom.fxml。
- **要点**：提供宠物短期恢复、展示与简单互动；界面调用服务层更新宠物状态并可选择保存。


### 3.2.8 数据管理与持久化
- **职责**：提供全局数据管理（GameDataManager）与数据库连接/DAO 层支持。
- **主要类/文件**：GameDataManager、DBConnection、各 DAO（UserDAO/PetDAO/BagDAO）。
- **要点**：集中管理运行时数据缓存，避免重复查询；DAO 负责 CRUD 并提供事务接口；记录必要的日志与异常信息。
## 3.3 数据库字典
### 3.3.1 用户（存档）表（users）

| 字段名 | 类型         | 约束                          | 说明           |
| ------ | ------------ | ----------------------------- | -------------- |
| UserID | int unsigned | PK, NOT NULL, AUTO_INCREMENT  | 用户唯一标识   |

### 3.3.2 背包表（bag）

| 字段名     | 类型         | 约束                                                  | 说明           |
| ---------- | ------------ | ----------------------------------------------------- | -------------- |
| BagID      | int          | PK, NOT NULL, AUTO_INCREMENT（主键倒序排序）           | 背包唯一标识   |
| UserID     | int unsigned | NOT NULL, FK → users(UserID) ON DELETE CASCADE ON UPDATE CASCADE | 关联的用户ID（外键） |
| EggCount   | int          | DEFAULT NULL                                           | 复活食物的数量 |
| RiceCount  | int          | DEFAULT NULL                                           | 经验食物的数量 |
| SoapCount  | int          | DEFAULT NULL                                           | 肥皂           |
| Coins      | int          | DEFAULT NULL                                           | 游戏货币数量   |

### 3.3.3 宠物表（pet）

| 字段名     | 类型         | 约束                                                  | 说明                   |
| ---------- | ------------ | ----------------------------------------------------- | ---------------------- |
| PetID      | int          | PK, NOT NULL, AUTO_INCREMENT                          | 宠物唯一标识           |
| UserID     | int unsigned | NOT NULL, FK → users(UserID) ON DELETE CASCADE ON UPDATE CASCADE | 关联的用户ID（外键）   |
| Type       | varchar(255) | NOT NULL                                              | 宠物类型/品种          |
| Level      | int          | NOT NULL                                              | 宠物等级               |
| Attack     | int          | NOT NULL                                              | 宠物攻击力             |
| Clean      | int          | DEFAULT NULL                                           | 宠物清洁度             |
| Experience | int          | DEFAULT NULL                                           | 当前级别积累的经验     |
| IsAlive    | tinyint      | DEFAULT NULL                                           | 宠物存活状态（1=存活，0=死亡） |

## 3.4 编码实现
### 3.4.1 存档与读取（对应 3.2.1）
#### 主要类与全局数据
#### 关键方法与实现要点
#### 职责划分与设计理由

### 3.4.2 剧情与过场（对应 3.2.2）
#### 主要类与全局数据
#### 关键方法与实现要点
#### 职责划分与设计理由
 
### 3.4.3 迷宫生成与地图表示（对应 3.2.3）
#### 主要类与全局数据
- **Maze**（src/main/java/core/Maze.java）：
    - 主要职责：初始化并生成迷宫格，放置遇敌点，设置起止点，提供格子查询接口
    - 核心内容：
        - 字段：int[][] grid、Point start、Point end；常量：SIZE、WALL、PATH、TREASURE
        - 方法：Maze()、initializeGrid()、generateMaze()、dfs(int,int,Random)、placeTreasures()、setStartEndPoints()、getSize()、getGrid()、getStart()、getEnd()、isWall(int,int)、isTreasure(int,int)、isEnd(int,int)
- **Point**（src/main/java/core/Point.java）：
    - 主要职责：表示不可变坐标点，支持相等比较与哈希。
    - 核心内容：
        - 字段 private final int x, y
        - 方法 getX()、getY()、equals()、hashCode()
- **MazePlayer**（src/main/java/Player/MazePlayer.java）：
    - 主要职责：管理玩家在迷宫中的位置并提供位置操作接口。
    - 核心内容：
        - 字段 Point position
        - 方法 moveUp()、moveDown()、moveLeft()、moveRight()、getPosition()、getX()、getY()、setPosition(int,int)
- **MazeController**（src/main/java/controller/MazeController.java）：
    - 主要职责：
        - 绘制与 UI：在 Canvas 上绘制迷宫、玩家、遇敌点与终点。
        - 输入与移动：监听键盘，计算目标格并进行碰撞检测，通过时更新玩家位置并重绘。
        - 事件与流程：步入遇敌点时清除该点并打开战斗界面；到达终点时结算并返回卧室；支持退出探索。
        - 辅助：管理画布焦点与尺寸、记录探索起始金币、显示提示
    - 核心内容：
        - 字段：Maze maze、MazePlayer player、Canvas canvas、GraphicsContext gc、int cellSize、Stage primaryStage、int coinsAtExplorationStart
        - 方法：initialize()、drawMaze()、handleMovement(KeyCode)、openFightingWindow()、goBackToBedroom()、onExitExploration(ActionEvent)、setPlayerPosition(int,int)、showAlert(String,String)
#### 关键方法与实现要点
- 迷宫生成（DFS）：初始化 grid 为墙，从入口开始，对当前格随机打乱四个方向并尝试访问相距两格的邻居；若邻居为墙，则打通中间格并递归访问，直至所有可达格处理完毕，保证连通性。
- 碰撞检测：由控制器调用 maze.isWall(newX, newY)，仅当返回 false 时才更新玩家位置并重绘。
- 遇敌点：通过 placeTreasures 在 PATH 格上随机放置 TREASURE；玩家踏上该格时控制器将其清除为 PATH 并调用 openFightingWindow() 触发战斗。
- 绘制：drawMaze() 按 grid 循环绘制墙体、地面、终点与遇敌点，并在玩家当前位置绘制红点；坐标映射注意行列到像素的转换（i 为行/y，j 为列/x）。

#### 职责划分与设计理由
- **模型（Maze/Point）**：封装地图数据与查询接口，提供纯业务逻辑，便于单元测试与复用。
- **玩家表示（MazePlayer）**：只负责位置管理（不可变 Point），将移动逻辑与显示/事件解耦。
- **控制器（MazeController）**：负责 UI 显示、输入映射与事件触发（遇敌/终点），通过调用模型的函数实现安全边界检查与事件驱动，保持单向职责。

### 3.4.4 战斗系统实现（对应 3.2.4）
#### 主要类与全局数据
- **BattleManager**（src/main/java/battle/BattleManager.java）：
    - 主要职责：封装战斗的核心流程与规则引擎，维护玩家/敌人队列，处理技能使用、回合切换、自动推进与捕获判定。
    - 核心内容：
        - 字段：Queue<Pokemon> playerQueue、Queue<Pokemon> enemyQueue、Pokemon currentPlayerPokemon、Pokemon currentEnemyPokemon、boolean isPlayerTurn、Random random、BattleResult battleResult、Pokemon lastDefeatedEnemy。
        - 方法：initBattle(List<Pet>)、playerUseMove(int)、enemyUseMove()、playerBasicAttack()、handleAutoTurn()、isBattleEnded()、tryCatchEnemy()、exitBattle()

- **BattleController**（src/main/java/controller/BattleController.java）：
    - 主要职责：负责战斗界面与用户输入的桥接，创建技能按钮并将用户交互转发给 BattleManager；在回合流转与战斗结束时更新 UI 与结算奖励。
    - 核心内容：
        - 字段：UI 组件与状态引用、战斗日志、战斗结果、当前显示的 Pokemon 引用、以及 BattleManager battleManager 用于驱动战斗逻辑。
        - 方法：initialize()，setupBattle(...)、onMoveButtonClicked(int)、updateUI()、appendLog(String)、endBattle()、runAutoProgressLoop()
- **BattleStepResult / BattleResult**（src/main/java/battle/..）：
    - 主要职责：承载与传递战斗引擎的单步执行结果与全局战斗结局，便于控制器以统一格式渲染日志与处理后续流程。
    - 核心内容：
        - BattleStepResult：
            - 字段：boolean success、String message
            - 方法：构造器与常用 getter（isSuccess()、getMessage()），控制器根据其返回值决定是否在界面追加日志或提示。
        - BattleResult：
            - 类型：枚举（例如 PLAYER_WIN、ENEMY_WIN 等）。
            - 用途：由 BattleManager.isBattleEnded() 设置并由控制器在 endBattle() 中读取以完成结算与触发捕获逻辑。

- **Pokemon / Move**（src/main/java/pokemon/..）：
    - Pokemon：提供 useMove(int, Pokemon)、basicAttack(Pokemon)、isFainted()、isAsleep()、isPpDepleted()、recoverPpEachTurn(int)、fullHeal()、enterBattle() / exitBattle() 等方法；包含 HP、等级、PP、技能列表与状态标志。
    - Move：提供技能的元数据与消耗（如 getPpCost()、getPower()、getName()），由 Pokemon 的 useMove 读取并执行具体逻辑。

#### 关键方法与实现要点
- 初始化：initBattle 将玩家可用宠物按等级放入队列，生成敌人并恢复双方状态。
- 玩家动作：playerUseMove 校验回合/状态/PP，调用 useMove，若敌方阵亡则设置 lastDefeatedEnemy 并出队。
- 敌人动作：enemyUseMove 选择可用的最高威力技能执行，处理濒死与消息。
- 自动推进：handleAutoTurn 用于处理跳过回合、自动触发对方动作或回复 PP 的场景。
- 胜负判断：isBattleEnded 根据队列是否有可行动宝可梦判断并设置 battleResult。
- 捕获：tryCatchEnemy 只在玩家胜利时生效；当前实现为固定捕获概率，成功时将生成 Pet 实体并加入内存列表，并尝试为实体设置模拟 ID。

#### 职责划分与设计理由
- **引擎层（BattleManager）**：将战斗规则与流程放在纯业务逻辑层，令 UI 可复用相同引擎；通过 BattleStepResult 跨层传递可读的文本消息，简化界面渲染逻辑。
- **表现层（BattleController）**：仅负责呈现与用户交互（按钮、日志、提示），并把用户命令与渲染同步到引擎；控制器在胜利结算时负责发放金币与调用捕获方法以统一展示流程。
- **实体层（Pokemon/Move/Pet）**：封装战斗相关状态与技能实现，使得 BattleManager 可基于通用接口驱动回合流程，而不依赖某一具体宝可梦实现细节。

### 3.4.5 角色与养成实现（对应 3.2.5）
#### 主要类与全局数据
#### 关键方法与实现要点
#### 职责划分与设计理由

### 3.4.6 商店系统实现（对应 3.2.6）
#### 主要类与全局数据
- **ShopController**（src/main/java/controller/ShopController.java）
    - 主要职责：展示商品并处理购买交互，更新背包与金币显示。
    - 核心内容：
        - 字段：Label coinLabel、Label eggCountLabel、Label soapCountLabel、Label riceCountLabel、Spinner<Integer> eggSpinner、Spinner<Integer> soapSpinner、Spinner<Integer> riceSpinner、GameDataManager dataManager。
        - 方法：initialize()、updateUI()、buyEgg(ActionEvent)、buySoap(ActionEvent)、buyRice(ActionEvent)、showAlert(String,String,AlertType)。

- **GameDataManager**（src/main/java/service/GameDataManager.java）
    - 主要职责：运行时缓存玩家数据（背包、宠物、货币等），在需要时同步到数据库。
    - 核心内容：
        - 字段：int currentUserId、Player currentPlayer、Bag playerBag、List<Pet> petList、List<Pokemon> pokemonList、Pokemon currentPokemon。
        - 方法：addPet(Pet)、addPokemon(Pokemon)、addCoins(int)、clearSession()、decreaseAllPetsClean(int)。
#### 关键方法与实现要点
- 加载商品：loadShopItems() 从配置或资源加载商品并显示玩家当前金币与持有数量。
- 购买流程：onBuyItem() 简要步骤：校验金币与库存 -> 扣除金币并增加背包数量 -> 更新 UI 并显示结果。
- 出售流程：onSellItem() 校验并减少背包数量、增加金币并同步到数据库。
- 道具效果：道具的使用效果由使用时的逻辑或服务层执行；商店仅处理交易与数量变更。

#### 职责划分与设计理由
- **控制器（ShopController）**：负责 UI 与用户交互，验证输入并调用服务；保持轻量，不直接处理复杂业务逻辑。
- **缓存层（GameDataManager）**：在缓存中维护玩家当前背包与货币显示，减少 DB 读取并提高界面响应性，变更后异步写回数据库。


### 3.4.7 家园系统实现（对应 3.2.7）
#### 主要类与全局数据
#### 关键方法与实现要点
#### 职责划分与设计理由


### 3.4.8 数据管理与持久化（对应 3.2.8）
#### 主要类与全局数据
- **DBConnection**（src/main/java/database/DBConnection.java）
    - 主要职责：统一管理 JDBC 连接池，提供获取数据库连接的单例接口。
    - 核心内容：
        - 字段：HikariDataSource dataSource（连接池实例）、JDBC 配置常量。
        - 方法：getInstance()（单例访问）、getConnection()。
- **BagDAO**（src/main/java/database/BagDAO.java）
    - 主要职责：封装 bag 表的 CRUD 操作，将背包数据持久化到数据库并提供按用户加载接口。
    - 核心方法：createBag(Bag)、getBagByUserId(int userId)、updateBag(Bag)、deleteBag(int bagId)、deleteBagByUserId(int userId)。

- **PetDAO**（src/main/java/database/PetDAO.java）
    - 主要职责：封装 pet 表的持久化操作，支持按用户查询、插入与更新宠物记录。
    - 核心方法：createPet(Pet)、getPetsByUserId(int userId)、updatePet(Pet)、deletePet(int petId)、deletePetsByUserId(int userId)。

- **UserDAO**（src/main/java/database/UserDAO.java）
    - 主要职责：封装 users 表的访问，负责用户记录的创建、读取与删除等操作。
    - 核心方法：createUser()、getUserById(int userId)、getAllUsers()、deleteUser(int userId)、ensureUserExistsWithId(int userId)。

#### 关键方法与实现要点
- 连接池：DBConnection 使用 HikariCP。所有 DAO 均通过 DBConnection.getInstance().getConnection() 获取连接，使用 try-with-resources 保证关闭。
- 事务边界：对涉及多表或多步更新的操作（例如购买多件商品或捕获并持久化新宠），应在上层服务（或 DAO）中显式使用同一 Connection 与事务（commit/rollback），避免部分提交。
- 异常与回滚：DAO 方法抛出 SQLException，上层控制器/服务需捕获并在必要时回滚并恢复 GameDataManager 缓存。
- 数据同步：GameDataManager.loadGame(userId) 从 PetDAO、BagDAO 加载数据并初始化内存对象；内存修改后使用对应 DAO 同步到数据库。

#### 职责划分与设计理由
- **DBConnection**：集中管理连接/连接池配置，降低连接创建开销并统一配置管理。
- **DAO 层**：封装 SQL 与持久化细节，提供操作接口并将异常抛给上层由服务/控制器处理。
- **GameDataManager**：维护运行时缓存，避免频繁 DB 访问；对变化提供更新方法。



# 4. 系统测试
## 4.1 健壮性

### 4.1.1 存读档
- 用例说明：验证在数据库断连或 DBConnection 异常时，存档/读档流程的容错与回退行为。
- 测试步骤：
    1. 启动游戏并载入存档界面。
    2. 模拟数据库不可用（停止数据库或修改 `DBConnection` 配置使连接失败）。
    3. 执行手动保存操作与自动保存触发点（若存在）。
    4. 尝试读取存档（loadGame）。
- 预期结果：
    - 保存操作应捕获异常并向用户提示错误，不应引发未捕获异常导致程序崩溃。
    - 若保存失败，内存中的 `GameDataManager` 状态保持不被部分覆盖（回滚或保持原状态）。
    - 读取失败应以友好提示退回到可操作界面，必要时允许重试。
- 实际结果：待执行/填写

### 4.1.2 商店系统（金币不足 / 输入边界）
- **金币不足**
    - 用例说明：验证当背包金币少于购买总价时，程序应阻止购买并显示友好提示，且不会修改背包或玩家金币数据。
    - 测试步骤：
        1. 通过数据库将玩家金币设为 50。
        2. 在商店界面将煎蛋购买数设置为 1（煎蛋单价 100），点击购买。
        3. 观察是否弹出提示并检查背包中的EggCount与Coins是否未变化。
    - 预期结果：
        - 控制器在检测到 coins < price 时直接调用 showAlert("提示", "金币不足，无法购买煎蛋", AlertType.INFORMATION) 并返回。
        - Bag 的 Coins 与 EggCount 保持原值，Player 的 money 不变。
        - 不应抛出未捕获异常或进入确认对话流程。
    - 实际结果：待执行
- **输入边界**
    - 用例说明：验证输入框spinner的边界（初始化为 1-99）与可编辑行为：非正整数应被限制为最小值；超出上限的输入应被约束到上限或拒绝；系统在计算价格时应按控件最终值进行校验。
    - 测试步骤：
        1. 在 eggSpinner 中手动输入 0 并触发购买（或失焦提交编辑）；观察 spinner 的最终值与购买行为。
        2. 在 eggSpinner 中手动输入 -1 并触发购买；观察行为。
        3. 在 eggSpinner 中输入 999 （超出上限）并触发购买；观察最终计价与控件值。
    - 预期结果：
        - Spinner 的最小值限制为 1：输入 0 或负数后，控件应保持或回到 1（不会使用小于 1 的数量进行结算）。
        - 对于超出上限的输入，控件应以最大允许值（99）或控件自身的行为为准进行结算；若玩家金币不足则阻止购买并提示。
        - 所有异常输入不会导致程序崩溃，且购买逻辑以控件的最终数值为准计算价格并执行金币校验。
    - 实际结果：待执行
### 4.1.3 养成系统
- 用例说明：验证在使用道具时，若背包中对应道具数量为 0，系统阻止使用并给出提示。
- 测试步骤：
    1. 确保 `GameDataManager` 或 `Bag` 中某道具数量为 0。
    2. 在养成/使用道具界面触发使用该道具的动作。
- 预期结果：
    - 操作被拒绝并弹出提示信息，`Bag` 数量与宠物状态保持不变。
    - 若代码上存在防护逻辑，应无异常抛出。
- 实际结果：待执行/填写

### 4.1.4 探索前置校验
- 用例说明：验证当玩家无可出战宝可梦时，系统在进入探索流程中的限制与提示行为。
- 测试步骤：
    1. 在运行前通过数据库将 pet 表中的 IsAlive 字段全部设为 0 并重新加载存档。
    2. 在主界面或卧室界面点击“进入迷宫/开始探索”按钮，触发探索入口逻辑。
    3. 观察界面行为：是否阻止跳转到迷宫界面，并弹出友好提示框；记录提示文本。
- 预期结果：
    - 界面阻止进入迷宫并弹出友好提示，提示文本为“您已没有可以出战的宠物，无法外出探索！”。
    - 控制器/入口逻辑应调用明确的前置校验方法并在校验失败时立即返回，不执行探索相关的初始化动作
- 实际结果：

### 4.1.5 战斗时校验
- 用例说明：验证战斗中技能使用的 PP 校验，确保玩家和敌方均不能使用消耗超过当前 PP 的技能，并验证系统在 PP 不足时的自动处理策略。
- 测试步骤：
    1. 在战斗中重复使用无伤害但是消耗pp的催眠技能（胖丁）
    2. 检查战斗日志，确认该回合是否按预期禁用了不能使用的技能。
    3. 重复上述用例以覆盖敌方在 PP 不足时的行动逻辑，验证敌方行为的鲁棒性和日志记录。
- 预期结果：
    - 当玩家尝试使用 PP 不足的技能时，界面应禁用该技能的按钮
    - 当敌人宝可梦所有技能均无 PP 时，系统应按设计优先级执行自动释放普攻
- 实际结果：待执行/填写
## 4.2 完整探索游玩流程（简化）
- 用例说明：验证迷宫探索中的遇敌、战斗结算、捕获、奖励发放与数据持久化流程。
- 测试步骤：
    1. 从卧室进入迷宫，记录初始状态（金币、背包、宠物）。
    2. 战斗胜利但捕获失败：检查战斗日志、奖励发放与界面提示，确认未新增宠物。
    3. 战斗胜利且捕获成功：确认提示、新宠物已加入内存并持久化，属性与等级正确。
    4. 抵达终点：弹出奖励播报并发放奖励，检查到账与界面一致。
    5. 退出返回卧室：确认背包、金币、宠物与数据库数据一致。
- 预期结果：
    - 战斗奖励正确发放，日志记录完整。
    - 捕获失败不新增宠物；捕获成功完成内存与数据库同步。
    - 终点奖励与播报一致，数据无重复或丢失。
- 实际结果：待执行

# 5. 项目展望
本系统作为宠物养成与对战平台的初步实现，未来可在以下方向扩展与优化：
- 功能拓展：增加更多宠物与技能、丰富活动/任务与成就体系，支持多人联机与社交互动，完善商店与道具机制。
- 性能优化：加强 GameDataManager 缓存与事务一致性，优化数据库访问与并发处理，补充自动化与压力测试。
- 平台兼容：推进跨平台部署（桌面/Web/移动），支持云存档与远程同步，提高可访问性与部署灵活性。
- 智能化发展：利用数据分析与机器学习优化迷宫生成、难度自适应、宠物/道具推荐与玩家行为预测，提升游戏体验与用户留存。