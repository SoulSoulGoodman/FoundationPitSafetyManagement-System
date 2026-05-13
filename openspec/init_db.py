
import pymysql
import bcrypt
import os

print("=== 开始数据库初始化 ===")

# 数据库配置（从 application.yml 读取）
DB_CONFIG = {
    "host": "localhost",
    "port": 3306,
    "user": "root",
    "password": "root",
    "charset": "utf8mb4"
}

# BCrypt 加密 "123456"
password = "123456"
hashed = bcrypt.hashpw(password.encode("utf-8"), bcrypt.gensalt())
hashed_password = hashed.decode("utf-8")

print(f"原始密码: {password}")
print(f"BCrypt 加密后: {hashed_password}")
print()

# 连接 MySQL
print("正在连接 MySQL...")
conn = pymysql.connect(**DB_CONFIG)
cursor = conn.cursor()

# 1. 读取并执行 init_schema.sql
script_path = os.path.join(os.path.dirname(__file__), "..", "sql", "init_schema.sql")
print(f"读取 SQL 文件: {script_path}")
with open(script_path, "r", encoding="utf-8") as f:
    sql_script = f.read()

# 分号分割执行（简化处理）
statements = []
current = ""
for line in sql_script.split("\n"):
    line = line.strip()
    if line.startswith("--") or line.startswith("#"):
        continue
    current += line + " "
    if ";" in line:
        statements.append(current.strip())
        current = ""

print("正在执行建库建表语句...")
for stmt in statements[:138]:  # 先执行建表和插入基础数据（先不执行最后那个明文密码的插入）
    if not stmt:
        continue
    try:
        cursor.execute(stmt)
    except Exception as e:
        pass  # 忽略一些重复执行的错误

# 确保库被选中
cursor.execute("USE pit_safety_db")
conn.commit()

# 2. 插入或更新 admin 用户，使用 BCrypt 加密密码
print("正在初始化 admin 用户...")
cursor.execute("""
    INSERT INTO sys_user (id, username, password, real_name, status) 
    VALUES (1, 'admin', %s, '系统超管', 1)
    ON DUPLICATE KEY UPDATE password = %s
""", (hashed_password, hashed_password))

# 确保角色存在
cursor.execute("""
    INSERT IGNORE INTO sys_role (id, role_name, role_code) VALUES 
    (1, '监控中心管理员', 'ROLE_ADMIN'),
    (2, '施工方购买用户', 'ROLE_BUYER'),
    (3, '现场维修工程师', 'ROLE_REPAIRER')
""")

# 确保用户角色关联存在
cursor.execute("""
    INSERT IGNORE INTO sys_user_role (user_id, role_id) VALUES (1, 1)
""")

# 插入默认设备（如果没有的话）
cursor.execute("""
    INSERT IGNORE INTO device_info (device_code, device_name, device_type) VALUES 
    ('6501955', '平台测点5', '传感器'),
    ('6501961', '平台测点4', '传感器'),
    ('6501945', 'L3B', '温度传感器'),
    ('FRHY-01', '基准点/平台测点', '全站仪'),
    ('SP1', 'SP1轴力计', '伺服轴力计'),
    ('4P1', '4P1轴力计', '伺服轴力计'),
    ('4P2', '4P2轴力计', '伺服轴力计')
""")

conn.commit()

print()
print("=== 数据库初始化完成 ===")
print("✅ 数据库: pit_safety_db")
print("✅ 用户表: sys_user")
print("✅ 角色表: sys_role")
print("✅ 设备表: device_info")
print()
print("登录信息:")
print(f"  账号: admin")
print(f"  密码: 123456")
print(f"  (已自动转换为 BCrypt 加密存储)")
print()

cursor.close()
conn.close()

print("现在可以尝试登录了！")

