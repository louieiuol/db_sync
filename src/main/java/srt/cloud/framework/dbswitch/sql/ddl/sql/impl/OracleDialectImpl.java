// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package srt.cloud.framework.dbswitch.sql.ddl.sql.impl;

import srt.cloud.framework.dbswitch.sql.ddl.AbstractDatabaseDialect;
import srt.cloud.framework.dbswitch.sql.ddl.pojo.ColumnDefinition;
import srt.cloud.framework.dbswitch.sql.ddl.type.OracleDataTypeEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 关于Oracle12c的自增列问题：
 * <p>
 * （1）一张表中，只能有一列为自增长列。
 * <p>
 * （2）列的数据类型，必须为数值型。
 * <p>
 * （3）不能设置默认值。
 * <p>
 * （4）会自动应用not null和not deferrable。
 * <p>
 * （5）使用CTAS方式无法继承自增长列的属性。
 * <p>
 * （6）如果执行回滚，事务会回滚，但是序列中的值不会回滚。
 *
 * @author jrl
 */
public class OracleDialectImpl extends AbstractDatabaseDialect {

  private static List<OracleDataTypeEnum> integerTypes;

  static {
    integerTypes = new ArrayList<>();
    integerTypes.add(OracleDataTypeEnum.NUMBER);
  }

  @Override
  public String getFieldTypeName(ColumnDefinition column) {
    int length = column.getLengthOrPrecision();
    int scale = column.getScale();
    StringBuilder sb = new StringBuilder();
    OracleDataTypeEnum type = null;
    try {
      type = OracleDataTypeEnum.valueOf(column.getColumnType().toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new RuntimeException(
          String.format("Invalid Oracle data type: %s", column.getColumnType()));
    }

    if (column.isAutoIncrement()) {
      if (!OracleDialectImpl.integerTypes.contains(type)) {
        throw new RuntimeException(
            String.format("Invalid Oracle auto increment data type: %s", column.getColumnType()));
      }
    }

    sb.append(type.name());
    switch (type) {
      case NUMBER:
        if (Objects.isNull(length) || length < 0) {
          throw new RuntimeException(
              String.format("Invalid Oracle data type length: %s(%d)", column.getColumnType(),
                  length));
        }

        if (length > 0) {
          sb.append(String.format("(%d)", length));
        } else {
          if (Objects.isNull(scale) || scale < 0) {
            throw new RuntimeException(String.format("Invalid Oracle data type scale: %s(%d,%d)",
                column.getColumnType(), length, scale));
          }

          sb.append(String.format("(%d,%d)", length, scale));
        }
        break;
      case CHAR:
      case NCHAR:
      case VARCHAR:
      case VARCHAR2:
        if (Objects.isNull(length) || length < 0) {
          throw new RuntimeException(String
              .format("Invalid Oracle data type length: %s(%d)", column.getColumnType(), length));
        }
        sb.append(String.format(" (%d) ", length));
      default:
        break;
    }

    return sb.toString();
  }

  @Override
  public String getFieldDefination(ColumnDefinition column) {
    String fieldname = column.getColumnName();
    boolean nullable = column.isNullable();
    String defaultValue = column.getDefaultValue();
    //String comment=column.getColumnComment();

    StringBuilder sb = new StringBuilder();
    sb.append(String.format("\"%s\" ", fieldname.trim()));
    sb.append(this.getFieldTypeName(column));

    if (column.isAutoIncrement() && column.isPrimaryKey()) {
      // 在Oracle12c数据库里只有主键是自增的
      sb.append(" GENERATED BY DEFAULT ON NULL AS IDENTITY ");
    } else {
      if (nullable) {
        sb.append(" DEFAULT NULL");
      } else if (Objects.nonNull(defaultValue) && !defaultValue.isEmpty()) {
        if (defaultValue.equalsIgnoreCase("NULL")) {
          sb.append(" DEFAULT NULL");
        } else if (defaultValue.equalsIgnoreCase("SYSDATE")) {
          sb.append(" DEFAULT SYSDATE");
        } else {
          sb.append(String.format(" DEFAULT '%s'", defaultValue));
        }
      } else {
        sb.append(" NOT NULL");
      }
    }

    return sb.toString();
  }

}
