#!/usr/bin/env python3
"""
基坑监测数据导入脚本
读取 CSV/Excel 数据文件，生成 MySQL INSERT SQL 文件

用法: python3 sql/import_data.py
输出: sql/import_steel_temp.sql, sql/import_total_station.sql, sql/import_axial_force.sql, sql/import_adjust.sql

导入顺序:
  mysql -u root -p pit_safety_db < sql/import_steel_temp.sql
  mysql -u root -p pit_safety_db < sql/import_total_station.sql
  mysql -u root -p pit_safety_db < sql/import_axial_force.sql
  mysql -u root -p pit_safety_db < sql/import_adjust.sql
"""

import pandas as pd
import os
import sys
from datetime import datetime
import math

# 项目根目录（脚本在 sql/ 下，需要回到项目根目录才能定位 sql/data/）
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_DIR = os.path.dirname(SCRIPT_DIR)
DATA_DIR = os.path.join(PROJECT_DIR, 'sql', 'data')
OUTPUT_DIR = os.path.join(PROJECT_DIR, 'sql')

BATCH_SIZE = 500  # 每批 INSERT 行数


def esc(value):
    """转义 SQL 值"""
    if value is None:
        return 'NULL'
    if isinstance(value, float) and (math.isnan(value) or math.isinf(value)):
        return 'NULL'
    if isinstance(value, bool):
        return '1' if value else '0'
    if isinstance(value, str):
        stripped = value.strip()
        if stripped == '' or stripped == '-':
            return 'NULL'
        s = stripped.replace('\\', '\\\\').replace("'", "\\'").replace('\x00', '')
        return f"'{s}'"
    if isinstance(value, (datetime, pd.Timestamp)):
        return f"'{pd.Timestamp(value).strftime('%Y-%m-%d %H:%M:%S')}'"
    return str(value)


def write_insert_sql(filepath, table, columns, data_rows):
    """将数据行写入 SQL 文件，使用批量 INSERT"""
    print(f"  写入 {filepath} ({len(data_rows)} 行)...")
    col_str = ', '.join(f'`{c}`' for c in columns)

    with open(filepath, 'w', encoding='utf-8') as f:
        f.write('USE `pit_safety_db`;\n\n')

        total = len(data_rows)
        for i in range(0, total, BATCH_SIZE):
            batch = data_rows[i:i + BATCH_SIZE]
            values_lines = []
            for row in batch:
                vals = ', '.join(esc(v) for v in row)
                values_lines.append(f'({vals})')
            f.write(f'INSERT INTO `{table}` ({col_str}) VALUES\n')
            f.write(',\n'.join(values_lines))
            f.write(';\n\n')

            if (i + BATCH_SIZE) % 5000 == 0:
                print(f"    进度: {min(i + BATCH_SIZE, total)}/{total}")


# ========================================
# 1. 钢支撑温度数据 → data_steel_temperature
# ========================================
def import_steel_temp():
    print("\n[1/4] 处理钢支撑温度数据...")
    csv_path = os.path.join(DATA_DIR, '钢支撑温度数据.csv')
    df = pd.read_csv(csv_path)

    # 只保留需要的列并映射
    rows = []
    for _, row in df.iterrows():
        rows.append((
            row['传感器编码'],           # sensor_code
            row['数据时间'],            # collect_time
            row['温度'],               # temperature
            row['测量值'],             # measure_val
        ))

    output = os.path.join(OUTPUT_DIR, 'import_steel_temp.sql')
    write_insert_sql(output, 'data_steel_temperature',
                     ['sensor_code', 'collect_time', 'temperature', 'measure_val'],
                     rows)
    print(f"  完成: {len(rows)} 条记录")


# ========================================
# 2. 全站仪数据 → data_total_station
# ========================================
def import_total_station():
    print("\n[2/4] 处理全站仪数据...")
    csv_path = os.path.join(DATA_DIR, '全站仪数据.csv')
    df = pd.read_csv(csv_path)

    rows = []
    for _, row in df.iterrows():
        rows.append((
            row['传感器编码'],                       # sensor_code
            row['数据时间'],                         # collect_time
            row['ΔX(mm)'],                          # delta_x
            row['ΔY(mm)'],                          # delta_y
            row['ΔH(mm)'],                          # delta_h
            row['∑X(mm)'],                          # total_x
            row['∑Y(mm)'],                          # total_y
            row['∑H(mm)'],                          # total_h
            row['温度(℃)'],                          # temperature
        ))

    output = os.path.join(OUTPUT_DIR, 'import_total_station.sql')
    write_insert_sql(output, 'data_total_station',
                     ['sensor_code', 'collect_time', 'delta_x', 'delta_y', 'delta_h',
                      'total_x', 'total_y', 'total_h', 'temperature'],
                     rows)
    print(f"  完成: {len(rows)} 条记录")


# ========================================
# 3. 伺服轴力数据 → data_axial_force
# ========================================
def import_axial_force():
    print("\n[3/4] 处理伺服轴力数据...")
    servo_dir = os.path.join(DATA_DIR, '伺服数据')

    file_sensor_map = {
        'sp1_data.xlsx': 'SP1',
        '4p1_data.xlsx': '4P1',
        '4p2-data.xlsx': '4P2',
    }

    all_rows = []
    for filename, sensor_code in file_sensor_map.items():
        path = os.path.join(servo_dir, filename)
        if not os.path.exists(path):
            print(f"  警告: {path} 不存在，跳过")
            continue
        print(f"  读取 {filename} ...")
        df = pd.read_excel(path)
        for _, row in df.iterrows():
            all_rows.append((
                sensor_code,       # sensor_code
                row['Time'],       # collect_time
                row['WForce'],     # w_force
                row['FPosition'],  # f_position
                row['Temperature'],# temperature
            ))

    output = os.path.join(OUTPUT_DIR, 'import_axial_force.sql')
    write_insert_sql(output, 'data_axial_force',
                     ['sensor_code', 'collect_time', 'w_force', 'f_position', 'temperature'],
                     all_rows)
    print(f"  完成: {len(all_rows)} 条记录")


# ========================================
# 4. 轴力调整记录 → maintenance_adjust_record
# ========================================
def import_adjust_records():
    print("\n[4/4] 处理轴力调整记录...")
    servo_dir = os.path.join(DATA_DIR, '伺服数据')
    path = os.path.join(servo_dir, '轴力调整记录.xlsx')

    if not os.path.exists(path):
        print(f"  警告: {path} 不存在，跳过")
        return

    df = pd.read_excel(path)
    rows = []
    for _, row in df.iterrows():
        rows.append((
            row['Name'],          # sensor_code
            row['Content'],       # adjust_content
            row['CreateTime'],    # operate_time
        ))

    output = os.path.join(OUTPUT_DIR, 'import_adjust.sql')
    write_insert_sql(output, 'maintenance_adjust_record',
                     ['sensor_code', 'adjust_content', 'operate_time'],
                     rows)
    print(f"  完成: {len(rows)} 条记录")


# ========================================
# 主函数
# ========================================
if __name__ == '__main__':
    print("=" * 60)
    print("基坑监测数据导入脚本 - 生成 SQL INSERT 文件")
    print("=" * 60)

    import_steel_temp()
    import_total_station()
    import_axial_force()
    import_adjust_records()

    print("\n" + "=" * 60)
    print("全部完成！生成的 SQL 文件在 sql/ 目录下：")
    print("  sql/import_steel_temp.sql     - 钢支撑温度数据")
    print("  sql/import_total_station.sql  - 全站仪位移数据")
    print("  sql/import_axial_force.sql    - 伺服轴力数据")
    print("  sql/import_adjust.sql         - 轴力调整记录")
    print()
    print("导入命令（先确保数据库 pit_safety_db 已创建）：")
    print("  mysql -u root -p pit_safety_db < sql/init_schema.sql   # 建表")
    print("  mysql -u root -p pit_safety_db < sql/import_steel_temp.sql")
    print("  mysql -u root -p pit_safety_db < sql/import_total_station.sql")
    print("  mysql -u root -p pit_safety_db < sql/import_axial_force.sql")
    print("  mysql -u root -p pit_safety_db < sql/import_adjust.sql")
    print("=" * 60)
